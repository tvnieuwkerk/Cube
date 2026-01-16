package nl.tvn.cube.viewmodel;

import java.util.ArrayList;
import java.util.List;
import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Material;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.utils.geometry.Box;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.utils.universe.PlatformGeometry;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3f;
import nl.tvn.cube.model.Axis;
import nl.tvn.cube.model.CubeModel;
import nl.tvn.cube.model.Cubie;
import nl.tvn.cube.model.Face;
import nl.tvn.cube.model.Move;
import nl.tvn.cube.model.TurnDirection;
import nl.tvn.cube.util.Vector3i;

public final class CubeViewModel {
    private static final float CUBIE_SIZE = 0.3f;
    private static final float CUBIE_GAP = 0.05f;

    private final CubeModel cubeModel;
    private final List<CubieNode> cubieNodes;

    public CubeViewModel() {
        this.cubeModel = new CubeModel();
        this.cubieNodes = new ArrayList<>();
    }

    public BranchGroup buildScene() {
        BranchGroup root = new BranchGroup();
        for (Cubie cubie : cubeModel.cubies()) {
            TransformGroup transformGroup = createCubieTransform(cubie.position());
            Box box = createCubieBox(cubie);
            transformGroup.addChild(box);
            transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
            transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            cubieNodes.add(new CubieNode(cubie, transformGroup));
            root.addChild(transformGroup);
        }
        root.addChild(createLighting());
        root.compile();
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
        TransformGroup group = node.transformGroup();
        Transform3D current = new Transform3D();
        group.getTransform(current);

        Transform3D rotation = new Transform3D();
        rotation.setRotation(new AxisAngle4f(axisVector(axis), (float) (Math.PI / 2) * angleSign));

        Transform3D updated = new Transform3D();
        updated.mul(rotation, current);
        group.setTransform(updated);

        Vector3i newPosition = rotatePosition(node.cubie().position(), axis, angleSign);
        node.cubie().setPosition(newPosition);
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

    private TransformGroup createCubieTransform(Vector3i position) {
        Transform3D transform = new Transform3D();
        float spacing = CUBIE_SIZE + CUBIE_GAP;
        transform.setTranslation(new Vector3f(position.x() * spacing, position.y() * spacing, position.z() * spacing));
        TransformGroup group = new TransformGroup(transform);
        group.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        group.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        return group;
    }

    private Box createCubieBox(Cubie cubie) {
        Box box = new Box(CUBIE_SIZE / 2, CUBIE_SIZE / 2, CUBIE_SIZE / 2, Box.GENERATE_NORMALS, new Appearance());
        setFaceAppearance(box.getShape(Box.FRONT), cubie.faceColors().get(Face.FRONT).color());
        setFaceAppearance(box.getShape(Box.BACK), cubie.faceColors().get(Face.BACK).color());
        setFaceAppearance(box.getShape(Box.RIGHT), cubie.faceColors().get(Face.RIGHT).color());
        setFaceAppearance(box.getShape(Box.LEFT), cubie.faceColors().get(Face.LEFT).color());
        setFaceAppearance(box.getShape(Box.TOP), cubie.faceColors().get(Face.UP).color());
        setFaceAppearance(box.getShape(Box.BOTTOM), cubie.faceColors().get(Face.DOWN).color());
        return box;
    }

    private void setFaceAppearance(Shape3D shape, Color3f color) {
        Appearance appearance = new Appearance();
        Material material = new Material();
        material.setDiffuseColor(color);
        material.setAmbientColor(color);
        material.setSpecularColor(new Color3f(0.2f, 0.2f, 0.2f));
        appearance.setMaterial(material);
        shape.setAppearance(appearance);
    }

    private PlatformGeometry createLighting() {
        BoundingSphere bounds = new BoundingSphere();
        javax.media.j3d.AmbientLight ambient = new javax.media.j3d.AmbientLight(new Color3f(0.4f, 0.4f, 0.4f));
        ambient.setInfluencingBounds(bounds);
        javax.media.j3d.DirectionalLight light = new javax.media.j3d.DirectionalLight(new Color3f(0.9f, 0.9f, 0.9f),
            new Vector3f(-1f, -1f, -1f));
        light.setInfluencingBounds(bounds);
        PlatformGeometry geometry = new PlatformGeometry();
        geometry.addChild(ambient);
        geometry.addChild(light);
        return geometry;
    }

    private Vector3f axisVector(Axis axis) {
        return switch (axis) {
            case X -> new Vector3f(1f, 0f, 0f);
            case Y -> new Vector3f(0f, 1f, 0f);
            case Z -> new Vector3f(0f, 0f, 1f);
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
