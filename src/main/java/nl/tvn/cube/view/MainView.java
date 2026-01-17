package nl.tvn.cube.view;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Camera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.AmbientLight;
import javafx.scene.PointLight;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import nl.tvn.cube.viewmodel.CubeViewModel;
import nl.tvn.cube.viewmodel.MoveFactory;

public final class MainView {
    private static final double CUBE_SCALE_TARGET_RATIO = 0.40;
    private static final double CUBE_FRUSTUM_MARGIN = 0.92;
    private static final double CAMERA_DISTANCE = 600.0;
    private static final double SCENE_PADDING = 10.0;
    private static final double CAMERA_YAW_STEP = 8.0;
    private static final double CAMERA_PITCH_STEP = 6.0;
    private static final double CAMERA_ROLL_STEP = 6.0;
    private static final double SQRT_3 = Math.sqrt(3.0);
    private final CubeViewModel viewModel;
    private final BorderPane root;
    private Rotate cameraYaw;
    private Rotate cameraPitch;
    private Rotate cameraRoll;

    public MainView(CubeViewModel viewModel) {
        this.viewModel = viewModel;
        this.root = new BorderPane();
        root.setTop(buildControls());
        root.setCenter(buildScene());
        root.setBottom(buildHelp());
    }

    public BorderPane root() {
        return root;
    }

