package nl.tvn.cube.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import nl.tvn.cube.viewmodel.MoveFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BeginnerMethodValidatorTest {
    private CubeModel model;
    private BeginnerMethodValidator validator;

    @BeforeEach
    void setUp() {
        model = new CubeModel();
        validator = new BeginnerMethodValidator(model);
    }

    @Test
    void whiteCrossSolvedOnSolvedCube() {
        assertTrue(validator.isStepSolved(BeginnerStep.WHITE_CROSS));
    }

    @Test
    void whiteCrossFailsAfterRightTurn() {
        applyMove('R');
        assertFalse(validator.isStepSolved(BeginnerStep.WHITE_CROSS));
    }

    @Test
    void whiteCornersSolvedOnSolvedCube() {
        assertTrue(validator.isStepSolved(BeginnerStep.WHITE_CORNERS));
    }

    @Test
    void whiteCornersFailAfterRightTurn() {
        applyMove('R');
        assertFalse(validator.isStepSolved(BeginnerStep.WHITE_CORNERS));
    }

    @Test
    void middleLayerSolvedOnSolvedCube() {
        assertTrue(validator.isStepSolved(BeginnerStep.MIDDLE_LAYER));
    }

    @Test
    void middleLayerFailsAfterFrontTurn() {
        applyMove('F');
        assertFalse(validator.isStepSolved(BeginnerStep.MIDDLE_LAYER));
    }

    @Test
    void yellowCrossSolvedOnSolvedCube() {
        assertTrue(validator.isStepSolved(BeginnerStep.YELLOW_CROSS));
    }

    @Test
    void yellowCrossFailsAfterFrontTurn() {
        applyMove('F');
        assertFalse(validator.isStepSolved(BeginnerStep.YELLOW_CROSS));
    }

    @Test
    void yellowEdgeAlignmentSolvedOnSolvedCube() {
        assertTrue(validator.isStepSolved(BeginnerStep.YELLOW_EDGE_ALIGNMENT));
    }

    @Test
    void yellowEdgeAlignmentFailsAfterDownTurn() {
        applyMove('D');
        assertTrue(validator.isStepSolved(BeginnerStep.YELLOW_CROSS));
        assertFalse(validator.isStepSolved(BeginnerStep.YELLOW_EDGE_ALIGNMENT));
    }

    @Test
    void yellowCornerPositionSolvedOnSolvedCube() {
        assertTrue(validator.isStepSolved(BeginnerStep.YELLOW_CORNER_POSITION));
    }

    @Test
    void yellowCornerPositionFailsAfterDownTurn() {
        applyMove('D');
        assertFalse(validator.isStepSolved(BeginnerStep.YELLOW_CORNER_POSITION));
    }

    @Test
    void yellowCornerOrientationSolvedOnSolvedCube() {
        assertTrue(validator.isStepSolved(BeginnerStep.YELLOW_CORNER_ORIENTATION));
    }

    @Test
    void yellowCornerOrientationFailsAfterRightTurn() {
        applyMove('R');
        assertFalse(validator.isStepSolved(BeginnerStep.YELLOW_CORNER_ORIENTATION));
    }

    private void applyMove(char token) {
        Move move = MoveFactory.fromNotation(token, false).orElseThrow();
        CubeRotator.applyMove(model, move);
    }
}
