package nl.tvn.cube.model;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class CubieModel {
    private final CubeCoordinate coordinate;
    private final EnumMap<Face, CubeColor> stickers;

    public CubieModel(int x, int y, int z) {
        this.coordinate = new CubeCoordinate(x, y, z);
        this.stickers = new EnumMap<>(Face.class);
        initializeStickers(x, y, z);
    }

    public CubeCoordinate coordinate() {
        return coordinate;
    }

    public CubeColor colorOnFace(Face face) {
        return stickers.get(face);
    }

    public Set<CubeColor> stickerColors() {
        return EnumSet.copyOf(stickers.values());
    }

    public void rotate(RotationAxis axis, int turn) {
        rotateCoordinate(axis, turn);
        rotateStickers(axis, turn);
    }

    public void resetStickers() {
        stickers.clear();
        initializeStickers(coordinate.x(), coordinate.y(), coordinate.z());
    }

    private void initializeStickers(int x, int y, int z) {
        if (x == 1) {
            stickers.put(Face.RIGHT, CubeColor.RED);
        }
        if (x == -1) {
            stickers.put(Face.LEFT, CubeColor.ORANGE);
        }
        if (y == 1) {
            stickers.put(Face.UP, CubeColor.WHITE);
        }
        if (y == -1) {
            stickers.put(Face.DOWN, CubeColor.YELLOW);
        }
        if (z == 1) {
            stickers.put(Face.FRONT, CubeColor.GREEN);
        }
        if (z == -1) {
            stickers.put(Face.BACK, CubeColor.BLUE);
        }
    }

    private void rotateCoordinate(RotationAxis axis, int turn) {
        int x = coordinate.x();
        int y = coordinate.y();
        int z = coordinate.z();

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

        coordinate.set(newX, newY, newZ);
    }

    private void rotateStickers(RotationAxis axis, int turn) {
        int steps = Math.abs(turn);
        int direction = Integer.signum(turn);
        for (int i = 0; i < steps; i++) {
            switch (axis) {
                case X -> rotateCycle(Face.UP, Face.BACK, Face.DOWN, Face.FRONT, direction);
                case Y -> rotateCycle(Face.FRONT, Face.LEFT, Face.BACK, Face.RIGHT, direction);
                case Z -> rotateCycle(Face.UP, Face.RIGHT, Face.DOWN, Face.LEFT, direction);
            }
        }
    }

    private void rotateCycle(Face a, Face b, Face c, Face d, int direction) {
        if (direction < 0) {
            rotateCycle(a, d, c, b, 1);
            return;
        }
        Map<Face, CubeColor> snapshot = new EnumMap<>(stickers);
        setFaceColor(b, snapshot.get(a));
        setFaceColor(c, snapshot.get(b));
        setFaceColor(d, snapshot.get(c));
        setFaceColor(a, snapshot.get(d));
    }

    private void setFaceColor(Face face, CubeColor color) {
        if (color == null) {
            stickers.remove(face);
            return;
        }
        stickers.put(face, color);
    }
}
