package nl.tvn.cube.model;

import nl.tvn.cube.util.Vector3i;

public final class Orientation {
    private Vector3i xAxis;
    private Vector3i yAxis;
    private Vector3i zAxis;

    private Orientation(Vector3i xAxis, Vector3i yAxis, Vector3i zAxis) {
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.zAxis = zAxis;
    }

    public static Orientation identity() {
        return new Orientation(
            new Vector3i(1, 0, 0),
            new Vector3i(0, 1, 0),
            new Vector3i(0, 0, 1)
        );
    }

    public Vector3i xAxis() {
        return xAxis;
    }

    public Vector3i yAxis() {
        return yAxis;
    }

    public Vector3i zAxis() {
        return zAxis;
    }

    public void rotate(Axis axis, int angleSign) {
        xAxis = CubeRotator.rotatePosition(xAxis, axis, angleSign);
        yAxis = CubeRotator.rotatePosition(yAxis, axis, angleSign);
        zAxis = CubeRotator.rotatePosition(zAxis, axis, angleSign);
    }
}
