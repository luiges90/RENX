package com.luiges90.renx;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionSpecAPI;
import exerelin.campaign.alliances.Alliance;
import exerelin.utilities.NexConfig;
import exerelin.utilities.NexFactionConfig;

import java.awt.*;
import java.util.Random;

public class RenxPlugin extends BaseModPlugin {
    public static final long SEED = 473208952;
    public static final int[] COLOR_VALUES = {0, 85, 170, 255};

    @Override
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();

        Random rng = new Random(SEED);

        int index = 0;
        while (true) {
            FactionSpecAPI spec = Global.getSettings().getFactionSpec("renx_faction" + index);
            if (spec == null) break;

            var color = new int[] {
                COLOR_VALUES[rng.nextInt(COLOR_VALUES.length)],
                COLOR_VALUES[rng.nextInt(COLOR_VALUES.length)],
                COLOR_VALUES[rng.nextInt(COLOR_VALUES.length)]
            };
            if (color[0] + color[1] + color[2] <= 100) continue;

            setNameAndColor(spec, color, rng);

            FactionAPI faction = Global.getSector().getFaction("renx_faction" + index);

            var hullTag = addHulls(rng, faction);
            addFighters(hullTag, faction);
            addWeapons(faction, hullTag);

            setDoctrine(faction, rng, hullTag);

            NexFactionConfig config = NexConfig.getFactionConfig("renx_faction" + index);

            config.pirateFaction = rng.nextFloat() < 0.2f;
            var alignments = config.getAlignments();
            alignments.get(Alliance.Alignment.CORPORATE).modifyFlat("renx", rng.nextInt(5) * 0.5f - 1);
            alignments.get(Alliance.Alignment.TECHNOCRATIC).modifyFlat("renx", rng.nextInt(5) * 0.5f - 1);
            alignments.get(Alliance.Alignment.HIERARCHICAL).modifyFlat("renx", rng.nextInt(5) * 0.5f - 1);
            alignments.get(Alliance.Alignment.MILITARIST).modifyFlat("renx", rng.nextInt(5) * 0.5f - 1);
            alignments.get(Alliance.Alignment.DIPLOMATIC).modifyFlat("renx", rng.nextInt(5) * 0.5f - 1);
            alignments.get(Alliance.Alignment.IDEOLOGICAL).modifyFlat("renx", rng.nextInt(5) * 0.5f - 1);

            index++;
        }
    }

    private static void setDoctrine(FactionAPI faction, Random rng, String hullTag) {
        var doctrine = faction.getDoctrine();
        doctrine.setAggression(rng.nextInt(5) + 1);
        doctrine.setNumShips(rng.nextInt(5) + 1);
        doctrine.setAutofitRandomizeProbability(rng.nextFloat());

        int phaseShips = hullTag.equals("hightech_bp") ? rng.nextInt(3) : 0;
        int warships = rng.nextInt(3) + 3 - phaseShips;
        int carriers = 7 - warships;
        doctrine.setPhaseShips(phaseShips);
        doctrine.setWarships(warships);
        doctrine.setCarriers(carriers);

        int shipQuality = rng.nextInt(3) + 1;
        int officerQuality = rng.nextInt(3) + 1;
        int numShips = 7 - shipQuality - officerQuality;
        doctrine.setShipQuality(shipQuality);
        doctrine.setOfficerQuality(officerQuality);
        doctrine.setNumShips(numShips);

        doctrine.setCombatFreighterProbability(rng.nextFloat());
        doctrine.setCombatFreighterCombatUseFraction(rng.nextFloat());
        doctrine.setCombatFreighterCombatUseFractionWhenPriority(rng.nextFloat());

        doctrine.setCommanderSkillsShuffleProbability(rng.nextFloat());
    }

    private static void addWeapons(FactionAPI faction, String hullTag) {
        var baseWeapons = Util.getAllWeaponsWithTag("base_bp");
        for (var baseWeapon : baseWeapons) {
            faction.addKnownWeapon(baseWeapon, true);
        }
        var baseTechWeapons = Util.getAllWeaponsWithTag(hullTag);
        for (var baseWeapon : baseTechWeapons) {
            faction.addKnownWeapon(baseWeapon, true);
        }
        var missileWeapons = Util.getAllWeaponsWithTag("missile_bp");
        for (var missileWeapon : missileWeapons) {
            faction.addKnownWeapon(missileWeapon, true);
        }
        if (hullTag.equals("pirates")) {
            var pirateWeapons = Util.getAllWeaponsWithTag("pirate_bp");
            for (var pirateWeapon : pirateWeapons) {
                faction.addKnownWeapon(pirateWeapon, true);
            }
        }
        String[] weaponList;
        switch (hullTag) {
            case "lowtech_bp":
                weaponList = new String[]{
                        "lightmg",
                        "lightdualmg",
                        "vulcan",
                        "lightac",
                        "lightdualac",
                        "lightag",
                        "lightneedler",
                        "railgun",

                        "heavymortar",
                        "chaingun",
                        "heavyac",
                        "hveldriver",
                        "heavymauler",
                        "heavyneedler",
                        "flak",
                        "dualflak",

                        "gauss",
                        "hephag",
                        "mark9",
                        "devastator",
                        "multineedler",


                        "reaper",
                        "atropos",
                        "atropos_single",
                        "swarmer",
                        "annihilator",
                        "heatseeker",
                        "harpoon",
                        "harpoon_single",
                        "breach",
                        "sabot",
                        "sabot_single",

                        "pilum",
                        "breachpod",
                        "harpoonpod",
                        "sabotpod",
                        "phasecl",
                        "typhoon",

                        "cyclone",
                        "hurricane",
                        "squall",
                        "locust",

                        "taclaser",
                        "pdlaser",
                        "lrpdlaser",
                        "ioncannon",
                        "gravitonbeam",
                        "pulselaser",
                        "phasebeam",

                        "shredder",
                        "heavymortar",
                        "arbalest",
                        "hellbore",

                        "hammer",
                        "hammer_single",
                        "jackhammer",
                        "salamanderpod",
                        "annihilatorpod",
                        "pilum_large",
                        "hammerrack",
                        "mininglaser",
                        "miningblaster"};
                break;
            case "midline_bp":
                weaponList = new String[]{
                        "gorgon",
                        "gorgonpod",
                        "gazer",
                        "gazerpod",
                        "dragon",
                        "dragonpod",
                        "hydra",
                        "lightmg",
                        "lightdualmg",
                        "lightac",
                        "lightdualac",
                        "lightag",
                        "heavymg",
                        "flak",
                        "heavymauler",
                        "heavyac",
                        "mjolnir",
                        "pdburst",
                        "pdlaser",
                        "lrpdlaser",
                        "taclaser",
                        "ioncannon",
                        "phasebeam",
                        "gravitonbeam",
                        "irautolance",
                        "heavyburst",
                        "ionbeam",
                        "guardian",
                        "hil",

                        "vulcan",
                        "railgun",
                        "heavymortar",
                        "chaingun",
                        "dualflak",
                        "hephag",
                        "mark9",
                        "devastator",
                        "reaper",
                        "atropos",
                        "atropos_single",
                        "swarmer",
                        "annihilator",
                        "heatseeker",
                        "harpoon",
                        "harpoon_single",
                        "breach",
                        "sabot",
                        "sabot_single",
                        "pilum",
                        "breachpod",
                        "harpoonpod",
                        "sabotpod",
                        "phasecl",
                        "typhoon",
                        "squall",
                        "locust",
                        "irpulse",
                        "pulselaser",
                        "autopulse",
                };
                break;
            case "hightech_bp":
                weaponList = new String[] {
                        "pdlaser",
                        "taclaser",
                        "ioncannon",
                        "irpulse",
                        "lrpdlaser",
                        "pdburst",
                        "amblaster",
                        "phasebeam",
                        "gravitonbeam",
                        "pulselaser",
                        "heavyblaster",
                        "heavyburst",
                        "ionpulser",
                        "ionbeam",
                        "plasma",
                        "hil",
                        "autopulse",
                        "guardian",
                        "tachyonlance",
                        "reaper",
                        "atropos",
                        "atropos_single",
                        "swarmer",
                        "annihilator",
                        "heatseeker",
                        "harpoon",
                        "harpoon_single",
                        "breach",
                        "sabot",
                        "sabot_single",
                        "harpoonpod",
                        "breachpod",
                        "sabotpod",
                        "salamanderpod",
                        "phasecl",
                        "typhoon",
                        "cyclone",
                        "hurricane",
                        "squall",
                        "locust",
                        "railgun",
                        "lightneedler",
                        "hveldriver",
                        "heavyneedler",
                        "mjolnir",
                        "multineedler",
                };
            default:
                weaponList = new String[]{
                        "hurricane",
                        "squall",
                        "locust",
                };
                break;
        }
        for (var weapon : weaponList) {
            faction.addKnownWeapon(weapon, true);
        }
    }

    private static void addFighters(String hullTag, FactionAPI faction) {
        var baseFighters = Util.getAllFightersWithTag(hullTag);
        for (var baseFighter : baseFighters) {
            faction.addKnownFighter(baseFighter, true);
        }
        String[] fighterList;
        switch (hullTag) {
            case "lowtech_bp":
                fighterList = new String[]{
                        "talon_wing",
                        "broadsword_wing",
                        "warthog_wing",
                        "piranha_wing",
                        "hoplon_wing",
                        "longbow_wing",
                        "perdition_wing",
                };
                break;
            case "midline_bp":
                fighterList = new String[]{
                        "thunder_wing",
                        "gladius_wing",
                        "talon_wing",
                        "claw_wing",
                };
                break;
            case "hightech_bp":
                fighterList = new String[]{
                        "broadsword_wing",
                        "dagger_wing",
                        "trident_wing",
                        "wasp_wing",
                        "xyphos_wing",
                        "claw_wing",
                        "trident_wing",
                        "cobra_wing",
                        "longbow_wing",
                };
                break;
            default:
                fighterList = new String[]{};
                break;
        }
        for (var fighter : fighterList) {
            faction.addKnownFighter(fighter, true);
        }
    }

    private static String addHulls(Random rng, FactionAPI faction) {
        var hullTags = new String[]{"lowtech_bp", "midline_bp", "hightech_bp", "pirate_bp"};
        var hullTag = Util.from(rng, hullTags);
        var baseHulls = Util.getAllBaseHullsWithTag("base_bp");
        for (var baseHull : baseHulls) {
            faction.addKnownShip(baseHull, true);
        }
        var baseTechHulls = Util.getAllBaseHullsWithTag(hullTag);
        for (var baseHull : baseTechHulls) {
            faction.addKnownShip(baseHull, true);
        }
        faction.addKnownShip("valkyrie", true);
        faction.addKnownShip("prometheus", true);
        faction.addKnownShip("atlas", true);
        String[] hullList;
        switch (hullTag) {
            case "lowtech_bp":
                hullList = new String[]{
                        "manticore",
                        "eradicator",
                        "onslaught",
                        "retribution",
                        "legion",
                        "invictus"};
                break;
            case "midline_bp":
                hullList = new String[]{
                        "monitor",
                        "eagle",
                        "champion",
                        "gryphon",
                        "conquest",
                        "pegasus"};
                break;
            case "hightech_bp":
                hullList = new String[]{
                        "omen",
                        "tempest",
                        "hyperion",
                        "scarab",
                        "medusa",
                        "apogee",
                        "aurora",
                        "astral",
                        "odyssey",
                        "paragon",
                        "shade",
                        "afflictor",
                        "harbinger",
                        "doom",
                        "phantom",
                        "revenant"};
                break;
            case "pirate_bp":
                hullList = new String[]{
                        "vanguard_pirates",
                        "manticore_pirates",
                        "falcon_p",
                        "eradicator_pirates",
                        "atlas2",};
                break;
            default:
                hullList = new String[]{};
                break;
        }
        for (var hull : hullList) {
            faction.addKnownShip(hull, true);
        }
        return hullTag;
    }

    private static void setNameAndColor(FactionSpecAPI spec, int[] color, Random rng) {
        spec.setColor(new Color(color[0], color[1], color[2]));
        String colorStr = String.format("%02X%02X%02X", color[0], color[1], color[2]);
        spec.setCrest("graphics/factions/renx_crest_" + colorStr + ".png");
        spec.setLogo("graphics/factions/renx_flag_" + colorStr + ".png");

        String name = Util.toTitleCase(WordGenerator.generateWord(rng));
        spec.setDisplayName(name);
        spec.setShipNamePrefix(String.format("%s%s%s", name.charAt(0), name.charAt(1), name.charAt(2)).toUpperCase());
    }

}
