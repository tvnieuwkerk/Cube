package nl.tvn.cube.model;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class BeginnerMethodValidator {
    public Map<BeginnerMethodStep, Boolean> validate(List<CubieModel> cubies) {
        EnumMap<BeginnerMethodStep, Boolean> results = new EnumMap<>(BeginnerMethodStep.class);
        results.put(BeginnerMethodStep.WHITE_CROSS, isWhiteCrossSolved(cubies));
        results.put(BeginnerMethodStep.WHITE_CORNERS, isWhiteCornersSolved(cubies));
        results.put(BeginnerMethodStep.MIDDLE_LAYER_EDGES, isMiddleLayerEdgesSolved(cubies));
        results.put(BeginnerMethodStep.YELLOW_CROSS, isYellowCrossSolved(cubies));
        results.put(BeginnerMethodStep.YELLOW_EDGE_ALIGNMENT, isYellowEdgesAligned(cubies));
        results.put(BeginnerMethodStep.YELLOW_CORNER_POSITION, areYellowCornersPositioned(cubies));
        results.put(BeginnerMethodStep.YELLOW_CORNER_ORIENTATION, isCubeSolved(cubies));
        return results;
    }

    public boolean isWhiteCrossSolved(List<CubieModel> cubies) {
        return cubies.stream()
            .filter(this::isWhiteEdge)
            .allMatch(this::isInHomePosition);
    }

    public boolean isWhiteCornersSolved(List<CubieModel> cubies) {
        return cubies.stream()
            .filter(this::isWhiteCorner)
            .allMatch(this::isInHomePosition);
    }

    public boolean isMiddleLayerEdgesSolved(List<CubieModel> cubies) {
        return cubies.stream()
            .filter(this::isMiddleEdge)
            .allMatch(this::isInHomePosition);
    }

    public boolean isYellowCrossSolved(List<CubieModel> cubies) {
        return cubies.stream()
            .filter(this::isYellowEdge)
            .allMatch(cubie -> cubie.coordinate().y() == -1);
    }

    public boolean isYellowEdgesAligned(List<CubieModel> cubies) {
        return cubies.stream()
            .filter(this::isYellowEdge)
            .allMatch(this::isInHomePosition);
    }

    public boolean areYellowCornersPositioned(List<CubieModel> cubies) {
        return cubies.stream()
            .filter(this::isYellowCorner)
            .allMatch(this::isInHomePosition);
    }

    public boolean isCubeSolved(List<CubieModel> cubies) {
        return cubies.stream().allMatch(this::isInHomePosition);
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

    private boolean isInHomePosition(CubieModel cubie) {
        return cubie.coordinate().x() == cubie.homeX()
            && cubie.coordinate().y() == cubie.homeY()
            && cubie.coordinate().z() == cubie.homeZ();
    }
}
