package com.luiges90.renx;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import lunalib.lunaSettings.LunaSettings;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Util {
    public static boolean getBoolSetting(String name, boolean defaultValue) {
        boolean result = defaultValue;
        if (Global.getSettings().getModManager().isModEnabled("lunalib"))
        {
            result = LunaSettings.getBoolean("renx", name);
        }
        return result;
    }

    public static int getIntSetting(String name, int defaultValue) {
        int result = defaultValue;
        if (Global.getSettings().getModManager().isModEnabled("lunalib"))
        {
            result = LunaSettings.getInt("renx", name);
        }
        return result;
    }

    public static double getDoubleSetting(String name, double defaultValue) {
        double result = defaultValue;
        if (Global.getSettings().getModManager().isModEnabled("lunalib"))
        {
            result = LunaSettings.getDouble("renx", name);
        }
        return result;
    }

    public static double getPersistedDoubleSetting(boolean newGame, String name, double defaultValue) {
        if (newGame) {
            double value = getDoubleSetting(name, defaultValue);
            Global.getSector().getPersistentData().put(name, value);
            return value;
        } else {
            return (double) Global.getSector().getPersistentData().get(name);
        }
    }

    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder titleCase = new StringBuilder();
        boolean nextTitleCase = true;

        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            } else {
                c = Character.toLowerCase(c);
            }

            titleCase.append(c);
        }

        return titleCase.toString();
    }

    public static <T> T randomFrom(Random random, T[] array) {
        return array[random.nextInt(array.length)];
    }

    public static void deleteFileInDirectory(String path) {
        Path directory = Paths.get(path);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path entry : stream) {
                Files.delete(entry);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getAllBaseHullsWithTag(String tag) {
        List<ShipHullSpecAPI> ships = Global.getSettings().getAllShipHullSpecs();
        ArrayList<String> result = new ArrayList<String>();
        for (ShipHullSpecAPI ship : ships) {
            if (ship.hasTag(tag)) {
                result.add(ship.getBaseHullId());
            }
        }
        return result;
    }

    public static List<String> getAllFightersWithTag(String tag) {
        List<FighterWingSpecAPI> fighters = Global.getSettings().getAllFighterWingSpecs();
        ArrayList<String> result = new ArrayList<String>();
        for (FighterWingSpecAPI fighter : fighters) {
            if (fighter.hasTag(tag)) {
                result.add(fighter.getId());
            }
        }
        return result;
    }

    public static List<String> getAllWeaponsWithTag(String tag) {
        List<WeaponSpecAPI> weapons = Global.getSettings().getAllWeaponSpecs();
        ArrayList<String> result = new ArrayList<String>();
        for (WeaponSpecAPI weapon : weapons) {
            if (weapon.hasTag(tag)) {
                result.add(weapon.getWeaponId());
            }
        }
        return result;
    }
}
