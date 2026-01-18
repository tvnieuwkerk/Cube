package nl.tvn.cube.view;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Pos;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import nl.tvn.cube.model.BeginnerMethodStep;
import nl.tvn.cube.model.Move;
import nl.tvn.cube.model.RotationAxis;
import nl.tvn.cube.viewmodel.CubeViewModel;

public final class HelpWindow {
    private static final Duration TURN_DURATION = Duration.seconds(0.35);
    private static final Duration PAUSE_DURATION = Duration.seconds(0.2);
    private static final double CARD_SPACING = 12;
    private static final double CARD_PADDING = 10;
    private static final double CUBE_SCENE_SIZE = 240;
    private static final double CUBE_SCALE = 0.525;
    private static final double CAMERA_DISTANCE = 420;
    private static final double WINDOW_GAP = 12;
    private final Stage owner;
    private final Stage stage;
    private final CubeViewModel viewModel;
    private final Consumer<String> algorithmRunner;

    public HelpWindow(Stage owner, CubeViewModel viewModel, Consumer<String> algorithmRunner) {
        this.owner = owner;
        this.viewModel = viewModel;
        this.algorithmRunner = algorithmRunner;
        this.stage = new Stage();
        stage.initOwner(owner);
        stage.setTitle("Cube Help");
        stage.setScene(new Scene(buildContent()));
    }

    public void show() {
        if (!stage.isShowing()) {
            stage.show();
        }
        stage.sizeToScene();
        positionToRightOfOwner();
        stage.toFront();
    }

    private void positionToRightOfOwner() {
        Rectangle2D bounds = screenBoundsForOwner();
        double targetX = owner.getX() + owner.getWidth() + WINDOW_GAP;
        double targetY = owner.getY();
        double clampedX = Math.min(targetX, bounds.getMaxX() - stage.getWidth());
        double clampedY = Math.min(Math.max(bounds.getMinY(), targetY), bounds.getMaxY() - stage.getHeight());
        stage.setX(Math.max(bounds.getMinX(), clampedX));
        stage.setY(clampedY);
    }

    private Rectangle2D screenBoundsForOwner() {
        List<Screen> screens = Screen.getScreensForRectangle(
            owner.getX(),
            owner.getY(),
            owner.getWidth(),
            owner.getHeight()
        );
        if (!screens.isEmpty()) {
            return screens.getFirst().getVisualBounds();
        }
        return Screen.getPrimary().getVisualBounds();
    }

    private StackPane buildContent() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab shortcutsTab = new Tab("Shortcuts", buildHelpContent());
        Tab beginnerTab = new Tab("Beginner Method", buildBeginnerContent());
        Tab aboutTab = new Tab("About", buildAboutContent());
        tabPane.getTabs().addAll(shortcutsTab, beginnerTab, aboutTab);

