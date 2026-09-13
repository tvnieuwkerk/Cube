package nl.tvn.cube.viewmodel;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import nl.tvn.cube.model.*;
import org.junit.jupiter.api.Test;

class BeginnerGuideTest {
    private final BeginnerMethodValidator validator = new BeginnerMethodValidator();

    @Test
    void everyDisplayedSequenceHasItsDocumentedSetupAndCompletesItsStage() {
        for (var step : BeginnerGuide.STEPS) {
            for (var algorithm : step.algorithms()) {
                if (algorithm.id().equals("yellow-twist")) continue; // Complete corner session tested below.
                CubeModel cube = yellowAbove();
                inverse(cube, algorithm.algorithm());
                assertSetup(cube, algorithm.id());
                assertFalse(validator.validate(cube.cubies()).get(step.step()), algorithm.id());
                BeginnerMethodStep prior = BeginnerMethodStep.values()[step.step().ordinal() - 1];
                assertTrue(validator.validate(cube.cubies()).get(prior), algorithm.id());
                run(cube, algorithm.algorithm());
                assertTrue(validator.validate(cube.cubies()).get(step.step()), algorithm.id());
            }
        }
    }

    @Test
    void completeGuidedFixtureSolvesEachStageInOrder() {
        List<String> solution = List.of(algorithm("white-corner"), algorithm("middle-right"),
            algorithm("yellow-line"), algorithm("yellow-edges"), algorithm("yellow-positions"), twistSession());
        CubeModel cube = yellowAbove();
        for (int i = solution.size() - 1; i >= 0; i--) inverse(cube, solution.get(i));
        assertTrue(validator.validate(cube.cubies()).get(BeginnerMethodStep.WHITE_CROSS));
        for (int i = 0; i < solution.size(); i++) {
            run(cube, solution.get(i));
            assertTrue(validator.validate(cube.cubies()).get(BeginnerMethodStep.values()[i + 1]), "Stage " + (i + 2));
        }
        assertTrue(validator.isCubeSolved(cube.cubies()));
    }

    @Test
    void positionedButTwistedCornersFailOnlyFinalStageAndSolveWithCompleteRepetitions() {
        CubeModel cube = yellowAbove();
        inverse(cube, twistSession());
        var status = validator.validate(cube.cubies());
        assertTrue(status.get(BeginnerMethodStep.YELLOW_CORNER_POSITION));
        assertFalse(status.get(BeginnerMethodStep.YELLOW_CORNER_ORIENTATION));
        String twist = algorithm("yellow-twist");
        run(cube, twist);
        assertFalse(validator.isCubeSolved(cube.cubies()));
        run(cube, twist);
        assertEquals(new CubeVector(0, 1, 0), yellowAt(cube, 1, 1).direction(new CubeVector(0, -1, 0)));
        run(cube, "U");
        for (int i = 0; i < 4; i++) run(cube, twist);
        run(cube, "U'");
        assertTrue(validator.isCubeSolved(cube.cubies()));
    }

    @Test
    void edgeInstructionsHandleAll24TopEdgePermutations() {
        Queue<String> queue = new ArrayDeque<>();
        Set<String> seen = new HashSet<>();
        queue.add("");
        while (!queue.isEmpty()) {
            String setup = queue.remove();
            CubeModel cube = yellowAbove();
            if (!setup.isBlank()) run(cube, setup);
            String key = cube.cubies().stream().filter(c -> c.homeY() == -1 && (c.homeX() == 0 ^ c.homeZ() == 0))
                .map(c -> c.coordinate().x() + "," + c.coordinate().z()).reduce("", (a, b) -> a + ";" + b);
            if (!seen.add(key)) continue;
            queue.add(setup + " U");
            queue.add(setup + " " + algorithm("yellow-edges"));
            for (int attempt = 0; attempt < 4 && !validator.isYellowEdgesAligned(cube.cubies()); attempt++) {
                for (int u = 0; u < 4; u++) {
                    if (validator.isYellowEdgesAligned(cube.cubies()) || matchingEdges(cube).size() == 1) break;
                    run(cube, "U");
                }
                if (validator.isYellowEdgesAligned(cube.cubies())) break;
                if (matchingEdges(cube).size() == 1) {
                    for (int y = 0; y < 4 && matchingEdges(cube).getFirst().coordinate().z() != -1; y++)
                        cube.applyMove(new Move(RotationAxis.Y, Set.of(-1, 0, 1), 1));
                    run(cube, algorithm("yellow-edges"));
                    if (!validator.isYellowEdgesAligned(cube.cubies())) run(cube, algorithm("yellow-edges"));
                } else {
                    run(cube, algorithm("yellow-edges"));
                }
            }
            assertTrue(validator.validate(cube.cubies()).get(BeginnerMethodStep.YELLOW_EDGE_ALIGNMENT), setup);
        }
        assertEquals(24, seen.size());
    }

