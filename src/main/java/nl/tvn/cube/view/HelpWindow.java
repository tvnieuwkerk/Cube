package nl.tvn.cube.view;

import java.util.List;
import java.util.Set;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import nl.tvn.cube.model.Move;
import nl.tvn.cube.model.RotationAxis;
import nl.tvn.cube.viewmodel.CubeViewModel;

public final class HelpWindow {
    private static final Duration TURN_DURATION = Duration.seconds(0.35);
    private static final Duration PAUSE_DURATION = Duration.seconds(0.2);
    private static final double CARD_SPACING = 12;
    private static final double CARD_PADDING = 10;
    private static final double CUBE_SCENE_SIZE = 240;
    private static final double CUBE_SCALE = 0.525;
    private static final double CAMERA_DISTANCE = 420;
    private static final double WINDOW_GAP = 12;
    private final Stage owner;
    private final Stage stage;

    public HelpWindow(Stage owner) {
        this.owner = owner;
        this.stage = new Stage();
        stage.initOwner(owner);
        stage.setTitle("Cube Help");
        stage.setScene(new Scene(buildContent()));
    }

    public void show() {
        if (!stage.isShowing()) {
            stage.show();
        }
        stage.sizeToScene();
        positionToRightOfOwner();
        stage.toFront();
    }

    private void positionToRightOfOwner() {
        Rectangle2D bounds = screenBoundsForOwner();
        double targetX = owner.getX() + owner.getWidth() + WINDOW_GAP;
        double targetY = owner.getY();
        double clampedX = Math.min(targetX, bounds.getMaxX() - stage.getWidth());
        double clampedY = Math.min(Math.max(bounds.getMinY(), targetY), bounds.getMaxY() - stage.getHeight());
        stage.setX(Math.max(bounds.getMinX(), clampedX));
        stage.setY(clampedY);
    }

    private Rectangle2D screenBoundsForOwner() {
        List<Screen> screens = Screen.getScreensForRectangle(
            owner.getX(),
            owner.getY(),
            owner.getWidth(),
            owner.getHeight()
        );
        if (!screens.isEmpty()) {
            return screens.getFirst().getVisualBounds();
        }
        return Screen.getPrimary().getVisualBounds();
    }

    private StackPane buildContent() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab shortcutsTab = new Tab("Shortcuts", buildHelpContent());
        Tab aboutTab = new Tab("About", buildAboutContent());
        tabPane.getTabs().addAll(shortcutsTab, aboutTab);

