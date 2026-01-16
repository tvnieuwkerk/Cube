package nl.tvn.cube.viewmodel;

import nl.tvn.cube.model.Cubie;
import javafx.scene.Group;

public final class CubieNode {
    private final Cubie cubie;
    private final Group group;

    public CubieNode(Cubie cubie, Group group) {
        this.cubie = cubie;
        this.group = group;
    }

    public Cubie cubie() {
        return cubie;
    }

    public Group group() {
        return group;
    }
}
