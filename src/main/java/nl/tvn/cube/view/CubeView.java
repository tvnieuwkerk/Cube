package nl.tvn.cube.view;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import nl.tvn.cube.model.Axis;
import nl.tvn.cube.model.Face;
import nl.tvn.cube.model.Move;
import nl.tvn.cube.model.TurnDirection;
import nl.tvn.cube.viewmodel.CubeViewModel;

public final class CubeView {
    private final SubScene subScene;
    private final CubeViewModel viewModel;

    public CubeView() {
        this.viewModel = new CubeViewModel();
        this.subScene = buildSubScene();
    }

    public Node getNode() {
        return subScene;
    }

    public void registerInput(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);
        subScene.widthProperty().bind(scene.widthProperty());
        subScene.heightProperty().bind(scene.heightProperty());
    }

    private SubScene buildSubScene() {
        Group root = new Group();
        Group cube = viewModel.buildScene();
        root.getChildren().add(cube);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(5000);
        camera.getTransforms().addAll(
            new Rotate(-25, Rotate.X_AXIS),
            new Rotate(-45, Rotate.Y_AXIS),
            new Translate(0, 0, -600)
        );

        SubScene scene = new SubScene(root, 900, 700, true, javafx.scene.SceneAntialiasing.BALANCED);
        scene.setCamera(camera);
        return scene;
    }

    private void handleKeyPress(KeyEvent event) {
        TurnDirection direction = event.isShiftDown() ? TurnDirection.COUNTER_CLOCKWISE : TurnDirection.CLOCKWISE;
        int turns = event.isControlDown() ? 2 : 1;
        boolean wide = event.isAltDown();
        Move move = mapKeyToMove(event.getCode(), direction, turns, wide);
        if (move != null) {
            viewModel.applyMove(move);
            event.consume();
        }
    }

    private Move mapKeyToMove(KeyCode code, TurnDirection direction, int turns, boolean wide) {
        return switch (code) {
            case F -> wide ? Move.wide(Face.FRONT, direction, turns) : Move.face(Face.FRONT, direction, turns);
            case B -> wide ? Move.wide(Face.BACK, direction, turns) : Move.face(Face.BACK, direction, turns);
            case R -> wide ? Move.wide(Face.RIGHT, direction, turns) : Move.face(Face.RIGHT, direction, turns);
            case L -> wide ? Move.wide(Face.LEFT, direction, turns) : Move.face(Face.LEFT, direction, turns);
            case U -> wide ? Move.wide(Face.UP, direction, turns) : Move.face(Face.UP, direction, turns);
            case D -> wide ? Move.wide(Face.DOWN, direction, turns) : Move.face(Face.DOWN, direction, turns);
            case M -> Move.slice(Axis.X, direction, turns);
            case E -> Move.slice(Axis.Y, direction, turns);
            case S -> Move.slice(Axis.Z, direction, turns);
            case X -> Move.rotation(Axis.X, direction, turns);
            case Y -> Move.rotation(Axis.Y, direction, turns);
            case Z -> Move.rotation(Axis.Z, direction, turns);
            default -> null;
        };
    }
}
