package nl.tvn.cube.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Shortest quarter-turn solutions that increase white-cross progress. No JavaFX or live state. */
public final class WhiteCrossEdgeSolver {
    private static final CubeVector WHITE = new CubeVector(0, 1, 0);

    public enum Edge {
        BLUE("White–blue", new CubeVector(0, 0, 1)),
        RED("White–red", new CubeVector(1, 0, 0)),
        GREEN("White–green", new CubeVector(0, 0, -1)),
        ORANGE("White–orange", new CubeVector(-1, 0, 0));

        private final String label;
        private final CubeVector side;

        Edge(String label, CubeVector side) {
            this.label = label;
            this.side = side;
        }

        public String label() { return label; }
    }

    // An edge's other sticker direction is position - whiteDirection, so this fully describes it.
    public record EdgeState(CubeVector position, CubeVector whiteDirection) { }

    /** Entries are ordered blue, red, green, orange. Both lists contain only immutable values. */
    public record Snapshot(List<EdgeState> current, List<EdgeState> goals) {
        public Snapshot {
            current = List.copyOf(current);
            goals = List.copyOf(goals);
        }

        public static Snapshot capture(List<CubieModel> cubies) {
            CubeVector whiteCentre = position(find(cubies, WHITE));
            List<EdgeState> current = new ArrayList<>();
            List<EdgeState> goals = new ArrayList<>();
            for (Edge edge : Edge.values()) {
                CubieModel cubie = find(cubies, add(WHITE, edge.side));
                current.add(new EdgeState(position(cubie), cubie.direction(WHITE)));
                goals.add(new EdgeState(add(whiteCentre, position(find(cubies, edge.side))), whiteCentre));
            }
            return new Snapshot(current, goals);
        }

        public int solvedMask() {
            int mask = 0;
            for (int i = 0; i < current.size(); i++) {
                if (current.get(i).equals(goals.get(i))) mask |= 1 << i;
            }
            return mask;
        }
    }

    public enum Status { FOUND, ALREADY_COMPLETE, NO_SOLUTION }

    public record Solution(Status status, Edge target, List<Move> moves) {
        public Solution {
            moves = List.copyOf(moves);
        }

        public String notation() {
            return String.join(" ", moves.stream().map(move -> NOTATION.get(MOVES.indexOf(move))).toList());
        }
    }

    // Stable expansion order is also the tie-breaker for equally short solutions.
    static final List<String> NOTATION = List.of("F", "F'", "B", "B'", "R", "R'",
        "L", "L'", "U", "U'", "D", "D'");
    static final List<Move> MOVES = List.of(
        face(RotationAxis.Z, 1, 1), face(RotationAxis.Z, 1, -1),
        face(RotationAxis.Z, -1, -1), face(RotationAxis.Z, -1, 1),
        face(RotationAxis.X, 1, 1), face(RotationAxis.X, 1, -1),
        face(RotationAxis.X, -1, -1), face(RotationAxis.X, -1, 1),
        face(RotationAxis.Y, 1, 1), face(RotationAxis.Y, 1, -1),
        face(RotationAxis.Y, -1, -1), face(RotationAxis.Y, -1, 1));
    private static final List<EdgeState> EDGE_STATES = buildEdgeStates();
    private static final Map<EdgeState, Integer> STATE_IDS = buildStateIds();
    private static final int[][] TRANSITIONS = buildTransitions();
    private static final int STATE_COUNT = 24 * 24 * 24 * 24;

    public Solution solve(Snapshot snapshot) {
        if (!valid(snapshot.current()) || !valid(snapshot.goals())) {
            return new Solution(Status.NO_SOLUTION, null, List.of());
        }
        int start = encode(snapshot.current());
        int goal = encode(snapshot.goals());
        int initialMask = solvedMask(start, goal);
        if (initialMask == 15) return new Solution(Status.ALREADY_COMPLETE, null, List.of());

        int[] parents = new int[STATE_COUNT];
        Arrays.fill(parents, -1);
        byte[] previousMoves = new byte[STATE_COUNT];
        int[] queue = new int[STATE_COUNT];
        int head = 0;
        int tail = 0;
        queue[tail++] = start;
        parents[start] = start;
        while (head < tail) {
            if (Thread.currentThread().isInterrupted()) {
                return new Solution(Status.NO_SOLUTION, null, List.of());
            }
            int state = queue[head++];
            for (int move = 0; move < MOVES.size(); move++) {
                int next = nextState(state, move);
                if (parents[next] != -1) continue;
                parents[next] = state;
                previousMoves[next] = (byte) move;
                int mask = solvedMask(next, goal);
                if ((mask & initialMask) == initialMask && mask != initialMask) {
                    List<Move> result = new ArrayList<>();
                    for (int cursor = next; cursor != start; cursor = parents[cursor]) {
                        result.add(MOVES.get(previousMoves[cursor]));
                    }
                    Collections.reverse(result);
                    int target = Integer.numberOfTrailingZeros(mask & ~initialMask);
                    return new Solution(Status.FOUND, Edge.values()[target], result);
                }
                queue[tail++] = next;
            }
        }
        return new Solution(Status.NO_SOLUTION, null, List.of());
    }