        return new StackPane(tabPane);
    }

    private StackPane buildHelpContent() {
        GridPane grid = new GridPane();
        grid.setPadding(Insets.EMPTY);
        grid.setHgap(CARD_SPACING);
        grid.setVgap(CARD_SPACING);

        List<TurnDefinition> turns = List.of(
            new TurnDefinition("U (Up) - U / Shift+U", faceMove(RotationAxis.Y, 1, 1)),
            new TurnDefinition("D (Down) - D / Shift+D", faceMove(RotationAxis.Y, -1, -1)),
            new TurnDefinition("R (Right) - R / Shift+R", faceMove(RotationAxis.X, 1, 1)),
            new TurnDefinition("L (Left) - L / Shift+L", faceMove(RotationAxis.X, -1, -1)),
            new TurnDefinition("F (Front) - F / Shift+F", faceMove(RotationAxis.Z, 1, 1)),
            new TurnDefinition("B (Back) - B / Shift+B", faceMove(RotationAxis.Z, -1, -1)),
            new TurnDefinition("M (Middle) - M / Shift+M", faceMove(RotationAxis.X, 0, 1)),
            new TurnDefinition("E (Equator) - E / Shift+E", faceMove(RotationAxis.Y, 0, 1)),
            new TurnDefinition("S (Standing) - S / Shift+S", faceMove(RotationAxis.Z, 0, 1))
        );

        for (int i = 0; i < turns.size(); i++) {
            TurnDefinition definition = turns.get(i);
            VBox card = buildTurnCard(definition);
            int column = i % 3;
            int row = i / 3;
            grid.add(card, column, row);
        }
        Label cameraKeys = new Label("Camera: Left/Right = Yaw | Up/Down = Pitch | PgUp/PgDn = Roll");
        cameraKeys.setTextFill(Color.LIGHTGRAY);
        cameraKeys.setStyle("-fx-font-size: 12px;");
        cameraKeys.setWrapText(true);

        VBox content = new VBox(CARD_SPACING, grid, cameraKeys);
        content.setPadding(new Insets(CARD_SPACING));

        StackPane helpContainer = new StackPane(content);
        helpContainer.setBackground(new Background(new BackgroundFill(Color.web("#1f1f1f"), CornerRadii.EMPTY, Insets.EMPTY)));
        return helpContainer;
    }

    private StackPane buildAboutContent() {
        Label description = new Label("Cube is an interactive Rubik's Cube simulator that lets you view, rotate, and"
            + " manipulate the cube using keyboard shortcuts and algorithm notation.");
        description.setTextFill(Color.LIGHTGRAY);
        description.setStyle("-fx-font-size: 12px;");
        description.setWrapText(true);

        VBox content = new VBox(12, description);
        content.setPadding(new Insets(CARD_SPACING));

        StackPane aboutContainer = new StackPane(content);
        aboutContainer.setBackground(new Background(new BackgroundFill(Color.web("#1f1f1f"), CornerRadii.EMPTY, Insets.EMPTY)));
        return aboutContainer;
    }

    private VBox buildTurnCard(TurnDefinition definition) {
        Label label = new Label(definition.label());
        label.setTextFill(Color.WHITE);
        label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        CubeViewModel viewModel = new CubeViewModel();
        StackPane preview = buildCubePreview(viewModel);
        preview.setPadding(new Insets(6));
        preview.setBackground(new Background(new BackgroundFill(Color.web("#2b2b2b"), new CornerRadii(6), Insets.EMPTY)));

        Move inverse = new Move(definition.move().axis(), definition.move().layers(), -definition.move().quarterTurns());
        Animation animation = buildAnimation(viewModel, definition.move(), inverse);
        animation.play();

        VBox card = new VBox(6, label, preview);
        card.setPadding(new Insets(CARD_PADDING));
        card.setBackground(new Background(new BackgroundFill(Color.web("#262626"), new CornerRadii(8), Insets.EMPTY)));
        return card;
    }

    private StackPane buildCubePreview(CubeViewModel viewModel) {
        Group root3d = new Group();
        Group cubeGroup = viewModel.cubeGroup();
        root3d.getChildren().add(cubeGroup);

        Box floor = new Box(300, 2, 300);
        floor.setTranslateY(100);
        floor.setMaterial(new PhongMaterial(Color.DARKGRAY));

        AmbientLight ambientLight = new AmbientLight(Color.color(0.7, 0.7, 0.7));
        PointLight keyLight = new PointLight(Color.WHITE);
        keyLight.getTransforms().add(new Translate(-200, -200, -200));

        root3d.getChildren().addAll(floor, ambientLight, keyLight);

        SubScene subScene = new SubScene(root3d, CUBE_SCENE_SIZE, CUBE_SCENE_SIZE, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.web("#1d1d1d"));
        subScene.setCamera(createCamera());

        cubeGroup.setScaleX(CUBE_SCALE);
        cubeGroup.setScaleY(CUBE_SCALE);
        cubeGroup.setScaleZ(CUBE_SCALE);

        return new StackPane(subScene);
    }

    private Animation buildAnimation(CubeViewModel viewModel, Move move, Move inverse) {
        PauseTransition applyMove = new PauseTransition(Duration.ZERO);
        applyMove.setOnFinished(event -> viewModel.applyMove(move));
        PauseTransition waitMove = new PauseTransition(TURN_DURATION.add(PAUSE_DURATION));

        PauseTransition applyInverse = new PauseTransition(Duration.ZERO);
        applyInverse.setOnFinished(event -> viewModel.applyMove(inverse));
        PauseTransition waitInverse = new PauseTransition(TURN_DURATION.add(PAUSE_DURATION));

        SequentialTransition sequence = new SequentialTransition(applyMove, waitMove, applyInverse, waitInverse);
        sequence.setCycleCount(Animation.INDEFINITE);
        return sequence;
    }

    private static Move faceMove(RotationAxis axis, int layer, int turns) {
        return new Move(axis, Set.of(layer), turns);
    }

    private Camera createCamera() {
        javafx.scene.PerspectiveCamera camera = new javafx.scene.PerspectiveCamera(true);
        Rotate pitch = new Rotate(-25, Rotate.X_AXIS);
        Rotate yaw = new Rotate(45, Rotate.Y_AXIS);
        Rotate roll = new Rotate(0, Rotate.Z_AXIS);
        camera.getTransforms().addAll(
            pitch,
            yaw,
            roll,
            new Translate(0, 0, -CAMERA_DISTANCE)
        );
        camera.setNearClip(0.1);
        camera.setFarClip(2000);
        return camera;
    }

    private record TurnDefinition(String label, Move move) {
    }
}
