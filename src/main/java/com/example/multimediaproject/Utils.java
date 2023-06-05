package com.example.multimediaproject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static int[][][] buildHistogram(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][][] histogram = new int[256][256][256]; // 3D array for RGB color space

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();

                histogram[r][g][b]++;
            }
        }

        return histogram;
    }

    public static int[] transformIntoIntColors(int[][][] histogram){
        Color[] colors = transformIntoColors(histogram);
        int[] intColors = new int[colors.length];
        for (int i = 0; i < colors.length; i++) {
            intColors[i] = colors[i].getRGB();
        }
        return intColors;
    }

    public static Color[] transformIntoColors(int[][][] histogram){
        List<Color> colors = new ArrayList<>();
        for(int r = 0; r < histogram.length; r++){
            for (int g = 0; g < histogram[r].length; g++){
                for (int b = 0; b < histogram[r][g].length; b++){
                    if(histogram[r][g][b] > 0){
                        Color color = new Color(r,g,b);
                        colors.add(color);
                    }
                }
            }
        }
        Color[] colorsArray = new Color[colors.size()];
        colorsArray = colors.toArray(colorsArray);
        return colorsArray;
    }

}