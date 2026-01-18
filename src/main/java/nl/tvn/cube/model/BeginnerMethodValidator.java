package nl.tvn.cube.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public final class BeginnerMethodValidator {
    private final CubeModel cubeModel;
    private final Map<Face, CubeColor> centers;

    public BeginnerMethodValidator(CubeModel cubeModel) {
        this.cubeModel = cubeModel;
        this.centers = buildCenterColors();
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
        return edgeMatches(0, 1, 1, Face.UP, centers.get(Face.UP), Face.FRONT, centers.get(Face.FRONT))
            && edgeMatches(0, 1, -1, Face.UP, centers.get(Face.UP), Face.BACK, centers.get(Face.BACK))
            && edgeMatches(1, 1, 0, Face.UP, centers.get(Face.UP), Face.RIGHT, centers.get(Face.RIGHT))
            && edgeMatches(-1, 1, 0, Face.UP, centers.get(Face.UP), Face.LEFT, centers.get(Face.LEFT));
    }

    private boolean isWhiteCornersSolved() {
        return isWhiteCrossSolved()
            && cornerMatches(1, 1, 1, Face.RIGHT, centers.get(Face.RIGHT), Face.FRONT, centers.get(Face.FRONT))
            && cornerMatches(-1, 1, 1, Face.LEFT, centers.get(Face.LEFT), Face.FRONT, centers.get(Face.FRONT))
            && cornerMatches(1, 1, -1, Face.RIGHT, centers.get(Face.RIGHT), Face.BACK, centers.get(Face.BACK))
            && cornerMatches(-1, 1, -1, Face.LEFT, centers.get(Face.LEFT), Face.BACK, centers.get(Face.BACK));
    }

    private boolean isMiddleLayerSolved() {
        return isWhiteCornersSolved()
            && edgeMatches(1, 0, 1, Face.RIGHT, centers.get(Face.RIGHT), Face.FRONT, centers.get(Face.FRONT))
            && edgeMatches(1, 0, -1, Face.RIGHT, centers.get(Face.RIGHT), Face.BACK, centers.get(Face.BACK))
            && edgeMatches(-1, 0, 1, Face.LEFT, centers.get(Face.LEFT), Face.FRONT, centers.get(Face.FRONT))
            && edgeMatches(-1, 0, -1, Face.LEFT, centers.get(Face.LEFT), Face.BACK, centers.get(Face.BACK));
    }

    private boolean isYellowCrossSolved() {
        return isMiddleLayerSolved()
            && edgeHasColor(0, -1, 1, Face.DOWN, centers.get(Face.DOWN))
            && edgeHasColor(0, -1, -1, Face.DOWN, centers.get(Face.DOWN))
            && edgeHasColor(1, -1, 0, Face.DOWN, centers.get(Face.DOWN))
            && edgeHasColor(-1, -1, 0, Face.DOWN, centers.get(Face.DOWN));
    }

    private boolean isYellowEdgeAlignmentSolved() {
        return isYellowCrossSolved()
            && edgeMatches(0, -1, 1, Face.DOWN, centers.get(Face.DOWN), Face.FRONT, centers.get(Face.FRONT))
            && edgeMatches(0, -1, -1, Face.DOWN, centers.get(Face.DOWN), Face.BACK, centers.get(Face.BACK))
            && edgeMatches(1, -1, 0, Face.DOWN, centers.get(Face.DOWN), Face.RIGHT, centers.get(Face.RIGHT))
            && edgeMatches(-1, -1, 0, Face.DOWN, centers.get(Face.DOWN), Face.LEFT, centers.get(Face.LEFT));
    }

    private boolean isYellowCornerPositionSolved() {
        return isYellowEdgeAlignmentSolved()
            && cornerColorSetMatches(1, -1, 1, Set.of(centers.get(Face.RIGHT), centers.get(Face.DOWN), centers.get(Face.FRONT)))
            && cornerColorSetMatches(-1, -1, 1, Set.of(centers.get(Face.LEFT), centers.get(Face.DOWN), centers.get(Face.FRONT)))
            && cornerColorSetMatches(1, -1, -1, Set.of(centers.get(Face.RIGHT), centers.get(Face.DOWN), centers.get(Face.BACK)))
            && cornerColorSetMatches(-1, -1, -1, Set.of(centers.get(Face.LEFT), centers.get(Face.DOWN), centers.get(Face.BACK)));
    }

    private boolean isYellowCornerOrientationSolved() {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    CubieModel cubie = cubeModel.cubieAt(x, y, z);
                    if (cubie == null) {
                        return false;
                    }
                }
            }
        }
        return isFaceSolved(Face.UP)
            && isFaceSolved(Face.DOWN)
            && isFaceSolved(Face.LEFT)
            && isFaceSolved(Face.RIGHT)
            && isFaceSolved(Face.FRONT)
            && isFaceSolved(Face.BACK);
    }

    private boolean edgeMatches(int x, int y, int z, Face firstFace, CubeColor firstColor, Face secondFace, CubeColor secondColor) {
        CubieModel cubie = cubeModel.cubieAt(x, y, z);
        if (cubie == null) {
            return false;
        }
        return firstColor == cubie.colorOnFace(firstFace)
            && secondColor == cubie.colorOnFace(secondFace);
    }

    private boolean edgeHasColor(int x, int y, int z, Face face, CubeColor color) {
        CubieModel cubie = cubeModel.cubieAt(x, y, z);
        if (cubie == null) {
            return false;
        }
        return color == cubie.colorOnFace(face);
    }

    private boolean cornerMatches(int x, int y, int z, Face faceA, CubeColor colorA, Face faceB, CubeColor colorB) {
        CubieModel cubie = cubeModel.cubieAt(x, y, z);
        if (cubie == null) {
            return false;
        }
        return centers.get(Face.UP) == cubie.colorOnFace(Face.UP)
            && colorA == cubie.colorOnFace(faceA)
            && colorB == cubie.colorOnFace(faceB);
    }

    private boolean cornerColorSetMatches(int x, int y, int z, Set<CubeColor> expected) {
        CubieModel cubie = cubeModel.cubieAt(x, y, z);
        if (cubie == null) {
            return false;
        }
        return cubie.stickerColors().equals(expected);
    }

    private boolean isFaceSolved(Face face) {
        CubeColor expected = centers.get(face);
        for (CubieModel cubie : cubeModel.cubies()) {
            if (cubie.colorOnFace(face) == null) {
                continue;
            }
            if (cubie.colorOnFace(face) != expected) {
                return false;
            }
        }
        return true;
    }

    private Map<Face, CubeColor> buildCenterColors() {
        EnumMap<Face, CubeColor> map = new EnumMap<>(Face.class);
        map.put(Face.UP, CubeColor.WHITE);
        map.put(Face.DOWN, CubeColor.YELLOW);
        map.put(Face.RIGHT, CubeColor.RED);
        map.put(Face.LEFT, CubeColor.ORANGE);
        map.put(Face.FRONT, CubeColor.GREEN);
        map.put(Face.BACK, CubeColor.BLUE);
        return map;
    }
}
