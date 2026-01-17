package nl.tvn.cube.model;

import java.util.Set;

public record Move(RotationAxis axis, Set<Integer> layers, int quarterTurns) {
    public Move {
        if (quarterTurns == 0) {
            throw new IllegalArgumentException("quarterTurns must be non-zero");
        }
    }
}