    // Package-visible for comparison against the production full-cube move engine in tests.
    static int encode(List<EdgeState> edges) {
        int encoded = 0;
        int factor = 1;
        for (EdgeState edge : edges) {
            encoded += STATE_IDS.get(edge) * factor;
            factor *= 24;
        }
        return encoded;
    }

    static int nextState(int state, int move) {
        int next = 0;
        int factor = 1;
        for (int i = 0; i < 4; i++) {
            next += TRANSITIONS[move][state % 24] * factor;
            state /= 24;
            factor *= 24;
        }
        return next;
    }

    private static int solvedMask(int state, int goal) {
        int mask = 0;
        for (int i = 0; i < 4; i++) {
            if (state % 24 == goal % 24) mask |= 1 << i;
            state /= 24;
            goal /= 24;
        }
        return mask;
    }

    private static boolean valid(List<EdgeState> states) {
        return states.size() == 4 && states.stream().allMatch(STATE_IDS::containsKey)
            && states.stream().map(EdgeState::position).distinct().count() == 4;
    }

    private static List<EdgeState> buildEdgeStates() {
        List<EdgeState> states = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (Math.abs(x) + Math.abs(y) + Math.abs(z) != 2) continue;
                    CubeVector position = new CubeVector(x, y, z);
                    if (x != 0) states.add(new EdgeState(position, new CubeVector(x, 0, 0)));
                    if (y != 0) states.add(new EdgeState(position, new CubeVector(0, y, 0)));
                    if (z != 0) states.add(new EdgeState(position, new CubeVector(0, 0, z)));
                }
            }
        }
        return List.copyOf(states);
    }

    private static Map<EdgeState, Integer> buildStateIds() {
        Map<EdgeState, Integer> result = new HashMap<>();
        for (int i = 0; i < EDGE_STATES.size(); i++) result.put(EDGE_STATES.get(i), i);
        return Map.copyOf(result);
    }

    private static int[][] buildTransitions() {
        int[][] result = new int[MOVES.size()][24];
        for (int m = 0; m < MOVES.size(); m++) {
            Move move = MOVES.get(m);
            for (int i = 0; i < EDGE_STATES.size(); i++) {
                EdgeState edge = EDGE_STATES.get(i);
                CubeVector position = edge.position();
                CubeVector direction = edge.whiteDirection();
                int layer = switch (move.axis()) {
                    case X -> position.x();
                    case Y -> position.y();
                    case Z -> position.z();
                };
                if (move.layers().contains(layer)) {
                    for (int turn = 0; turn < Math.floorMod(move.quarterTurns(), 4); turn++) {
                        position = position.quarterTurn(move.axis());
                        direction = direction.quarterTurn(move.axis());
                    }
                }
                result[m][i] = STATE_IDS.get(new EdgeState(position, direction));
            }
        }
        return result;
    }

    private static Move face(RotationAxis axis, int layer, int turns) {
        return new Move(axis, Set.of(layer), turns);
    }

    private static CubieModel find(List<CubieModel> cubies, CubeVector home) {
        return cubies.stream().filter(c -> c.homeX() == home.x() && c.homeY() == home.y() && c.homeZ() == home.z())
            .findFirst().orElseThrow(() -> new IllegalArgumentException("Missing cube piece: " + home));
    }

    private static CubeVector position(CubieModel cubie) {
        return new CubeVector(cubie.coordinate().x(), cubie.coordinate().y(), cubie.coordinate().z());
    }

    private static CubeVector add(CubeVector a, CubeVector b) {
        return new CubeVector(a.x() + b.x(), a.y() + b.y(), a.z() + b.z());
    }
}
