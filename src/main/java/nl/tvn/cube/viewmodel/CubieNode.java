package nl.tvn.cube.viewmodel;

import javax.media.j3d.TransformGroup;
import nl.tvn.cube.model.Cubie;

public final class CubieNode {
    private final Cubie cubie;
    private final TransformGroup transformGroup;

    public CubieNode(Cubie cubie, TransformGroup transformGroup) {
        this.cubie = cubie;
        this.transformGroup = transformGroup;
    }

    public Cubie cubie() {
        return cubie;
    }

    public TransformGroup transformGroup() {
        return transformGroup;
    }
}
