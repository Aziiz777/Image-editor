package com.example.multimediaproject;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.FlowPane;
import javafx.scene.SubScene;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Set;
import java.util.HashSet;

public class ImageEditor extends Application {

    private ImageView originalImageView;
    private ImageView editedImageView;
    private Label originalImageInfoLabel;
    private Label editedImageInfoLabel;
    private BufferedImage editedBufferedImage;
    private String path;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Image Editor");

        Button chooseImageButton = new Button("Choose Image");
        chooseImageButton.setOnAction(actionEvent -> path = chooseImage(primaryStage));

        originalImageView = new ImageView();
        originalImageView.setPreserveRatio(true);
        originalImageView.setFitWidth(400);

        editedImageView = new ImageView();
        editedImageView.setPreserveRatio(true);
        editedImageView.setFitWidth(400);

        originalImageInfoLabel = new Label();
        originalImageInfoLabel.setVisible(false);
        originalImageInfoLabel.setStyle("-fx-font-size: 14px;");

        editedImageInfoLabel = new Label();
        editedImageInfoLabel.setVisible(false);
        editedImageInfoLabel.setStyle("-fx-font-size: 14px;");

        VBox originalImageContainer = new VBox(originalImageView, originalImageInfoLabel);
        originalImageContainer.setAlignment(Pos.CENTER);
        originalImageContainer.setSpacing(20);
        originalImageContainer.setPadding(new Insets(20));

        VBox editedImageContainer = new VBox(editedImageView, editedImageInfoLabel);
        editedImageContainer.setAlignment(Pos.CENTER);
        editedImageContainer.setSpacing(20);
        editedImageContainer.setPadding(new Insets(20));

        Button quantizationButton1 = new Button("Popularity Algorithm");
        quantizationButton1.setOnAction(event -> {
            Pair<String, BufferedImage> image;
            image = PopularityAlgo.start(path);
            String editedImage = image.getKey();
            editedBufferedImage = image.getValue();
            System.out.println("edited image path: " + editedImage);
            Image newImage = new Image(editedImage);
            updateImageInfo(newImage, editedImageInfoLabel);
            editedImageInfoLabel.setVisible(true);
            editedImageView.setImage(newImage);
        });

        Button quantizationButton2 = new Button("Uniform Algorithm");
        quantizationButton2.setOnAction(event -> {
            Pair<String, BufferedImage> image;
            image = UniformColorQuantization.start(path);
            String editedImage = image.getKey();
            editedBufferedImage = image.getValue();
            System.out.println("edited image path: " + editedImage);
            Image newImage = new Image(editedImage);
            updateImageInfo(newImage, editedImageInfoLabel);
            editedImageInfoLabel.setVisible(true);
            editedImageView.setImage(new Image(editedImage));
        });

        Button quantizationButton3 = new Button("MedianCut Algorithm");
        quantizationButton3.setOnAction(event -> {
            Pair<String, BufferedImage> image;
            image = MedianCutQuantization.start(path);
            String editedImage = image.getKey();
            editedBufferedImage = image.getValue();
            Image newImage = new Image(editedImage);
            System.out.println("edited image path: " + editedImage);
            updateImageInfo(newImage, editedImageInfoLabel);
            editedImageInfoLabel.setVisible(true);
            editedImageView.setImage(new Image(editedImage));
        });

        Button colorPaletteButton = new Button("Color Palette");
        colorPaletteButton.setOnAction(event -> {
            if (editedBufferedImage != null) {
                int[][][] histogram = Utils.buildHistogram(editedBufferedImage);
                showColorPalette(primaryStage, histogram);
            }
        });
        Button showHistogramButton = new Button("Histogram");
        showHistogramButton.setOnAction(event -> showHistogram(primaryStage)
        );

        VBox buttonContainer = new VBox(10);
        buttonContainer.getChildren().addAll(chooseImageButton, quantizationButton1, quantizationButton2, quantizationButton3, colorPaletteButton, showHistogramButton);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPadding(new Insets(10));

        BorderPane borderPane = new BorderPane();
        borderPane.setRight(buttonContainer);

        HBox imageContainer = new HBox(20);
        imageContainer.getChildren().addAll(originalImageContainer, editedImageContainer);
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setPadding(new Insets(10));

        borderPane.setCenter(imageContainer);

        // Create the color palette sub-scene
        Group colorPaletteGroup = new Group();
        // Declare the SubScene and Group
        SubScene colorPaletteSubScene = new SubScene(colorPaletteGroup, 400, 400);
        colorPaletteSubScene.setFill(Color.WHITE);
        colorPaletteSubScene.setManaged(false);
        colorPaletteSubScene.setVisible(false);

