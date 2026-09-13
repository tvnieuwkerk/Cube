package nl.tvn.cube.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class BeginnerMethodValidatorTest {
    private final BeginnerMethodValidator validator = new BeginnerMethodValidator();

    @Test
    void validatesWhiteCross() {
        CubeModel model = new CubeModel();
        assertTrue(validator.isWhiteCrossSolved(model.cubies()));

        model.applyMove(new Move(RotationAxis.Y, Set.of(1), 1));
        assertFalse(validator.isWhiteCrossSolved(model.cubies()));
    }

    @Test
    void validatesWhiteCorners() {
        CubeModel model = new CubeModel();
        assertTrue(validator.isWhiteCornersSolved(model.cubies()));

        model.applyMove(new Move(RotationAxis.Y, Set.of(1), 1));
        assertFalse(validator.isWhiteCornersSolved(model.cubies()));
    }

    @Test
    void validatesMiddleLayerEdges() {
        CubeModel model = new CubeModel();
        assertTrue(validator.isMiddleLayerEdgesSolved(model.cubies()));

        model.applyMove(new Move(RotationAxis.Z, Set.of(1), 1));
        assertFalse(validator.isMiddleLayerEdgesSolved(model.cubies()));
    }

    @Test
    void validatesYellowCross() {
        CubeModel model = new CubeModel();
        assertTrue(validator.isYellowCrossSolved(model.cubies()));

        model.applyMove(new Move(RotationAxis.X, Set.of(1), 1));
        assertFalse(validator.isYellowCrossSolved(model.cubies()));
    }

    @Test
    void validatesYellowEdgeAlignment() {
        CubeModel model = new CubeModel();
        assertTrue(validator.isYellowEdgesAligned(model.cubies()));

        model.applyMove(new Move(RotationAxis.Y, Set.of(-1), 1));
        assertFalse(validator.isYellowEdgesAligned(model.cubies()));
    }

    @Test
    void validatesYellowCornerPosition() {
        CubeModel model = new CubeModel();
        assertTrue(validator.areYellowCornersPositioned(model.cubies()));

        model.applyMove(new Move(RotationAxis.Y, Set.of(-1), 1));
        assertFalse(validator.areYellowCornersPositioned(model.cubies()));
    }

    @Test
    void validatesFinalLayerOrientation() {
        CubeModel model = new CubeModel();
        assertTrue(validator.isCubeSolved(model.cubies()));

        model.applyMove(new Move(RotationAxis.X, Set.of(1), 1));
        assertFalse(validator.isCubeSolved(model.cubies()));
    }

}
