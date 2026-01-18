package nl.tvn.cube.viewmodel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import nl.tvn.cube.model.BeginnerMethodValidator;
import nl.tvn.cube.model.BeginnerStep;
import nl.tvn.cube.model.CubeModel;
import nl.tvn.cube.model.CubieModel;
import nl.tvn.cube.model.Move;
import nl.tvn.cube.model.RotationAxis;
import nl.tvn.cube.view.CubieView;

public final class CubeViewModel {
    private static final Duration TURN_DURATION = Duration.seconds(0.25);
    private static final Duration RANDOM_TURN_DURATION = Duration.seconds(0.05);
    private final CubeModel model;
    private final Group cubeGroup;
    private final Map<CubieModel, CubieView> cubieViews;
    private final Random random;
    private final BeginnerMethodValidator validator;
    private final Map<BeginnerStep, BooleanProperty> beginnerStepStatus;
    private boolean animating;

    public CubeViewModel() {
        this.model = new CubeModel();
        this.cubeGroup = new Group();
        this.cubieViews = new HashMap<>();
        this.random = new Random();
        this.validator = new BeginnerMethodValidator(model);
        this.beginnerStepStatus = new EnumMap<>(BeginnerStep.class);
        buildViews();
        initializeBeginnerSteps();
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
            animateTurn(affected, move.axis(), turns, TURN_DURATION);
        }
    }

    public void reset() {
        if (animating) {
            return;
        }
        model.reset();
        for (CubieView view : cubieViews.values()) {
            view.resetOrientation();
            view.updateTranslation();
        }
        updateBeginnerSteps();
    }

    public void randomize() {
        if (animating) {
            return;
        }
        int turnCount = 50 + random.nextInt(51);
        List<Move> moves = new ArrayList<>(turnCount);
        RotationAxis[] axes = RotationAxis.values();
        int[] layers = new int[] { -1, 1 };
        int[] turns = new int[] { -1, 1, 2, -2 };
        for (int i = 0; i < turnCount; i++) {
            RotationAxis axis = axes[random.nextInt(axes.length)];
            int layer = layers[random.nextInt(layers.length)];
            int turn = turns[random.nextInt(turns.length)];
            moves.add(new Move(axis, Set.of(layer), turn));
        }
        playMoveSequence(moves, RANDOM_TURN_DURATION);
    }

    public void applyMoves(List<Move> moves) {
        if (animating || moves.isEmpty()) {
            return;
        }
        playMoveSequence(moves, TURN_DURATION);
    }

    public ReadOnlyBooleanProperty beginnerStepStatus(BeginnerStep step) {
        return beginnerStepStatus.get(step);
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
            cubie.rotate(axis, turn);
            view.updateTranslation();
        }
    }

    private void animateTurn(List<CubieModel> affected, RotationAxis axis, int turns, Duration duration) {
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
            new KeyFrame(duration, new KeyValue(rotate.angleProperty(), angle))
        );
        animating = true;
        timeline.setOnFinished(event -> {
            sliceGroup.getTransforms().clear();
            cubeGroup.getChildren().remove(sliceGroup);
            applyFinalTurns(affected, axis, turns);
            cubeGroup.getChildren().addAll(views);
            updateBeginnerSteps();
            animating = false;
        });
        timeline.play();
    }

    private void playMoveSequence(List<Move> moves, Duration duration) {
        Deque<Move> queue = new ArrayDeque<>(moves);
        animating = true;
        playNextMove(queue, duration);
    }

    private void playNextMove(Deque<Move> queue, Duration duration) {
        Move move = queue.pollFirst();
        if (move == null) {
            animating = false;
            updateBeginnerSteps();
            return;
        }
        List<CubieModel> affected = new ArrayList<>();
        for (CubieModel cubie : model.cubies()) {
            if (isInLayer(cubie, move.axis(), move.layers())) {
                affected.add(cubie);
            }
        }
        int turns = normalizeTurns(move.quarterTurns());
        if (turns == 0) {
            playNextMove(queue, duration);
            return;
        }
        List<CubieView> views = new ArrayList<>(affected.size());
        for (CubieModel cubie : affected) {
            views.add(cubieViews.get(cubie));
        }
        cubeGroup.getChildren().removeAll(views);

        Group sliceGroup = new Group();
        sliceGroup.getChildren().addAll(views);
        cubeGroup.getChildren().add(sliceGroup);

        Rotate rotate = new Rotate(0, axisVector(move.axis()));
        sliceGroup.getTransforms().add(rotate);

        double angle = rotationAngle(move.axis(), turns);
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(rotate.angleProperty(), 0)),
            new KeyFrame(duration, new KeyValue(rotate.angleProperty(), angle))
        );
        timeline.setOnFinished(event -> {
            sliceGroup.getTransforms().clear();
            cubeGroup.getChildren().remove(sliceGroup);
            applyFinalTurns(affected, move.axis(), turns);
            cubeGroup.getChildren().addAll(views);
            playNextMove(queue, duration);
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

    private void initializeBeginnerSteps() {
        for (BeginnerStep step : BeginnerStep.values()) {
            beginnerStepStatus.put(step, new SimpleBooleanProperty(false));
        }
        updateBeginnerSteps();
    }

    private void updateBeginnerSteps() {
        for (Map.Entry<BeginnerStep, BooleanProperty> entry : beginnerStepStatus.entrySet()) {
            entry.getValue().set(validator.isStepSolved(entry.getKey()));
        }
    }

    private static javafx.geometry.Point3D axisVector(RotationAxis axis) {
        return switch (axis) {
            case X -> Rotate.X_AXIS;
            case Y -> Rotate.Y_AXIS;
            case Z -> Rotate.Z_AXIS;
        };
    }
}
