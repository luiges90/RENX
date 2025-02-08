package com.luiges90.renx.ext_generators;

import com.luiges90.renx.Util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;

public class FlagGenerator {
    public static final int[] COLOR_VALUES = {0, 64, 128, 192, 255};

    public static void main(String[] args) {
        Util.deleteFileInDirectory("graphics/factions");
        List<String> colorList = new ArrayList<>();
        for (int i : COLOR_VALUES) {
            for (int j : COLOR_VALUES) {
                for (int k : COLOR_VALUES) {
                    if (i + j + k <= 128) continue;
                    generateImage(i, j, k);
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

    private static void generateImage(int red, int green, int blue) {
        Random rng = new Random();

        BufferedImage flagImage = new BufferedImage(410, 256, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = flagImage.createGraphics();
        g.setColor(new Color(red, green, blue));
        g.fillRect(0, 0, 410, 256);

        int cnt = rng.nextInt(4);
        for (int i = 0; i < cnt; ++i) {
            g.setColor(new Color(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256)));
            g.setStroke(new BasicStroke(rng.nextInt(100) + 20));
            if (rng.nextBoolean()) {
                g.drawLine(rng.nextInt(410), 0, rng.nextInt(410), 256);
            } else {
                g.drawLine(0, rng.nextInt(256), 410, rng.nextInt(256));
            }
        }
        int sides = rng.nextInt(10);
        if (sides >= 3) {
            float rotate = rng.nextFloat() * 2 * (float) Math.PI;
            int sizeLarge = rng.nextInt(192);
            int sizeSmall = rng.nextInt(192);
            if (sizeSmall > sizeLarge) {
                int tmp = sizeSmall;
                sizeSmall = sizeLarge;
                sizeLarge = tmp;
            }
            int[] x = new int[sides * 2];
            int[] y = new int[sides * 2];
            for (int i = 0; i < sides * 2; i += 2) {
                x[i] = 205 + (int) (sizeLarge * Math.cos(2 * Math.PI * i / sides + rotate));
                y[i] = 128 + (int) (sizeLarge * Math.sin(2 * Math.PI * i / sides + rotate));
                x[i + 1] = 205 + (int) (sizeSmall * Math.cos(2 * Math.PI * (i + 1) / sides + rotate));
                y[i + 1] = 128 + (int) (sizeSmall * Math.sin(2 * Math.PI * (i + 1) / sides + rotate));
            }
            g.setColor(new Color(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256)));
            g.setStroke(new BasicStroke(rng.nextInt(50) + 10));
            if (rng.nextBoolean()) {
                g.fillPolygon(x, y, sides * 2);
            } else {
                g.drawPolygon(x, y, sides * 2);
            }
        }

        g.dispose();

        BufferedImage crestImage = flagImage.getSubimage(77, 0, 256, 256);

        // Save the image to the file
        try {
            File flagFile = new File(String.format("graphics/factions/renx_%s_%02X%02X%02X.png", "flag", red, green, blue));
            ImageIO.write(flagImage, "png", flagFile);

            File crestFile = new File(String.format("graphics/factions/renx_%s_%02X%02X%02X.png", "crest", red, green, blue));
            ImageIO.write(crestImage, "png", crestFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