    public void bindInput(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT) {
                rotateCameraYaw(-CAMERA_YAW_STEP);
                event.consume();
                return;
            }
            if (event.getCode() == KeyCode.RIGHT) {
                rotateCameraYaw(CAMERA_YAW_STEP);
                event.consume();
                return;
            }
            if (event.getCode() == KeyCode.UP) {
                rotateCameraPitch(-CAMERA_PITCH_STEP);
                event.consume();
                return;
            }
            if (event.getCode() == KeyCode.DOWN) {
                rotateCameraPitch(CAMERA_PITCH_STEP);
                event.consume();
                return;
            }
            if (event.getCode() == KeyCode.PAGE_UP) {
                rotateCameraRoll(-CAMERA_ROLL_STEP);
                event.consume();
                return;
            }
            if (event.getCode() == KeyCode.PAGE_DOWN) {
                rotateCameraRoll(CAMERA_ROLL_STEP);
                event.consume();
                return;
            }
            MoveFactory.wideMove(event)
                .or(() -> MoveFactory.fromKeyEvent(event))
                .ifPresent(viewModel::applyMove);
        });
    }

    private StackPane buildScene() {
        Group root3d = new Group();
        Group cubeGroup = viewModel.cubeGroup();
        root3d.getChildren().add(cubeGroup);

        Box floor = new Box(800, 2, 800);
        floor.setTranslateY(150);
        floor.setMaterial(new PhongMaterial(Color.DARKGRAY));
        root3d.getChildren().add(floor);
        root3d.getChildren().add(buildAxes());

        AmbientLight ambientLight = new AmbientLight(Color.color(0.7, 0.7, 0.7));
        PointLight keyLight = new PointLight(Color.WHITE);
        keyLight.getTransforms().add(new Translate(-300, -300, -300));
        root3d.getChildren().addAll(ambientLight, keyLight);

        SubScene subScene = new SubScene(root3d, 900, 600, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.web("#202020"));
        Camera camera = createCamera();
        subScene.setCamera(camera);

        StackPane container = new StackPane(subScene);
        container.setPadding(new Insets(SCENE_PADDING));
        container.setMinSize(0, 0);
        subScene.widthProperty().bind(Bindings.max(1, container.widthProperty().subtract(SCENE_PADDING * 2)));
        subScene.heightProperty().bind(Bindings.max(1, container.heightProperty().subtract(SCENE_PADDING * 2)));
        cubeGroup.scaleXProperty().bind(Bindings.createDoubleBinding(
            () -> cubeScale(camera, subScene.getWidth(), subScene.getHeight()),
            subScene.widthProperty(),
            subScene.heightProperty()));
        cubeGroup.scaleYProperty().bind(cubeGroup.scaleXProperty());
        cubeGroup.scaleZProperty().bind(cubeGroup.scaleXProperty());
        return container;
    }

    private VBox buildHelp() {
        Label title = new Label("Controls: F B R L U D (Shift = counter, Ctrl = 180°, Alt = wide)");
        Label slices = new Label("Slices: M E S | Cube rotations: X Y Z | Camera: L/R arrows, Up/Down = X, PgUp/PgDn = Z");
        Label buttons = new Label("Buttons: Reset | Randomize");
        VBox help = new VBox(4, title, slices, buttons);
        help.setPadding(new Insets(10));
        help.setMinHeight(Region.USE_PREF_SIZE);
        help.setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: white;");
        title.setTextFill(Color.WHITE);
        slices.setTextFill(Color.LIGHTGRAY);
        buttons.setTextFill(Color.LIGHTGRAY);
        return help;
    }

    private HBox buildControls() {
        Button reset = new Button("Reset");
        reset.setFocusTraversable(false);
        reset.setOnAction(event -> viewModel.reset());
        Button randomize = new Button("Randomize");
        randomize.setFocusTraversable(false);
        randomize.setOnAction(event -> viewModel.randomize());
        HBox controls = new HBox(10, reset, randomize);
        controls.setPadding(new Insets(10));
        controls.setStyle("-fx-background-color: #252525;");
        return controls;
    }

    private void rotateCameraYaw(double deltaDegrees) {
        if (cameraYaw == null) {
            return;
        }
        cameraYaw.setAngle(cameraYaw.getAngle() + deltaDegrees);
    }

    private void rotateCameraPitch(double deltaDegrees) {
        if (cameraPitch == null) {
            return;
        }
        cameraPitch.setAngle(cameraPitch.getAngle() + deltaDegrees);
    }

    private void rotateCameraRoll(double deltaDegrees) {
        if (cameraRoll == null) {
            return;
        }
        cameraRoll.setAngle(cameraRoll.getAngle() + deltaDegrees);
    }

    private Group buildAxes() {
        double baseCubeSize = CubeConstants.CUBIE_SIZE * 3 + CubeConstants.CUBIE_GAP * 2;
        double axisLength = baseCubeSize;
        double axisThickness = CubeConstants.STICKER_THICKNESS;

        Box xAxis = new Box(axisLength, axisThickness, axisThickness);
        xAxis.setTranslateX(axisLength / 2);
        xAxis.setMaterial(new PhongMaterial(Color.RED));

        Box yAxis = new Box(axisThickness, axisLength, axisThickness);
        yAxis.setTranslateY(-axisLength / 2);
        yAxis.setMaterial(new PhongMaterial(Color.LAWNGREEN));

        Box zAxis = new Box(axisThickness, axisThickness, axisLength);
        zAxis.setTranslateZ(-axisLength / 2);
        zAxis.setMaterial(new PhongMaterial(Color.DODGERBLUE));

        return new Group(xAxis, yAxis, zAxis);
    }

    private double cubeScale(Camera camera, double width, double height) {
        double minDimension = Math.min(width, height);
        double baseCubeSize = CubeConstants.CUBIE_SIZE * 3 + CubeConstants.CUBIE_GAP * 2;
        double baseCubeDiagonal = baseCubeSize * SQRT_3;
        double targetSize = minDimension * CUBE_SCALE_TARGET_RATIO;
        double scale = targetSize / baseCubeDiagonal;
        if (camera instanceof javafx.scene.PerspectiveCamera perspectiveCamera) {
            double fov = perspectiveCamera.getFieldOfView();
            double maxWorldHeight = 2 * CAMERA_DISTANCE * Math.tan(Math.toRadians(fov / 2));
            double safeHeight = Math.max(1, height);
            double safeWidth = Math.max(1, width);
            double aspectRatio = safeWidth / safeHeight;
            double maxWorldWidth = maxWorldHeight * aspectRatio;
            double maxWorldSize = Math.min(maxWorldHeight, maxWorldWidth) * CUBE_FRUSTUM_MARGIN;
            double maxScale = maxWorldSize / baseCubeDiagonal;
            scale = Math.min(scale, maxScale);
        }
        return scale;
    }

    private Camera createCamera() {
        javafx.scene.PerspectiveCamera camera = new javafx.scene.PerspectiveCamera(true);
        cameraPitch = new Rotate(-25, Rotate.X_AXIS);
        cameraYaw = new Rotate(45, Rotate.Y_AXIS);
        cameraRoll = new Rotate(0, Rotate.Z_AXIS);
        camera.getTransforms().addAll(
            cameraPitch,
            cameraYaw,
            cameraRoll,
            new Translate(0, 0, -600)
        );
        camera.setNearClip(0.1);
        camera.setFarClip(2000);
        return camera;
    }
}
