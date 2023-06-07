package com.example.multimediaproject;

import javafx.util.Pair;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.io.File;
import javax.imageio.ImageIO;

public class UniformQuantization {

    public static Pair<String, BufferedImage> start(String path) {

        // Load the input image
        BufferedImage inputImage = null;
        try {
            inputImage = ImageIO.read(new File(path));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return new Pair<>("Quantization failed.", null);
        }

        // Set the number of colors for quantization
        int numColors = 16;

        // Get the dimensions of the input image
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();

        // Create a new BufferedImage for the quantized image
        BufferedImage quantizedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED);

        // Compute the size of each color bin
        int binSize = 256 / numColors;

        // Quantize each pixel in the input image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Get the color of the current pixel
                Color color = new Color(inputImage.getRGB(x, y));

                // Quantize the color by converting it to the nearest bin center
                int red = (color.getRed() / binSize) * binSize;
                int green = (color.getGreen() / binSize) * binSize;
                int blue = (color.getBlue() / binSize) * binSize;

                // Set the quantized color of the current pixel in the output image
                Color quantizedColor = new Color(red, green, blue);
                quantizedImage.setRGB(x, y, quantizedColor.getRGB());
            }
        }

        //remove .jpg
        path = path.substring(0, path.length() - 4);

        // Convert the quantized image to indexed
        int[][][] hist = Utils.buildHistogram(quantizedImage);
        int[] colors = Utils.transformIntoIntColors(hist);
        IndexColorModel indexedModel = new IndexColorModel(8, colors.length, colors, 0, false, -1, DataBuffer.TYPE_BYTE);
        BufferedImage indexedImage = new BufferedImage(quantizedImage.getWidth(), quantizedImage.getHeight(), BufferedImage.TYPE_BYTE_INDEXED, indexedModel);
        indexedImage.getGraphics().drawImage(quantizedImage, 0, 0, null);

        try {
            // Save the quantized-indexed image
            ImageIO.write(indexedImage, "png", new File(path + "-uni-quantized.png"));

            System.out.println("Quantization completed.");
            return new Pair<>(path + "-uni-quantized.png", indexedImage);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return new Pair<>("Quantization failed.", null);
        }
    }
}