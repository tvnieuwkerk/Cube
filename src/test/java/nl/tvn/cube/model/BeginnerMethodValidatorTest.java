package nl.tvn.cube.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BeginnerMethodValidatorTest {
    private final BeginnerMethodValidator validator = new BeginnerMethodValidator();

    @Test
    void validatesWhiteCross() {
        CubeModel model = new CubeModel();
        assertTrue(validator.isWhiteCrossSolved(model.cubies()));

        applyMove(model, new Move(RotationAxis.Y, Set.of(1), 1));
        assertFalse(validator.isWhiteCrossSolved(model.cubies()));
    }

    @Test
    void validatesWhiteCorners() {
        CubeModel model = new CubeModel();
        assertTrue(validator.isWhiteCornersSolved(model.cubies()));

        applyMove(model, new Move(RotationAxis.Y, Set.of(1), 1));
        assertFalse(validator.isWhiteCornersSolved(model.cubies()));
    }

    @Test
    void validatesMiddleLayerEdges() {
        CubeModel model = new CubeModel();
        assertTrue(validator.isMiddleLayerEdgesSolved(model.cubies()));

        applyMove(model, new Move(RotationAxis.Z, Set.of(1), 1));
        assertFalse(validator.isMiddleLayerEdgesSolved(model.cubies()));
    }

    @Test
    void validatesYellowCross() {
        CubeModel model = new CubeModel();
        assertTrue(validator.isYellowCrossSolved(model.cubies()));

        applyMove(model, new Move(RotationAxis.X, Set.of(1), 1));
        assertFalse(validator.isYellowCrossSolved(model.cubies()));
    }

    @Test
    void validatesYellowEdgeAlignment() {
        CubeModel model = new CubeModel();
        assertTrue(validator.isYellowEdgesAligned(model.cubies()));

        applyMove(model, new Move(RotationAxis.Y, Set.of(-1), 1));
        assertFalse(validator.isYellowEdgesAligned(model.cubies()));
    }

    @Test
    void validatesYellowCornerPosition() {
        CubeModel model = new CubeModel();
        assertTrue(validator.areYellowCornersPositioned(model.cubies()));

        applyMove(model, new Move(RotationAxis.Y, Set.of(-1), 1));
        assertFalse(validator.areYellowCornersPositioned(model.cubies()));
    }

    @Test
    void validatesFinalLayerOrientation() {
        CubeModel model = new CubeModel();
        assertTrue(validator.isCubeSolved(model.cubies()));

        applyMove(model, new Move(RotationAxis.X, Set.of(1), 1));
        assertFalse(validator.isCubeSolved(model.cubies()));
    }

    private static void applyMove(CubeModel model, Move move) {
        int turns = normalizeTurns(move.quarterTurns());
        if (turns == 0) {
            return;
        }
        List<CubieModel> affected = new ArrayList<>();
        for (CubieModel cubie : model.cubies()) {
            if (isInLayer(cubie, move.axis(), move.layers())) {
                affected.add(cubie);
            }
        }
        int step = turns > 0 ? 1 : -1;
        for (int i = 0; i < Math.abs(turns); i++) {
            applyQuarterTurn(affected, move.axis(), step);
        }
    }

    private static void applyQuarterTurn(List<CubieModel> affected, RotationAxis axis, int turn) {
        for (CubieModel cubie : affected) {
            rotateCoordinate(cubie, axis, turn);
        }
    }

    private static boolean isInLayer(CubieModel cubie, RotationAxis axis, Set<Integer> layers) {
        return switch (axis) {
            case X -> layers.contains(cubie.coordinate().x());
            case Y -> layers.contains(cubie.coordinate().y());
            case Z -> layers.contains(cubie.coordinate().z());
        };
    }

    private static void rotateCoordinate(CubieModel cubie, RotationAxis axis, int turn) {
        int x = cubie.coordinate().x();
        int y = cubie.coordinate().y();
        int z = cubie.coordinate().z();

        int newX = x;
        int newY = y;
        int newZ = z;

        if (axis == RotationAxis.X) {
            if (turn > 0) {
                newY = z;
                newZ = -y;
            } else {
                newY = -z;
                newZ = y;
            }
        } else if (axis == RotationAxis.Y) {
            if (turn > 0) {
                newX = -z;
                newZ = x;
            } else {
                newX = z;
                newZ = -x;
            }
        } else if (axis == RotationAxis.Z) {
            if (turn > 0) {
                newX = y;
                newY = -x;
            } else {
                newX = -y;
                newY = x;
            }
        }

        cubie.coordinate().set(newX, newY, newZ);
    }

    private static int normalizeTurns(int turns) {
        int normalized = turns % 4;
        if (normalized == 3) {
            return -1;
        }
        if (normalized == -3) {
            return 1;
        }
        return normalized;
    }
}
