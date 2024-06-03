package com.sunrider.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;

import java.util.ArrayList;
import java.util.List;

public class CombatMusicPlugin implements EveryFrameCombatPlugin {

    protected int countdown = 3;
    protected String track;

    protected String getTrack() {
        List<CampaignFleetAPI> checked = new ArrayList<>();
        List<FleetMemberAPI> toCheck = new ArrayList<>(Global.getCombatEngine().getFleetManager(FleetSide.ENEMY).getReservesCopy());
        toCheck.addAll(Global.getCombatEngine().getFleetManager(FleetSide.ENEMY).getDeployedCopy());

        Global.getCombatEngine().getContext().getOtherFleet().getTags()

        for (FleetMemberAPI enemy : toCheck) {
            Global.getLogger(this.getClass()).info("Checking fleet member " + enemy.getShipName());
            if (enemy.getFleetData() == null) continue;
            CampaignFleetAPI fleet = enemy.getFleetData().getFleet();
            if (fleet == null) continue;
            if (checked.contains(fleet)) continue;
            checked.add(fleet);

            // tags are empty for some reason
            /*
            for (String tag : fleet.getTags()) {
                Global.getLogger(this.getClass()).info("wallahi " + tag);
                if (tag.startsWith("cbm_")) return tag;
            }
            */
            String str = fleet.getMemoryWithoutUpdate().getString("$sunrider_music");
            if (str != null) return str;
        }
        return null;
    }

    protected boolean needMod() {
        return !Global.getSettings().getModManager().isModEnabled("battle_music");  // in future will want Audio Plus check too
    }

    protected void removeSelf() {
        Global.getCombatEngine().removePlugin(this);
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (track == null) {
            if (!needMod()) {
                //Global.getLogger(this.getClass()).info("Battle music mod is running, remove");
                removeSelf();
                return;
            }

            track = getTrack();
            if (track == null) {
                //Global.getLogger(this.getClass()).info("No track found, remove");
                removeSelf();
                return;
            }
        }

        if (countdown == 0) {
            //Global.getSoundPlayer().setSuspendDefaultMusicPlayback(true);
            Global.getSoundPlayer().playCustomMusic(0, 0, track, true);
            removeSelf();
            return;
        }

        countdown--;
    }

    @Override
    public void renderInWorldCoords(ViewportAPI viewport) {}

    @Override
    public void renderInUICoords(ViewportAPI viewport) {}

    @Override
    public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {}

    @Override
    public void init(CombatEngineAPI engine) {}
}
