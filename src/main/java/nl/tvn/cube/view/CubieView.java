package nl.tvn.cube.view;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import nl.tvn.cube.model.CubieModel;
import nl.tvn.cube.model.RotationAxis;

public final class CubieView extends Group {
    private final CubieModel model;
    private final Affine orientation;

    public CubieView(CubieModel model) {
        this.model = model;
        this.orientation = new Affine();
        getTransforms().add(orientation);
        buildGeometry();
        updateTranslation();
    }

    public CubieModel model() {
        return model;
    }

    public void rotateAroundWorld(RotationAxis axis, double angleDegrees) {
        orientation.prepend(new Rotate(angleDegrees, axisVector(axis)));
    }

    public void resetOrientation() {
        orientation.setToIdentity();
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
            getChildren().add(stickerOnX(stickerSize, offset, Color.RED));
        }
        if (model.coordinate().x() == -1) {
            getChildren().add(stickerOnX(stickerSize, -offset, Color.ORANGE));
        }
        if (model.coordinate().y() == 1) {
            getChildren().add(stickerOnY(stickerSize, -offset, Color.WHITE));
        }
        if (model.coordinate().y() == -1) {
            getChildren().add(stickerOnY(stickerSize, offset, Color.YELLOW));
        }
        if (model.coordinate().z() == 1) {
            getChildren().add(stickerOnZ(stickerSize, -offset, Color.GREEN));
        }
        if (model.coordinate().z() == -1) {
            getChildren().add(stickerOnZ(stickerSize, offset, Color.BLUE));
        }
    }

    private Box stickerOnX(double size, double x, Color color) {
        Box sticker = new Box(CubeConstants.STICKER_THICKNESS, size, size);
        sticker.setMaterial(new PhongMaterial(color));
        sticker.setTranslateX(x);
        return sticker;
    }

    private Box stickerOnY(double size, double y, Color color) {
        Box sticker = new Box(size, CubeConstants.STICKER_THICKNESS, size);
        sticker.setMaterial(new PhongMaterial(color));
        sticker.setTranslateY(y);
        return sticker;
    }

    private Box stickerOnZ(double size, double z, Color color) {
        Box sticker = new Box(size, size, CubeConstants.STICKER_THICKNESS);
        sticker.setMaterial(new PhongMaterial(color));
        sticker.setTranslateZ(z);
        return sticker;
    }

    private static javafx.geometry.Point3D axisVector(RotationAxis axis) {
        return switch (axis) {
            case X -> Rotate.X_AXIS;
            case Y -> Rotate.Y_AXIS;
            case Z -> Rotate.Z_AXIS;
        };
    }
}
