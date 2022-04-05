package com.github.martonr.picalc.gui;

import com.github.martonr.picalc.gui.controller.ControllerMain;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public final class MainApplication extends Application {

    private ControllerMain mainController;

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Configure the stage
     *
     * @param stage Stage instance
     */
    private void configureStage(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("fxml/main.fxml"));
            Parent main = loader.load();
            mainController = loader.getController();

            stage.setTitle("Power Index Calculator");
            stage.getIcons()
                    .add(new Image(MainApplication.class.getResourceAsStream("pi_icon64.png")));
            stage.setScene(new Scene(main));
            stage.setOnCloseRequest(event -> {
                mainController.shutdown();
                Platform.exit();
                System.exit(0);
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to load FXML file for the UI.");
        }
    }

    @Override
    public void start(Stage primaryStage) {
        configureStage(primaryStage);
        primaryStage.show();
    }
}
