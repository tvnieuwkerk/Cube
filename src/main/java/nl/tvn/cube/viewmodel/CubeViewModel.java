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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import nl.tvn.cube.model.BeginnerMethodStep;
import nl.tvn.cube.model.BeginnerMethodValidator;
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
    private final BeginnerMethodValidator beginnerValidator;
    private final Map<BeginnerMethodStep, BooleanProperty> beginnerStepStatus;
    private final BooleanProperty busy = new SimpleBooleanProperty(false);
    private boolean animating;
    private boolean interacting;

    public CubeViewModel() {
        this.model = new CubeModel();
        this.cubeGroup = new Group();
        this.cubieViews = new HashMap<>();
        this.random = new Random();
        this.beginnerValidator = new BeginnerMethodValidator();
        this.beginnerStepStatus = new EnumMap<>(BeginnerMethodStep.class);
        for (BeginnerMethodStep step : BeginnerMethodStep.values()) {
            beginnerStepStatus.put(step, new SimpleBooleanProperty(false));
        }
        buildViews();
        updateBeginnerValidation();
    }

    public Group cubeGroup() {
        return cubeGroup;
    }

    public ReadOnlyBooleanProperty beginnerStepProperty(BeginnerMethodStep step) {
        return beginnerStepStatus.get(step);
    }

    public ReadOnlyBooleanProperty busyProperty() {
        return busy;
    }

    private void setAnimating(boolean value) {
        animating = value;
        busy.set(animating || interacting);
    }

    private void setInteracting(boolean value) {
        interacting = value;
        busy.set(animating || interacting);
    }

    public boolean isBusy() {
        return animating || interacting;
    }

    public void applyMove(Move move) {
        if (animating || interacting) {
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
        if (animating || interacting) {
            return;
        }
        model.reset();
        for (CubieView view : cubieViews.values()) {
            view.updateTranslation();
        }
        updateBeginnerValidation();
    }

    public void randomize() {
        if (animating || interacting) {
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
        if (animating || interacting || moves.isEmpty()) {
            return;
        }
        playMoveSequence(moves, TURN_DURATION);
    }

    public InteractiveSlice beginInteractiveCube(RotationAxis axis) {
        return beginInteractiveSlice(axis, 0, Set.of(-1, 0, 1));
    }

    public InteractiveSlice beginInteractiveSlice(RotationAxis axis, int layer) {
        return beginInteractiveSlice(axis, layer, Set.of(layer));
    }

    private InteractiveSlice beginInteractiveSlice(RotationAxis axis, int layer, Set<Integer> layers) {
        if (animating || interacting) {
            return null;
        }
        List<CubieModel> affected = new ArrayList<>();
        for (CubieModel cubie : model.cubies()) {
            if (isInLayer(cubie, axis, layers)) {
                affected.add(cubie);
            }
        }
        if (affected.isEmpty()) {
            return null;
        }
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
        setInteracting(true);
        return new InteractiveSlice(axis, layer, rotate, sliceGroup, views, affected);
    }

    public void updateInteractiveSlice(InteractiveSlice slice, double angleDegrees) {
        if (slice == null) {
            return;
        }
        slice.rotate().setAngle(angleDegrees);
    }

    public void finishInteractiveSlice(InteractiveSlice slice, int turns) {
        if (slice == null) {
            return;
        }
        slice.sliceGroup().getTransforms().clear();
        cubeGroup.getChildren().remove(slice.sliceGroup());
        int adjustedTurns = normalizeTurns(adjustTurnsForAxis(slice.axis(), turns));
        if (adjustedTurns != 0) {
            applyFinalTurns(slice.affected(), slice.axis(), adjustedTurns);
        }
        cubeGroup.getChildren().addAll(slice.views());
        setInteracting(false);
        if (!animating) {
            updateBeginnerValidation();
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
        setAnimating(true);
        timeline.setOnFinished(event -> {
            sliceGroup.getTransforms().clear();
            cubeGroup.getChildren().remove(sliceGroup);
            applyFinalTurns(affected, axis, turns);
            cubeGroup.getChildren().addAll(views);
            setAnimating(false);
            updateBeginnerValidation();
        });
        timeline.play();
    }

    private void playMoveSequence(List<Move> moves, Duration duration) {
        Deque<Move> queue = new ArrayDeque<>(moves);
        setAnimating(true);
        playNextMove(queue, duration);
    }

    private void playNextMove(Deque<Move> queue, Duration duration) {
        Move move = queue.pollFirst();
        if (move == null) {
            setAnimating(false);
            updateBeginnerValidation();
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
        Set<Integer> layers = affected.stream().map(cubie -> switch (axis) {
            case X -> cubie.coordinate().x();
            case Y -> cubie.coordinate().y();
            case Z -> cubie.coordinate().z();
        }).collect(java.util.stream.Collectors.toSet());
        model.applyMove(new Move(axis, layers, turns));
        affected.forEach(cubie -> cubieViews.get(cubie).updateTranslation());
        updateBeginnerValidation();
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

    private int adjustTurnsForAxis(RotationAxis axis, int turns) {
        if (axis == RotationAxis.X) {
            return -turns;
        }
        return turns;
    }

    private void updateBeginnerValidation() {
        Map<BeginnerMethodStep, Boolean> results = beginnerValidator.validate(model.cubies());
        for (Map.Entry<BeginnerMethodStep, Boolean> entry : results.entrySet()) {
            beginnerStepStatus.get(entry.getKey()).set(entry.getValue());
        }
    }

    private static javafx.geometry.Point3D axisVector(RotationAxis axis) {
        return switch (axis) {
            case X -> Rotate.X_AXIS;
            case Y -> Rotate.Y_AXIS;
            case Z -> Rotate.Z_AXIS;
        };
    }

    public record InteractiveSlice(
        RotationAxis axis,
        int layer,
        Rotate rotate,
        Group sliceGroup,
        List<CubieView> views,
        List<CubieModel> affected
    ) {
    }
}
