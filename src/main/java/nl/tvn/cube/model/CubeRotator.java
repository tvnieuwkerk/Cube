package nl.tvn.cube.model;

import java.util.ArrayList;
import java.util.List;

public final class CubeRotator {
    private CubeRotator() {
    }

    public static void applyMove(CubeModel model, Move move) {
        List<CubieModel> affected = new ArrayList<>();
        for (CubieModel cubie : model.cubies()) {
            if (isInLayer(cubie, move.axis(), move.layers())) {
                affected.add(cubie);
            }
        }
        int turns = normalizeTurns(move.quarterTurns());
        if (turns == 0) {
            return;
        }
        int step = turns > 0 ? 1 : -1;
        for (int i = 0; i < Math.abs(turns); i++) {
            for (CubieModel cubie : affected) {
                cubie.rotate(move.axis(), step);
            }
        }
    }

    private static boolean isInLayer(CubieModel cubie, RotationAxis axis, java.util.Set<Integer> layers) {
        return switch (axis) {
            case X -> layers.contains(cubie.coordinate().x());
            case Y -> layers.contains(cubie.coordinate().y());
            case Z -> layers.contains(cubie.coordinate().z());
        };
    }

    private static int normalizeTurns(int turns) {
        int normalized = turns % 4;
        if (normalized == 3) {
            return -1;
        }
        if (normalized == -3) {
            return 1;
        }
        return normalized;
    }
}
