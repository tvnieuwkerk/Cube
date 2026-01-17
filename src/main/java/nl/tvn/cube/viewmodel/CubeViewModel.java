package nl.tvn.cube.viewmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import nl.tvn.cube.model.CubeModel;
import nl.tvn.cube.model.CubieModel;
import nl.tvn.cube.model.Move;
import nl.tvn.cube.model.RotationAxis;
import nl.tvn.cube.view.CubieView;

public final class CubeViewModel {
    private static final Duration TURN_DURATION = Duration.seconds(0.25);
    private final CubeModel model;
    private final Group cubeGroup;
    private final Map<CubieModel, CubieView> cubieViews;
    private boolean animating;

    public CubeViewModel() {
        this.model = new CubeModel();
        this.cubeGroup = new Group();
        this.cubieViews = new HashMap<>();
        buildViews();
    }

    public Group cubeGroup() {
        return cubeGroup;
    }

    public void applyMove(Move move) {
        if (animating) {
            return;
        }
        List<CubieModel> affected = new ArrayList<>();
        for (CubieModel cubie : model.cubies()) {
            if (isInLayer(cubie, move.axis(), move.layers())) {
                affected.add(cubie);
            }
        }

        int turns = normalizeTurns(move.quarterTurns());
        if (turns != 0) {
            animateTurn(affected, move.axis(), turns);
        }
    }

    private void buildViews() {
        for (CubieModel cubie : model.cubies()) {
            CubieView view = new CubieView(cubie);
            cubieViews.put(cubie, view);
            cubeGroup.getChildren().add(view);
        }
    }

    private boolean isInLayer(CubieModel cubie, RotationAxis axis, Set<Integer> layers) {
        return switch (axis) {
            case X -> layers.contains(cubie.coordinate().x());
            case Y -> layers.contains(cubie.coordinate().y());
            case Z -> layers.contains(cubie.coordinate().z());
        };
    }

    private void applyQuarterTurn(List<CubieModel> affected, RotationAxis axis, int turn) {
        double angle = rotationAngle(axis, turn);
        for (CubieModel cubie : affected) {
            CubieView view = cubieViews.get(cubie);
            view.rotateAroundWorld(axis, angle);
            rotateCoordinate(cubie, axis, turn);
            view.updateTranslation();
        }
    }

    private void animateTurn(List<CubieModel> affected, RotationAxis axis, int turns) {
        List<CubieView> views = new ArrayList<>(affected.size());
        for (CubieModel cubie : affected) {
            views.add(cubieViews.get(cubie));
        }
        cubeGroup.getChildren().removeAll(views);

        Group sliceGroup = new Group();
        sliceGroup.getChildren().addAll(views);
        cubeGroup.getChildren().add(sliceGroup);

        Rotate rotate = new Rotate(0, axisVector(axis));
        sliceGroup.getTransforms().add(rotate);

        double angle = rotationAngle(axis, turns);
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(rotate.angleProperty(), 0)),
            new KeyFrame(TURN_DURATION, new KeyValue(rotate.angleProperty(), angle))
        );
        animating = true;
        timeline.setOnFinished(event -> {
            sliceGroup.getTransforms().clear();
            cubeGroup.getChildren().remove(sliceGroup);
            applyFinalTurns(affected, axis, turns);
            cubeGroup.getChildren().addAll(views);
            animating = false;
        });
        timeline.play();
    }

    private void applyFinalTurns(List<CubieModel> affected, RotationAxis axis, int turns) {
        int step = turns > 0 ? 1 : -1;
        for (int i = 0; i < Math.abs(turns); i++) {
            applyQuarterTurn(affected, axis, step);
        }
    }

    private double rotationAngle(RotationAxis axis, int turn) {
        double baseAngle = 90.0 * turn;
        return switch (axis) {
            case X -> -baseAngle;
            case Y, Z -> baseAngle;
        };
    }

    private void rotateCoordinate(CubieModel cubie, RotationAxis axis, int turn) {
        int x = cubie.coordinate().x();
        int y = cubie.coordinate().y();
        int z = cubie.coordinate().z();

        int newX = x;
        int newY = y;
        int newZ = z;

        if (axis == RotationAxis.X) {
            if (turn > 0) {
                newY = z;
                newZ = -y;
            } else {
                newY = -z;
                newZ = y;
            }
        } else if (axis == RotationAxis.Y) {
            if (turn > 0) {
                newX = -z;
                newZ = x;
            } else {
                newX = z;
                newZ = -x;
            }
        } else if (axis == RotationAxis.Z) {
            if (turn > 0) {
                newX = y;
                newY = -x;
            } else {
                newX = -y;
                newY = x;
            }
        }

        cubie.coordinate().set(newX, newY, newZ);
    }

    private int normalizeTurns(int turns) {
        int normalized = turns % 4;
        if (normalized == 3) {
            return -1;
        }
        if (normalized == -3) {
            return 1;
        }
        return normalized;
    }

    private static javafx.geometry.Point3D axisVector(RotationAxis axis) {
        return switch (axis) {
            case X -> Rotate.X_AXIS;
            case Y -> Rotate.Y_AXIS;
            case Z -> Rotate.Z_AXIS;
        };
    }
}