        VBox root = new VBox(20);
        root.getChildren().addAll(borderPane, colorPaletteSubScene);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 1200, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private String chooseImage(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose an image file");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            String imagePath = selectedFile.toURI().toString();
            System.out.println("original image path: " + imagePath);
            Image image = new Image(imagePath);
            originalImageView.setImage(image);
            originalImageInfoLabel.setVisible(true);
            updateImageInfo(image, originalImageInfoLabel);
            editedImageView.setImage(null);
            editedImageInfoLabel.setText("");
            return selectedFile.getAbsolutePath();
        }
        return "No File Chosen";
    }

    private void updateImageInfo(Image image, Label label) {

        // Get the size of the image in kilobytes or megabytes
        String imageSize = getImageSize(image);

        // Get the number of colors in the image
        int numColors = getImageColorCount(image);

        String imageInfo = "Image Size: " + imageSize + ", Number Of Colors: " + numColors;
        System.out.println(imageInfo);
        label.setText(imageInfo);
    }

    private String getImageSize(Image image) {
        try {
            File file;
            String imageUrl = image.getUrl();
            if (imageUrl.startsWith("file:/")) {
                file = new File(imageUrl.substring(5)); // Remove "file:" from the URL
            } else {
                file = new File(imageUrl);
            }
            long fileSizeBytes = file.length();
            long fileSizeKB = fileSizeBytes / 1024; // Convert bytes to kilobytes
            long fileSizeMB = fileSizeKB / 1024; // Convert kilobytes to megabytes

            if (fileSizeMB > 0) {
                return fileSizeMB + " MB";
            } else {
                return fileSizeKB + " KB";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
        }
    }

    private int getImageColorCount(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        Set<Integer> uniqueColors = new HashSet<>();
        if (image.getPixelReader() != null) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    Color color = image.getPixelReader().getColor(x, y);
                    if (color != null) {
                        int rgb = getColorRGB(color);
                        uniqueColors.add(rgb);
                    }
                }
            }
        }

        return uniqueColors.size();
    }

    private int getColorRGB(Color color) {
        int r = (int) (color.getRed() * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue() * 255);

        return (r << 16) | (g << 8) | b;
    }

    private void showColorPalette(Stage primaryStage, int[][][] histogram) {
        Stage colorPaletteStage = new Stage();
        colorPaletteStage.initOwner(primaryStage);
        FlowPane root = new FlowPane();
        root.setPadding(new Insets(10, 0, 0, 0));
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

        Scene scene = new Scene(root, 450, 200);
        colorPaletteStage.setTitle("Color Palette");
        colorPaletteStage.setScene(scene);
        colorPaletteStage.show();
    }

    private Rectangle createColorBox(Color color) {
        Rectangle colorBox = new Rectangle(20, 20);
        colorBox.setFill(color);
        colorBox.setStroke(Color.BLACK);
        colorBox.setStrokeWidth(1);
        return colorBox;
    }

    private void showHistogram(Stage primaryStage) {
        if (editedBufferedImage != null) {
            int[][][] histogram = Utils.buildHistogram(editedBufferedImage);
            displayHistogramChart(primaryStage, histogram);
        }
    }

    private void displayHistogramChart(Stage primaryStage, int[][][] histogram) {

        java.awt.Color[] colors = Utils.transformIntoColors(histogram);

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("values");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("pixels number");

        AreaChart<Number, Number> redChart = new AreaChart<>(xAxis, yAxis);
        redChart.setTitle("Red Histogram");
        XYChart.Series<Number, Number> redSeries = new XYChart.Series<>();
        for (int i = 0; i < colors.length; i++) {
            redSeries.getData().add(new XYChart.Data<>(i, colors[i].getRed()));
        }
        redChart.getData().add(redSeries);

        LineChart<Number, Number> greenChart = new LineChart<>(xAxis, yAxis);
        greenChart.setTitle("Green Histogram");
        XYChart.Series<Number, Number> greenSeries = new XYChart.Series<>();
        for (int i = 0; i < colors.length; i++) {
            greenSeries.getData().add(new XYChart.Data<>(i, colors[i].getGreen()));
        }
        greenChart.getData().add(greenSeries);

        LineChart<Number, Number> blueChart = new LineChart<>(xAxis, yAxis);
        blueChart.setTitle("Blue Histogram");
        XYChart.Series<Number, Number> blueSeries = new XYChart.Series<>();
        for (int i = 0; i < colors.length; i++) {
            blueSeries.getData().add(new XYChart.Data<>(i, colors[i].getBlue()));
        }
        blueChart.getData().add(blueSeries);

        Stage chartStage = new Stage();
        chartStage.initOwner(primaryStage);
        chartStage.setTitle("Histogram Chart");
        VBox allCharts = new VBox(redChart, greenChart, blueChart);
        Scene chartScene = new Scene(allCharts, 800, 600);
        chartStage.setScene(chartScene);
        chartStage.show();

    }

    //    private void displayHistogramChart(Stage primaryStage, int[][][] histogram) {
//        Stage chartStage = new Stage();
//        chartStage.initOwner(primaryStage);
//        chartStage.setTitle("Histogram Chart");
//
//        CategoryAxis xAxis = new CategoryAxis();
//        NumberAxis yAxis = new NumberAxis();
//        BarChart<String, Number> histogramChart = new BarChart<>(xAxis, yAxis);
//        histogramChart.setTitle("Color Histogram");
//        xAxis.setLabel("Color");
//        yAxis.setLabel("Count");
//
//        XYChart.Series<String, Number> redSeries = new XYChart.Series<>();
//        redSeries.setName("Red");
//        XYChart.Series<String, Number> greenSeries = new XYChart.Series<>();
//        greenSeries.setName("Green");
//        XYChart.Series<String, Number> blueSeries = new XYChart.Series<>();
//        blueSeries.setName("Blue");
//
//        for (int r = 0; r < histogram.length; r++) {
//            for (int g = 0; g < histogram[r].length; g++) {
//                for (int b = 0; b < histogram[r][g].length; b++) {
//                    int count = histogram[r][g][b];
//                    if (count > 0) {
//                        String colorLabel = String.format("#%02X%02X%02X", r, g, b);
//                        redSeries.getData().add(new XYChart.Data<>(colorLabel, count));
//                        greenSeries.getData().add(new XYChart.Data<>(colorLabel, count));
//                        blueSeries.getData().add(new XYChart.Data<>(colorLabel, count));
//                    }
//                }
//            }
//        }
//
//        histogramChart.getData().addAll(redSeries, greenSeries, blueSeries);
//
//        Scene chartScene = new Scene(histogramChart, 800, 600);
//        chartStage.setScene(chartScene);
//        chartStage.show();
//    }

}
