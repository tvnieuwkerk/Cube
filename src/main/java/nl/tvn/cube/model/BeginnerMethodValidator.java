package nl.tvn.cube.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public final class BeginnerMethodValidator {
    private final CubeModel cubeModel;

    public BeginnerMethodValidator(CubeModel cubeModel) {
        this.cubeModel = cubeModel;
    }

    public boolean isStepSolved(BeginnerStep step) {
        return switch (step) {
            case WHITE_CROSS -> isWhiteCrossSolved();
            case WHITE_CORNERS -> isWhiteCornersSolved();
            case MIDDLE_LAYER -> isMiddleLayerSolved();
            case YELLOW_CROSS -> isYellowCrossSolved();
            case YELLOW_EDGE_ALIGNMENT -> isYellowEdgeAlignmentSolved();
            case YELLOW_CORNER_POSITION -> isYellowCornerPositionSolved();
            case YELLOW_CORNER_ORIENTATION -> isYellowCornerOrientationSolved();
        };
    }

    private boolean isWhiteCrossSolved() {
        Orientation orientation = orientation();
        return edgeMatches(orientation, 0, 1, 1, Face.UP, CubeColor.WHITE, Face.FRONT, CubeColor.GREEN)
            && edgeMatches(orientation, 0, 1, -1, Face.UP, CubeColor.WHITE, Face.BACK, CubeColor.BLUE)
            && edgeMatches(orientation, 1, 1, 0, Face.UP, CubeColor.WHITE, Face.RIGHT, CubeColor.RED)
            && edgeMatches(orientation, -1, 1, 0, Face.UP, CubeColor.WHITE, Face.LEFT, CubeColor.ORANGE);
    }

    private boolean isWhiteCornersSolved() {
        Orientation orientation = orientation();
        return isWhiteCrossSolved()
            && cornerMatches(orientation, 1, 1, 1, Face.RIGHT, CubeColor.RED, Face.FRONT, CubeColor.GREEN)
            && cornerMatches(orientation, -1, 1, 1, Face.LEFT, CubeColor.ORANGE, Face.FRONT, CubeColor.GREEN)
            && cornerMatches(orientation, 1, 1, -1, Face.RIGHT, CubeColor.RED, Face.BACK, CubeColor.BLUE)
            && cornerMatches(orientation, -1, 1, -1, Face.LEFT, CubeColor.ORANGE, Face.BACK, CubeColor.BLUE);
    }

    private boolean isMiddleLayerSolved() {
        Orientation orientation = orientation();
        return isWhiteCornersSolved()
            && edgeMatches(orientation, 1, 0, 1, Face.RIGHT, CubeColor.RED, Face.FRONT, CubeColor.GREEN)
            && edgeMatches(orientation, 1, 0, -1, Face.RIGHT, CubeColor.RED, Face.BACK, CubeColor.BLUE)
            && edgeMatches(orientation, -1, 0, 1, Face.LEFT, CubeColor.ORANGE, Face.FRONT, CubeColor.GREEN)
            && edgeMatches(orientation, -1, 0, -1, Face.LEFT, CubeColor.ORANGE, Face.BACK, CubeColor.BLUE);
    }

    private boolean isYellowCrossSolved() {
        Orientation orientation = orientation();
        return isMiddleLayerSolved()
            && edgeHasColor(orientation, 0, -1, 1, Face.DOWN, CubeColor.YELLOW)
            && edgeHasColor(orientation, 0, -1, -1, Face.DOWN, CubeColor.YELLOW)
            && edgeHasColor(orientation, 1, -1, 0, Face.DOWN, CubeColor.YELLOW)
            && edgeHasColor(orientation, -1, -1, 0, Face.DOWN, CubeColor.YELLOW);
    }

    private boolean isYellowEdgeAlignmentSolved() {
        Orientation orientation = orientation();
        return isYellowCrossSolved()
            && edgeMatches(orientation, 0, -1, 1, Face.DOWN, CubeColor.YELLOW, Face.FRONT, CubeColor.GREEN)
            && edgeMatches(orientation, 0, -1, -1, Face.DOWN, CubeColor.YELLOW, Face.BACK, CubeColor.BLUE)
            && edgeMatches(orientation, 1, -1, 0, Face.DOWN, CubeColor.YELLOW, Face.RIGHT, CubeColor.RED)
            && edgeMatches(orientation, -1, -1, 0, Face.DOWN, CubeColor.YELLOW, Face.LEFT, CubeColor.ORANGE);
    }

    private boolean isYellowCornerPositionSolved() {
        Orientation orientation = orientation();
        return isYellowEdgeAlignmentSolved()
            && cornerColorSetMatches(orientation, 1, -1, 1, Set.of(CubeColor.RED, CubeColor.YELLOW, CubeColor.GREEN))
            && cornerColorSetMatches(orientation, -1, -1, 1, Set.of(CubeColor.ORANGE, CubeColor.YELLOW, CubeColor.GREEN))
            && cornerColorSetMatches(orientation, 1, -1, -1, Set.of(CubeColor.RED, CubeColor.YELLOW, CubeColor.BLUE))
            && cornerColorSetMatches(orientation, -1, -1, -1, Set.of(CubeColor.ORANGE, CubeColor.YELLOW, CubeColor.BLUE));
    }

    private boolean isYellowCornerOrientationSolved() {
        Orientation orientation = orientation();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    Vector3 actual = orientation.toActualCoordinate(x, y, z);
                    CubieModel cubie = cubeModel.cubieAt(actual.x(), actual.y(), actual.z());
                    if (cubie == null) {
                        return false;
                    }
                }
            }
        }
        return isFaceSolved(orientation, Face.UP, CubeColor.WHITE)
            && isFaceSolved(orientation, Face.DOWN, CubeColor.YELLOW)
            && isFaceSolved(orientation, Face.LEFT, CubeColor.ORANGE)
            && isFaceSolved(orientation, Face.RIGHT, CubeColor.RED)
            && isFaceSolved(orientation, Face.FRONT, CubeColor.GREEN)
            && isFaceSolved(orientation, Face.BACK, CubeColor.BLUE);
    }

    private boolean edgeMatches(
        Orientation orientation,
        int x,
        int y,
        int z,
        Face firstFace,
        CubeColor firstColor,
        Face secondFace,
        CubeColor secondColor
    ) {
        Vector3 actual = orientation.toActualCoordinate(x, y, z);
        CubieModel cubie = cubeModel.cubieAt(actual.x(), actual.y(), actual.z());
        if (cubie == null) {
            return false;
        }
        Face actualFirst = orientation.actualFaceForLogicalFace(firstFace);
        Face actualSecond = orientation.actualFaceForLogicalFace(secondFace);
        return firstColor == cubie.colorOnFace(actualFirst)
            && secondColor == cubie.colorOnFace(actualSecond);
    }

    private boolean edgeHasColor(Orientation orientation, int x, int y, int z, Face face, CubeColor color) {
        Vector3 actual = orientation.toActualCoordinate(x, y, z);
        CubieModel cubie = cubeModel.cubieAt(actual.x(), actual.y(), actual.z());
        if (cubie == null) {
            return false;
        }
        Face actualFace = orientation.actualFaceForLogicalFace(face);
        return color == cubie.colorOnFace(actualFace);
    }

    private boolean cornerMatches(
        Orientation orientation,
        int x,
        int y,
        int z,
        Face faceA,
        CubeColor colorA,
        Face faceB,
        CubeColor colorB
    ) {
        Vector3 actual = orientation.toActualCoordinate(x, y, z);
        CubieModel cubie = cubeModel.cubieAt(actual.x(), actual.y(), actual.z());
        if (cubie == null) {
            return false;
        }
        Face actualUp = orientation.actualFaceForLogicalFace(Face.UP);
        Face actualA = orientation.actualFaceForLogicalFace(faceA);
        Face actualB = orientation.actualFaceForLogicalFace(faceB);
        return CubeColor.WHITE == cubie.colorOnFace(actualUp)
            && colorA == cubie.colorOnFace(actualA)
            && colorB == cubie.colorOnFace(actualB);
    }

    private boolean cornerColorSetMatches(Orientation orientation, int x, int y, int z, Set<CubeColor> expected) {
        Vector3 actual = orientation.toActualCoordinate(x, y, z);
        CubieModel cubie = cubeModel.cubieAt(actual.x(), actual.y(), actual.z());
        if (cubie == null) {
            return false;
        }
        return cubie.stickerColors().equals(expected);
    }

    private boolean isFaceSolved(Orientation orientation, Face logicalFace, CubeColor expected) {
        Face actualFace = orientation.actualFaceForLogicalFace(logicalFace);
        for (CubieModel cubie : cubeModel.cubies()) {
            if (cubie.colorOnFace(actualFace) == null) {
                continue;
            }
            if (cubie.colorOnFace(actualFace) != expected) {
                return false;
            }
        }
        return true;
    }

    private Orientation orientation() {
        Map<CubeColor, Face> faceByColor = new EnumMap<>(CubeColor.class);
        for (Face face : Face.values()) {
            CubeColor color = centerColor(face);
            if (color != null) {
                faceByColor.put(color, face);
            }
        }
        Face upFace = faceByColor.getOrDefault(CubeColor.WHITE, Face.UP);
        Face frontFace = faceByColor.getOrDefault(CubeColor.GREEN, Face.FRONT);
        Vector3 up = Vector3.fromFace(upFace);
        Vector3 front = Vector3.fromFace(frontFace);
        Vector3 right = up.cross(front);
        if (right.isZero()) {
            right = Vector3.fromFace(Face.RIGHT);
        }
        return new Orientation(up, front, right);
    }

    private CubeColor centerColor(Face face) {
        Vector3 position = Vector3.fromFace(face);
        CubieModel center = cubeModel.cubieAt(position.x(), position.y(), position.z());
        if (center == null) {
            return null;
        }
        return center.colorOnFace(face);
    }

    private record Orientation(Vector3 up, Vector3 front, Vector3 right) {
        Face actualFaceForLogicalFace(Face logicalFace) {
            return Vector3.fromFace(logicalFace)
                .transform(right, up, front)
                .toFace();
        }

        Vector3 toActualCoordinate(int logicalX, int logicalY, int logicalZ) {
            Vector3 fromLogical = new Vector3(logicalX, logicalY, logicalZ);
            return fromLogical.transform(right, up, front);
        }
    }

    private record Vector3(int x, int y, int z) {
        static Vector3 fromFace(Face face) {
            return switch (face) {
                case UP -> new Vector3(0, 1, 0);
                case DOWN -> new Vector3(0, -1, 0);
                case LEFT -> new Vector3(-1, 0, 0);
                case RIGHT -> new Vector3(1, 0, 0);
                case FRONT -> new Vector3(0, 0, 1);
                case BACK -> new Vector3(0, 0, -1);
            };
        }

        Vector3 cross(Vector3 other) {
            return new Vector3(
                y * other.z - z * other.y,
                z * other.x - x * other.z,
                x * other.y - y * other.x
            );
        }

        boolean isZero() {
            return x == 0 && y == 0 && z == 0;
        }

        Vector3 transform(Vector3 basisX, Vector3 basisY, Vector3 basisZ) {
            return new Vector3(
                x * basisX.x + y * basisY.x + z * basisZ.x,
                x * basisX.y + y * basisY.y + z * basisZ.y,
                x * basisX.z + y * basisY.z + z * basisZ.z
            );
        }

        Face toFace() {
            if (x == 1) {
                return Face.RIGHT;
            }
            if (x == -1) {
                return Face.LEFT;
            }
            if (y == 1) {
                return Face.UP;
            }
            if (y == -1) {
                return Face.DOWN;
            }
            if (z == 1) {
                return Face.FRONT;
            }
            return Face.BACK;
        }
    }
}
