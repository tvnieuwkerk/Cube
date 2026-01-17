package nl.tvn.cube.model;

import java.util.ArrayList;
import java.util.List;
import nl.tvn.cube.util.Vector3i;

public final class CubeRotator {
    private CubeRotator() {
    }

    public static int angleSign(RotationSpec spec, TurnDirection direction) {
        int directionSign = direction == TurnDirection.CLOCKWISE ? -1 : 1;
        return directionSign * spec.axisSign() * spec.directionMultiplier();
    }

    public static List<Cubie> selectCubies(List<Cubie> cubies, RotationSpec spec) {
        List<Cubie> selected = new ArrayList<>();
        for (Cubie cubie : cubies) {
            Vector3i pos = cubie.position();
            if (spec.layer() == null) {
                selected.add(cubie);
                continue;
            }
            int coordinate = switch (spec.axis()) {
                case X -> pos.x();
                case Y -> pos.y();
                case Z -> pos.z();
            };
            if (spec.layers().contains(coordinate)) {
                selected.add(cubie);
            }
        }
        return selected;
    }

    public static void rotateCubies(List<Cubie> cubies, Axis axis, int angleSign) {
        for (Cubie cubie : cubies) {
            Vector3i newPosition = rotatePosition(cubie.position(), axis, angleSign);
            cubie.setPosition(newPosition);
        }
    }

    public static Vector3i rotatePosition(Vector3i position, Axis axis, int angleSign) {
        Vector3i rotated = position;
        int steps = Math.abs(angleSign);
        int stepSign = angleSign >= 0 ? 1 : -1;
        for (int i = 0; i < steps; i++) {
            rotated = rotatePosition90(rotated, axis, stepSign);
        }
        return rotated;
    }

    private static Vector3i rotatePosition90(Vector3i position, Axis axis, int angleSign) {
        int x = position.x();
        int y = position.y();
        int z = position.z();
        return switch (axis) {
            case X -> angleSign > 0
                ? new Vector3i(x, -z, y)
                : new Vector3i(x, z, -y);
            case Y -> angleSign > 0
                ? new Vector3i(z, y, -x)
                : new Vector3i(-z, y, x);
            case Z -> angleSign > 0
                ? new Vector3i(-y, x, z)
                : new Vector3i(y, -x, z);
        };
    }
}
