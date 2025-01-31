package com.luiges90.renx.ext_generators;

import com.luiges90.renx.Util;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FactionTemplateGenerator {
    public static final int FACTION_COUNT = 12;

    public static void main(String[] args) {
        Util.deleteFileInDirectory("data/world/factions");
        writeFactionList();
        writeFaction();

        Util.deleteFileInDirectory("data/config/exerelinFactionConfig");
        writeNexFactionList();
        writeNexFactionConfig();
    }

    private static void writeFactionList() {
        try (FileWriter writer = new FileWriter("data/world/factions/factions.csv")) {
            writer.write("faction" + "\n");
            for (int i = 0; i < FACTION_COUNT; ++i) {
                writer.write("data/world/factions/renx_faction" + i + ".faction\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeNexFactionList() {
        try (FileWriter writer = new FileWriter("data/config/exerelinFactionConfig/mod_factions.csv")) {
            writer.write("faction" + "\n");
            for (int i = 0; i < FACTION_COUNT; ++i) {
                writer.write("renx_faction" + i + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeFaction() {
        try {
            String template = new String(Files.readAllBytes(Paths.get("data-template/faction.json")));
            for (int i = 0; i < FACTION_COUNT; ++i) {
                try (FileWriter writer = new FileWriter("data/world/factions/renx_faction" + i + ".faction")) {
                    writer.write(
                            template
                                    .replace("{{id}}", "renx_faction" + i)
                                    .replace("{{index}}", i + "")
                    );
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeNexFactionConfig() {
        try {
            String template = new String(Files.readAllBytes(Paths.get("data-template/nex_faction_config.json")));
            for (int i = 0; i < FACTION_COUNT; ++i) {
                try (FileWriter writer = new FileWriter("data/config/exerelinFactionConfig/renx_faction" + i + ".json")) {
                    writer.write(template);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
