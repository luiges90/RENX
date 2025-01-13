package com.luiges90.renx;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

public class RenxPlugin extends BaseModPlugin {
    @Override
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();
        System.out.println("ddd onApplicationLoad");
        // com.fs.starfarer.api.Global.getSector().getAllFactions()
    }
}
