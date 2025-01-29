package com.luiges90.renx;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModPlugin;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DiplomaticRelationScript extends BaseCampaignEventListener implements EveryFrameScript {
    public static final String PERSISTENT_DATA_KEY_TIMESTAMP = "renx_diplomaticRelationScript_timestamp";

    public DiplomaticRelationScript() {
        super(true);
    }

    private static final Random random = new Random();

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        Object otsn = Global.getSector().getPersistentData().get(PERSISTENT_DATA_KEY_TIMESTAMP);
        if (otsn == null) {
            Global.getSector().getPersistentData().put("renx_diplomaticRelationScript_timestamp", Global.getSector().getClock().getDay());
            return;
        }

        int ots = (int) otsn;
        int ts = Global.getSector().getClock().getDay();
        if (ts == ots) return;

        Global.getSector().getPersistentData().put("renx_diplomaticRelationScript_timestamp", Global.getSector().getClock().getDay());

        if (random.nextFloat() > 1 / 30f) return;

        List<FactionAPI> allAllFactions = Global.getSector().getAllFactions();
        List<FactionAPI> allFactions = new ArrayList<>();
        for (FactionAPI faction : allAllFactions) {
            if (faction.isPlayerFaction() || faction.isShowInIntelTab()) {
                allFactions.add(faction);
            }
        }

        for (FactionAPI i : allFactions) {
            for (FactionAPI j : allFactions) {
                if (i == j) {
                    continue;
                }

                if (i.getRelationship(j.getId()) < -0.25f) {
                    for (FactionAPI k : allFactions) {
                        if (k == i || k == j) {
                            continue;
                        }

                        if (i.getRelationship(k.getId()) > -1) {
                            if (j.getRelationship(k.getId()) > 0.25f && random.nextFloat() < 0.05f) {
                                float delta = (i.getRelationship(j.getId()) - j.getRelationship(k.getId()) + 0.5f) * 0.05f * (random.nextFloat() + 0.5f);
                                i.adjustRelationship(k.getId(), delta);

                                String logStr = "DiplomaticRelationScript: " + i.getDisplayName() + " - " + j.getDisplayName() + " rel: " + i.getRelationship(j.getId()) + "; " +
                                        j.getDisplayName() + " - " + k.getDisplayName() + " rel: " + j.getRelationship(k.getId()) + "; " +
                                        i.getDisplayName() + " - " + k.getDisplayName() + " rel adjusted: " + delta;
                                Global.getLogger(DiplomaticRelationScript.class).info(logStr);

                                if (i.isPlayerFaction()) {
                                    CoreReputationPlugin.addAdjustmentMessage(delta, k, null, null, null, null, true, 0f, "Change caused by hostile relations with " + j.getDisplayName());
                                    Global.getSoundPlayer().playUISound("ui_rep_drop", 1f, 1f);
                                } else if (k.isPlayerFaction()) {
                                    CoreReputationPlugin.addAdjustmentMessage(delta, i, null, null, null, null, true, 0f, "Change caused by friendly relations with " + j.getDisplayName());
                                    Global.getSoundPlayer().playUISound("ui_rep_drop", 1f, 1f);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
