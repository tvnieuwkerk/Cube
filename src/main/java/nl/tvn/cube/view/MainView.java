package nl.tvn.cube.view;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
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
    private final CubeViewModel viewModel;
    private final BorderPane root;

    public MainView(CubeViewModel viewModel) {
        this.viewModel = viewModel;
        this.root = new BorderPane();
        root.setCenter(buildScene());
        root.setBottom(buildHelp());
    }

    public BorderPane root() {
        return root;
    }

    public void bindInput(Scene scene) {
        scene.setOnKeyPressed(event -> MoveFactory.wideMove(event)
            .or(() -> MoveFactory.fromKeyEvent(event))
            .ifPresent(viewModel::applyMove));
    }

    private StackPane buildScene() {
        Group root3d = new Group();
        root3d.getChildren().add(viewModel.cubeGroup());

        Box floor = new Box(800, 2, 800);
        floor.setTranslateY(150);
        floor.setMaterial(new PhongMaterial(Color.DARKGRAY));
        root3d.getChildren().add(floor);

        AmbientLight ambientLight = new AmbientLight(Color.color(0.7, 0.7, 0.7));
        PointLight keyLight = new PointLight(Color.WHITE);
        keyLight.getTransforms().add(new Translate(-300, -300, -300));
        root3d.getChildren().addAll(ambientLight, keyLight);

        SubScene subScene = new SubScene(root3d, 900, 600, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.web("#202020"));
        subScene.setCamera(CameraFactory.createCamera());

        StackPane container = new StackPane(subScene);
        container.setPadding(new Insets(10));
        return container;
    }

    private VBox buildHelp() {
        Label title = new Label("Controls: F B R L U D (Shift = counter, Ctrl = 180°, Alt = wide)");
        Label slices = new Label("Slices: M E S | Cube rotations: X Y Z");
        VBox help = new VBox(4, title, slices);
        help.setPadding(new Insets(10));
        help.setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: white;");
        title.setTextFill(Color.WHITE);
        slices.setTextFill(Color.LIGHTGRAY);
        return help;
    }

    private static final class CameraFactory {
        private static javafx.scene.Camera createCamera() {
            javafx.scene.PerspectiveCamera camera = new javafx.scene.PerspectiveCamera(true);
            camera.getTransforms().addAll(
                new Rotate(-25, Rotate.X_AXIS),
                new Rotate(45, Rotate.Y_AXIS),
                new Translate(0, 0, -600)
            );
            camera.setNearClip(0.1);
            camera.setFarClip(2000);
            return camera;
        }
    }
}
