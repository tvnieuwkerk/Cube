package nl.tvn.cube.viewmodel;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import nl.tvn.cube.model.Axis;
import nl.tvn.cube.model.CubeColor;
import nl.tvn.cube.model.CubeModel;
import nl.tvn.cube.model.Cubie;
import nl.tvn.cube.model.Face;
import nl.tvn.cube.model.Move;
import nl.tvn.cube.model.TurnDirection;
import nl.tvn.cube.util.Vector3i;

public final class CubeViewModel {
    private static final double CUBIE_SIZE = 40;
    private static final double CUBIE_GAP = 6;

    private final CubeModel cubeModel;
    private final List<CubieNode> cubieNodes;

    public CubeViewModel() {
        this.cubeModel = new CubeModel();
        this.cubieNodes = new ArrayList<>();
    }

    public Group buildScene() {
        Group root = new Group();
        for (Cubie cubie : cubeModel.cubies()) {
            Group cubieGroup = new Group();
            Translate translate = createTranslate(cubie.position());
            cubieGroup.getTransforms().add(translate);
            cubieGroup.getChildren().add(createCubieBox(cubie));
            cubieNodes.add(new CubieNode(cubie, cubieGroup, translate));
            root.getChildren().add(cubieGroup);
        }
        root.getChildren().addAll(createLighting());
        return root;
    }

    public void applyMove(Move move) {
        switch (move.kind()) {
            case FACE -> applyFaceMove(move);
            case SLICE -> applySliceMove(move);
            case WIDE -> applyWideMove(move);
            case ROTATION -> applyRotationMove(move);
            default -> throw new IllegalArgumentException("Unknown move kind: " + move.kind());
        }
    }

    private void applyFaceMove(Move move) {
        Face face = move.face();
        RotationSpec spec = RotationSpec.fromFace(face);
        rotateLayer(spec, move.direction(), move.turns());
    }

    private void applySliceMove(Move move) {
        RotationSpec spec = RotationSpec.fromSlice(move.axis());
        rotateLayer(spec, move.direction(), move.turns());
    }

    private void applyWideMove(Move move) {
        Face face = move.face();
        RotationSpec spec = RotationSpec.fromWide(face);
        rotateLayer(spec, move.direction(), move.turns());
    }

    private void applyRotationMove(Move move) {
        RotationSpec spec = RotationSpec.fromRotation(move.axis());
        rotateLayer(spec, move.direction(), move.turns());
    }

    private void rotateLayer(RotationSpec spec, TurnDirection direction, int turns) {
        int steps = Math.max(1, Math.min(turns, 2));
        for (int step = 0; step < steps; step++) {
            rotateLayerOnce(spec, direction);
        }
    }

    private void rotateLayerOnce(RotationSpec spec, TurnDirection direction) {
        int directionSign = direction == TurnDirection.CLOCKWISE ? -1 : 1;
        int angleSign = directionSign * spec.axisSign() * spec.directionMultiplier();
        List<CubieNode> nodes = selectCubies(spec);
        for (CubieNode node : nodes) {
            applyRotation(node, spec.axis(), angleSign);
        }
    }

    private List<CubieNode> selectCubies(RotationSpec spec) {
        List<CubieNode> selected = new ArrayList<>();
        for (CubieNode node : cubieNodes) {
            Vector3i pos = node.cubie().position();
            if (spec.layer() == null) {
                selected.add(node);
                continue;
            }
            int coordinate = switch (spec.axis()) {
                case X -> pos.x();
                case Y -> pos.y();
                case Z -> pos.z();
            };
            if (spec.layers().contains(coordinate)) {
                selected.add(node);
            }
        }
        return selected;
    }

    private void applyRotation(CubieNode node, Axis axis, int angleSign) {
        Group group = node.group();
        Rotate rotate = new Rotate(90 * angleSign, 0, 0, 0, axisVector(axis));
        group.getTransforms().add(0, rotate);

        Vector3i newPosition = rotatePosition(node.cubie().position(), axis, angleSign);
        node.cubie().setPosition(newPosition);
        updateTranslate(node.translate(), newPosition);
    }

    private Vector3i rotatePosition(Vector3i position, Axis axis, int angleSign) {
        Vector3i rotated = position;
        int steps = Math.abs(angleSign);
        int stepSign = angleSign >= 0 ? 1 : -1;
        for (int i = 0; i < steps; i++) {
            rotated = rotatePosition90(rotated, axis, stepSign);
        }
        return rotated;
    }

    private Vector3i rotatePosition90(Vector3i position, Axis axis, int angleSign) {
        int x = position.x();
        int y = position.y();
        int z = position.z();
        return switch (axis) {
            case X -> angleSign > 0
                ? new Vector3i(x, -z, y)
                : new Vector3i(x, z, -y);
            case Y -> angleSign > 0
                ? new Vector3i(z, y, -x)
                : new Vector3i(-z, y, x);
            case Z -> angleSign > 0
                ? new Vector3i(-y, x, z)
                : new Vector3i(y, -x, z);
        };
    }

