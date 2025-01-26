package com.luiges90.renx;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModPlugin;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;

import java.util.List;
import java.util.Random;

public class DiplomaticRelationScript extends BaseCampaignEventListener implements EveryFrameScript {
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
        if (random.nextFloat() > 1 / Global.getSector().getClock().getSecondsPerDay() || random.nextFloat() > 1 / 30f) return;

        List<FactionAPI> allFactions = Global.getSector().getAllFactions();
        for (FactionAPI i : allFactions) {
            for (FactionAPI j : allFactions) {
                if (i == j) {
                    continue;
                }

                if (i.getRelationship(j.getId()) < 0) {
                    for (FactionAPI k : allFactions) {
                        if (k == i || k == j) {
                            continue;
                        }

                        if (j.getRelationship(k.getId()) > 0.1f && random.nextFloat() < 0.25f) {
                            float delta = (i.getRelationship(j.getId()) - j.getRelationship(k.getId())) * 0.05f;
                            i.adjustRelationship(k.getId(), delta);

                            CoreReputationPlugin.addAdjustmentMessage(delta, i, null, null, null, null, true, 0f, "Change caused by friendly relation with " + j.getDisplayName());
                            if ("player".equals(i.getId()) || "player".equals(k.getId())) {
                                Global.getSoundPlayer().playUISound("ui_rep_drop", 0.85f, 0.5f);
                            }
                        }
                    }
                }
            }
        }
    }
}
