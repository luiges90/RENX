package com.luiges90.renx;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.FactionSpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.MutableStat;
import exerelin.campaign.alliances.Alliance;
import exerelin.utilities.NexConfig;
import exerelin.utilities.NexFactionConfig;
import org.json.JSONException;

import java.awt.*;
import java.util.*;
import java.util.List;

public class RenxPlugin extends BaseModPlugin {
    public static final int[] COLOR_VALUES = {0, 64, 128, 192, 255};

    @Override
    public void onGameLoad(boolean newGame) {
        super.onGameLoad(newGame);

        String seedStr = Global.getSector().getSeedString();
        Random rng = new Random(Long.parseLong(seedStr.substring(3)));

        Map<String, List<MarketAPI>> factionMarkets = new HashMap<>();
        if (newGame) {
            List<MarketAPI> markets = Global.getSector().getEconomy().getMarketsCopy();
            for (MarketAPI market : markets) {
                String marketFaction = market.getFaction().getFactionSpec().getId();
                if (factionMarkets.containsKey(marketFaction)) {
                    factionMarkets.get(marketFaction).add(market);
                } else {
                    factionMarkets.put(marketFaction, new ArrayList<>(Collections.singletonList(market)));
                }
            }
        }

        Set<Integer[]> selectedColors = new HashSet<>();
        int index = 0;
        while (true) {
            FactionSpecAPI spec = Global.getSettings().getFactionSpec("renx_faction" + index);
            if (spec == null) break;

            FactionAPI faction = Global.getSector().getFaction("renx_faction" + index);

            // If color selection changed, synchronize with FlagGenerator
            int[] color = new int[] {
                    COLOR_VALUES[rng.nextInt(COLOR_VALUES.length)],
                    COLOR_VALUES[rng.nextInt(COLOR_VALUES.length)],
                    COLOR_VALUES[rng.nextInt(COLOR_VALUES.length)]
            };
            if (color[0] + color[1] + color[2] <= 100) continue;
            if (selectedColors.contains(new Integer[]{color[0], color[1], color[2]})) continue;
            selectedColors.add(new Integer[]{color[0], color[1], color[2]});
            setColor(spec, color);

            setFactionName(newGame, spec, factionMarkets, faction);
            setNameAndVoices(spec, rng);

            try {
                spec.getCustom().put("AICoreValueMult", rng.nextInt(4) * 0.5f + 0.5f);
                spec.getCustom().put("AICoreRepMult", rng.nextInt(4) * 0.5f + 0.5f);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String[] hullTags = new String[]{"lowtech_bp", "midline_bp", "hightech_bp", "pirate_bp"};
            String hullTag = Util.randomFrom(rng, hullTags);
            addHulls(faction, hullTag, rng);
            addFighters(faction, hullTag, rng);
            addWeapons(faction, hullTag, rng);

            setDoctrine(faction, rng, hullTag);
            NexFactionConfig config = NexConfig.getFactionConfig("renx_faction" + index);

            setNexFactionValues(config, rng, faction);

            setDefenceStations(config, hullTag, rng);
            setBonusSeeds(config, rng);

            setAlignments(config, rng, index);

            if (config.pirateFaction) {
                setPirateMusic(spec);
            }

            index++;
        }

        if (newGame) {
            setInitialPirateRelations();
        }
        setNextDiplomaticValues(index, rng);

        buffPirates();

        Global.getSector().addScript(new DiplomaticRelationScript());
    }

    private static void setNameAndVoices(FactionSpecAPI spec, Random rng) {
        Integer[] nameWeight = new Integer[]{0, 0, 0, 0, 1, 1, 2, 3};
        for (int i = 0; i < spec.getNameCategories().getItems().size(); ++i) {
            spec.getNameCategories().setWeight(i, Util.randomFrom(rng, nameWeight));
        }
        for (int i = 0; i < spec.getShipNameSources().getItems().size(); ++i) {
            spec.getShipNameSources().setWeight(i, Util.randomFrom(rng, nameWeight));
        }
        nameWeight = new Integer[]{0, 0, 1, 1, 2};
        for (int i = 0; i < spec.getVoicePickerLow().getItems().size(); ++i) {
            spec.getVoicePickerLow().setWeight(i, Util.randomFrom(rng, nameWeight));
        }
        for (int i = 0; i < spec.getVoicePickerMedium().getItems().size(); ++i) {
            spec.getVoicePickerMedium().setWeight(i, Util.randomFrom(rng, nameWeight));
        }
        for (int i = 0; i < spec.getVoicePickerHigh().getItems().size(); ++i) {
            spec.getVoicePickerHigh().setWeight(i, Util.randomFrom(rng, nameWeight));
        }
    }

    private static void setFactionName(boolean newGame, FactionSpecAPI spec, Map<String, List<MarketAPI>> factionMarkets, FactionAPI faction) {
        String seedStr = Global.getSector().getSeedString();
        Random rng = new Random(Long.parseLong(seedStr.substring(3)));

        if (newGame) {
            String name = spec.getId();
            List<MarketAPI> markets = factionMarkets.get(faction.getId());

            if (markets != null) {
                MarketAPI largestMarket;
                int size = 0;
                for (MarketAPI market : markets) {
                    if (market.getSize() > size) {
                        size = market.getSize();
                        largestMarket = market;
                        name = largestMarket.getName();
                    }
                }

                char last = name.charAt(name.length() - 1);
                if (last == 'a') {
                    name = name + "n";
                } else if (last == 'n') {
                    name = name + "ese";
                } else {
                    name = name + "an";
                }

                String name2 = Util.randomFrom(rng, POLITIES);
                setName(spec, name, name2);

                MemoryAPI memory = faction.getMemoryWithoutUpdate();
                memory.set("$renx_faction_name", name);
                memory.set("$renx_faction_name2", name2);
            }
        } else {
            setName(spec, faction.getMemoryWithoutUpdate().getString("$renx_faction_name"), faction.getMemoryWithoutUpdate().getString("$renx_faction_name2"));
        }
    }

    private static void setInitialPirateRelations() {
        int index;
        index = 0;
        while (true) {
            FactionAPI faction = Global.getSector().getFaction("renx_faction" + index);
            if (faction == null) break;

            NexFactionConfig config = NexConfig.getFactionConfig("renx_faction" + index);
            boolean thisPirate = config.pirateFaction;

            List<FactionAPI> allFactions = Global.getSector().getAllFactions();
            for (FactionAPI f : allFactions) {
                boolean otherPirate = NexConfig.getFactionConfig(f.getId()).pirateFaction;

                if (thisPirate != otherPirate) {
                    faction.setRelationship(f.getId(), -0.6f);
                }
            }

            index++;
        }
    }

    private static void buffPirates() {
        for (FactionAPI faction: Global.getSector().getAllFactions()) {
            NexFactionConfig config = NexConfig.getFactionConfig(faction.getId());
            if (config.pirateFaction) {
                if (config.groundBattleSettings == null) {
                    config.groundBattleSettings = new HashMap<>();
                    config.groundBattleSettings.put("defenseMult", 2f);
                }
                else if (!config.groundBattleSettings.containsKey("defenseMult"))
                {
                    config.groundBattleSettings.put("defenseMult", 2f);
                }
                else
                {
                    config.groundBattleSettings.put("defenseMult", (float) config.groundBattleSettings.get("defenseMult") * 2f);
                }
            }

            List<MarketAPI> markets = Global.getSector().getEconomy().getMarketsCopy();
            for (MarketAPI market : markets) {
                String marketFaction = market.getFaction().getFactionSpec().getId();
                boolean isPirate = NexConfig.getFactionConfig(marketFaction).pirateFaction;

                if (isPirate) {
                    MarketAPI mutMarket = Global.getSector().getEconomy().getMarket(market.getId());
                    mutMarket.getAccessibilityMod().modifyFlat("renx_pirate_access_buff", 0.5f);
                }
            }
        }
    }

    private static void setPirateMusic(FactionSpecAPI spec) {
        spec.getMusicMap().put("theme", "music_pirates_market_neutral");
        spec.getMusicMap().put("market_neutral", "music_pirates_market_neutral");
        spec.getMusicMap().put("market_hostile", "music_pirates_market_hostile");
        spec.getMusicMap().put("market_friendly", "music_pirates_market_friendly");
        spec.getMusicMap().put("encounter_neutral", "music_pirates_encounter_neutral");
        spec.getMusicMap().put("encounter_hostile", "music_pirates_encounter_hostile");
        spec.getMusicMap().put("encounter_friendly", "music_pirates_encounter_friendly");
    }

    private static void setNextDiplomaticValues(int index, Random rng) {
        for (int i = 0; i < index; ++i) {
            NexFactionConfig config = NexConfig.getFactionConfig("renx_faction" + index);

            for (int j = 0; j < index; ++j) {
                if (i == j) continue;

                if (rng.nextFloat() < 0.2f) {
                    int degree = rng.nextInt(4);
                    float rel = degree * 0.2f + 0.1f;
                    config.startRelationships.put("renx_faction" + j, rel);
                    Global.getSector().getFaction("renx_faction" + i).setRelationship("renx_faction" + j, rel);
                    config.minRelationships.put("renx_faction" + j, degree * 0.25f - 1f);
                    config.diplomacyPositiveChance.put("renx_faction" + j, rng.nextFloat() * 0.4f + 0.8f + degree * 0.2f);
                    config.diplomacyNegativeChance.put("renx_faction" + j, rng.nextFloat() * 0.4f + 0.8f - degree * 0.1f);
                } else if (rng.nextFloat() < 0.25f) {
                    int degree = rng.nextInt(4);
                    float rel = -degree * 0.2f - 0.1f;
                    config.startRelationships.put("renx_faction" + j, rel);
                    Global.getSector().getFaction("renx_faction" + i).setRelationship("renx_faction" + j, rel);
                    config.startRelationships.put("renx_faction" + j, rel);
                    config.maxRelationships.put("renx_faction" + j, 1 - degree * 0.25f);
                    config.diplomacyPositiveChance.put("renx_faction" + j, rng.nextFloat() * 0.4f + 0.8f - degree * 0.1f);
                    config.diplomacyNegativeChance.put("renx_faction" + j, rng.nextFloat() * 0.4f + 0.8f + degree * 0.2f);
                }
            }
        }
    }

    private static void setNexFactionValues(NexFactionConfig config, Random rng, FactionAPI faction) {
        config.pirateFaction = rng.nextFloat() < 0.3f;
        config.hostileToAll = config.pirateFaction ? 2 : 0;
        setDiplomacyTraits(config, rng);
        config.freeMarket = config.diplomacyTraits.contains("anarchist");
        config.morality = Util.randomFrom(rng, NexFactionConfig.Morality.values());
        if (!config.freeMarket) {
            setIllegalCommodities(faction, rng);
        }

        config.groundBattleSettings = new HashMap<>();
        config.groundBattleSettings.put("attackMult", rng.nextFloat() * 0.4f + 0.8f);
        config.groundBattleSettings.put("defenseMult", rng.nextFloat() * 0.4f + 0.8f);
        config.groundBattleSettings.put("moraleDamageTakenMult", rng.nextFloat() * 0.4f + 0.8f);
        config.invasionFleetSizeMod = rng.nextFloat() * 0.4f + 0.8f;
        config.responseFleetSizeMod = rng.nextFloat() * 0.4f + 0.8f;
        config.invasionPointMult = rng.nextFloat() * 0.4f + 0.8f;

        config.colonyExpeditionChance = rng.nextFloat() * 0.4f + 0.8f;
        config.specialForcesPointMult = rng.nextFloat() * 0.4f + 0.8f;
        config.specialForcesSizeMult = rng.nextFloat() * 0.4f + 0.8f;
        config.vengeanceFleetSizeMult = rng.nextFloat() * 0.4f + 0.8f;
    }

    private static void setDefenceStations(NexFactionConfig config, String hullTag, Random rng) {
        config.defenceStations.clear();
        if (hullTag.equals("lowtech_bp") || hullTag.equals("pirate_bp") || rng.nextFloat() < 0.2f) {
            config.defenceStations.add(new NexFactionConfig.DefenceStationSet(1, "orbitalstation", "battlestation", "starfortress"));
        }
        if (hullTag.equals("midline_bp") || hullTag.equals("pirate_bp") || rng.nextFloat() < 0.2f) {
            config.defenceStations.add(new NexFactionConfig.DefenceStationSet(1, "orbitalstation_mid", "battlestation_mid", "starfortress_mid"));
        }
        if (hullTag.equals("hightech_bp") || hullTag.equals("pirate_bp") || rng.nextFloat() < 0.2f) {
            config.defenceStations.add(new NexFactionConfig.DefenceStationSet(1, "orbitalstation_high", "battlestation_high", "starfortress_high"));
        }
    }

    private static void setBonusSeeds(NexFactionConfig config, Random rng) {
        if (!config.diplomacyTraits.contains("dislikes_ai") && !config.diplomacyTraits.contains("hates_ai")) {
            if (rng.nextFloat() < 0.7f) config.bonusSeeds.add(new NexFactionConfig.BonusSeed("nanoforge_pristine", 1, rng.nextFloat() * 0.5f));
            if (rng.nextFloat() < 0.7f) config.bonusSeeds.add(new NexFactionConfig.BonusSeed("synchrotron", 1, rng.nextFloat() * 0.5f));
            if (rng.nextFloat() < 0.7f) config.bonusSeeds.add(new NexFactionConfig.BonusSeed("population_upsize", 1, rng.nextFloat() * 0.5f));
        } else if (!config.diplomacyTraits.contains("likes_ai")) {
            if (rng.nextFloat() < 0.3f) config.bonusSeeds.add(new NexFactionConfig.BonusSeed("nanoforge_pristine", 1, rng.nextFloat() * 0.2f));
            if (rng.nextFloat() < 0.3f) config.bonusSeeds.add(new NexFactionConfig.BonusSeed("synchrotron", 1, rng.nextFloat() * 0.2f));
            if (rng.nextFloat() < 0.1f) config.bonusSeeds.add(new NexFactionConfig.BonusSeed("population_upsize", 1, rng.nextFloat() * 0.2f));
            if (rng.nextFloat() < 0.2f) config.bonusSeeds.add(new NexFactionConfig.BonusSeed("aiCore_heavyindustry", 1, rng.nextFloat() * 0.2f));
            if (rng.nextFloat() < 0.2f) config.bonusSeeds.add(new NexFactionConfig.BonusSeed("aiCore_fuelprod", 1, rng.nextFloat() * 0.2f));
            if (rng.nextFloat() < 0.2f) config.bonusSeeds.add(new NexFactionConfig.BonusSeed("aiCore_military", 1, rng.nextFloat() * 0.2f));
            if (rng.nextFloat() < 0.2f) config.bonusSeeds.add(new NexFactionConfig.BonusSeed("aiCore_station", 1, rng.nextFloat() * 0.2f));
            if (rng.nextFloat() < 0.2f) config.bonusSeeds.add(new NexFactionConfig.BonusSeed("aiCore_other", 1, rng.nextFloat() * 0.2f));
            if (rng.nextFloat() < 0.2f) config.bonusSeeds.add(new NexFactionConfig.BonusSeed("aiCore_any", 1, rng.nextFloat() * 0.2f));
        } else {
            if (rng.nextFloat() < 0.1f) config.bonusSeeds.add(new NexFactionConfig.BonusSeed("nanoforge_pristine", 1, rng.nextFloat() * 0.1f));
            if (rng.nextFloat() < 0.1f) config.bonusSeeds.add(new NexFactionConfig.BonusSeed("synchrotron", 1, rng.nextFloat() * 0.1f));
            if (rng.nextFloat() < 0.5f) config.bonusSeeds.add(new NexFactionConfig.BonusSeed("aiCore_heavyindustry", 1, rng.nextFloat() * 0.5f));
            if (rng.nextFloat() < 0.5f) config.bonusSeeds.add(new NexFactionConfig.BonusSeed("aiCore_fuelprod", 1, rng.nextFloat() * 0.5f));
            if (rng.nextFloat() < 0.5f) config.bonusSeeds.add(new NexFactionConfig.BonusSeed("aiCore_military", 1, rng.nextFloat() * 0.5f));
            if (rng.nextFloat() < 0.5f) config.bonusSeeds.add(new NexFactionConfig.BonusSeed("aiCore_station", 1, rng.nextFloat() * 0.5f));
            if (rng.nextFloat() < 0.5f) config.bonusSeeds.add(new NexFactionConfig.BonusSeed("aiCore_other", 1, rng.nextFloat() * 0.5f));
            if (rng.nextFloat() < 0.5f) config.bonusSeeds.add(new NexFactionConfig.BonusSeed("aiCore_any", 1, rng.nextFloat() * 0.5f));
        }
    }

    private static void setDiplomacyTraits(NexFactionConfig config, Random rng) {
        config.diplomacyTraits.clear();
        if (rng.nextFloat() < 0.15f) config.diplomacyTraits.add("paranoid");
        else if (rng.nextFloat() < 0.15f) config.diplomacyTraits.add("pacifist");
        if (rng.nextFloat() < 0.15f) config.diplomacyTraits.add("predatory");
        if (rng.nextFloat() < 0.15f) config.diplomacyTraits.add("helps_allies");
        if (rng.nextFloat() < 0.15f) config.diplomacyTraits.add("irredentist");
        if (rng.nextFloat() < 0.15f) config.diplomacyTraits.add("stalwart");
        else if (rng.nextFloat() < 0.15f) config.diplomacyTraits.add("weak-willed");
        else if (rng.nextFloat() < 0.15f) config.diplomacyTraits.add("foreverwar");
        if (rng.nextFloat() < 0.15f) config.diplomacyTraits.add("selfrighteous");
        if (rng.nextFloat() < 0.15f) config.diplomacyTraits.add("temperamental");
        if (rng.nextFloat() < 0.15f) config.diplomacyTraits.add("dislikes_ai");
        else if (rng.nextFloat() < 0.15f) config.diplomacyTraits.add("hates_ai");
        else if (rng.nextFloat() < 0.15f) config.diplomacyTraits.add("likes_ai");
        if (rng.nextFloat() < 0.15f) config.diplomacyTraits.add("envious");
        else if (rng.nextFloat() < 0.15f) config.diplomacyTraits.add("submissive");
        else if (rng.nextFloat() < 0.15f) config.diplomacyTraits.add("neutralist");
        if (rng.nextFloat() < 0.15f) config.diplomacyTraits.add("monopolist");
        if (rng.nextFloat() < 0.15f) config.diplomacyTraits.add("law_and_order");
        else if (rng.nextFloat() < 0.15f) config.diplomacyTraits.add("anarchist");
        if (rng.nextFloat() < 0.15f) config.diplomacyTraits.add("lowprofile");
        if (rng.nextFloat() < 0.15f) config.diplomacyTraits.add("devious");
    }

    private static void setAlignments(NexFactionConfig config, Random rng, int index) {
        Map<Alliance.Alignment, MutableStat> alignments = config.getAlignments();
        for (Alliance.Alignment alignment : Arrays.asList(
                Alliance.Alignment.CORPORATE, Alliance.Alignment.TECHNOCRATIC, Alliance.Alignment.HIERARCHICAL, Alliance.Alignment.MILITARIST, Alliance.Alignment.DIPLOMATIC, Alliance.Alignment.IDEOLOGICAL)) {
            alignments.get(alignment).modifyFlat("renx", rng.nextInt(5) * 0.5f - 1);
        }
        MemoryAPI mem = Global.getSector().getFaction("renx_faction" + index).getMemoryWithoutUpdate();
        mem.set(Alliance.MEMORY_KEY_ALIGNMENTS, alignments);
    }

    private static void setIllegalCommodities(FactionAPI faction, Random rng) {
        if (rng.nextFloat() < 0.9f) {
            faction.makeCommodityIllegal("ai_cores");
        }
        if (rng.nextFloat() < 0.5f) {
            faction.makeCommodityIllegal("drugs");
        }
        if (rng.nextFloat() < 0.5f) {
            faction.makeCommodityIllegal("organs");
        }
        if (rng.nextFloat() < 0.3f) {
            faction.makeCommodityIllegal("hand_weapons");
        }
    }

    private static void setDoctrine(FactionAPI faction, Random rng, String hullTag) {
        FactionDoctrineAPI doctrine = faction.getDoctrine();
        doctrine.setAggression(rng.nextInt(5) + 1);
        doctrine.setNumShips(rng.nextInt(5) + 1);
        doctrine.setAutofitRandomizeProbability(rng.nextFloat());

        int phaseShips = hullTag.equals("hightech_bp") ? rng.nextInt(3) : 0;
        int warships = rng.nextInt(2) + 4 - phaseShips;
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

    private static void addWeapons(FactionAPI faction, String hullTag, Random rng) {
        List<String> baseWeapons = Util.getAllWeaponsWithTag("base_bp");
        for (String baseWeapon : baseWeapons) {
            faction.addKnownWeapon(baseWeapon, true);
        }
        List<String> baseTechWeapons = Util.getAllWeaponsWithTag(hullTag);
        for (String baseWeapon : baseTechWeapons) {
            if (rng.nextFloat() < 0.9f) {
                faction.addKnownWeapon(baseWeapon, true);
            }
        }
        List<String> missileWeapons = Util.getAllWeaponsWithTag("missile_bp");
        for (String missileWeapon : missileWeapons) {
            if (rng.nextFloat() < 0.9f) {
                faction.addKnownWeapon(missileWeapon, true);
            }
        }
        String[] weaponList;
        switch (hullTag) {
            case "lowtech_bp":
                weaponList = new String[]{
                    "lightmg", "lightdualmg", "vulcan", "lightac", "lightdualac", "lightag", "lightneedler", "railgun",
                    "heavymortar", "chaingun", "heavyac", "hveldriver", "heavymauler", "heavyneedler", "flak", "dualflak",
                    "gauss", "hephag", "mark9", "devastator", "multineedler", "reaper", "atropos", "atropos_single", "swarmer",
                    "annihilator", "heatseeker", "harpoon", "harpoon_single", "breach", "sabot", "sabot_single", "pilum",
                    "breachpod", "harpoonpod", "sabotpod", "phasecl", "typhoon", "cyclone", "hurricane", "squall", "locust",
                    "taclaser", "pdlaser", "lrpdlaser", "ioncannon", "gravitonbeam", "pulselaser", "phasebeam", "shredder",
                    "heavymortar", "arbalest", "hellbore", "hammer", "hammer_single", "jackhammer", "salamanderpod",
                    "annihilatorpod", "pilum_large", "hammerrack", "mininglaser", "miningblaster"
                };
                break;
            case "midline_bp":
                weaponList = new String[]{
                    "gorgon", "gorgonpod", "gazer", "gazerpod", "dragon", "dragonpod", "hydra", "lightmg", "lightdualmg",
                    "lightac", "lightdualac", "lightag", "heavymg", "flak", "heavymauler", "heavyac", "mjolnir", "pdburst",
                    "pdlaser", "lrpdlaser", "taclaser", "ioncannon", "phasebeam", "gravitonbeam", "irautolance", "heavyburst",
                    "ionbeam", "guardian", "hil", "vulcan", "railgun", "heavymortar", "chaingun", "dualflak", "hephag", "mark9",
                    "devastator", "reaper", "atropos", "atropos_single", "swarmer", "annihilator", "heatseeker", "harpoon",
                    "harpoon_single", "breach", "sabot", "sabot_single", "pilum", "breachpod", "harpoonpod", "sabotpod",
                    "phasecl", "typhoon", "squall", "locust", "irpulse", "pulselaser", "autopulse"
                };
                break;
            case "hightech_bp":
                weaponList = new String[] {
                    "pdlaser", "taclaser", "ioncannon", "irpulse", "lrpdlaser", "pdburst", "amblaster", "phasebeam",
                    "gravitonbeam", "pulselaser", "heavyblaster", "heavyburst", "ionpulser", "ionbeam", "plasma",
                    "hil", "autopulse", "guardian", "tachyonlance", "reaper", "atropos", "atropos_single", "swarmer",
                    "annihilator", "heatseeker", "harpoon", "harpoon_single", "breach", "sabot", "sabot_single",
                    "harpoonpod", "breachpod", "sabotpod", "salamanderpod", "phasecl", "typhoon", "cyclone",
                    "hurricane", "squall", "locust", "railgun", "lightneedler", "hveldriver", "heavyneedler",
                    "mjolnir", "multineedler"
                };
                break;
            default:
                weaponList = new String[]{
                        "hurricane", "squall", "locust",
                };
                break;
        }
        for (String weapon : weaponList) {
            if (rng.nextFloat() < 0.8f) {
                faction.addKnownWeapon(weapon, true);
            }
        }

        String[] allWeaponList = new String[]{
                "lightmg", "lightdualmg", "vulcan", "lightac", "lightdualac", "lightag", "lightneedler", "railgun",
                "heavymortar", "chaingun", "heavyac", "hveldriver", "heavymauler", "heavyneedler", "flak", "dualflak",
                "gauss", "hephag", "mark9", "devastator", "multineedler", "reaper", "atropos", "atropos_single", "swarmer",
                "annihilator", "heatseeker", "harpoon", "harpoon_single", "breach", "sabot", "sabot_single", "pilum",
                "breachpod", "harpoonpod", "sabotpod", "phasecl", "typhoon", "cyclone", "hurricane", "squall", "locust",
                "taclaser", "pdlaser", "lrpdlaser", "ioncannon", "gravitonbeam", "pulselaser", "phasebeam", "shredder",
                "heavymortar", "arbalest", "hellbore", "hammer", "hammer_single", "jackhammer", "salamanderpod",
                "annihilatorpod", "pilum_large", "hammerrack", "mininglaser", "miningblaster",
                "gorgon", "gorgonpod", "gazer", "gazerpod", "dragon", "dragonpod", "hydra", "lightmg", "lightdualmg",
                "lightac", "lightdualac", "lightag", "heavymg", "flak", "heavymauler", "heavyac", "mjolnir", "pdburst",
                "pdlaser", "lrpdlaser", "taclaser", "ioncannon", "phasebeam", "gravitonbeam", "irautolance", "heavyburst",
                "ionbeam", "guardian", "hil", "vulcan", "railgun", "heavymortar", "chaingun", "dualflak", "hephag", "mark9",
                "devastator", "reaper", "atropos", "atropos_single", "swarmer", "annihilator", "heatseeker", "harpoon",
                "harpoon_single", "breach", "sabot", "sabot_single", "pilum", "breachpod", "harpoonpod", "sabotpod",
                "phasecl", "typhoon", "squall", "locust", "irpulse", "pulselaser", "autopulse",
                "pdlaser", "taclaser", "ioncannon", "irpulse", "lrpdlaser", "pdburst", "amblaster", "phasebeam",
                "gravitonbeam", "pulselaser", "heavyblaster", "heavyburst", "ionpulser", "ionbeam", "plasma",
                "hil", "autopulse", "guardian", "tachyonlance", "reaper", "atropos", "atropos_single", "swarmer",
                "annihilator", "heatseeker", "harpoon", "harpoon_single", "breach", "sabot", "sabot_single",
                "harpoonpod", "breachpod", "sabotpod", "salamanderpod", "phasecl", "typhoon", "cyclone",
                "hurricane", "squall", "locust", "railgun", "lightneedler", "hveldriver", "heavyneedler",
                "mjolnir", "multineedler"
        };
        for (String weapon : allWeaponList) {
            if (rng.nextFloat() < 0.1f) {
                faction.addKnownWeapon(weapon, true);
            }
        }
    }

    private static void addFighters(FactionAPI faction, String hullTag, Random rng) {
        List<String> baseFighters = Util.getAllFightersWithTag(hullTag);
        for (String baseFighter : baseFighters) {
            if (rng.nextFloat() < 0.9f) {
                faction.addKnownFighter(baseFighter, true);
            }
        }
        String[] fighterList;
        switch (hullTag) {
            case "lowtech_bp":
                fighterList = new String[]{
                        "talon_wing", "broadsword_wing", "warthog_wing", "piranha_wing", "hoplon_wing", "longbow_wing", "perdition_wing"
                };
                break;
            case "midline_bp":
                fighterList = new String[]{
                        "thunder_wing", "gladius_wing", "talon_wing", "claw_wing"
                };
                break;
            case "hightech_bp":
                fighterList = new String[]{
                        "broadsword_wing", "dagger_wing", "trident_wing", "wasp_wing", "xyphos_wing", "claw_wing", "trident_wing", "cobra_wing", "longbow_wing"
                };
                break;
            default:
                fighterList = new String[]{};
                break;
        }
        String[] allFighterList = new String[]{
                "talon_wing", "broadsword_wing", "warthog_wing", "piranha_wing", "hoplon_wing", "longbow_wing", "perdition_wing",
                "thunder_wing", "gladius_wing", "talon_wing", "claw_wing",
                "broadsword_wing", "dagger_wing", "trident_wing", "wasp_wing", "xyphos_wing", "claw_wing", "trident_wing", "cobra_wing", "longbow_wing"
        };
        for (String fighter : fighterList) {
            if (rng.nextFloat() < 0.8f) {
                faction.addKnownFighter(fighter, true);
            }
        }
        for (String fighter : allFighterList) {
            if (rng.nextFloat() < 0.1f) {
                faction.addKnownFighter(fighter, true);
            }
        }
    }

    private static void addHulls(FactionAPI faction, String hullTag, Random rng) {
        List<String> baseHulls = Util.getAllBaseHullsWithTag("base_bp");
        for (String baseHull : baseHulls) {
            faction.addKnownShip(baseHull, true);
        }
        List<String> baseTechHulls = Util.getAllBaseHullsWithTag(hullTag);
        for (String baseHull : baseTechHulls) {
            if (rng.nextFloat() < 0.9f) {
                faction.addKnownShip(baseHull, true);
            }
        }
        faction.addKnownShip("valkyrie", true);
        faction.addKnownShip("prometheus", true);
        faction.addKnownShip("atlas", true);
        String[] hullList;
        switch (hullTag) {
            case "lowtech_bp":
                hullList = new String[]{
                        "manticore", "eradicator", "onslaught", "retribution", "legion", "invictus"
                };
                break;
            case "midline_bp":
                hullList = new String[]{
                        "monitor", "eagle", "champion", "gryphon", "conquest",  "pegasus"
                };
                break;
            case "hightech_bp":
                hullList = new String[]{
                        "omen", "tempest", "hyperion", "scarab", "medusa", "apogee", "aurora", "astral", "odyssey", "paragon",
                        "shade", "afflictor", "harbinger", "doom", "phantom",  "revenant"
                };
                break;
            case "pirate_bp":
                hullList = new String[]{
                        "vanguard_pirates", "manticore_pirates", "falcon_p", "eradicator_pirates", "atlas2",
                        "manticore_luddic_path", "venture_pather", "prometheus2"
                };
                break;
            default:
                hullList = new String[]{};
                break;
        }
        String[] allHullList = new String[]{
                "manticore", "eradicator", "onslaught", "retribution", "legion", "invictus",
                "monitor", "eagle", "champion", "gryphon", "conquest", "pegasus",
                "omen", "tempest", "hyperion", "scarab", "medusa", "apogee", "aurora", "astral", "odyssey", "paragon",
                "shade", "afflictor", "harbinger", "doom", "phantom", "revenant",
                "vanguard_pirates", "manticore_pirates", "falcon_p", "eradicator_pirates", "atlas2",
                "manticore_luddic_path", "venture_pather", "prometheus2"
        };
        for (String hull : hullList) {
            if (rng.nextFloat() < 0.8f) {
                faction.addKnownShip(hull, true);
            }
        }
        for (String hull : allHullList) {
            if (rng.nextFloat() < 0.05f) {
                faction.addKnownShip(hull, true);
            }
        }
    }

    private static void setColor(FactionSpecAPI spec, int[] color) {
        spec.setColor(new Color(color[0], color[1], color[2]));
        spec.setBaseUIColor(new Color(color[0], color[1], color[2]).darker().darker());
        spec.setBrightUIColor(new Color(color[0], color[1], color[2]).brighter());
        spec.setDarkUIColor(new Color(color[0], color[1], color[2]).darker().darker());
        spec.setGridUIColor(new Color(color[0], color[1], color[2]).darker().darker());
        spec.setSecondaryUIColor(new Color(color[0], color[1], color[2]));
        String colorStr = String.format("%02X%02X%02X", color[0], color[1], color[2]);
        spec.setCrest("graphics/factions/renx_crest_" + colorStr + ".png");
        spec.setLogo("graphics/factions/renx_flag_" + colorStr + ".png");
    }

    private static void setName(FactionSpecAPI spec, String name1, String name2) {
        String name = name1 + " " + name2;

        spec.setDisplayName(name1);
        spec.setDisplayNameIsOrAre(name1);
        spec.setDisplayNameWithArticle(name1);
        spec.setDisplayNameLong(name);
        spec.setDisplayNameLongWithArticle(name);
        spec.setPersonNamePrefix(name1);
        spec.setPersonNamePrefixAOrAn(Arrays.asList('a', 'e', 'i', 'o', 'u').contains(name.charAt(0)) ? "an" : "a");
        spec.setShipNamePrefix(String.format("%s%sS", name1.charAt(0), name2.charAt(0)).toUpperCase());
        spec.setEntityNamePrefix(name1);
    }

    private static final String[] POLITIES = new String[]
    {
            "Collective", "Association", "Coalition", "Federation", "Confederation", "Alliance",
            "Empire", "Republic", "Union", "League", "Commonwealth", "Hegemony", "Dominion", "Diktat",
            "Authority", "Command", "Imperium", "Navy", "Organization", "Alliance", "Starworks", "Group",
            "Shipyards", "Industries", "Combine", "Lab", "Syndicate", "Corporation", "Manufacture", "Bureau", "Corp", "Corporation",
            "Security", "Union", "Solutions", "Conglomerate", "Empire", "Initiative",
            "Church", "Cult", "Path", "Order", "Clan",
            "Stars", "Planets", "Systems",
    };
}
