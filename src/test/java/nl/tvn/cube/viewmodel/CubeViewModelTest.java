package nl.tvn.cube.viewmodel;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane;
import javafx.scene.SubScene;
import javafx.scene.layout.StackPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.event.Event;
import javafx.scene.transform.Rotate;
import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import nl.tvn.cube.model.*;
import nl.tvn.cube.view.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CubeViewModelTest {
    private static boolean started;

    @BeforeAll
    static void startToolkit() throws Exception {
        assumeTrue(!System.getProperty("os.name").toLowerCase().contains("linux") || System.getenv("DISPLAY") != null,
            "JavaFX integration tests require a display");
        CompletableFuture<Void> ready = new CompletableFuture<>();
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            ready.complete(null);
        });
        ready.get(10, TimeUnit.SECONDS);
        started = true;
    }

    @AfterAll
    static void stopToolkit() {
        if (started) Platform.exit();
    }

    @Test
    void interactiveCommitRendersModelOrientationAndResetRestoresIt() throws Exception {
        fx(() -> {
            CubeViewModel vm = new CubeViewModel();
            var slice = vm.beginInteractiveSlice(RotationAxis.X, 1);
            assertTrue(vm.busyProperty().get());
            vm.updateInteractiveSlice(slice, -90);
            vm.finishInteractiveSlice(slice, -1);
            assertFalse(vm.isBusy());
            assertFalse(vm.beginnerStepProperty(BeginnerMethodStep.WHITE_CROSS).get());
            for (var node : vm.cubeGroup().getChildren()) {
                CubieView view = (CubieView) node;
                CubeVector logical = view.model().direction(new CubeVector(0, 1, 0));
                Point3D visual = view.getTransforms().getFirst().deltaTransform(new Point3D(0, -1, 0));
                assertEquals(new Point3D(logical.x(), -logical.y(), -logical.z()), visual);
                assertEquals(view.model().coordinate().x() * CubeConstants.step(), view.getTranslateX());
                assertEquals(-view.model().coordinate().y() * CubeConstants.step(), view.getTranslateY());
                assertEquals(-view.model().coordinate().z() * CubeConstants.step(), view.getTranslateZ());
            }
            vm.reset();
            assertTrue(vm.beginnerStepProperty(BeginnerMethodStep.YELLOW_CORNER_ORIENTATION).get());
            for (var node : vm.cubeGroup().getChildren()) assertTrue(node.getTransforms().getFirst().isIdentity());
        });
    }

    @Test
    void sequencePublishesIntermediateStatusWhileStillBusy() throws Exception {
        CompletableFuture<Void> finished = new CompletableFuture<>();
        List<Boolean> statuses = new ArrayList<>();
        fx(() -> {
            CubeViewModel vm = new CubeViewModel();
            vm.beginnerStepProperty(BeginnerMethodStep.WHITE_CROSS).addListener((obs, old, value) -> {
                assertTrue(vm.isBusy());
                statuses.add(value);
            });
            vm.busyProperty().addListener((obs, old, value) -> {
                if (!value) finished.complete(null);
            });
            vm.applyMoves(AlgorithmParser.parse("R R'").moves());
            assertTrue(vm.isBusy());
        });
        finished.get(10, TimeUnit.SECONDS);
        assertEquals(List.of(false, true), statuses);
    }

    @Test
    void helpButtonsRunSequencesAndDisableDuringWholeCubeDrags() throws Exception {
        CompletableFuture<Void> finished = new CompletableFuture<>();
        fx(() -> {
            CubeViewModel vm = new CubeViewModel();
            MainView main = new MainView(vm);
            Stage owner = new Stage();
            owner.setScene(new Scene(main.root(), 900, 700, true));
            main.bindInput(owner.getScene());
            HelpWindow help = new HelpWindow(owner, vm, main::runAlgorithm);
            owner.show();
            help.show();
            Stage helpStage = (Stage) Window.getWindows().stream().filter(w -> w instanceof Stage stage && "Cube Help".equals(stage.getTitle())).findFirst().orElseThrow();
            assertTrue(helpStage.getWidth() > 200);
            assertTrue(helpStage.getHeight() > 200);
            TabPane tabs = (TabPane) helpStage.getScene().getRoot().getChildrenUnmodifiable().getFirst();
            tabs.getSelectionModel().select(1);
            helpStage.getScene().getRoot().applyCss();
            helpStage.getScene().getRoot().layout();
            List<Button> buttons = tabs.getTabs().get(1).getContent().lookupAll(".button").stream()
                .filter(n -> n instanceof Button b && b.getText().equals("Run once")).map(n -> (Button) n).toList();
            assertEquals(8, buttons.size());
            var drag = vm.beginInteractiveCube(RotationAxis.X);
            assertTrue(buttons.stream().allMatch(Button::isDisabled));
            vm.finishInteractiveSlice(drag, 2);
            assertFalse(vm.isBusy());
            assertTrue(vm.beginnerStepProperty(BeginnerMethodStep.YELLOW_CORNER_ORIENTATION).get());
            assertTrue(buttons.stream().noneMatch(Button::isDisabled));
            vm.busyProperty().addListener((obs, old, value) -> {
                if (!value) {
                    helpStage.close();
                    owner.close();
                    finished.complete(null);
                }
            });
            buttons.getFirst().fire();
            assertTrue(vm.isBusy());
            assertTrue(buttons.stream().allMatch(Button::isDisabled));
        });
        finished.get(10, TimeUnit.SECONDS);
    }

    @Test
    void solveEdgeButtonCalculatesOffThreadLocksInputAndDisplaysPlayback() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CompletableFuture<Void> finished = new CompletableFuture<>();
        AtomicReference<CubeViewModel> reference = new AtomicReference<>();
        AtomicReference<Stage> window = new AtomicReference<>();
        AtomicReference<Stage> ownerWindow = new AtomicReference<>();
        AtomicReference<Button> button = new AtomicReference<>();
        List<Boolean> busyChanges = new ArrayList<>();
        fx(() -> {
            CubeViewModel vm = new CubeViewModel(snapshot -> {
                assertFalse(Platform.isFxApplicationThread());
                entered.countDown();
                try {
                    if (!release.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Test did not release calculation");
                } catch (InterruptedException error) {
                    throw new IllegalStateException(error);
                }
                return new WhiteCrossEdgeSolver().solve(snapshot);
            });
            reference.set(vm);
            Stage owner = new Stage();
            owner.setScene(new Scene(new javafx.scene.Group(), 300, 200));
            owner.show();
            ownerWindow.set(owner);
            new HelpWindow(owner, vm, text -> vm.applyMoves(AlgorithmParser.parse(text).moves())).show();
            Stage help = (Stage) Window.getWindows().stream().filter(w -> w instanceof Stage stage
                && "Cube Help".equals(stage.getTitle())).findFirst().orElseThrow();
            window.set(help);
            TabPane tabs = (TabPane) help.getScene().getRoot().getChildrenUnmodifiable().getFirst();
            tabs.getSelectionModel().select(1);
            help.getScene().getRoot().applyCss();
            help.getScene().getRoot().layout();
            Button solve = (Button) tabs.getTabs().get(1).getContent().lookup("#solve-white-edge");
            button.set(solve);
            assertTrue(solve.isDisabled());
            var slice = vm.beginInteractiveSlice(RotationAxis.X, 1);
            vm.finishInteractiveSlice(slice, -1);
            assertFalse(solve.isDisabled());
            vm.busyProperty().addListener((obs, old, value) -> {
                busyChanges.add(value);
                if (!value) finished.complete(null);
            });
            solve.fire();
            assertTrue(solve.isDisabled());
            assertEquals("Finding moves…", vm.whiteEdgeSolutionTextProperty().get());
        });
        assertTrue(entered.await(10, TimeUnit.SECONDS));
        try {
            fx(() -> {
                CubeViewModel vm = reference.get();
                var before = snapshot(vm);
                vm.reset();
                vm.randomize();
                vm.applyMove(new Move(RotationAxis.Y, Set.of(1), 1));
                vm.applyMoves(AlgorithmParser.parse("F U").moves());
                vm.solveOneWhiteEdge();
                assertNull(vm.beginInteractiveCube(RotationAxis.Y));
                assertNull(vm.beginInteractiveSlice(RotationAxis.Z, 1));
                assertEquals(before, snapshot(vm));
                assertEquals(List.of(true), busyChanges);
            });
        } finally {
            release.countDown();
        }
        finished.get(10, TimeUnit.SECONDS);
        fx(() -> {
            CubeViewModel vm = reference.get();
            assertEquals(List.of(true, false), busyChanges);
            assertEquals(15, snapshot(vm).solvedMask());
            assertTrue(button.get().isDisabled());
            assertEquals("Last solution — White–red: R'", vm.whiteEdgeSolutionTextProperty().get());
            Label shown = (Label) window.get().getScene().lookup("#white-edge-solution");
            assertEquals(vm.whiteEdgeSolutionTextProperty().get(), shown.getText());
            assertTrue(shown.isVisible());
            window.get().close();
            ownerWindow.get().close();
        });
    }

    @Test
    void edgeCalculationFailureAndNoSolutionReleaseLockWithoutChangingCube() throws Exception {
        for (boolean fail : List.of(false, true)) {
            CompletableFuture<Void> finished = new CompletableFuture<>();
            AtomicReference<CubeViewModel> reference = new AtomicReference<>();
            AtomicReference<WhiteCrossEdgeSolver.Snapshot> before = new AtomicReference<>();
            fx(() -> {
                CubeViewModel vm = new CubeViewModel(snapshot -> {
                    if (fail) throw new IllegalStateException("Simulated calculation failure");
                    return new WhiteCrossEdgeSolver.Solution(WhiteCrossEdgeSolver.Status.NO_SOLUTION, null, List.of());
                });
                reference.set(vm);
                var slice = vm.beginInteractiveSlice(RotationAxis.Z, 1);
                vm.finishInteractiveSlice(slice, 1);
                before.set(snapshot(vm));
                vm.busyProperty().addListener((obs, old, value) -> {
                    if (!value) finished.complete(null);
                });
                vm.solveOneWhiteEdge();
            });
            finished.get(10, TimeUnit.SECONDS);
            fx(() -> {
                CubeViewModel vm = reference.get();
                assertFalse(vm.isBusy());
                assertEquals(before.get(), snapshot(vm));
                assertTrue(vm.whiteEdgeSolutionTextProperty().get().contains("The cube was not changed."));
            });
        }
    }

    @Test
    void completedCrossDoesNotStartCalculation() throws Exception {
        fx(() -> {
            CubeViewModel vm = new CubeViewModel(snapshot -> { throw new AssertionError("Must not calculate"); });
            vm.solveOneWhiteEdge();
            assertFalse(vm.isBusy());
            assertEquals("White cross complete.", vm.whiteEdgeSolutionTextProperty().get());
        });
    }

    @Test
    void keyboardMovesFromEitherSceneReachMainCubeOnceWithAllModifiers() throws Exception {
        AtomicReference<KeyboardWindows> windows = new AtomicReference<>();
        fx(() -> windows.set(openKeyboardWindows()));
        try {
            List<KeyEvent> keys = new ArrayList<>();
            for (KeyCode code : List.of(KeyCode.F, KeyCode.B, KeyCode.R, KeyCode.L, KeyCode.U, KeyCode.D,
                    KeyCode.M, KeyCode.E, KeyCode.S, KeyCode.X, KeyCode.Y, KeyCode.Z)) {
                keys.add(key(code, false, false, false));
            }
            keys.add(key(KeyCode.R, true, false, false));
            keys.add(key(KeyCode.F, false, true, false));
            keys.add(key(KeyCode.U, false, false, true));
            keys.add(key(KeyCode.L, true, false, true));
            keys.add(key(KeyCode.B, false, true, true));
            for (boolean useHelp : List.of(false, true)) {
                for (int i = 0; i < keys.size(); i++) {
                    KeyEvent event = keys.get(i);
                    int tab = i % 3;
                    CompletableFuture<Void> finished = new CompletableFuture<>();
                    CubeModel expected = new CubeModel();
                    expected.applyMove(MoveFactory.wideMove(event).or(() -> MoveFactory.fromKeyEvent(event)).orElseThrow());
                    fx(() -> {
                        var w = windows.get();
                        w.vm().reset();
                        w.main().root().requestFocus();
                        TabPane tabs = (TabPane) w.help().scene().getRoot().getChildrenUnmodifiable().getFirst();
                        tabs.getSelectionModel().select(tab);
                        tabs.requestFocus();
                        w.vm().busyProperty().addListener(new javafx.beans.value.ChangeListener<Boolean>() {
                            @Override
                            public void changed(javafx.beans.value.ObservableValue<? extends Boolean> obs, Boolean old, Boolean busy) {
                                if (!busy) {
                                    w.vm().busyProperty().removeListener(this);
                                    finished.complete(null);
                                }
                            }
                        });
                        Event.fireEvent(useHelp ? tabs : w.main().root(), event);
                        assertTrue(w.vm().isBusy());
                    });
                    finished.get(10, TimeUnit.SECONDS);
                    fx(() -> assertEquals(pieceState(expected.cubies()), pieceState(cubies(windows.get().vm()))));
                }
            }
        } finally {
            fx(() -> closeKeyboardWindows(windows.get()));
        }
    }

    @Test
    void cameraShortcutsOverrideHelpNavigationButAlgorithmEditingKeepsItsKeys() throws Exception {
        AtomicReference<KeyboardWindows> windows = new AtomicReference<>();
        CompletableFuture<Void> enteredAlgorithm = new CompletableFuture<>();
        fx(() -> {
            var w = openKeyboardWindows();
            windows.set(w);
            var tabs = (TabPane) w.help().scene().getRoot().getChildrenUnmodifiable().getFirst();
            tabs.getSelectionModel().select(1);
            var scroll = (ScrollPane) ((StackPane) tabs.getTabs().get(1).getContent()).getChildren().getFirst();
            for (var target : List.of(w.main().root(), tabs, scroll)) {
                target.requestFocus();
                double[] initial = cameraAngles(w.main());
                Event.fireEvent(target, key(KeyCode.LEFT, false, false, false));
                assertEquals(initial[1] - 8, cameraAngles(w.main())[1]);
                Event.fireEvent(target, key(KeyCode.RIGHT, false, false, false));
                Event.fireEvent(target, key(KeyCode.UP, false, false, false));
                assertEquals(initial[0] - 6, cameraAngles(w.main())[0]);
                Event.fireEvent(target, key(KeyCode.DOWN, false, false, false));
                Event.fireEvent(target, key(KeyCode.PAGE_UP, false, false, false));
                assertEquals(initial[2] - 6, cameraAngles(w.main())[2]);
                Event.fireEvent(target, key(KeyCode.PAGE_DOWN, false, false, false));
                assertArrayEquals(initial, cameraAngles(w.main()));
                assertEquals(1, tabs.getSelectionModel().getSelectedIndex());
            }
            var drag = w.vm().beginInteractiveSlice(RotationAxis.X, 1);
            Event.fireEvent(tabs, key(KeyCode.F, false, false, false));
            double[] initial = cameraAngles(w.main());
            Event.fireEvent(tabs, key(KeyCode.RIGHT, false, false, false));
            assertEquals(initial[1] + 8, cameraAngles(w.main())[1]);
            w.vm().finishInteractiveSlice(drag, 0);
            assertTrue(w.vm().beginnerStepProperty(BeginnerMethodStep.YELLOW_CORNER_ORIENTATION).get());

            TextField input = (TextField) w.main().root().lookup(".text-field");
            input.requestFocus();
            assertSame(input, w.owner().getScene().getFocusOwner());
            double[] beforeEditing = cameraAngles(w.main());
            input.setText("");
            Event.fireEvent(input, key(KeyCode.R, false, false, false));
            Event.fireEvent(input, new KeyEvent(KeyEvent.KEY_TYPED, "R", "", KeyCode.UNDEFINED, false, false, false, false));
            assertEquals("R", input.getText());
            assertFalse(w.vm().isBusy());
            Event.fireEvent(input, key(KeyCode.LEFT, false, false, false));
            assertEquals(0, input.getCaretPosition());
            Event.fireEvent(input, key(KeyCode.RIGHT, false, false, false));
            assertEquals(1, input.getCaretPosition());
            assertArrayEquals(beforeEditing, cameraAngles(w.main()));
            w.vm().busyProperty().addListener((obs, old, busy) -> { if (!busy) enteredAlgorithm.complete(null); });
            Event.fireEvent(input, key(KeyCode.ENTER, false, false, false));
            assertTrue(w.vm().isBusy());
        });
        try {
            enteredAlgorithm.get(10, TimeUnit.SECONDS);
            fx(() -> assertFalse(windows.get().vm().beginnerStepProperty(BeginnerMethodStep.WHITE_CROSS).get()));
        } finally {
            fx(() -> closeKeyboardWindows(windows.get()));
        }
    }

    private record KeyboardWindows(CubeViewModel vm, MainView main, HelpWindow help, Stage owner) { }

    private static KeyboardWindows openKeyboardWindows() {
        CubeViewModel vm = new CubeViewModel();
        MainView main = new MainView(vm);
        Stage owner = new Stage();
        owner.setScene(new Scene(main.root(), 900, 700, true));
        main.bindInput(owner.getScene());
        main.bindInput(owner.getScene());
        HelpWindow help = new HelpWindow(owner, vm, main::runAlgorithm);
        main.attachHelpWindow(help);
        main.attachHelpWindow(help);
        owner.show();
        help.show();
        ((Stage) help.scene().getWindow()).hide();
        help.show();
        main.root().applyCss();
        main.root().layout();
        help.scene().getRoot().applyCss();
        help.scene().getRoot().layout();
        return new KeyboardWindows(vm, main, help, owner);
    }

    private static void closeKeyboardWindows(KeyboardWindows windows) {
        ((Stage) windows.help().scene().getWindow()).close();
        windows.owner().close();
    }

    private static KeyEvent key(KeyCode code, boolean shift, boolean control, boolean alt) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", code.getName(), code, shift, control, alt, false);
    }

    private static double[] cameraAngles(MainView main) {
        SubScene scene = (SubScene) ((StackPane) main.root().getCenter()).getChildren().getFirst();
        return scene.getCamera().getTransforms().stream().filter(t -> t instanceof Rotate)
            .mapToDouble(t -> ((Rotate) t).getAngle()).toArray();
    }

    private static List<CubieModel> cubies(CubeViewModel vm) {
        return vm.cubeGroup().getChildren().stream().map(n -> ((CubieView) n).model()).toList();
    }

    private static List<String> pieceState(List<CubieModel> cubies) {
        return cubies.stream().map(c -> c.homeX() + "," + c.homeY() + "," + c.homeZ() + ":"
            + c.coordinate().x() + "," + c.coordinate().y() + "," + c.coordinate().z()
            + c.direction(new CubeVector(1, 0, 0)) + c.direction(new CubeVector(0, 1, 0))
            + c.direction(new CubeVector(0, 0, 1))).sorted().toList();
    }

    private static WhiteCrossEdgeSolver.Snapshot snapshot(CubeViewModel vm) {
        return WhiteCrossEdgeSolver.Snapshot.capture(vm.cubeGroup().getChildren().stream()
            .map(node -> ((CubieView) node).model()).toList());
    }

    private static void fx(Runnable action) throws Exception {
        CompletableFuture<Void> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                action.run();
                result.complete(null);
            } catch (Throwable error) {
                result.completeExceptionally(error);
            }
        });
        result.get(10, TimeUnit.SECONDS);
    }
}
