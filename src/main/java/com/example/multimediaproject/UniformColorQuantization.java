package com.example.multimediaproject;

import javafx.util.Pair;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class UniformColorQuantization {

    public static Pair<String, BufferedImage> start(String path) {
        try {
            // Load the image
            BufferedImage image = ImageIO.read(new File(path));

            // Apply the Popularity Algorithm
            // Specify the number of colors
            BufferedImage quantizedImage = quantizeImage(image);

            //remove .jpg
            path = path.substring(0, path.length() - 4);

            // Convert the quantized image to indexed
            int[][][] hist = Utils.buildHistogram(quantizedImage);
            int[] colors = Utils.transformIntoIntColors(hist);
            IndexColorModel indexedModel = new IndexColorModel(8, colors.length, colors, 0, false, -1, DataBuffer.TYPE_BYTE);
            BufferedImage indexedImage = new BufferedImage(quantizedImage.getWidth(), quantizedImage.getHeight(), BufferedImage.TYPE_BYTE_INDEXED, indexedModel);
            indexedImage.getGraphics().drawImage(quantizedImage, 0, 0, null);

            // Save the quantized image
            ImageIO.write(indexedImage, "png", new File(path + "-uni-quantized.png"));

            System.out.println("Quantization completed.");

            return new Pair<>(path + "-uni-quantized.png", indexedImage);

        } catch (IOException e) {
            e.printStackTrace();
            return new Pair<>("Quantization failed.", null);
        }
    }


    public static BufferedImage quantizeImage(BufferedImage inputImage) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();

        int[][] rRegionMappings = new int[8][];
        int[][] gRegionMappings = new int[8][];
        int[][] bRegionMappings = new int[8][];

        // Initialize region mappings
        for (int i = 0; i < 8; i++) {
            rRegionMappings[i] = new int[width * height];
            gRegionMappings[i] = new int[width * height];
            bRegionMappings[i] = new int[width * height];
        }

        // Loop through all pixels and put the colors into the respective color regions
        int pixelIndex = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = inputImage.getRGB(x, y);

                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                int redRegionIndex = getRegionIndex(red);
                int greenRegionIndex = getRegionIndex(green);
                int blueRegionIndex = getRegionIndex(blue);

                rRegionMappings[redRegionIndex][pixelIndex] = red;
                gRegionMappings[greenRegionIndex][pixelIndex] = green;
                bRegionMappings[blueRegionIndex][pixelIndex] = blue;

                pixelIndex++;
            }
        }

        // Find the color that represents each region
        int[] rRepresentativeColorPerRegion = new int[8];
        int[] gRepresentativeColorPerRegion = new int[8];
        int[] bRepresentativeColorPerRegion = new int[8];

        for (int i = 0; i < 8; i++) {
            rRepresentativeColorPerRegion[i] = getAverage(rRegionMappings[i], pixelIndex);
            gRepresentativeColorPerRegion[i] = getAverage(gRegionMappings[i], pixelIndex);
            bRepresentativeColorPerRegion[i] = getAverage(bRegionMappings[i], pixelIndex);
        }

        // Create output image with quantized colors
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        pixelIndex = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = inputImage.getRGB(x, y);

                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                int quantizedRed = rRepresentativeColorPerRegion[getRegionIndex(red)];
                int quantizedGreen = gRepresentativeColorPerRegion[getRegionIndex(green)];
                int quantizedBlue = bRepresentativeColorPerRegion[getRegionIndex(blue)];

                int quantizedRGB = (quantizedRed << 16) | (quantizedGreen << 8) | quantizedBlue;

                outputImage.setRGB(x, y, quantizedRGB);

                pixelIndex++;
            }
        }

        return outputImage;
    }

    public static int getRegionIndex(int colorValue) {
        int[][] eightRegions = {
                {0, 31}, {32, 63}, {64, 95}, {96, 127},
                {128, 159}, {160, 191}, {192, 223}, {224, 255}
        };

        for (int index = 0; index < eightRegions.length; index++) {
            int[] regionValue = eightRegions[index];
            if (colorValue >= regionValue[0] && colorValue <= regionValue[1]) {
                return index;
            }
        }

        return 0; // Default region index
    }

    public static int getAverage(int[] regionMapping, int count) {
        int sum = 0;
        for (int i = 0; i < count; i++) {
            sum += regionMapping[i];
        }
        return sum / count;
    }
}
