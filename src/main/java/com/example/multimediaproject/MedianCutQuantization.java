package com.example.multimediaproject;

import javafx.util.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class MedianCutQuantization {

    public static Pair<String, BufferedImage> start(String path) {
        try {
            // Load the image
            BufferedImage image = ImageIO.read(new File(path));

            // Create a flattened array of the image pixels (RGB values and x, y locations)
            List<int[]> flattenedImgArray = new ArrayList<>();
            for (int rIndex = 0; rIndex < image.getHeight(); rIndex++) {
                for (int cIndex = 0; cIndex < image.getWidth(); cIndex++) {
                    Color color = new Color(image.getRGB(cIndex, rIndex));
                    int r = color.getRed();
                    int g = color.getGreen();
                    int b = color.getBlue();
                    flattenedImgArray.add(new int[]{r, g, b, rIndex, cIndex});
                }
            }

            // Call the splitIntoBuckets function to quantize the image
            splitIntoBuckets(image, flattenedImgArray, 6);

            //remove .jpg
            path = path.substring(0, path.length() - 4);

            // Convert the quantized image to indexed
            int[][][] hist = Utils.buildHistogram(image);
            int[] colors = Utils.transformIntoIntColors(hist);
            IndexColorModel indexedModel = new IndexColorModel(8, colors.length, colors, 0, false, -1, DataBuffer.TYPE_BYTE);
            BufferedImage indexedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_INDEXED, indexedModel);
            indexedImage.getGraphics().drawImage(image, 0, 0, null);

            // Save the indexed image
            ImageIO.write(indexedImage, "png", new File(path + "-mc-quantized.png"));

            System.out.println("Quantization completed.");

            return new Pair<>(path + "-mc-quantized.png", indexedImage);

        } catch (IOException e) {
            e.printStackTrace();
            return new Pair<>("Quantization failed.", null);
        }
    }

    public static void splitIntoBuckets(BufferedImage img, List<int[]> imgArr, int depth) {
        if (imgArr.size() == 0) {
            return;
        }

        // Base case - if depth is 0, quantize the pixels in the bucket
        if (depth == 0) {
            medianCutQuantize(img, imgArr);
            return;
        }

        // Find the range of RGB values (max - min) for the current set of pixels
        int rMin = Integer.MAX_VALUE;
        int rMax = Integer.MIN_VALUE;
        int gMin = Integer.MAX_VALUE;
        int gMax = Integer.MIN_VALUE;
        int bMin = Integer.MAX_VALUE;
        int bMax = Integer.MIN_VALUE;
        for (int[] pixel : imgArr) {
            rMin = Math.min(rMin, pixel[0]);
            rMax = Math.max(rMax, pixel[0]);
            gMin = Math.min(gMin, pixel[1]);
            gMax = Math.max(gMax, pixel[1]);
            bMin = Math.min(bMin, pixel[2]);
            bMax = Math.max(bMax, pixel[2]);
        }
        int rRange = rMax - rMin;
        int gRange = gMax - gMin;
        int bRange = bMax - bMin;

        // Determine which color space to split on (the one with the largest range)
        int spaceWithHighestRange = Math.max(Math.max(rRange, gRange), bRange);
        if(spaceWithHighestRange == rRange){
            spaceWithHighestRange = 0;
        }else if(spaceWithHighestRange == gRange){
            spaceWithHighestRange = 1;
        }else {
            spaceWithHighestRange = 2;
        }

        // Sort the pixels based on the selected color space and find the median
        int finalSpaceWithHighestRange = spaceWithHighestRange;
        imgArr.sort((p1, p2) -> Integer.compare(p1[finalSpaceWithHighestRange], p2[finalSpaceWithHighestRange]));
        int medianIndex = imgArr.size() / 2;

        // Split into two buckets on either side of the median and recurse
        List<int[]> imgArr1 = imgArr.subList(0, medianIndex);
        List<int[]> imgArr2 = imgArr.subList(medianIndex, imgArr.size());
        splitIntoBuckets(img, imgArr1, depth - 1);
        splitIntoBuckets(img, imgArr2, depth - 1);
    }

    public static void medianCutQuantize(BufferedImage img, List<int[]> imgArr) {
        // Calculate the average RGB values
        double rAvg = 0;
        double gAvg = 0;
        double bAvg = 0;
        for (int[] pixel : imgArr) {
            rAvg += pixel[0];
            gAvg += pixel[1];
            bAvg += pixel[2];
        }
        rAvg /= imgArr.size();
        gAvg /= imgArr.size();
        bAvg /= imgArr.size();

        // Set all pixels in the image to the average RGB value
        for (int[] pixel : imgArr) {
            img.setRGB(pixel[4], pixel[3], ((int) rAvg << 16) | ((int) gAvg << 8) | (int) bAvg);
        }
    }
}