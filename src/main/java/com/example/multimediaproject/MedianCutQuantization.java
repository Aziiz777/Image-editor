package com.example.multimediaproject;

import javafx.util.Pair;

import java.awt.image.BufferedImage;
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
            for (int rindex = 0; rindex < image.getHeight(); rindex++) {
                for (int cindex = 0; cindex < image.getWidth(); cindex++) {
                    int color = image.getRGB(cindex, rindex);
                    int r = (color & 0xff0000) >> 16;
                    int g = (color & 0x00ff00) >> 8;
                    int b = (color & 0x0000ff);
                    flattenedImgArray.add(new int[] {r, g, b, rindex, cindex});
                }
            }

            // Call the splitIntoBuckets function to quantize the image
            splitIntoBuckets(image, flattenedImgArray, 6);


            //remove .jpg
            path = path.substring(0, path.length() - 4);
            // Save the quantized image
            ImageIO.write(image, "jpg", new File(path + "quantized.jpg"));

            System.out.println("Quantization completed.");

            return new Pair<>(path + "quantized.jpg", image);

        } catch (IOException e) {
            System.out.println("errrror"+e);
            e.printStackTrace();
            return new Pair<>("Quantization failed.", null);
        }
    }
//    public static void main(String[] args) throws IOException {
//        // Read in the image
//        File input = new File("C:\\Users\\Laptop Syria\\Pictures\\girl.jpg");
//        BufferedImage sampleImg = ImageIO.read(input);
//
//        // Create a flattened array of the image pixels (RGB values and x, y locations)
//        List<int[]> flattenedImgArray = new ArrayList<>();
//        for (int rindex = 0; rindex < sampleImg.getHeight(); rindex++) {
//            for (int cindex = 0; cindex < sampleImg.getWidth(); cindex++) {
//                int color = sampleImg.getRGB(cindex, rindex);
//                int r = (color & 0xff0000) >> 16;
//                int g = (color & 0x00ff00) >> 8;
//                int b = (color & 0x0000ff);
//                flattenedImgArray.add(new int[] {r, g, b, rindex, cindex});
//            }
//        }
//
//        // Call the splitIntoBuckets function to quantize the image
//        splitIntoBuckets(sampleImg, flattenedImgArray, 6);
//
//        // Save the quantized image
//        File output = new File("girl_reduced_128_colors.jpg");
//        ImageIO.write(sampleImg, "jpg", output);
//    }

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
        int spaceWithHighestRange = 0;
        if (gRange >= rRange && gRange >= bRange) {
            spaceWithHighestRange = 1;
        } else if (bRange >= rRange && bRange >= gRange) {
            spaceWithHighestRange = 2;
        } else if (rRange >= bRange && rRange >= gRange) {
            spaceWithHighestRange = 0;
        }

        // Sort the pixels based on the selected color space and find the median
        int finalSpaceWithHighestRange = spaceWithHighestRange;
        imgArr.sort((p1, p2) -> Integer.compare(p1[finalSpaceWithHighestRange],p2[finalSpaceWithHighestRange]));
        int medianIndex = imgArr.size() / 2;

        // Split into two buckets on either side of the median and recurse
        List<int[]> imgArr1 = imgArr.subList(0, medianIndex);
        List<int[]> imgArr2 = imgArr.subList(medianIndex, imgArr.size());
        splitIntoBuckets(img, imgArr1, depth - 1);
        splitIntoBuckets(img, imgArr2, depth - 1);
    }
}