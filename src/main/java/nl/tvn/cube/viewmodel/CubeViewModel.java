package nl.tvn.cube.viewmodel;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import nl.tvn.cube.model.Axis;
import nl.tvn.cube.model.CubeColor;
import nl.tvn.cube.model.CubeRotator;
import nl.tvn.cube.model.CubeModel;
import nl.tvn.cube.model.Cubie;
import nl.tvn.cube.model.Face;
import nl.tvn.cube.model.Move;
import nl.tvn.cube.model.RotationSpec;
import nl.tvn.cube.model.TurnDirection;
import nl.tvn.cube.util.Vector3i;

public final class CubeViewModel {
    private static final double CUBIE_SIZE = 40;
    private static final double CUBIE_GAP = 6;

    private final CubeModel cubeModel;
    private final List<CubieNode> cubieNodes;
    private Group rootGroup;

    public CubeViewModel() {
        this.cubeModel = new CubeModel();
        this.cubieNodes = new ArrayList<>();
    }

    public Group buildScene() {
        rootGroup = new Group();
        rootGroup.getTransforms().add(new Scale(1, -1, 1));
        for (Cubie cubie : cubeModel.cubies()) {
            Group cubieGroup = new Group();
            Translate translate = createTranslate(cubie.position());
            Affine orientation = new Affine();
            updateOrientation(orientation, cubie);
            cubieGroup.getTransforms().addAll(orientation, translate);
            cubieGroup.getChildren().add(createCubieBox(cubie));
            cubieNodes.add(new CubieNode(cubie, cubieGroup, translate, orientation));
            rootGroup.getChildren().add(cubieGroup);
        }
        rootGroup.getChildren().addAll(createLighting());
        return rootGroup;
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
        int angleSign = CubeRotator.angleSign(spec, direction);
        List<CubieNode> nodes = selectCubies(spec);
        applyLayerRotation(nodes, spec.axis(), angleSign);
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

    private void applyLayerRotation(List<CubieNode> nodes, Axis axis, int angleSign) {
        CubeRotator.rotateCubies(nodes.stream().map(CubieNode::cubie).toList(), axis, angleSign);
        for (CubieNode node : nodes) {
            Vector3i newPosition = node.cubie().position();
            updateTranslate(node.translate(), newPosition);
            updateOrientation(node.orientation(), node.cubie());
        }
    }

    private Translate createTranslate(Vector3i position) {
        double spacing = CUBIE_SIZE + CUBIE_GAP;
        return new Translate(position.x() * spacing, position.y() * spacing, position.z() * spacing);
    }

    private void updateTranslate(Translate translate, Vector3i position) {
        double spacing = CUBIE_SIZE + CUBIE_GAP;
        translate.setX(position.x() * spacing);
        translate.setY(position.y() * spacing);
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

    private void updateOrientation(Affine affine, Cubie cubie) {
        var xAxis = cubie.orientation().xAxis();
        var yAxis = cubie.orientation().yAxis();
        var zAxis = cubie.orientation().zAxis();
        affine.setToTransform(
            xAxis.x(), yAxis.x(), zAxis.x(), 0,
            xAxis.y(), yAxis.y(), zAxis.y(), 0,
            xAxis.z(), yAxis.z(), zAxis.z(), 0
        );
    }

}
