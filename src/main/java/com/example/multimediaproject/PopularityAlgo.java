package com.example.multimediaproject;

import javafx.util.Pair;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;

public class PopularityAlgo {

    public static Pair<String,BufferedImage> start(String path){
        try {
            // Load the image
            BufferedImage image = ImageIO.read(new File(path));

            // Apply the Popularity Algorithm
            // Specify the number of colors
            BufferedImage quantizedImage = applyPopularityAlgorithm(image, 256);

            //remove .jpg
            path = path.substring(0,path.length()-4);

            // Convert the quantized image to indexed
            int[][][] hist = Utils.buildHistogram(quantizedImage);
            int[] colors = Utils.transformIntoIntColors(hist);
            IndexColorModel indexedModel = new IndexColorModel(8, colors.length, colors, 0, false, -1, DataBuffer.TYPE_BYTE);
            BufferedImage indexedImage = new BufferedImage(quantizedImage.getWidth(), quantizedImage.getHeight(), BufferedImage.TYPE_BYTE_INDEXED, indexedModel);
            indexedImage.getGraphics().drawImage(quantizedImage, 0, 0, null);

            // Save the quantized image
            ImageIO.write(indexedImage, "png", new File(path+"-pop-quantized.png"));

            System.out.println("Quantization completed.");

            return new Pair<>(path+"-pop-quantized.png", indexedImage);

        } catch (IOException e) {
            e.printStackTrace();
            return new Pair<>( "Quantization failed.",null);
        }
    }

    public static BufferedImage applyPopularityAlgorithm(BufferedImage image, int numColors) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Step 1: Build a color histogram
        int[][][] histogram = Utils.buildHistogram(image);

        // Step 2: Find the most frequent colors
        Color[] dominantColors = findDominantColors(histogram, numColors);

        // Step 3: Replace each pixel with the closest dominant color
        BufferedImage quantizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixelColor = new Color(image.getRGB(x, y));
                Color closestColor = findClosestColor(pixelColor, dominantColors);
                quantizedImage.setRGB(x, y, closestColor.getRGB());
            }
        }

        return quantizedImage;
    }

    public static Color[] findDominantColors(int[][][] histogram, int numColors) {
        int width = histogram.length;
        int height = histogram[0].length;
        int depth = histogram[0][0].length;

        Color[] dominantColors = new Color[numColors];
        int[] maxFrequencies = new int[numColors];

        for (int i = 0; i < numColors; i++) {
            int maxFrequency = 0;
            int maxR = 0, maxG = 0, maxB = 0;

            for (int r = 0; r < width; r++) {
                for (int g = 0; g < height; g++) {
                    for (int b = 0; b < depth; b++) {
                        int frequency = histogram[r][g][b];

                        if (frequency > maxFrequency) {
                            maxFrequency = frequency;
                            maxR = r;
                            maxG = g;
                            maxB = b;
                        }
                    }
                }
            }

            dominantColors[i] = new Color(maxR, maxG, maxB);
            maxFrequencies[i] = maxFrequency;

            // Set the max frequency to 0 so that the next iteration finds the next most frequent color
            histogram[maxR][maxG][maxB] = 0;
        }

        return dominantColors;
    }

    public static Color findClosestColor(Color targetColor, Color[] colors) {
        Color closestColor = colors[0];
        double minDistance = calculateColorDistance(targetColor, closestColor);

        double distance = 0;
        for (int i = 1; i < colors.length; i++) {
            distance = calculateColorDistance(targetColor, colors[i]);

            if (distance < minDistance) {
                minDistance = distance;
                closestColor = colors[i];
            }
        }
            return closestColor;
        }


    public static double calculateColorDistance(Color color1, Color color2) {
        int rDiff = color1.getRed() - color2.getRed();
        int gDiff = color1.getGreen() - color2.getGreen();
        int bDiff = color1.getBlue() - color2.getBlue();

        return Math.sqrt(rDiff * rDiff + gDiff * gDiff + bDiff * bDiff);
    }
}