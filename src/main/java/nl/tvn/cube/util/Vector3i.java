package nl.tvn.cube.util;

import java.util.Objects;

public final class Vector3i {
    private final int x;
    private final int y;
    private final int z;

    public Vector3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public Vector3i withX(int newX) {
        return new Vector3i(newX, y, z);
    }

    public Vector3i withY(int newY) {
        return new Vector3i(x, newY, z);
    }

    public Vector3i withZ(int newZ) {
        return new Vector3i(x, y, newZ);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Vector3i that)) {
            return false;
        }
        return x == that.x && y == that.y && z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "Vector3i{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
    }
}
