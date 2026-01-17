package nl.tvn.cube.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;
import nl.tvn.cube.util.Vector3i;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CubeRotatorTest {

    static Stream<Arguments> faceRotations() {
        Stream.Builder<Arguments> builder = Stream.builder();
        for (Face face : Face.values()) {
            for (TurnDirection direction : EnumSet.allOf(TurnDirection.class)) {
                builder.add(Arguments.of(face, direction));
            }
        }
        return builder.build();
    }

    @ParameterizedTest
    @MethodSource("faceRotations")
    void rotatesFaceLayerPositionsCorrectly(Face face, TurnDirection direction) {
        CubeModel model = new CubeModel();
        List<Cubie> cubies = model.cubies();
        List<Vector3i> originalPositions = cubies.stream()
            .map(Cubie::position)
            .toList();
        RotationSpec spec = RotationSpec.fromFace(face);
        int angleSign = CubeRotator.angleSign(spec, direction);
        List<Cubie> selected = CubeRotator.selectCubies(cubies, spec);
        CubeRotator.rotateCubies(selected, spec.axis(), angleSign);

        for (int index = 0; index < cubies.size(); index++) {
            Cubie cubie = cubies.get(index);
            Vector3i expected = originalPositions.get(index);
            if (selected.contains(cubie)) {
                expected = rotatePosition(expected, spec.axis(), angleSign);
            }
            assertEquals(expected, cubie.position(), "Cubie position should match expected rotation");
        }
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
}
