package com.luiges90.renx.ext_generators;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class FlagGenerator {
    public static final int[] COLOR_VALUES = {0, 85, 170, 255};

    public static void main(String[] args) {
        for (int i : COLOR_VALUES) {
            for (int j : COLOR_VALUES) {
                for (int k : COLOR_VALUES) {
                    if (i + COLOR_VALUES[j] + COLOR_VALUES[k] <= 100) continue;
                    generateImage(i, COLOR_VALUES[j], COLOR_VALUES[k], 410, 256, "flag");
                    generateImage(i, COLOR_VALUES[j], COLOR_VALUES[k], 256, 256, "crest");
                }
            }
        }
    }

    private static void generateImage(int red, int green, int blue, int width, int height, String filePrefix) {
        BufferedImage flagImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = flagImage.createGraphics();
        g.setColor(new Color(red, green, blue));
        g.fillRect(0, 0, width, height);
        g.dispose();

        // Prepare file name
        String filename = String.format("graphics/factions/renx_%s_%02X%02X%02X.png", filePrefix, red, green, blue);
        File file = new File(filename);

        // Save the image to the file
        try {
            ImageIO.write(flagImage, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