        return new StackPane(tabPane);
    }

    private StackPane buildHelpContent() {
        GridPane grid = new GridPane();
        grid.setPadding(Insets.EMPTY);
        grid.setHgap(CARD_SPACING);
        grid.setVgap(CARD_SPACING);

        List<TurnDefinition> turns = List.of(
            new TurnDefinition("U (Up) - U / Shift+U", faceMove(RotationAxis.Y, 1, 1)),
            new TurnDefinition("D (Down) - D / Shift+D", faceMove(RotationAxis.Y, -1, -1)),
            new TurnDefinition("R (Right) - R / Shift+R", faceMove(RotationAxis.X, 1, 1)),
            new TurnDefinition("L (Left) - L / Shift+L", faceMove(RotationAxis.X, -1, -1)),
            new TurnDefinition("F (Front) - F / Shift+F", faceMove(RotationAxis.Z, 1, 1)),
            new TurnDefinition("B (Back) - B / Shift+B", faceMove(RotationAxis.Z, -1, -1)),
            new TurnDefinition("M (Middle) - M / Shift+M", faceMove(RotationAxis.X, 0, 1)),
            new TurnDefinition("E (Equator) - E / Shift+E", faceMove(RotationAxis.Y, 0, 1)),
            new TurnDefinition("S (Standing) - S / Shift+S", faceMove(RotationAxis.Z, 0, 1))
        );

        for (int i = 0; i < turns.size(); i++) {
            TurnDefinition definition = turns.get(i);
            VBox card = buildTurnCard(definition);
            int column = i % 3;
            int row = i / 3;
            grid.add(card, column, row);
        }
        Label cameraKeys = new Label("Camera: Left/Right = Yaw | Up/Down = Pitch | PgUp/PgDn = Roll");
        cameraKeys.setTextFill(Color.LIGHTGRAY);
        cameraKeys.setStyle("-fx-font-size: 12px;");
        cameraKeys.setWrapText(true);
        Label mouseHints = new Label("Mouse: Click face to select | Click again and drag to rotate cube | "
            + "Click-drag a cubie to turn its row/column | Scroll to zoom");
        mouseHints.setTextFill(Color.LIGHTGRAY);
        mouseHints.setStyle("-fx-font-size: 12px;");
        mouseHints.setWrapText(true);

        VBox content = new VBox(CARD_SPACING, grid, cameraKeys, mouseHints);
        content.setPadding(new Insets(CARD_SPACING));

        StackPane helpContainer = new StackPane(content);
        helpContainer.setBackground(new Background(new BackgroundFill(Color.web("#1f1f1f"), CornerRadii.EMPTY, Insets.EMPTY)));
        return helpContainer;
    }

    private StackPane buildAboutContent() {
        Label description = new Label("Cube is an interactive Rubik's Cube simulator that lets you view, rotate, and"
            + " manipulate the cube using keyboard shortcuts and algorithm notation.");
        description.setTextFill(Color.LIGHTGRAY);
        description.setStyle("-fx-font-size: 12px;");
        description.setWrapText(true);

        VBox content = new VBox(12, description);
        content.setPadding(new Insets(CARD_SPACING));

        StackPane aboutContainer = new StackPane(content);
        aboutContainer.setBackground(new Background(new BackgroundFill(Color.web("#1f1f1f"), CornerRadii.EMPTY, Insets.EMPTY)));
        return aboutContainer;
    }

    private StackPane buildBeginnerContent() {
        VBox content = new VBox(CARD_SPACING);
        content.setPadding(new Insets(CARD_SPACING));

        content.getChildren().addAll(
            buildIntroCard(),
            buildStepCard(
                "1. White Cross (Top Face)",
                viewModel.beginnerStepProperty(BeginnerMethodStep.WHITE_CROSS),
                "Goal:",
                List.of(
                    "Make a cross on the white face",
                    "Side colors of the cross must match the center pieces"
                ),
                "Notes:",
                List.of(
                    "Mostly intuitive",
                    "No long algorithms",
                    "Focus on edge pieces only"
                ),
                null,
                List.of()
            ),
            buildStepCard(
                "2. White Corners (Finish First Layer)",
                viewModel.beginnerStepProperty(BeginnerMethodStep.WHITE_CORNERS),
                "Goal:",
                List.of(
                    "Place the four white corners correctly",
                    "First layer becomes fully solved"
                ),
                null,
                List.of(),
                "Typical Algorithms:",
                List.of(
                    new AlgorithmDefinition("Insert corner on the right", "R U R'"),
                    new AlgorithmDefinition("Insert corner on the left", "L' U' L")
                )
            ),
            buildStepCard(
                "3. Middle Layer Edges",
                viewModel.beginnerStepProperty(BeginnerMethodStep.MIDDLE_LAYER_EDGES),
                "Goal:",
                List.of(
                    "Solve the four edge pieces in the middle layer",
                    "No yellow on these edges"
                ),
                null,
                List.of(),
                "Algorithms:",
                List.of(
                    new AlgorithmDefinition("Edge goes to the right", "U R U' R' U' F' U F"),
                    new AlgorithmDefinition("Edge goes to the left", "U' L' U L U F U' F'")
                )
            ),
            buildStepCard(
                "4. Yellow Cross (Last Layer – Part 1)",
                viewModel.beginnerStepProperty(BeginnerMethodStep.YELLOW_CROSS),
                "Goal:",
                List.of(
                    "Form a yellow cross on the bottom face",
                    "Ignore side colors for now"
                ),
                "Notes:",
                List.of(
                    "Possible cases: Dot, L-shape, Line (repeat the algorithm until you get the cross)"
                ),
                "Algorithm:",
                List.of(new AlgorithmDefinition(null, "F R U R' U' F'"))
            ),
            buildStepCard(
                "5. Orient Yellow Edges (Last Layer – Part 2)",
                viewModel.beginnerStepProperty(BeginnerMethodStep.YELLOW_EDGE_ALIGNMENT),
                "Goal:",
                List.of(
                    "Match the yellow cross edges with side centers"
                ),
                null,
                List.of(),
                "Algorithm:",
                List.of(new AlgorithmDefinition(null, "R U R' U R U2 R' U"))
            ),
            buildStepCard(
                "6. Position Yellow Corners (Last Layer – Part 3)",
                viewModel.beginnerStepProperty(BeginnerMethodStep.YELLOW_CORNER_POSITION),
                "Goal:",
                List.of(
                    "Put yellow corners in the correct location",
                    "Orientation does not matter yet"
                ),
                "Notes:",
                List.of(
                    "Repeat until all corners are in the right place."
                ),
                "Algorithm:",
                List.of(new AlgorithmDefinition(null, "U R U' L' U R' U' L"))
            ),
            buildStepCard(
                "7. Orient Yellow Corners (Finish the Cube)",
                viewModel.beginnerStepProperty(BeginnerMethodStep.YELLOW_CORNER_ORIENTATION),
                "Goal:",
                List.of(
                    "Twist yellow corners so the cube is fully solved"
                ),
                "How to use it:",
                List.of(
                    "Keep one unsolved yellow corner at the front-right",
                    "Repeat the algorithm until it is correct",
                    "Turn only the U face to bring the next corner into position",
                    "Repeat until solved"
                ),
                "Algorithm (Right-hand version):",
                List.of(new AlgorithmDefinition(null, "R' D' R D"))
            ),
            buildTipsCard(),
            buildSummaryCard()
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: #1f1f1f; -fx-background-color: #1f1f1f;");

        StackPane container = new StackPane(scrollPane);
        container.setBackground(new Background(new BackgroundFill(Color.web("#1f1f1f"), CornerRadii.EMPTY, Insets.EMPTY)));
        return container;
    }

    private VBox buildIntroCard() {
        Label title = buildCardTitle("Beginner Method (Layer by Layer)");
        Label subtitle = buildBodyText("White on top and yellow on bottom. Follow each step in order.");
        VBox card = new VBox(8, title, subtitle);
        return wrapCard(card);
    }

    private VBox buildStepCard(
        String titleText,
        ReadOnlyBooleanProperty status,
        String goalHeader,
        List<String> goalItems,
        String notesHeader,
        List<String> notesItems,
        String algorithmHeader,
        List<AlgorithmDefinition> algorithms
    ) {
        VBox content = new VBox(8);
        content.getChildren().add(buildStepHeader(titleText, status));
        content.getChildren().add(buildSection(goalHeader, goalItems));
        if (!notesItems.isEmpty()) {
            content.getChildren().add(buildSection(notesHeader, notesItems));
        }
        if (!algorithms.isEmpty()) {
            VBox algorithmSection = new VBox(8);
            Label header = buildSectionHeader(algorithmHeader);
            algorithmSection.getChildren().add(header);
            algorithms.forEach(definition -> algorithmSection.getChildren().add(buildAlgorithmRow(definition)));
            content.getChildren().add(algorithmSection);
        }
        return wrapCard(content);
    }

    private VBox buildTipsCard() {
        Label title = buildCardTitle("Key Beginner Tips");
        VBox tips = buildSection(
            null,
            List.of(
                "Do not panic if the cube looks scrambled during steps 6–7",
                "Always keep the same face on top",
                "Centers never move—use them as references",
                "Accuracy matters more than speed"
            )
        );
        VBox card = new VBox(8, title, tips);
        return wrapCard(card);
    }

    private VBox buildSummaryCard() {
        Label title = buildCardTitle("Summary Table");
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(6);

        Label stepHeader = buildSectionHeader("Step");
        Label solveHeader = buildSectionHeader("What You Solve");
        grid.add(stepHeader, 0, 0);
        grid.add(solveHeader, 1, 0);

        addSummaryRow(grid, 1, "1", "White cross");
        addSummaryRow(grid, 2, "2", "White corners");
        addSummaryRow(grid, 3, "3", "Middle layer");
        addSummaryRow(grid, 4, "4", "Yellow cross");
        addSummaryRow(grid, 5, "5", "Yellow edge alignment");
        addSummaryRow(grid, 6, "6", "Yellow corner positioning");
        addSummaryRow(grid, 7, "7", "Yellow corner orientation");

        VBox card = new VBox(8, title, grid);
        return wrapCard(card);
    }

    private void addSummaryRow(GridPane grid, int row, String step, String description) {
        Label stepLabel = buildBodyText(step);
        Label descriptionLabel = buildBodyText(description);
        grid.add(stepLabel, 0, row);
        grid.add(descriptionLabel, 1, row);
    }

    private VBox buildSection(String headerText, List<String> items) {
        VBox section = new VBox(4);
        if (headerText != null && !headerText.isBlank()) {
            section.getChildren().add(buildSectionHeader(headerText));
        }
        for (String item : items) {
            Label label = buildBodyText("• " + item);
            section.getChildren().add(label);
        }
        return section;
    }

    private HBox buildAlgorithmRow(AlgorithmDefinition definition) {
        Label label = buildBodyText(definition.label() == null ? "Algorithm" : definition.label());
        label.setStyle(label.getStyle() + " -fx-font-size: 11px;");

        Label algorithm = new Label(definition.algorithm());
        algorithm.setStyle("-fx-font-size: 12px; -fx-font-family: 'Consolas'; -fx-text-fill: #f6f6f6;");
        algorithm.setWrapText(true);

        VBox textGroup = new VBox(2, label, algorithm);
        Button execute = new Button("Execute");
        execute.setOnAction(event -> algorithmRunner.accept(definition.algorithm()));
        execute.setFocusTraversable(false);
        execute.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white; -fx-background-radius: 6;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(10, textGroup, spacer, execute);
        row.setPadding(new Insets(8));
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setBackground(new Background(new BackgroundFill(Color.web("#2b2b2b"), new CornerRadii(6), Insets.EMPTY)));
        return row;
    }

    private Label buildCardTitle(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        label.setWrapText(true);
        return label;
    }

    private Label buildSectionHeader(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web("#d6d6d6"));
        label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        label.setWrapText(true);
        return label;
    }

    private Label buildBodyText(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.LIGHTGRAY);
        label.setStyle("-fx-font-size: 12px;");
        label.setWrapText(true);
        return label;
    }

    private VBox wrapCard(VBox content) {
        VBox card = new VBox();
        card.getChildren().add(content);
        card.setPadding(new Insets(CARD_PADDING));
        card.setBackground(new Background(new BackgroundFill(Color.web("#262626"), new CornerRadii(8), Insets.EMPTY)));
        return card;
    }

    private HBox buildStepHeader(String text, ReadOnlyBooleanProperty status) {
        Label label = buildCardTitle(text);
        Label indicator = buildStatusIndicator(status);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(8, label, spacer, indicator);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private Label buildStatusIndicator(ReadOnlyBooleanProperty status) {
        Label indicator = new Label();
        indicator.setMinSize(18, 18);
        indicator.setPrefSize(18, 18);
        indicator.setMaxSize(18, 18);
        indicator.setAlignment(Pos.CENTER);
        indicator.textProperty().bind(Bindings.when(status).then("✓").otherwise("✕"));
        indicator.styleProperty().bind(Bindings.when(status)
            .then("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-size: 12px;"
                + " -fx-font-weight: bold; -fx-background-radius: 9;")
            .otherwise("-fx-background-color: #6b2f2f; -fx-text-fill: #f5f5f5; -fx-font-size: 12px;"
                + " -fx-font-weight: bold; -fx-background-radius: 9;"));
        return indicator;
    }

    private VBox buildTurnCard(TurnDefinition definition) {
        Label label = new Label(definition.label());
        label.setTextFill(Color.WHITE);
        label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        CubeViewModel viewModel = new CubeViewModel();
        StackPane preview = buildCubePreview(viewModel);
        preview.setPadding(new Insets(6));
        preview.setBackground(new Background(new BackgroundFill(Color.web("#2b2b2b"), new CornerRadii(6), Insets.EMPTY)));

        Move inverse = new Move(definition.move().axis(), definition.move().layers(), -definition.move().quarterTurns());
        Animation animation = buildAnimation(viewModel, definition.move(), inverse);
        animation.play();

        VBox card = new VBox(6, label, preview);
        card.setPadding(new Insets(CARD_PADDING));
        card.setBackground(new Background(new BackgroundFill(Color.web("#262626"), new CornerRadii(8), Insets.EMPTY)));
        return card;
    }

    private StackPane buildCubePreview(CubeViewModel viewModel) {
        Group root3d = new Group();
        Group cubeGroup = viewModel.cubeGroup();
        root3d.getChildren().add(cubeGroup);

        Box floor = new Box(300, 2, 300);
        floor.setTranslateY(100);
        floor.setMaterial(new PhongMaterial(Color.DARKGRAY));

        AmbientLight ambientLight = new AmbientLight(Color.color(0.7, 0.7, 0.7));
        PointLight keyLight = new PointLight(Color.WHITE);
        keyLight.getTransforms().add(new Translate(-200, -200, -200));

        root3d.getChildren().addAll(floor, ambientLight, keyLight);

        SubScene subScene = new SubScene(root3d, CUBE_SCENE_SIZE, CUBE_SCENE_SIZE, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.web("#1d1d1d"));
        subScene.setCamera(createCamera());

        cubeGroup.setScaleX(CUBE_SCALE);
        cubeGroup.setScaleY(CUBE_SCALE);
        cubeGroup.setScaleZ(CUBE_SCALE);

        return new StackPane(subScene);
    }

    private Animation buildAnimation(CubeViewModel viewModel, Move move, Move inverse) {
        PauseTransition applyMove = new PauseTransition(Duration.ZERO);
        applyMove.setOnFinished(event -> viewModel.applyMove(move));
        PauseTransition waitMove = new PauseTransition(TURN_DURATION.add(PAUSE_DURATION));

        PauseTransition applyInverse = new PauseTransition(Duration.ZERO);
        applyInverse.setOnFinished(event -> viewModel.applyMove(inverse));
        PauseTransition waitInverse = new PauseTransition(TURN_DURATION.add(PAUSE_DURATION));

        SequentialTransition sequence = new SequentialTransition(applyMove, waitMove, applyInverse, waitInverse);
        sequence.setCycleCount(Animation.INDEFINITE);
        return sequence;
    }

    private static Move faceMove(RotationAxis axis, int layer, int turns) {
        return new Move(axis, Set.of(layer), turns);
    }

    private Camera createCamera() {
        javafx.scene.PerspectiveCamera camera = new javafx.scene.PerspectiveCamera(true);
        Rotate pitch = new Rotate(-25, Rotate.X_AXIS);
        Rotate yaw = new Rotate(45, Rotate.Y_AXIS);
        Rotate roll = new Rotate(0, Rotate.Z_AXIS);
        camera.getTransforms().addAll(
            pitch,
            yaw,
            roll,
            new Translate(0, 0, -CAMERA_DISTANCE)
        );
        camera.setNearClip(0.1);
        camera.setFarClip(2000);
        return camera;
    }

    private record TurnDefinition(String label, Move move) {
    }

    private record AlgorithmDefinition(String label, String algorithm) {
    }
}
