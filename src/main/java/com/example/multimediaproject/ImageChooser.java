package com.example.multimediaproject;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class ImageChooser {
    public static String start(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.*"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png")
        );
        File file = fileChooser.showOpenDialog(stage);
        if(file != null){

            return file.getAbsolutePath();
        }
        return "No File Chosen";
    }
}