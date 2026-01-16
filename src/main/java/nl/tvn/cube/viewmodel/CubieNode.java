package nl.tvn.cube.viewmodel;

import javafx.scene.Group;
import javafx.scene.transform.Translate;
import nl.tvn.cube.model.Cubie;

public final class CubieNode {
    private final Cubie cubie;
    private final Group group;
    private final Translate translate;

    public CubieNode(Cubie cubie, Group group, Translate translate) {
        this.cubie = cubie;
        this.group = group;
        this.translate = translate;
    }

    public Cubie cubie() {
        return cubie;
    }

    public Group group() {
        return group;
    }

    public Translate translate() {
        return translate;
    }
}
