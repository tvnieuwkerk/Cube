package nl.tvn.cube.view;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import nl.tvn.cube.model.CubieModel;

public final class CubieView extends Group {
    private final CubieModel model;

    public CubieView(CubieModel model) {
        this.model = model;
        buildGeometry();
        updateTranslation();
    }

    public CubieModel model() {
        return model;
    }

    public void updateTranslation() {
        double step = CubeConstants.step();
        setTranslateX(model.coordinate().x() * step);
        setTranslateY(-model.coordinate().y() * step);
        setTranslateZ(-model.coordinate().z() * step);
    }

    private void buildGeometry() {
        Box base = new Box(CubeConstants.CUBIE_SIZE, CubeConstants.CUBIE_SIZE, CubeConstants.CUBIE_SIZE);
        base.setMaterial(new PhongMaterial(Color.BLACK));
        getChildren().add(base);

        double stickerSize = CubeConstants.CUBIE_SIZE * 0.9;
        double offset = CubeConstants.CUBIE_SIZE / 2 + CubeConstants.STICKER_THICKNESS / 2;

        if (model.coordinate().x() == 1) {
            getChildren().add(stickerBox(stickerSize, stickerSize, CubeConstants.STICKER_THICKNESS,
                offset, 0, 0, Color.RED));
        }
        if (model.coordinate().x() == -1) {
            getChildren().add(stickerBox(stickerSize, stickerSize, CubeConstants.STICKER_THICKNESS,
                -offset, 0, 0, Color.ORANGE));
        }
        if (model.coordinate().y() == 1) {
            getChildren().add(stickerBox(stickerSize, CubeConstants.STICKER_THICKNESS, stickerSize,
                0, -offset, 0, Color.WHITE));
        }
        if (model.coordinate().y() == -1) {
            getChildren().add(stickerBox(stickerSize, CubeConstants.STICKER_THICKNESS, stickerSize,
                0, offset, 0, Color.YELLOW));
        }
        if (model.coordinate().z() == 1) {
            getChildren().add(stickerBox(stickerSize, stickerSize, CubeConstants.STICKER_THICKNESS,
                0, 0, -offset, Color.GREEN));
        }
        if (model.coordinate().z() == -1) {
            getChildren().add(stickerBox(stickerSize, stickerSize, CubeConstants.STICKER_THICKNESS,
                0, 0, offset, Color.BLUE));
        }
    }

    private Box stickerBox(double width, double height, double depth,
                           double x, double y, double z, Color color) {
        Box sticker = new Box(width, height, depth);
        sticker.setMaterial(new PhongMaterial(color));
        sticker.setTranslateX(x);
        sticker.setTranslateY(y);
        sticker.setTranslateZ(z);
        return sticker;
    }
}
