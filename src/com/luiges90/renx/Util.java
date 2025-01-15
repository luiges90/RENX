package com.luiges90.renx;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

import java.nio.ByteBuffer;
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
