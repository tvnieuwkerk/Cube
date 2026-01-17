package nl.tvn.cube.viewmodel;

import java.util.List;
import nl.tvn.cube.model.Move;

public record AlgorithmParseResult(List<Move> moves, String errorMessage) {
    public boolean isValid() {
        return errorMessage == null;
    }

    public static AlgorithmParseResult success(List<Move> moves) {
        return new AlgorithmParseResult(List.copyOf(moves), null);
    }

    public static AlgorithmParseResult error(String message) {
        return new AlgorithmParseResult(List.of(), message);
    }
}
