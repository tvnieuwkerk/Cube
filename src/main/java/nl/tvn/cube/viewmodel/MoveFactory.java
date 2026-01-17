package nl.tvn.cube.viewmodel;

import java.util.Optional;
import java.util.Set;
import javafx.scene.input.KeyEvent;
import nl.tvn.cube.model.Move;
import nl.tvn.cube.model.RotationAxis;

public final class MoveFactory {
    private MoveFactory() {
    }

    public static Optional<Move> fromKeyEvent(KeyEvent event) {
        if (event.isAltDown()) {
            return Optional.empty();
        }
        boolean counterClockwise = event.isShiftDown();
        boolean doubleTurn = event.isControlDown();

        String key = event.getCode().getName().toUpperCase();
        int baseTurns = counterClockwise ? -1 : 1;
        int turns = doubleTurn ? baseTurns * 2 : baseTurns;

        return switch (key) {
            case "F" -> Optional.of(faceMove(RotationAxis.Z, Set.of(1), turns));
            case "B" -> Optional.of(faceMove(RotationAxis.Z, Set.of(-1), -turns));
            case "R" -> Optional.of(faceMove(RotationAxis.X, Set.of(1), turns));
            case "L" -> Optional.of(faceMove(RotationAxis.X, Set.of(-1), -turns));
            case "U" -> Optional.of(faceMove(RotationAxis.Y, Set.of(1), turns));
            case "D" -> Optional.of(faceMove(RotationAxis.Y, Set.of(-1), -turns));
            case "M" -> Optional.of(faceMove(RotationAxis.X, Set.of(0), turns));
            case "E" -> Optional.of(faceMove(RotationAxis.Y, Set.of(0), turns));
            case "S" -> Optional.of(faceMove(RotationAxis.Z, Set.of(0), turns));
            case "X" -> Optional.of(faceMove(RotationAxis.X, Set.of(-1, 0, 1), turns));
            case "Y" -> Optional.of(faceMove(RotationAxis.Y, Set.of(-1, 0, 1), turns));
            case "Z" -> Optional.of(faceMove(RotationAxis.Z, Set.of(-1, 0, 1), turns));
            default -> Optional.empty();
        };
    }

    public static Optional<Move> wideMove(KeyEvent event) {
        if (!event.isAltDown()) {
            return Optional.empty();
        }
        boolean counterClockwise = event.isShiftDown();
        boolean doubleTurn = event.isControlDown();
        String key = event.getCode().getName().toUpperCase();
        int baseTurns = counterClockwise ? -1 : 1;
        int turns = doubleTurn ? baseTurns * 2 : baseTurns;

        return switch (key) {
            case "R" -> Optional.of(new Move(RotationAxis.X, Set.of(1, 0), turns));
            case "L" -> Optional.of(new Move(RotationAxis.X, Set.of(-1, 0), -turns));
            case "U" -> Optional.of(new Move(RotationAxis.Y, Set.of(1, 0), turns));
            case "D" -> Optional.of(new Move(RotationAxis.Y, Set.of(-1, 0), -turns));
            case "F" -> Optional.of(new Move(RotationAxis.Z, Set.of(1, 0), turns));
            case "B" -> Optional.of(new Move(RotationAxis.Z, Set.of(-1, 0), -turns));
            default -> Optional.empty();
        };
    }

    private static Move faceMove(RotationAxis axis, Set<Integer> layers, int turns) {
        return new Move(axis, layers, turns);
    }
}
