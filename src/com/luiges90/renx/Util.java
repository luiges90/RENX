package com.luiges90.renx;

import com.fs.starfarer.api.Global;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Util {
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

    public static <T> T from(Random random, T[] array) {
        return array[random.nextInt(array.length)];
    }

    public static List<String> getAllBaseHullsWithTag(String tag) {
        var ships = Global.getSettings().getAllShipHullSpecs();
        var result = new ArrayList<String>();
        for (var ship : ships) {
            if (ship.hasTag(tag)) {
                result.add(ship.getBaseHullId());
            }
        }
        return result;
    }

    public static List<String> getAllFightersWithTag(String tag) {
        var fighters = Global.getSettings().getAllFighterWingSpecs();
        var result = new ArrayList<String>();
        for (var fighter : fighters) {
            if (fighter.hasTag(tag)) {
                result.add(fighter.getId());
            }
        }
        return result;
    }

    public static List<String> getAllWeaponsWithTag(String tag) {
        var weapons = Global.getSettings().getAllWeaponSpecs();
        var result = new ArrayList<String>();
        for (var weapon : weapons) {
            if (weapon.hasTag(tag)) {
                result.add(weapon.getWeaponId());
            }
        }
        return result;
    }
}
