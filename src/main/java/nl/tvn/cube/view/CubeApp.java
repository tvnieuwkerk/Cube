package nl.tvn.cube.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public final class CubeApp extends Application {
    @Override
    public void start(Stage stage) {
        CubeView cubeView = new CubeView();
        BorderPane root = new BorderPane(cubeView.getNode());
        Scene scene = new Scene(root, 900, 700);
        cubeView.registerInput(scene);
        stage.setTitle("Rubik's Cube");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
