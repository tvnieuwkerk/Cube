package nl.tvn.cube.model;

import javax.vecmath.Color3f;

public enum CubeColor {
    WHITE(new Color3f(1f, 1f, 1f)),
    YELLOW(new Color3f(1f, 1f, 0f)),
    RED(new Color3f(1f, 0f, 0f)),
    ORANGE(new Color3f(1f, 0.5f, 0f)),
    BLUE(new Color3f(0f, 0f, 1f)),
    GREEN(new Color3f(0f, 1f, 0f)),
    BLACK(new Color3f(0f, 0f, 0f));

    private final Color3f color;

    CubeColor(Color3f color) {
        this.color = color;
    }

    public Color3f color() {
        return color;
    }
}
