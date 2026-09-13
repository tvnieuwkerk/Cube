package nl.tvn.cube.viewmodel;

import java.util.List;
import nl.tvn.cube.model.BeginnerMethodStep;

/** The instructions and exact sequences displayed by the manual solving guide. */
public final class BeginnerGuide {
    private BeginnerGuide() { }

    public record AlgorithmDefinition(String id, String label, String algorithm) { }
    public record StepDefinition(BeginnerMethodStep step, String title, String goal,
                                 List<String> instructions, List<AlgorithmDefinition> algorithms) { }

    public static final List<StepDefinition> STEPS = List.of(
        new StepDefinition(BeginnerMethodStep.WHITE_CROSS, "1. White Cross", "Solve the four white edges.",
            List.of("Start with the white centre on top. Use face turns to bring each white edge to the top with white facing up.",
                "Match each edge's other colour with its side centre.",
                "For assistance, click Solve one edge. It displays and immediately plays the shortest sequence of face turns and inverses that solves at least one more white edge.",
                "Already-solved white edges may move temporarily but are restored by the end. Other white edges may also be solved incidentally; other pieces are not preserved.",
                "Half turns are played as two quarter turns. Repeat until the cross is complete; the button then becomes disabled."), List.of()),
        new StepDefinition(BeginnerMethodStep.WHITE_CORNERS, "2. White Corners", "Finish the white layer.",
            List.of("After the white cross, press Ctrl+X once to turn the whole cube upside down: white below, yellow above. Keep this orientation for stages 2–7.",
                "Choose a white corner in the top layer. Turn the whole cube with Y so its destination is lower-front-right; use U to place the corner directly above it.",
                "Run the insertion sequence repeatedly, checking after each complete sequence, until that corner is solved (at most five repetitions).",
                "If a white corner is trapped below incorrectly, put that slot at lower-front-right and run once to lift it out. Then set it up above its destination.",
                "Repeat for all four corners. Changing the front with Y keeps white below."),
            List.of(new AlgorithmDefinition("white-corner", "Insert at lower-front-right", "R U R' U'"))),
        new StepDefinition(BeginnerMethodStep.MIDDLE_LAYER_EDGES, "3. Middle Layer Edges", "Solve the four edges without yellow in the middle layer.",
            List.of("Choose a top edge without yellow. Turn U until its side sticker matches a centre, then use Y to make that centre the front.",
                "Its top sticker identifies the destination: use the right insertion if it matches the right centre, otherwise the left insertion.",
                "Run the chosen insertion once, then select the next edge. If an edge is trapped or flipped in the middle, place that slot at front-right (or front-left) and run the corresponding insertion to eject it; set it up again."),
            List.of(new AlgorithmDefinition("middle-right", "Insert top-front edge to the right", "U R U' R' U' F' U F"),
                new AlgorithmDefinition("middle-left", "Insert top-front edge to the left", "U' L' U L U F U' F'"))),
        new StepDefinition(BeginnerMethodStep.YELLOW_CROSS, "4. Yellow Cross", "Make a yellow cross on top; ignore side alignment.",
            List.of("Look only at yellow edge stickers facing up. Use Y to arrange the pattern without disturbing the solved layers.",
                "Line: hold its two yellow edges at left and right, then run the line sequence once.",
                "L: hold its two yellow edges at back and left, then run the L sequence once.",
                "Dot: run the line sequence once from any front, then inspect and set up the resulting L. Stop when all four yellow edges face up."),
            List.of(new AlgorithmDefinition("yellow-line", "Line at left/right", "F R U R' U' F'"),
                new AlgorithmDefinition("yellow-l", "L at back/left", "F U R U' R' F'"))),
        new StepDefinition(BeginnerMethodStep.YELLOW_EDGE_ALIGNMENT, "5. Align Yellow Edges", "Match all four top edges with their side centres.",
            List.of("Try the four U positions. If all edges match, stop. Otherwise find a position with exactly one matching edge.",
                "Use Y to put that matching edge at the back. Run once; if needed run a second time from the same front. The back edge stays fixed while the other three cycle.",
                "If no U position has exactly one match, run once from any front and retry the U positions. Stop when all four side colours match."),
            List.of(new AlgorithmDefinition("yellow-edges", "Keep the matching edge at the back", "R U' R U R U R U' R' U' R2"))),
        new StepDefinition(BeginnerMethodStep.YELLOW_CORNER_POSITION, "6. Position Yellow Corners", "Put each yellow corner between its matching centres; ignore twists.",
            List.of("Find a corner whose three colours belong between the surrounding centres, even if yellow does not face up.",
                "Use Y to place that correct corner at upper-front-right. Run once or twice from the same front until all corners occupy their correct locations.",
                "If no corner is correctly positioned, run once from any front, then locate the correct corner. Do not use U during this stage: the edges are already aligned."),
            List.of(new AlgorithmDefinition("yellow-positions", "Keep the correct corner at upper-front-right", "U R U' L' U R' U' L"))),
        new StepDefinition(BeginnerMethodStep.YELLOW_CORNER_ORIENTATION, "7. Orient Yellow Corners", "Finish every sticker and align the top layer.",
            List.of("Use Y once to place an unsolved corner at upper-front-right. From now on keep the same front and keep yellow above.",
                "Run the sequence two or four times until that corner's yellow sticker faces up. Always finish all four moves of each repetition.",
                "Use only U to bring the next unsolved corner to upper-front-right; repeat the same process. Do not rotate the whole cube between corners.",
                "The lower layers and earlier indicators may temporarily become unsolved. Continue through all corners; do not reset or change method midway.",
                "When all yellow corners face up, turn U to align the side colours. All seven indicators should now be green."),
            List.of(new AlgorithmDefinition("yellow-twist", "Twist the upper-front-right corner", "R' D' R D")))
    );
}
