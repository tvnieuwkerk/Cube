package nl.tvn.cube.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import nl.tvn.cube.util.Vector3i;

public final class CubeModel {
    private final List<Cubie> cubies;

    public CubeModel() {
        this.cubies = buildIdentityCube();
    }

    public List<Cubie> cubies() {
        return cubies;
    }

    private List<Cubie> buildIdentityCube() {
        List<Cubie> items = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    Vector3i position = new Vector3i(x, y, z);
                    items.add(new Cubie(position, buildFaceColors(position)));
                }
            }
        }
        return items;
    }

    private Map<Face, CubeColor> buildFaceColors(Vector3i position) {
        Map<Face, CubeColor> colors = new EnumMap<>(Face.class);
        colors.put(Face.FRONT, position.z() == 1 ? CubeColor.GREEN : CubeColor.BLACK);
        colors.put(Face.BACK, position.z() == -1 ? CubeColor.BLUE : CubeColor.BLACK);
        colors.put(Face.RIGHT, position.x() == 1 ? CubeColor.RED : CubeColor.BLACK);
        colors.put(Face.LEFT, position.x() == -1 ? CubeColor.ORANGE : CubeColor.BLACK);
        colors.put(Face.UP, position.y() == 1 ? CubeColor.WHITE : CubeColor.BLACK);
        colors.put(Face.DOWN, position.y() == -1 ? CubeColor.YELLOW : CubeColor.BLACK);
        return colors;
    }
}
