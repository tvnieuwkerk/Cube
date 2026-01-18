package nl.tvn.cube.model;

public final class CubieModel {
    private final int homeX;
    private final int homeY;
    private final int homeZ;
    private final CubeCoordinate coordinate;

    public CubieModel(int x, int y, int z) {
        this.homeX = x;
        this.homeY = y;
        this.homeZ = z;
        this.coordinate = new CubeCoordinate(x, y, z);
    }

    public int homeX() {
        return homeX;
    }

    public int homeY() {
        return homeY;
    }

    public int homeZ() {
        return homeZ;
    }

    public CubeCoordinate coordinate() {
        return coordinate;
    }
}
