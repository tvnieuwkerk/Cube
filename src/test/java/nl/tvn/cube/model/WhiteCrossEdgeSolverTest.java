package nl.tvn.cube.model;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import nl.tvn.cube.viewmodel.AlgorithmParser;
import nl.tvn.cube.model.WhiteCrossEdgeSolver.Snapshot;
import nl.tvn.cube.model.WhiteCrossEdgeSolver.Status;

class WhiteCrossEdgeSolverTest {
    private final WhiteCrossEdgeSolver solver = new WhiteCrossEdgeSolver();

    @Test
    void completeCrossAndInvalidSnapshotProduceNoMoves() {
        var complete = solver.solve(Snapshot.capture(new CubeModel().cubies()));
        assertEquals(Status.ALREADY_COMPLETE, complete.status());
        assertTrue(complete.moves().isEmpty());
        var invalid = solver.solve(new Snapshot(List.of(), List.of()));
        assertEquals(Status.NO_SOLUTION, invalid.status());
        assertTrue(invalid.moves().isEmpty());
    }

    @Test
    void findsKnownShortestSequencesAndUsesTwoQuarterTurnsForHalfTurn() {
        CubeModel cube = new CubeModel();
        run(cube, "F");
        var single = solver.solve(Snapshot.capture(cube.cubies()));
        assertEquals("F'", single.notation());
        assertEquals(WhiteCrossEdgeSolver.Edge.BLUE, single.target());
        run(cube, "F");
        var half = solver.solve(Snapshot.capture(cube.cubies()));
        assertEquals("F F", half.notation());
        assertEquals(2, half.moves().size());
        assertEquals(half, solver.solve(Snapshot.capture(cube.cubies())));
        assertProgress(cube);
    }

    @Test
    void shortestSolutionsAgreeWithIndependentFullCubeEnumeration() {
        for (String scramble : List.of("U", "R U", "F R B", "F F R R", "L F U B")) {
            CubeModel cube = new CubeModel();
            run(cube, scramble);
            int initial = Snapshot.capture(cube.cubies()).solvedMask();
            var result = solver.solve(Snapshot.capture(cube.cubies()));
            assertEquals(Status.FOUND, result.status());
            assertFalse(canImprove(cube, initial, result.moves().size() - 1), scramble);
            assertProgress(cube);
        }
    }

    @Test
    void compactTransitionsAgreeWithFullModelAcrossScrambledStates() {
        CubeModel cube = new CubeModel();
        Random random = new Random(4831);
        for (int sample = 0; sample < 100; sample++) {
            cube.applyMove(new Move(RotationAxis.values()[random.nextInt(3)],
                Set.of(random.nextInt(3) - 1), random.nextBoolean() ? 1 : -1));
            int before = WhiteCrossEdgeSolver.encode(Snapshot.capture(cube.cubies()).current());
            for (int m = 0; m < WhiteCrossEdgeSolver.MOVES.size(); m++) {
                Move move = WhiteCrossEdgeSolver.MOVES.get(m);
                cube.applyMove(move);
                assertEquals(WhiteCrossEdgeSolver.encode(Snapshot.capture(cube.cubies()).current()),
                    WhiteCrossEdgeSolver.nextState(before, m));
                cube.applyMove(new Move(move.axis(), move.layers(), -move.quarterTurns()));
            }
        }
    }

    @Test
    void solvesFlippedWhiteEdgesAlreadyInTheirSlots() {
        CubeModel cube = new CubeModel();
        CubieModel red = cube.cubies().stream().filter(c -> c.homeX() == 1 && c.homeY() == 1 && c.homeZ() == 0).findFirst().orElseThrow();
        red.quarterTurn(RotationAxis.Z);
        red.quarterTurn(RotationAxis.X);
        red.quarterTurn(RotationAxis.X);
        CubieModel orange = cube.cubies().stream().filter(c -> c.homeX() == -1 && c.homeY() == 1 && c.homeZ() == 0).findFirst().orElseThrow();
        orange.quarterTurn(RotationAxis.Z);
        orange.quarterTurn(RotationAxis.Y);
        orange.quarterTurn(RotationAxis.Y);
        assertEquals(new CubeCoordinate(1, 1, 0), red.coordinate());
        assertEquals(new CubeCoordinate(-1, 1, 0), orange.coordinate());
        assertEquals(2, Integer.bitCount(Snapshot.capture(cube.cubies()).solvedMask()));
        finishCross(cube);
    }

