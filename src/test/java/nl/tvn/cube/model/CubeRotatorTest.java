package nl.tvn.cube.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
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
        RotationSpec spec = RotationSpec.fromFace(face);
        int angleSign = CubeRotator.angleSign(spec, direction);
        List<Cubie> selected = CubeRotator.selectCubies(cubies, spec);

        Map<Cubie, Integer> labels = labelFaceCubies(cubies, face);
        int[][] expectedLabels = expectedFaceLabels(direction);
        CubeRotator.rotateCubies(selected, spec.axis(), angleSign);

        Vector3i[][] grid = faceGrid(face);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Vector3i position = grid[row][col];
                Cubie cubie = findCubieAt(cubies, position);
                int expectedLabel = expectedLabels[row][col];
                assertEquals(expectedLabel, labels.get(cubie), "Cubie label mismatch at row " + row + " col " + col);
            }
        }
    }

    private Map<Cubie, Integer> labelFaceCubies(List<Cubie> cubies, Face face) {
        Map<Cubie, Integer> labels = new IdentityHashMap<>();
        Vector3i[][] grid = faceGrid(face);
        int label = 1;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Cubie cubie = findCubieAt(cubies, grid[row][col]);
                labels.put(cubie, label++);
            }
        }
        return labels;
    }

    private int[][] expectedFaceLabels(TurnDirection direction) {
        int[][] clockwise = {
            {7, 4, 1},
            {8, 5, 2},
            {9, 6, 3}
        };
        int[][] counterClockwise = {
            {3, 6, 9},
            {2, 5, 8},
            {1, 4, 7}
        };
        return direction == TurnDirection.CLOCKWISE ? clockwise : counterClockwise;
    }

    private Vector3i[][] faceGrid(Face face) {
        return switch (face) {
            case FRONT -> buildGrid(
                new Vector3i(-1, 1, 1), new Vector3i(0, 1, 1), new Vector3i(1, 1, 1),
                new Vector3i(-1, 0, 1), new Vector3i(0, 0, 1), new Vector3i(1, 0, 1),
                new Vector3i(-1, -1, 1), new Vector3i(0, -1, 1), new Vector3i(1, -1, 1)
            );
            case BACK -> buildGrid(
                new Vector3i(1, 1, -1), new Vector3i(0, 1, -1), new Vector3i(-1, 1, -1),
                new Vector3i(1, 0, -1), new Vector3i(0, 0, -1), new Vector3i(-1, 0, -1),
                new Vector3i(1, -1, -1), new Vector3i(0, -1, -1), new Vector3i(-1, -1, -1)
            );
            case RIGHT -> buildGrid(
                new Vector3i(1, 1, 1), new Vector3i(1, 1, 0), new Vector3i(1, 1, -1),
                new Vector3i(1, 0, 1), new Vector3i(1, 0, 0), new Vector3i(1, 0, -1),
                new Vector3i(1, -1, 1), new Vector3i(1, -1, 0), new Vector3i(1, -1, -1)
            );
            case LEFT -> buildGrid(
                new Vector3i(-1, 1, -1), new Vector3i(-1, 1, 0), new Vector3i(-1, 1, 1),
                new Vector3i(-1, 0, -1), new Vector3i(-1, 0, 0), new Vector3i(-1, 0, 1),
                new Vector3i(-1, -1, -1), new Vector3i(-1, -1, 0), new Vector3i(-1, -1, 1)
            );
            case UP -> buildGrid(
                new Vector3i(-1, 1, -1), new Vector3i(0, 1, -1), new Vector3i(1, 1, -1),
                new Vector3i(-1, 1, 0), new Vector3i(0, 1, 0), new Vector3i(1, 1, 0),
                new Vector3i(-1, 1, 1), new Vector3i(0, 1, 1), new Vector3i(1, 1, 1)
            );
            case DOWN -> buildGrid(
                new Vector3i(-1, -1, 1), new Vector3i(0, -1, 1), new Vector3i(1, -1, 1),
                new Vector3i(-1, -1, 0), new Vector3i(0, -1, 0), new Vector3i(1, -1, 0),
                new Vector3i(-1, -1, -1), new Vector3i(0, -1, -1), new Vector3i(1, -1, -1)
            );
        };
    }

    private Vector3i[][] buildGrid(
        Vector3i v00, Vector3i v01, Vector3i v02,
        Vector3i v10, Vector3i v11, Vector3i v12,
        Vector3i v20, Vector3i v21, Vector3i v22
    ) {
        return new Vector3i[][] {
            {v00, v01, v02},
            {v10, v11, v12},
            {v20, v21, v22}
        };
    }

    private Cubie findCubieAt(List<Cubie> cubies, Vector3i position) {
        return cubies.stream()
            .filter(cubie -> cubie.position().equals(position))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Missing cubie at position " + position));
    }
}
