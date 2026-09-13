package nl.tvn.cube.viewmodel;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import java.util.*;
import java.util.concurrent.*;
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
