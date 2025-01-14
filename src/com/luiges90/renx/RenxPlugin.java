package com.luiges90.renx;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import exerelin.utilities.NexConfig;
import exerelin.utilities.NexFactionConfig;

import java.util.List;
import java.util.Objects;

public class RenxPlugin extends BaseModPlugin {
    @Override
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();
        System.out.println("ddd onApplicationLoad");

        List<FactionAPI> allFactions = Global.getSector().getAllFactions();

        for (FactionAPI faction : allFactions) {
            if (Objects.equals(faction.getId(), "fpp_lavoie")) {
                faction.setDisplayNameOverride("LowTech");
            }
            NexFactionConfig config = NexConfig.getFactionConfig(faction.getId());
        }
    }
}
