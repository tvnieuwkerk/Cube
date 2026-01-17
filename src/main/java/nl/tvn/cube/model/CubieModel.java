package nl.tvn.cube.model;

public final class CubieModel {
    private final CubeCoordinate coordinate;

    public CubieModel(int x, int y, int z) {
        this.coordinate = new CubeCoordinate(x, y, z);
    }

    public CubeCoordinate coordinate() {
        return coordinate;
    }
}
