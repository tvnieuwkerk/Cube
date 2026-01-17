package nl.tvn.cube.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nl.tvn.cube.viewmodel.CubeViewModel;

public final class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        CubeViewModel viewModel = new CubeViewModel();
        MainView view = new MainView(viewModel);
        Scene scene = new Scene(view.root(), 900, 700, true);
        view.bindInput(scene);

        stage.setTitle("Rubik's Cube");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
