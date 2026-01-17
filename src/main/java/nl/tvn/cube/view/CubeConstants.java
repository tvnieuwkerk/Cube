package nl.tvn.cube.view;

public final class CubeConstants {
    public static final double CUBIE_SIZE = 50.0;
    public static final double CUBIE_GAP = 3.0;
    public static final double STICKER_THICKNESS = 2.0;

    private CubeConstants() {
    }

    public static double step() {
        return CUBIE_SIZE + CUBIE_GAP;
    }
}
