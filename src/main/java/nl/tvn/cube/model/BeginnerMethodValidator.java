package nl.tvn.cube.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class BeginnerMethodValidator {
    private static final List<Rotation> ROTATIONS = buildRotations();

    public Map<BeginnerMethodStep, Boolean> validate(List<CubieModel> cubies) {
        Rotation rotation = resolveOrientation(cubies);
        EnumMap<BeginnerMethodStep, Boolean> results = new EnumMap<>(BeginnerMethodStep.class);
        results.put(BeginnerMethodStep.WHITE_CROSS, isWhiteCrossSolved(cubies, rotation));
        results.put(BeginnerMethodStep.WHITE_CORNERS, isWhiteCornersSolved(cubies, rotation));
        results.put(BeginnerMethodStep.MIDDLE_LAYER_EDGES, isMiddleLayerEdgesSolved(cubies, rotation));
        results.put(BeginnerMethodStep.YELLOW_CROSS, isYellowCrossSolved(cubies, rotation));
        results.put(BeginnerMethodStep.YELLOW_EDGE_ALIGNMENT, isYellowEdgesAligned(cubies, rotation));
        results.put(BeginnerMethodStep.YELLOW_CORNER_POSITION, areYellowCornersPositioned(cubies, rotation));
        results.put(BeginnerMethodStep.YELLOW_CORNER_ORIENTATION, isCubeSolved(cubies, rotation));
        boolean previous = true;
        for (BeginnerMethodStep step : BeginnerMethodStep.values()) {
            previous = previous && results.get(step);
            results.put(step, previous);
        }
        return results;
    }

    public boolean isWhiteCrossSolved(List<CubieModel> cubies, Rotation rotation) {
        return cubies.stream()
            .filter(this::isWhiteEdge)
            .allMatch(cubie -> isPieceSolved(cubie, rotation));
    }

    public boolean isWhiteCrossSolved(List<CubieModel> cubies) {
        return isWhiteCrossSolved(cubies, resolveOrientation(cubies));
    }

    public boolean isWhiteCornersSolved(List<CubieModel> cubies, Rotation rotation) {
        return cubies.stream()
            .filter(this::isWhiteCorner)
            .allMatch(cubie -> isPieceSolved(cubie, rotation));
    }

    public boolean isWhiteCornersSolved(List<CubieModel> cubies) {
        return isWhiteCornersSolved(cubies, resolveOrientation(cubies));
    }

    public boolean isMiddleLayerEdgesSolved(List<CubieModel> cubies, Rotation rotation) {
        return cubies.stream()
            .filter(this::isMiddleEdge)
            .allMatch(cubie -> isPieceSolved(cubie, rotation));
    }

    public boolean isMiddleLayerEdgesSolved(List<CubieModel> cubies) {
        return isMiddleLayerEdgesSolved(cubies, resolveOrientation(cubies));
    }

    public boolean isYellowCrossSolved(List<CubieModel> cubies, Rotation rotation) {
        return cubies.stream()
            .filter(this::isYellowEdge)
            .allMatch(cubie -> rotation.applyY(cubie.coordinate()) == -1
                && stickerMatches(cubie, rotation, new CubeVector(0, -1, 0)));
    }

    public boolean isYellowCrossSolved(List<CubieModel> cubies) {
        return isYellowCrossSolved(cubies, resolveOrientation(cubies));
    }

    public boolean isYellowEdgesAligned(List<CubieModel> cubies, Rotation rotation) {
        return cubies.stream()
            .filter(this::isYellowEdge)
            .allMatch(cubie -> isPieceSolved(cubie, rotation));
    }

    public boolean isYellowEdgesAligned(List<CubieModel> cubies) {
        return isYellowEdgesAligned(cubies, resolveOrientation(cubies));
    }

    public boolean areYellowCornersPositioned(List<CubieModel> cubies, Rotation rotation) {
        return cubies.stream()
            .filter(this::isYellowCorner)
            .allMatch(cubie -> isInHomePosition(cubie, rotation));
    }

    public boolean areYellowCornersPositioned(List<CubieModel> cubies) {
        return areYellowCornersPositioned(cubies, resolveOrientation(cubies));
    }

    public boolean isCubeSolved(List<CubieModel> cubies, Rotation rotation) {
        return cubies.stream().allMatch(cubie -> isPieceSolved(cubie, rotation));
    }

    public boolean isCubeSolved(List<CubieModel> cubies) {
        return isCubeSolved(cubies, resolveOrientation(cubies));
    }

    private boolean isWhiteEdge(CubieModel cubie) {
        return cubie.homeY() == 1 && isEdge(cubie);
    }

    private boolean isWhiteCorner(CubieModel cubie) {
        return cubie.homeY() == 1 && isCorner(cubie);
    }

    private boolean isYellowEdge(CubieModel cubie) {
        return cubie.homeY() == -1 && isEdge(cubie);
    }

    private boolean isYellowCorner(CubieModel cubie) {
        return cubie.homeY() == -1 && isCorner(cubie);
    }

    private boolean isMiddleEdge(CubieModel cubie) {
        return cubie.homeY() == 0 && isEdge(cubie);
    }

    private boolean isEdge(CubieModel cubie) {
        int zeros = 0;
        if (cubie.homeX() == 0) {
            zeros++;
        }
        if (cubie.homeY() == 0) {
            zeros++;
        }
        if (cubie.homeZ() == 0) {
            zeros++;
        }
        return zeros == 1;
    }

    private boolean isCorner(CubieModel cubie) {
        return cubie.homeX() != 0 && cubie.homeY() != 0 && cubie.homeZ() != 0;
    }

    private boolean isCenter(CubieModel cubie) {
        int zeros = 0;
        if (cubie.homeX() == 0) {
            zeros++;
        }
        if (cubie.homeY() == 0) {
            zeros++;
        }
        if (cubie.homeZ() == 0) {
            zeros++;
        }
        return zeros == 2;
    }

    private boolean isInHomePosition(CubieModel cubie, Rotation rotation) {
        return rotation.applyX(cubie.coordinate()) == cubie.homeX()
            && rotation.applyY(cubie.coordinate()) == cubie.homeY()
            && rotation.applyZ(cubie.coordinate()) == cubie.homeZ();
    }

    private boolean isPieceSolved(CubieModel cubie, Rotation rotation) {
        return isInHomePosition(cubie, rotation)
            && (cubie.homeX() == 0 || stickerMatches(cubie, rotation, new CubeVector(cubie.homeX(), 0, 0)))
            && (cubie.homeY() == 0 || stickerMatches(cubie, rotation, new CubeVector(0, cubie.homeY(), 0)))
            && (cubie.homeZ() == 0 || stickerMatches(cubie, rotation, new CubeVector(0, 0, cubie.homeZ())));
    }

    private boolean stickerMatches(CubieModel cubie, Rotation rotation, CubeVector original) {
        CubeVector actual = cubie.direction(original);
        CubeCoordinate coordinate = new CubeCoordinate(actual.x(), actual.y(), actual.z());
        return rotation.applyX(coordinate) == original.x()
            && rotation.applyY(coordinate) == original.y()
            && rotation.applyZ(coordinate) == original.z();
    }

    private Rotation resolveOrientation(List<CubieModel> cubies) {
        List<CubieModel> centers = cubies.stream()
            .filter(this::isCenter)
            .toList();
        for (Rotation rotation : ROTATIONS) {
            boolean matches = true;
            for (CubieModel center : centers) {
                if (rotation.applyX(center.coordinate()) != center.homeX()
                    || rotation.applyY(center.coordinate()) != center.homeY()
                    || rotation.applyZ(center.coordinate()) != center.homeZ()) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                return rotation;
            }
        }
        return Rotation.identity();
    }

    private static List<Rotation> buildRotations() {
        List<Rotation> rotations = new ArrayList<>();
        int[][] axes = {
            {1, 0, 0},
            {-1, 0, 0},
            {0, 1, 0},
            {0, -1, 0},
            {0, 0, 1},
            {0, 0, -1}
        };
        for (int[] xAxis : axes) {
            for (int[] yAxis : axes) {
                if (dot(xAxis, yAxis) != 0) {
                    continue;
                }
                int[] zAxis = cross(xAxis, yAxis);
                rotations.add(new Rotation(xAxis, yAxis, zAxis));
            }
        }
        return rotations;
    }

    private static int dot(int[] a, int[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    private static int[] cross(int[] a, int[] b) {
        return new int[] {
            a[1] * b[2] - a[2] * b[1],
            a[2] * b[0] - a[0] * b[2],
            a[0] * b[1] - a[1] * b[0]
        };
    }

    public record Rotation(int[] xAxis, int[] yAxis, int[] zAxis) {
        public int applyX(CubeCoordinate coordinate) {
            return dot(xAxis, coordinate);
        }

        public int applyY(CubeCoordinate coordinate) {
            return dot(yAxis, coordinate);
        }

        public int applyZ(CubeCoordinate coordinate) {
            return dot(zAxis, coordinate);
        }

        public static Rotation identity() {
            return new Rotation(new int[] {1, 0, 0}, new int[] {0, 1, 0}, new int[] {0, 0, 1});
        }

        private static int dot(int[] axis, CubeCoordinate coordinate) {
            return axis[0] * coordinate.x()
                + axis[1] * coordinate.y()
                + axis[2] * coordinate.z();
        }
    }
}
