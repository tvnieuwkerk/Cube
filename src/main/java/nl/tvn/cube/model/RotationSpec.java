package nl.tvn.cube.model;

import java.util.List;

public record RotationSpec(Axis axis, Integer layer, List<Integer> layers, int axisSign, int directionMultiplier) {
    public static RotationSpec fromFace(Face face) {
        return switch (face) {
            case FRONT -> new RotationSpec(Axis.Z, 1, List.of(1), 1, 1);
            case BACK -> new RotationSpec(Axis.Z, -1, List.of(-1), -1, 1);
            case RIGHT -> new RotationSpec(Axis.X, 1, List.of(1), 1, 1);
            case LEFT -> new RotationSpec(Axis.X, -1, List.of(-1), -1, 1);
            case UP -> new RotationSpec(Axis.Y, 1, List.of(1), 1, 1);
            case DOWN -> new RotationSpec(Axis.Y, -1, List.of(-1), -1, 1);
        };
    }

    public static RotationSpec fromSlice(Axis axis) {
        int directionMultiplier = switch (axis) {
            case X -> -1;
            case Y -> -1;
            case Z -> 1;
        };
        return new RotationSpec(axis, 0, List.of(0), 1, directionMultiplier);
    }

    public static RotationSpec fromWide(Face face) {
        return switch (face) {
            case FRONT -> new RotationSpec(Axis.Z, 1, List.of(1, 0), 1, 1);
            case BACK -> new RotationSpec(Axis.Z, -1, List.of(-1, 0), -1, 1);
            case RIGHT -> new RotationSpec(Axis.X, 1, List.of(1, 0), 1, 1);
            case LEFT -> new RotationSpec(Axis.X, -1, List.of(-1, 0), -1, 1);
            case UP -> new RotationSpec(Axis.Y, 1, List.of(1, 0), 1, 1);
            case DOWN -> new RotationSpec(Axis.Y, -1, List.of(-1, 0), -1, 1);
        };
    }

    public static RotationSpec fromRotation(Axis axis) {
        return new RotationSpec(axis, null, List.of(-1, 0, 1), 1, 1);
    }
}
