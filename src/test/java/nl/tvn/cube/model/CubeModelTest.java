package nl.tvn.cube.model;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class CubeModelTest {
    private static final List<CubeVector> BASIS = List.of(new CubeVector(1, 0, 0),
        new CubeVector(0, 1, 0), new CubeVector(0, 0, 1));

    @Test
    void movesAndInversesRestoreExactStateForEveryLayerCombination() {
        for (RotationAxis axis : RotationAxis.values()) {
            for (Set<Integer> layers : List.of(Set.of(-1), Set.of(0), Set.of(1),
                    Set.of(-1, 0), Set.of(0, 1), Set.of(-1, 0, 1))) {
                for (int turns : new int[] {-2, -1, 1, 2}) {
                    CubeModel cube = new CubeModel();
                    List<Object> before = state(cube);
                    cube.applyMove(new Move(axis, layers, turns));
                    assertNotEquals(before, state(cube));
                    cube.applyMove(new Move(axis, layers, -turns));
                    assertEquals(before, state(cube));
                    for (int i = 0; i < 4; i++) cube.applyMove(new Move(axis, layers, 1));
                    assertEquals(before, state(cube));
                    cube.applyMove(new Move(axis, layers, 2));
                    List<Object> halfTurn = state(cube);
                    cube.reset();
                    cube.applyMove(new Move(axis, layers, 1));
                    cube.applyMove(new Move(axis, layers, 1));
                    assertEquals(halfTurn, state(cube));
                    cube.reset();
                    assertEquals(before, state(cube));
                }
            }
        }
    }

    @Test
    void rightTurnMovesFrontUpperCornerAndItsStickersTogether() {
        CubeModel cube = new CubeModel();
        CubieModel corner = cube.cubies().stream().filter(c -> c.homeX() == 1 && c.homeY() == 1 && c.homeZ() == 1).findFirst().orElseThrow();
        cube.applyMove(new Move(RotationAxis.X, Set.of(1), 1));
        assertEquals(new CubeCoordinate(1, 1, -1), corner.coordinate());
        assertEquals(new CubeVector(0, 0, -1), corner.direction(new CubeVector(0, 1, 0)));
        assertEquals(new CubeVector(0, 1, 0), corner.direction(new CubeVector(0, 0, 1)));
    }

    @Test
    void solvedValidationIsInvariantUnderAll24CubeOrientations() {
        Queue<List<Move>> queue = new ArrayDeque<>();
        Set<List<Object>> seen = new HashSet<>();
        queue.add(List.of());
        BeginnerMethodValidator validator = new BeginnerMethodValidator();
        while (!queue.isEmpty()) {
            List<Move> moves = queue.remove();
            CubeModel cube = new CubeModel();
            moves.forEach(cube::applyMove);
            if (!seen.add(state(cube))) continue;
            assertTrue(validator.validate(cube.cubies()).values().stream().allMatch(Boolean::booleanValue));
            for (RotationAxis axis : RotationAxis.values()) {
                List<Move> next = new ArrayList<>(moves);
                next.add(new Move(axis, Set.of(-1, 0, 1), 1));
                queue.add(next);
            }
        }
        assertEquals(24, seen.size());
    }

    @Test
    void centreSpinDoesNotMakeSolvedCubeUnsolved() {
        CubeModel cube = new CubeModel();
        CubieModel centre = cube.cubies().stream().filter(c -> c.homeX() == 1 && c.homeY() == 0 && c.homeZ() == 0).findFirst().orElseThrow();
        centre.quarterTurn(RotationAxis.X);
        assertTrue(new BeginnerMethodValidator().isCubeSolved(cube.cubies()));
    }

    @Test
    void flippedWhiteEdgeInItsSlotFailsCrossAndAllLaterIndicators() {
        CubeModel cube = new CubeModel();
        CubieModel edge = cube.cubies().stream().filter(c -> c.homeX() == 1 && c.homeY() == 1 && c.homeZ() == 0).findFirst().orElseThrow();
        edge.quarterTurn(RotationAxis.Z);
        edge.quarterTurn(RotationAxis.X);
        edge.quarterTurn(RotationAxis.X);
        assertEquals(new CubeCoordinate(1, 1, 0), edge.coordinate());
        assertEquals(new CubeVector(1, 0, 0), edge.direction(new CubeVector(0, 1, 0)));
        assertTrue(new BeginnerMethodValidator().validate(cube.cubies()).values().stream().noneMatch(Boolean::booleanValue));
    }

    @Test
    void yellowEdgeInYellowLayerMustActuallyFaceYellowSide() {
        CubeModel cube = new CubeModel();
        CubieModel edge = cube.cubies().stream().filter(c -> c.homeX() == 1 && c.homeY() == -1 && c.homeZ() == 0).findFirst().orElseThrow();
        edge.quarterTurn(RotationAxis.Z);
        edge.quarterTurn(RotationAxis.Y);
        edge.quarterTurn(RotationAxis.Y);
        assertEquals(new CubeCoordinate(1, -1, 0), edge.coordinate());
        var results = new BeginnerMethodValidator().validate(cube.cubies());
        assertTrue(results.get(BeginnerMethodStep.MIDDLE_LAYER_EDGES));
        assertFalse(results.get(BeginnerMethodStep.YELLOW_CROSS));
        assertFalse(results.get(BeginnerMethodStep.YELLOW_CORNER_ORIENTATION));
    }

    private static List<Object> state(CubeModel cube) {
        List<Object> result = new ArrayList<>();
        for (CubieModel cubie : cube.cubies()) {
            result.add(new CubeVector(cubie.coordinate().x(), cubie.coordinate().y(), cubie.coordinate().z()));
            BASIS.forEach(axis -> result.add(cubie.direction(axis)));
        }
        return result;
    }
}
