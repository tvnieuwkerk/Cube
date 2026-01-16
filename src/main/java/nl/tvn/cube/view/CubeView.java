package nl.tvn.cube.view;

import java.awt.BorderLayout;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.utils.universe.SimpleUniverse;
import javax.swing.JPanel;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import nl.tvn.cube.model.Axis;
import nl.tvn.cube.model.Face;
import nl.tvn.cube.model.Move;
import nl.tvn.cube.model.TurnDirection;
import nl.tvn.cube.viewmodel.CubeViewModel;

public final class CubeView {
    private final SwingNode swingNode;
    private final CubeViewModel viewModel;

    public CubeView() {
        this.swingNode = new SwingNode();
        this.viewModel = new CubeViewModel();
        initialize3D();
    }

    public Node getNode() {
        return swingNode;
    }

    public void registerInput(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);
    }

    private void initialize3D() {
        swingNode.setContent(buildSwingContent());
    }

    private JPanel buildSwingContent() {
        JPanel panel = new JPanel(new BorderLayout());
        Canvas3D canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
        panel.add(canvas, BorderLayout.CENTER);

        SimpleUniverse universe = new SimpleUniverse(canvas);
        TransformGroup viewTransform = universe.getViewingPlatform().getViewPlatformTransform();
        universe.addBranchGraph(viewModel.buildScene());
        setInitialCamera(viewTransform);
        return panel;
    }

    private void setInitialCamera(TransformGroup viewTransform) {
        Transform3D transform = new Transform3D();
        Point3d eye = new Point3d(3.5, 3.0, 3.5);
        Point3d center = new Point3d(0, 0, 0);
        Vector3d up = new Vector3d(0, 1, 0);
        transform.lookAt(eye, center, up);
        transform.invert();
        viewTransform.setTransform(transform);
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