    private Translate createTranslate(Vector3i position) {
        double spacing = CUBIE_SIZE + CUBIE_GAP;
        return new Translate(position.x() * spacing, -position.y() * spacing, position.z() * spacing);
    }

    private void updateTranslate(Translate translate, Vector3i position) {
        double spacing = CUBIE_SIZE + CUBIE_GAP;
        translate.setX(position.x() * spacing);
        translate.setY(-position.y() * spacing);
        translate.setZ(position.z() * spacing);
    }

    private Group createCubieBox(Cubie cubie) {
        Group group = new Group();
        double half = CUBIE_SIZE / 2;
        double faceThickness = CUBIE_SIZE * 0.1;

        group.getChildren().add(createFace(CUBIE_SIZE, CUBIE_SIZE, faceThickness, 0, 0, half,
            cubie.faceColors().get(Face.FRONT)));
        group.getChildren().add(createFace(CUBIE_SIZE, CUBIE_SIZE, faceThickness, 0, 0, -half,
            cubie.faceColors().get(Face.BACK)));
        group.getChildren().add(createFace(faceThickness, CUBIE_SIZE, CUBIE_SIZE, half, 0, 0,
            cubie.faceColors().get(Face.RIGHT)));
        group.getChildren().add(createFace(faceThickness, CUBIE_SIZE, CUBIE_SIZE, -half, 0, 0,
            cubie.faceColors().get(Face.LEFT)));
        group.getChildren().add(createFace(CUBIE_SIZE, faceThickness, CUBIE_SIZE, 0, -half, 0,
            cubie.faceColors().get(Face.UP)));
        group.getChildren().add(createFace(CUBIE_SIZE, faceThickness, CUBIE_SIZE, 0, half, 0,
            cubie.faceColors().get(Face.DOWN)));
        return group;
    }

    private Box createFace(double width, double height, double depth, double x, double y, double z, CubeColor color) {
        Box face = new Box(width, height, depth);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(mapColor(color));
        material.setSpecularColor(Color.color(0.2, 0.2, 0.2));
        face.setMaterial(material);
        face.setTranslateX(x);
        face.setTranslateY(y);
        face.setTranslateZ(z);
        return face;
    }

    private Color mapColor(CubeColor color) {
        return switch (color) {
            case WHITE -> Color.WHITE;
            case YELLOW -> Color.YELLOW;
            case RED -> Color.RED;
            case ORANGE -> Color.ORANGE;
            case BLUE -> Color.DODGERBLUE;
            case GREEN -> Color.GREEN;
            case BLACK -> Color.BLACK;
        };
    }

    private List<Node> createLighting() {
        javafx.scene.AmbientLight ambient = new javafx.scene.AmbientLight(Color.color(0.4, 0.4, 0.4));
        javafx.scene.PointLight light = new javafx.scene.PointLight(Color.WHITE);
        light.setTranslateX(-300);
        light.setTranslateY(-300);
        light.setTranslateZ(-300);
        return List.of(ambient, light);
    }

    private Point3D axisVector(Axis axis) {
        return switch (axis) {
            case X -> Rotate.X_AXIS;
            case Y -> Rotate.Y_AXIS;
            case Z -> Rotate.Z_AXIS;
        };
    }

    private record RotationSpec(Axis axis, Integer layer, List<Integer> layers, int axisSign, int directionMultiplier) {
        static RotationSpec fromFace(Face face) {
            return switch (face) {
                case FRONT -> new RotationSpec(Axis.Z, 1, List.of(1), 1, 1);
                case BACK -> new RotationSpec(Axis.Z, -1, List.of(-1), -1, 1);
                case RIGHT -> new RotationSpec(Axis.X, 1, List.of(1), 1, 1);
                case LEFT -> new RotationSpec(Axis.X, -1, List.of(-1), -1, 1);
                case UP -> new RotationSpec(Axis.Y, 1, List.of(1), 1, 1);
                case DOWN -> new RotationSpec(Axis.Y, -1, List.of(-1), -1, 1);
            };
        }

        static RotationSpec fromSlice(Axis axis) {
            int directionMultiplier = switch (axis) {
                case X -> -1;
                case Y -> -1;
                case Z -> 1;
            };
            return new RotationSpec(axis, 0, List.of(0), 1, directionMultiplier);
        }

        static RotationSpec fromWide(Face face) {
            return switch (face) {
                case FRONT -> new RotationSpec(Axis.Z, 1, List.of(1, 0), 1, 1);
                case BACK -> new RotationSpec(Axis.Z, -1, List.of(-1, 0), -1, 1);
                case RIGHT -> new RotationSpec(Axis.X, 1, List.of(1, 0), 1, 1);
                case LEFT -> new RotationSpec(Axis.X, -1, List.of(-1, 0), -1, 1);
                case UP -> new RotationSpec(Axis.Y, 1, List.of(1, 0), 1, 1);
                case DOWN -> new RotationSpec(Axis.Y, -1, List.of(-1, 0), -1, 1);
            };
        }

        static RotationSpec fromRotation(Axis axis) {
            return new RotationSpec(axis, null, List.of(-1, 0, 1), 1, 1);
        }
    }
}
