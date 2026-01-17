package nl.tvn.cube.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CubeModel {
    private final List<CubieModel> cubies;

    public CubeModel() {
        this.cubies = buildIdentityCube();
    }

    public List<CubieModel> cubies() {
        return Collections.unmodifiableList(cubies);
    }

    private List<CubieModel> buildIdentityCube() {
        List<CubieModel> result = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    result.add(new CubieModel(x, y, z));
                }
            }
        }
        return result;
    }
}
