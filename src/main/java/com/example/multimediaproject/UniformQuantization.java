package com.example.multimediaproject;

import javafx.util.Pair;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class UniformQuantization {

    public static Pair<String, BufferedImage> start(String path) {
        // Load the input image
        BufferedImage inputImage;
        try {
            inputImage = ImageIO.read(new File(path));

            // Set the number of quantization regions
            int regions = 8;

            // Perform uniform color quantization
            BufferedImage quantizedImage = performUniformColorQuantization(inputImage, regions);

            //remove .jpg
            path = path.substring(0,path.length()-4);

            // Convert the quantized image to indexed
            int[][][] hist = Utils.buildHistogram(quantizedImage);
            int[] colors = Utils.transformIntoIntColors(hist);
            IndexColorModel indexedModel = new IndexColorModel(8, colors.length, colors, 0, false, -1, DataBuffer.TYPE_BYTE);
            BufferedImage indexedImage = new BufferedImage(quantizedImage.getWidth(), quantizedImage.getHeight(), BufferedImage.TYPE_BYTE_INDEXED, indexedModel);
            indexedImage.getGraphics().drawImage(quantizedImage, 0, 0, null);

            // Save the quantized image
            ImageIO.write(indexedImage, "png", new File(path+"-uni-quantized.png"));

            System.out.println("Quantization completed.");

            return new Pair<>(path+"-uni-quantized.png", indexedImage);

        }catch (IOException e){
            e.printStackTrace();
            return new Pair<>( "Quantization failed.",null);
        }
    }

    public static BufferedImage performUniformColorQuantization(BufferedImage inputImage, int regions) {
        BufferedImage quantizedImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);
        int step = 256 / regions;

        for (int y = 0; y < inputImage.getHeight(); y++) {
            for (int x = 0; x < inputImage.getWidth(); x++) {
                Color inputColor = new Color(inputImage.getRGB(x, y));
                int r = ((inputColor.getRed() / step) * step) + (step / 2);
                int g = ((inputColor.getGreen() / step) * step) + (step / 2);
                int b = ((inputColor.getBlue() / step) * step) + (step / 2);
                Color outputColor = new Color(r, g, b);
                quantizedImage.setRGB(x, y, outputColor.getRGB());
            }
        }
        return quantizedImage;
    }
}