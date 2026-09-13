package nl.tvn.cube.model;

public final class CubieModel {
    private final int homeX;
    private final int homeY;
    private final int homeZ;
    private final CubeCoordinate coordinate;
    private CubeVector xBasis = new CubeVector(1, 0, 0);
    private CubeVector yBasis = new CubeVector(0, 1, 0);
    private CubeVector zBasis = new CubeVector(0, 0, 1);

    public CubeVector direction(CubeVector original) {
        return new CubeVector(
            original.x() * xBasis.x() + original.y() * yBasis.x() + original.z() * zBasis.x(),
            original.x() * xBasis.y() + original.y() * yBasis.y() + original.z() * zBasis.y(),
            original.x() * xBasis.z() + original.y() * yBasis.z() + original.z() * zBasis.z());
    }

    void quarterTurn(RotationAxis axis) {
        CubeVector position = new CubeVector(coordinate.x(), coordinate.y(), coordinate.z()).quarterTurn(axis);
        coordinate.set(position.x(), position.y(), position.z());
        xBasis = xBasis.quarterTurn(axis);
        yBasis = yBasis.quarterTurn(axis);
        zBasis = zBasis.quarterTurn(axis);
    }

    void reset() {
        coordinate.set(homeX, homeY, homeZ);
        xBasis = new CubeVector(1, 0, 0);
        yBasis = new CubeVector(0, 1, 0);
        zBasis = new CubeVector(0, 0, 1);
    }

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