    @Test
    void seededScramblesWithSlicesAndWideMovesFinishWithinFourClicks() {
        Random random = new Random(78219);
        Set<Integer> observedProgress = new HashSet<>();
        List<Set<Integer>> layers = List.of(Set.of(-1), Set.of(0), Set.of(1), Set.of(-1, 0), Set.of(0, 1), Set.of(-1, 0, 1));
        for (int sample = 0; sample < 100; sample++) {
            CubeModel cube = new CubeModel();
            for (int i = 0; i < 25; i++) {
                cube.applyMove(new Move(RotationAxis.values()[random.nextInt(3)],
                    layers.get(random.nextInt(layers.size())), random.nextBoolean() ? 1 : -1));
            }
            for (int click = 0; click < 4 && Snapshot.capture(cube.cubies()).solvedMask() != 15; click++) {
                observedProgress.add(Integer.bitCount(Snapshot.capture(cube.cubies()).solvedMask()));
                assertProgress(cube);
            }
            assertTrue(new BeginnerMethodValidator().isWhiteCrossSolved(cube.cubies()));
        }
        assertEquals(Set.of(0, 1, 2, 3), observedProgress);
    }

    @Test
    void worksInAll24WholeCubeOrientations() {
        Queue<List<Move>> queue = new ArrayDeque<>();
        Set<List<WhiteCrossEdgeSolver.EdgeState>> seen = new HashSet<>();
        queue.add(List.of());
        while (!queue.isEmpty()) {
            List<Move> orientation = queue.remove();
            CubeModel cube = new CubeModel();
            run(cube, "F R U B' L");
            orientation.forEach(cube::applyMove);
            if (!seen.add(Snapshot.capture(cube.cubies()).goals())) continue;
            finishCross(cube);
            for (RotationAxis axis : RotationAxis.values()) {
                List<Move> next = new ArrayList<>(orientation);
                next.add(new Move(axis, Set.of(-1, 0, 1), 1));
                queue.add(next);
            }
        }
        assertEquals(24, seen.size());
    }

    @Test
    void calculationDoesNotMutateLiveCubeAndSnapshotSurvivesLaterMoves() {
        CubeModel cube = new CubeModel();
        run(cube, "F R U");
        Snapshot snapshot = Snapshot.capture(cube.cubies());
        var solution = solver.solve(snapshot);
        assertEquals(snapshot, Snapshot.capture(cube.cubies()));
        run(cube, "D B");
        assertEquals(solution, solver.solve(snapshot));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.current().clear());
        assertThrows(UnsupportedOperationException.class, () -> solution.moves().clear());
    }

    private void finishCross(CubeModel cube) {
        for (int click = 0; click < 4 && Snapshot.capture(cube.cubies()).solvedMask() != 15; click++) assertProgress(cube);
        assertTrue(new BeginnerMethodValidator().isWhiteCrossSolved(cube.cubies()));
    }

    private void assertProgress(CubeModel cube) {
        Snapshot before = Snapshot.capture(cube.cubies());
        var result = solver.solve(before);
        assertEquals(Status.FOUND, result.status());
        assertFalse(result.moves().isEmpty());
        assertEquals(result.moves(), AlgorithmParser.parse(result.notation()).moves());
        for (Move move : result.moves()) {
            assertEquals(1, move.layers().size());
            assertFalse(move.layers().contains(0));
            assertEquals(1, Math.abs(move.quarterTurns()));
            cube.applyMove(move);
        }
        int after = Snapshot.capture(cube.cubies()).solvedMask();
        assertEquals(before.solvedMask(), after & before.solvedMask());
        assertTrue(Integer.bitCount(after) > Integer.bitCount(before.solvedMask()));
        int newEdges = after & ~before.solvedMask();
        assertEquals(Integer.numberOfTrailingZeros(newEdges), result.target().ordinal());
    }

    private boolean canImprove(CubeModel cube, int initial, int remaining) {
        int mask = Snapshot.capture(cube.cubies()).solvedMask();
        if ((mask & initial) == initial && mask != initial) return true;
        if (remaining == 0) return false;
        for (Move move : WhiteCrossEdgeSolver.MOVES) {
            cube.applyMove(move);
            boolean found = canImprove(cube, initial, remaining - 1);
            cube.applyMove(new Move(move.axis(), move.layers(), -move.quarterTurns()));
            if (found) return true;
        }
        return false;
    }

    private static void run(CubeModel cube, String notation) {
        AlgorithmParser.parse(notation).moves().forEach(cube::applyMove);
    }
}
