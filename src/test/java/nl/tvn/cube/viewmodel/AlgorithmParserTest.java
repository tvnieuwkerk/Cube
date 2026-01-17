package nl.tvn.cube.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import nl.tvn.cube.model.Move;
import nl.tvn.cube.model.RotationAxis;
import org.junit.jupiter.api.Test;

class AlgorithmParserTest {
    @Test
    void parsesSimpleAlgorithm() {
        AlgorithmParseResult result = AlgorithmParser.parse("R U R' U'");

        assertTrue(result.isValid());
        List<Move> moves = result.moves();
        assertEquals(4, moves.size());
        assertEquals(new Move(RotationAxis.X, Set.of(1), 1), moves.get(0));
        assertEquals(new Move(RotationAxis.Y, Set.of(1), 1), moves.get(1));
        assertEquals(new Move(RotationAxis.X, Set.of(1), -1), moves.get(2));
        assertEquals(new Move(RotationAxis.Y, Set.of(1), -1), moves.get(3));
    }

    @Test
    void repeatsMovesWithDigits() {
        AlgorithmParseResult result = AlgorithmParser.parse("M2 R'2");

        assertTrue(result.isValid());
        List<Move> moves = result.moves();
        assertEquals(4, moves.size());
        assertEquals(new Move(RotationAxis.X, Set.of(0), 1), moves.get(0));
        assertEquals(new Move(RotationAxis.X, Set.of(0), 1), moves.get(1));
        assertEquals(new Move(RotationAxis.X, Set.of(1), -1), moves.get(2));
        assertEquals(new Move(RotationAxis.X, Set.of(1), -1), moves.get(3));
    }

    @Test
    void rejectsDigitsWithoutMove() {
        AlgorithmParseResult result = AlgorithmParser.parse("2R");

        assertFalse(result.isValid());
    }

    @Test
    void rejectsInvalidCharacters() {
        AlgorithmParseResult result = AlgorithmParser.parse("R A");

        assertFalse(result.isValid());
    }

    @Test
    void rejectsZeroRepeatCount() {
        AlgorithmParseResult result = AlgorithmParser.parse("R0");

        assertFalse(result.isValid());
    }
}
