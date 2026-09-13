package nl.tvn.cube.model;

/** An exact direction in the cube's logical coordinate system. */
public record CubeVector(int x, int y, int z) {
    public CubeVector quarterTurn(RotationAxis axis) {
        return switch (axis) {
            case X -> new CubeVector(x, z, -y);
            case Y -> new CubeVector(-z, y, x);
            case Z -> new CubeVector(y, -x, z);
        };
    }
}
