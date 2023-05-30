package com.example.multimediaproject;

import java.awt.*;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import javax.swing.*;
import java.awt.image.BufferedImage;
import  javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.FlowPane;
import javafx.geometry.Insets;


public class BaseApp extends Application {

    Controller controller = new Controller();

    @Override
    public void start(Stage stage) {
        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(400);
        imageView.setFitHeight(400);
        imageView.setManaged(false);
        //Buttons
        Button chooseImage = new Button("Choose Image");
        Button quantize = new Button("Quantize");
        //Visibility
        quantize.setVisible(false);
        quantize.setManaged(false);

        var ref = new Object() {
            int[][][] histogram = null;
        };
        //onActions
        chooseImage.setOnAction(actionEvent -> {
            controller.path = ImageChooser.start(new Stage());
            if(controller.path.compareTo("No File Chosen") != 0) {
                quantize.setManaged(true);
                quantize.setVisible(true);
                imageView.setManaged(true);
                imageView.setImage(new Image(controller.path));
            }
        });

        quantize.setOnAction(actionEvent -> {
            Pair<String, BufferedImage> popularityResult = PopularityAlgo.start(controller.path);
            BufferedImage image = null;
            image=popularityResult.getValue();
            ref.histogram =PopularityAlgo.buildHistogram(image);
            startaa(stage, ref.histogram);

            controller.path=popularityResult.getKey();

            if(controller.path.compareTo("Quantization failed.") != 0) {
                imageView.setImage(new Image(controller.path));
//                showColorPalette(image);
            }
        });


        //Spacers
        Region space1 = new Region();
        space1.setMinHeight(10);
        Region space2 = new Region();
        space2.setMinHeight(10);
        //VBox
        VBox vBox = new VBox(chooseImage, space1, imageView, space2, quantize);
        vBox.setAlignment(Pos.CENTER);
        Scene scene = new Scene(vBox, 640, 480);
        stage.setTitle("Multimedia Project");
        stage.setScene(scene);
        stage.show();
    }

    public void startaa(Stage primaryStage,int [][][] histogram) {
        FlowPane root = new FlowPane();
        root.setPadding(new Insets(10,0,0,0));
        root.setAlignment(Pos.CENTER);

        // Iterate over the histogram data and create rectangles for each color
        for (int r = 0; r < histogram.length; r++) {
            for (int g = 0; g < histogram[r].length; g++) {
                for (int b = 0; b < histogram[r][g].length; b++) {
                    int count = histogram[r][g][b];
                    if (count > 0) {
                        Color color = Color.rgb(r, g, b);
                        Rectangle colorBox = createColorBox(color);
                        root.getChildren().add(colorBox);
                    }
                }
            }
        }

        Scene scene = new Scene(root);
        primaryStage.setTitle("Color Palette");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Rectangle createColorBox(Color color) {
        Rectangle colorBox = new Rectangle(50, 50);
        colorBox.setFill(color);
        colorBox.setStroke(Color.BLACK);
        colorBox.setStrokeWidth(1);
        return colorBox;
    }




    public static void main(String[] args) {
        launch();
    }
}