    private static List<CubieModel> matchingEdges(CubeModel cube) {
        return cube.cubies().stream().filter(c -> c.homeY() == -1 && (c.homeX() == 0 ^ c.homeZ() == 0))
            .filter(edge -> {
                CubieModel sideCentre = cube.cubies().stream().filter(c -> c.homeY() == 0
                    && c.homeX() == edge.homeX() && c.homeZ() == edge.homeZ()).findFirst().orElseThrow();
                return edge.coordinate().x() == sideCentre.coordinate().x()
                    && edge.coordinate().z() == sideCentre.coordinate().z();
            }).toList();
    }

    private void assertSetup(CubeModel cube, String id) {
        switch (id) {
            case "white-corner" -> {
                CubieModel corner = cube.cubies().stream().filter(c -> c.homeX() == 1 && c.homeY() == 1 && c.homeZ() == -1).findFirst().orElseThrow();
                assertEquals(new CubeCoordinate(1, 1, 1), corner.coordinate());
            }
            case "middle-right", "middle-left" -> {
                CubieModel edge = at(cube, 0, 1, 1);
                assertEquals(0, edge.homeY());
                assertEquals(new CubeVector(0, 0, 1), edge.direction(new CubeVector(0, 0, -1)));
                assertEquals(new CubeVector(0, 1, 0), edge.direction(new CubeVector(id.equals("middle-right") ? 1 : -1, 0, 0)));
            }
            case "yellow-line" -> assertEquals(Set.of("-1,0", "1,0"), upwardYellowEdges(cube));
            case "yellow-l" -> assertEquals(Set.of("-1,0", "0,-1"), upwardYellowEdges(cube));
            case "yellow-edges" -> {
                assertEquals(4, upwardYellowEdges(cube).size());
                assertEquals(1, alignedEdges(cube));
                assertEquals(1, at(cube, 0, 1, -1).homeZ());
            }
            case "yellow-positions" -> {
                assertEquals(4, alignedEdges(cube));
                assertEquals(-1, yellowAt(cube, 1, 1).homeZ());
                assertEquals(1, yellowAt(cube, 1, 1).homeX());
            }
            default -> fail("Missing setup assertion: " + id);
        }
    }

    private static Set<String> upwardYellowEdges(CubeModel cube) {
        Set<String> result = new HashSet<>();
        for (CubieModel c : cube.cubies()) {
            if (c.homeY() == -1 && (c.homeX() == 0 ^ c.homeZ() == 0)
                    && c.direction(new CubeVector(0, -1, 0)).equals(new CubeVector(0, 1, 0)))
                result.add(c.coordinate().x() + "," + c.coordinate().z());
        }
        return result;
    }

    private static int alignedEdges(CubeModel cube) {
        return (int) cube.cubies().stream().filter(c -> c.homeY() == -1 && (c.homeX() == 0 ^ c.homeZ() == 0)
            && c.coordinate().equals(new CubeCoordinate(c.homeX(), 1, -c.homeZ()))).count();
    }

    private static CubieModel at(CubeModel cube, int x, int y, int z) {
        return cube.cubies().stream().filter(c -> c.coordinate().equals(new CubeCoordinate(x, y, z))).findFirst().orElseThrow();
    }

    private static CubieModel yellowAt(CubeModel cube, int x, int z) {
        CubieModel corner = at(cube, x, 1, z);
        assertEquals(-1, corner.homeY());
        return corner;
    }

    private static String twistSession() {
        String twist = algorithm("yellow-twist") + " ";
        return twist.repeat(2) + "U " + twist.repeat(4) + "U'";
    }

    private static String algorithm(String id) {
        return BeginnerGuide.STEPS.stream().flatMap(s -> s.algorithms().stream()).filter(a -> a.id().equals(id)).findFirst().orElseThrow().algorithm();
    }

    private static CubeModel yellowAbove() {
        CubeModel cube = new CubeModel();
        cube.applyMove(new Move(RotationAxis.X, Set.of(-1, 0, 1), 2));
        return cube;
    }

    private static void run(CubeModel cube, String notation) {
        var parsed = AlgorithmParser.parse(notation);
        assertTrue(parsed.isValid(), parsed.errorMessage());
        parsed.moves().forEach(cube::applyMove);
    }

    private static void inverse(CubeModel cube, String notation) {
        var parsed = AlgorithmParser.parse(notation);
        assertTrue(parsed.isValid(), parsed.errorMessage());
        for (int i = parsed.moves().size() - 1; i >= 0; i--) {
            Move move = parsed.moves().get(i);
            cube.applyMove(new Move(move.axis(), move.layers(), -move.quarterTurns()));
        }
    }
}
