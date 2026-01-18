package nl.tvn.cube.view;

import javafx.scene.Group;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import nl.tvn.cube.model.CubieModel;
import nl.tvn.cube.model.RotationAxis;
import java.util.ArrayList;
import java.util.List;

public final class CubieView extends Group {
    private final CubieModel model;
    private final Affine orientation;
    private final List<StickerInfo> stickers;

    public CubieView(CubieModel model) {
        this.model = model;
        this.orientation = new Affine();
        this.stickers = new ArrayList<>();
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
        updatePickInfo();
    }

    private void buildGeometry() {
        Box base = new Box(CubeConstants.CUBIE_SIZE, CubeConstants.CUBIE_SIZE, CubeConstants.CUBIE_SIZE);
        base.setMaterial(new PhongMaterial(Color.BLACK));
        getChildren().add(base);

        double stickerSize = CubeConstants.CUBIE_SIZE * 0.9;
        double offset = CubeConstants.CUBIE_SIZE / 2 + CubeConstants.STICKER_THICKNESS / 2;

        if (model.coordinate().x() == 1) {
            Box sticker = stickerOnX(stickerSize, offset, Color.RED);
            registerSticker(sticker, new Point3D(1, 0, 0));
            getChildren().add(sticker);
        }
        if (model.coordinate().x() == -1) {
            Box sticker = stickerOnX(stickerSize, -offset, Color.ORANGE);
            registerSticker(sticker, new Point3D(-1, 0, 0));
            getChildren().add(sticker);
        }
        if (model.coordinate().y() == 1) {
            Box sticker = stickerOnY(stickerSize, -offset, Color.WHITE);
            registerSticker(sticker, new Point3D(0, 1, 0));
            getChildren().add(sticker);
        }
        if (model.coordinate().y() == -1) {
            Box sticker = stickerOnY(stickerSize, offset, Color.YELLOW);
            registerSticker(sticker, new Point3D(0, -1, 0));
            getChildren().add(sticker);
        }
        if (model.coordinate().z() == 1) {
            Box sticker = stickerOnZ(stickerSize, -offset, Color.BLUE);
            registerSticker(sticker, new Point3D(0, 0, 1));
            getChildren().add(sticker);
        }
        if (model.coordinate().z() == -1) {
            Box sticker = stickerOnZ(stickerSize, offset, Color.GREEN);
            registerSticker(sticker, new Point3D(0, 0, -1));
            getChildren().add(sticker);
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

    private void registerSticker(Box sticker, Point3D localNormal) {
        stickers.add(new StickerInfo(sticker, localNormal));
    }

    private void updatePickInfo() {
        int x = model.coordinate().x();
        int y = model.coordinate().y();
        int z = model.coordinate().z();
        for (StickerInfo sticker : stickers) {
            Point3D worldNormal = orientation.deltaTransform(sticker.localNormal());
            Point3D modelNormal = new Point3D(worldNormal.getX(), -worldNormal.getY(), -worldNormal.getZ());
            RotationAxis axis = dominantAxis(modelNormal);
            int layer = axisLayerFromCoordinate(axis, x, y, z);
            sticker.sticker().setUserData(new FacePickInfo(axis, layer, x, y, z));
        }
    }

    private RotationAxis dominantAxis(Point3D normal) {
        double absX = Math.abs(normal.getX());
        double absY = Math.abs(normal.getY());
        double absZ = Math.abs(normal.getZ());
        if (absX >= absY && absX >= absZ) {
            return RotationAxis.X;
        }
        if (absY >= absZ) {
            return RotationAxis.Y;
        }
        return RotationAxis.Z;
    }

    private int axisLayerFromCoordinate(RotationAxis axis, int x, int y, int z) {
        return switch (axis) {
            case X -> Integer.compare(x, 0);
            case Y -> Integer.compare(y, 0);
            case Z -> Integer.compare(z, 0);
        };
    }

    private record StickerInfo(Box sticker, Point3D localNormal) {
    }

    private static javafx.geometry.Point3D axisVector(RotationAxis axis) {
        return switch (axis) {
            case X -> Rotate.X_AXIS;
            case Y -> Rotate.Y_AXIS;
            case Z -> Rotate.Z_AXIS;
        };
    }
}
