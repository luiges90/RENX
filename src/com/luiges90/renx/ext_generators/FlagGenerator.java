package com.luiges90.renx.ext_generators;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class FlagGenerator {
    public static void main(String[] args) {
        for (int red = 0; red <= 255; red += 85) {
            for (int green = 0; green <= 255; green += 85) {
                for (int blue = 0; blue <= 255; blue += 85) {
                    if (red + green + blue <= 100) continue;
                    generateImage(red, green, blue, 410, 256, "flag");
                    generateImage(red, green, blue, 256, 256, "crest");
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
