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

    public void reset() {
        cubies.forEach(CubieModel::reset);
    }

    /** Commits a move to both piece positions and sticker directions. */
    public void applyMove(Move move) {
        List<CubieModel> affected = cubies.stream().filter(cubie -> move.layers().contains(
            switch (move.axis()) {
                case X -> cubie.coordinate().x();
                case Y -> cubie.coordinate().y();
                case Z -> cubie.coordinate().z();
            })).toList();
        int turns = Math.floorMod(move.quarterTurns(), 4);
        for (int i = 0; i < turns; i++) {
            affected.forEach(cubie -> cubie.quarterTurn(move.axis()));
        }
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
