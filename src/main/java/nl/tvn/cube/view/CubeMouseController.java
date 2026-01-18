package nl.tvn.cube.view;

import javafx.scene.Group;
import javafx.scene.SubScene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import nl.tvn.cube.model.Move;
import nl.tvn.cube.model.RotationAxis;
import nl.tvn.cube.viewmodel.CubeViewModel;

import java.util.Set;

public final class CubeMouseController {
    private static final double DRAG_THRESHOLD = 4.0;
    private static final double ROTATION_SENSITIVITY = 0.45;
    private final SubScene scene;
    private final Group cubeGroup;
    private final CubeViewModel viewModel;
    private FaceSelection selectedFace;
    private Box selectionOutline;
    private InteractionState state = InteractionState.IDLE;
    private FacePickInfo pressPick;
    private double pressSceneX;
    private double pressSceneY;
    private Rotate activeRotate;
    private CubeViewModel.InteractiveSlice activeSlice;
    private RotationAxis activeAxis;
    private int activeLayer;
    private int activeDirection;

    public CubeMouseController(SubScene scene, Group cubeGroup, CubeViewModel viewModel) {
        this.scene = scene;
        this.cubeGroup = cubeGroup;
        this.viewModel = viewModel;
        attachHandlers();
    }

    private void attachHandlers() {
        scene.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handlePressed);
        scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleDragged);
        scene.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleReleased);
    }

    private void handlePressed(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY || viewModel.isBusy()) {
            return;
        }
        pressPick = pickInfo(event);
        pressSceneX = event.getSceneX();
        pressSceneY = event.getSceneY();
        state = InteractionState.PENDING;
    }

    private void handleDragged(MouseEvent event) {
        if (viewModel.isBusy() && state == InteractionState.PENDING) {
            state = InteractionState.IDLE;
            return;
        }
        if (state == InteractionState.PENDING) {
            double deltaX = event.getSceneX() - pressSceneX;
            double deltaY = event.getSceneY() - pressSceneY;
            if (Math.hypot(deltaX, deltaY) < DRAG_THRESHOLD) {
                return;
            }
            if (pressPick == null) {
                state = InteractionState.IDLE;
                return;
            }
            if (selectedFace != null && selectedFace.matches(pressPick)) {
                startFaceRotation();
            } else {
                startSliceDrag(deltaX, deltaY);
            }
        }
        if (state == InteractionState.FACE_ROTATE_DRAG && activeRotate != null) {
            double deltaX = event.getSceneX() - pressSceneX;
            double angle = deltaX * ROTATION_SENSITIVITY * activeDirection;
            activeRotate.setAngle(angle);
        }
        if (state == InteractionState.SLICE_DRAG && activeSlice != null) {
            double deltaX = event.getSceneX() - pressSceneX;
            double deltaY = event.getSceneY() - pressSceneY;
            double dominant = Math.abs(deltaX) >= Math.abs(deltaY) ? deltaX : -deltaY;
            double angle = dominant * ROTATION_SENSITIVITY * activeDirection;
            viewModel.updateInteractiveSlice(activeSlice, angle);
        }
    }

    private void handleReleased(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        if (state == InteractionState.PENDING) {
            FacePickInfo releasePick = pickInfo(event);
            if (releasePick != null) {
                selectFace(releasePick);
            } else {
                clearSelection();
            }
        }
        if (state == InteractionState.FACE_ROTATE_DRAG) {
            finishFaceRotation();
        }
        if (state == InteractionState.SLICE_DRAG) {
            finishSliceRotation();
        }
        state = InteractionState.IDLE;
        pressPick = null;
    }

    private void startFaceRotation() {
        if (selectedFace == null) {
            state = InteractionState.IDLE;
            return;
        }
        activeAxis = selectedFace.axis();
        activeLayer = selectedFace.layer();
        activeDirection = activeLayer > 0 ? 1 : -1;
        activeRotate = new Rotate(0, axisVector(activeAxis));
        cubeGroup.getTransforms().add(activeRotate);
        state = InteractionState.FACE_ROTATE_DRAG;
    }

    private void startSliceDrag(double deltaX, double deltaY) {
        if (pressPick == null) {
            state = InteractionState.IDLE;
            return;
        }
        boolean horizontal = Math.abs(deltaX) >= Math.abs(deltaY);
        DragMapping mapping = dragMapping(pressPick, horizontal, deltaX, deltaY);
        activeAxis = mapping.axis();
        activeLayer = mapping.layer();
        activeDirection = mapping.direction();
        activeSlice = viewModel.beginInteractiveSlice(activeAxis, activeLayer);
        if (activeSlice == null) {
            state = InteractionState.IDLE;
            return;
        }
        state = InteractionState.SLICE_DRAG;
    }

    private void finishFaceRotation() {
        if (activeRotate == null) {
            return;
        }
        double angle = activeRotate.getAngle();
        cubeGroup.getTransforms().remove(activeRotate);
        activeRotate = null;
        int turns = (int) Math.round(angle / 90.0);
        if (turns != 0) {
            viewModel.applyMove(new Move(activeAxis, Set.of(-1, 0, 1), turns));
        }
        clearSelection();
    }

    private void finishSliceRotation() {
        if (activeSlice == null) {
            return;
        }
        double angle = activeSlice.rotate().getAngle();
        int turns = (int) Math.round(angle / 90.0);
        viewModel.finishInteractiveSlice(activeSlice, turns);
        activeSlice = null;
    }

    private void selectFace(FacePickInfo pick) {
        selectedFace = new FaceSelection(pick.axis(), pick.layer());
        updateSelectionOutline();
    }

    private void clearSelection() {
        selectedFace = null;
        removeSelectionOutline();
    }

    private void updateSelectionOutline() {
        removeSelectionOutline();
        if (selectedFace == null) {
            return;
        }
        selectionOutline = buildSelectionOutline(selectedFace.axis(), selectedFace.layer());
        cubeGroup.getChildren().add(selectionOutline);
    }

    private void removeSelectionOutline() {
        if (selectionOutline != null) {
            cubeGroup.getChildren().remove(selectionOutline);
            selectionOutline = null;
        }
    }

    private Box buildSelectionOutline(RotationAxis axis, int layer) {
        double faceSize = CubeConstants.CUBIE_SIZE * 3 + CubeConstants.CUBIE_GAP * 2;
        double thickness = CubeConstants.STICKER_THICKNESS * 1.5;
        double offset = CubeConstants.STICKER_THICKNESS * 1.25;
        Box outline;
        switch (axis) {
            case X -> {
                outline = new Box(thickness, faceSize, faceSize);
                outline.setTranslateX(layer * CubeConstants.step() + (layer > 0 ? offset : -offset));
            }
            case Y -> {
                outline = new Box(faceSize, thickness, faceSize);
                outline.setTranslateY(-layer * CubeConstants.step() + (layer > 0 ? -offset : offset));
            }
            case Z -> {
                outline = new Box(faceSize, faceSize, thickness);
                outline.setTranslateZ(-layer * CubeConstants.step() + (layer > 0 ? -offset : offset));
            }
            default -> throw new IllegalStateException("Unexpected axis " + axis);
        }
        outline.setDrawMode(DrawMode.LINE);
        outline.setMaterial(new PhongMaterial(Color.GOLD));
        outline.setMouseTransparent(true);
        return outline;
    }

    private static FacePickInfo pickInfo(MouseEvent event) {
        if (event.getPickResult() == null || event.getPickResult().getIntersectedNode() == null) {
            return null;
        }
        Object data = event.getPickResult().getIntersectedNode().getUserData();
        if (data instanceof FacePickInfo pickInfo) {
            return pickInfo;
        }
        return null;
    }

    private static DragMapping dragMapping(FacePickInfo pick, boolean horizontal, double deltaX, double deltaY) {
        int direction = horizontal ? (deltaX >= 0 ? 1 : -1) : (deltaY >= 0 ? 1 : -1);
        RotationAxis faceAxis = pick.axis();
        int faceLayer = pick.layer();
        RotationAxis axis;
        int layer;
        if (faceAxis == RotationAxis.Z) {
            if (horizontal) {
                axis = RotationAxis.Y;
                layer = pick.y();
                direction *= faceLayer > 0 ? 1 : -1;
            } else {
                axis = RotationAxis.X;
                layer = pick.x();
                direction *= faceLayer > 0 ? -1 : 1;
            }
        } else if (faceAxis == RotationAxis.X) {
            if (horizontal) {
                axis = RotationAxis.Y;
                layer = pick.y();
                direction *= faceLayer > 0 ? -1 : 1;
            } else {
                axis = RotationAxis.Z;
                layer = pick.z();
                direction *= faceLayer > 0 ? 1 : -1;
            }
        } else {
            if (horizontal) {
                axis = RotationAxis.Z;
                layer = pick.z();
                direction *= faceLayer > 0 ? 1 : -1;
            } else {
                axis = RotationAxis.X;
                layer = pick.x();
                direction *= faceLayer > 0 ? 1 : -1;
            }
        }
        return new DragMapping(axis, layer, direction);
    }

    private static javafx.geometry.Point3D axisVector(RotationAxis axis) {
        return switch (axis) {
            case X -> Rotate.X_AXIS;
            case Y -> Rotate.Y_AXIS;
            case Z -> Rotate.Z_AXIS;
        };
    }

    private record FaceSelection(RotationAxis axis, int layer) {
        boolean matches(FacePickInfo pick) {
            return axis == pick.axis() && layer == pick.layer();
        }
    }

    private record DragMapping(RotationAxis axis, int layer, int direction) {
    }

    private enum InteractionState {
        IDLE,
        PENDING,
        FACE_ROTATE_DRAG,
        SLICE_DRAG
    }
}
