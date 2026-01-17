package nl.tvn.cube.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import nl.tvn.cube.util.Vector3i;

public final class Cubie {
    private Vector3i position;
    private final Map<Face, CubeColor> faceColors;
    private final Orientation orientation;

    public Cubie(Vector3i position, Map<Face, CubeColor> faceColors) {
        this.position = position;
        this.faceColors = new EnumMap<>(faceColors);
        this.orientation = Orientation.identity();
    }

    public Vector3i position() {
        return position;
    }

    public void setPosition(Vector3i position) {
        this.position = position;
    }

    public Map<Face, CubeColor> faceColors() {
        return faceColors;
    }

    public Orientation orientation() {
        return orientation;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Cubie cubie)) {
            return false;
        }
        return Objects.equals(position, cubie.position)
            && Objects.equals(faceColors, cubie.faceColors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, faceColors);
    }
}
