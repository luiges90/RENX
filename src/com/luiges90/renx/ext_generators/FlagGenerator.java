package com.luiges90.renx.ext_generators;

import com.luiges90.renx.Util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class FlagGenerator {
    public static final int[] COLOR_VALUES = {0, 85, 170, 255};

    public static void main(String[] args) {
        Util.deleteFileInDirectory("graphics/factions");
        List<String> colorList = new ArrayList<>();
        for (int i : COLOR_VALUES) {
            for (int j : COLOR_VALUES) {
                for (int k : COLOR_VALUES) {
                    if (i + j + k <= 100) continue;
                    generateImage(i, j, k, 410, 256, "flag");
                    generateImage(i, j, k, 256, 256, "crest");
                    colorList.add(String.format("%02X%02X%02X", i, j, k));
                }
            }
        }

        try {
            String template = new String(Files.readAllBytes(Paths.get("data-template/settings.json")));
            try (FileWriter writer = new FileWriter("data/config/settings.json")) {
                StringBuilder sb = new StringBuilder();
                for (String color : colorList) {
                    sb.append("\"").append("flag_").append(color).append("\":\"").append("graphics/factions/renx_flag_").append(color).append(".png\",\n");
                    sb.append("\"").append("crest_").append(color).append("\":\"").append("graphics/factions/renx_crest_").append(color).append(".png\",\n");
                }
                writer.write(template.replace("{{flags}}", sb.toString()));
            }
        } catch (IOException e) {
            e.printStackTrace();
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
