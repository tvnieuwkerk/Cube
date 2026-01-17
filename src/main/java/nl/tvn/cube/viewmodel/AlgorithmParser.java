package nl.tvn.cube.viewmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import nl.tvn.cube.model.Move;

public final class AlgorithmParser {
    private AlgorithmParser() {
    }

    public static AlgorithmParseResult parse(String input) {
        if (input == null || input.isBlank()) {
            return AlgorithmParseResult.error("Enter an algorithm string.");
        }
        List<Move> moves = new ArrayList<>();
        int index = 0;
        while (index < input.length()) {
            char raw = input.charAt(index);
            if (Character.isWhitespace(raw)) {
                index++;
                continue;
            }
            char token = Character.toUpperCase(raw);
            if (isMoveToken(token)) {
                boolean counterClockwise = false;
                index++;
                if (index < input.length() && input.charAt(index) == '\'') {
                    counterClockwise = true;
                    index++;
                }
                int repeat = 0;
                int repeatStart = index;
                while (index < input.length() && Character.isDigit(input.charAt(index))) {
                    repeat = repeat * 10 + Character.digit(input.charAt(index), 10);
                    index++;
                }
                if (repeatStart == index) {
                    repeat = 1;
                }
                if (repeat < 1) {
                    return AlgorithmParseResult.error("Repeat count must be at least 1 at position " + (repeatStart + 1) + ".");
                }
                Optional<Move> move = MoveFactory.fromNotation(token, counterClockwise);
                if (move.isEmpty()) {
                    return AlgorithmParseResult.error("Unsupported move '" + token + "' at position " + (index + 1) + ".");
                }
                for (int i = 0; i < repeat; i++) {
                    moves.add(move.get());
                }
                continue;
            }
            if (raw == '\'') {
                return AlgorithmParseResult.error("Apostrophe must follow a move at position " + (index + 1) + ".");
            }
            if (Character.isDigit(raw)) {
                return AlgorithmParseResult.error("Repeat count must follow a move at position " + (index + 1) + ".");
            }
            return AlgorithmParseResult.error("Invalid character '" + raw + "' at position " + (index + 1) + ".");
        }
        if (moves.isEmpty()) {
            return AlgorithmParseResult.error("Enter an algorithm string.");
        }
        return AlgorithmParseResult.success(moves);
    }

    private static boolean isMoveToken(char token) {
        return switch (token) {
            case 'F', 'B', 'R', 'L', 'U', 'D', 'M', 'E', 'S' -> true;
            default -> false;
        };
    }
}
