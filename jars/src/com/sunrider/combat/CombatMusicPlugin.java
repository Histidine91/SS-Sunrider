package com.sunrider.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class CombatMusicPlugin implements EveryFrameCombatPlugin {

    public static Logger log = Global.getLogger(CombatMusicPlugin.class);

    protected int countdown = 3;
    protected String track;
    protected boolean playedMusic = false;
    protected CombatEngineAPI engine;

    protected String getTrack() {
        List<CampaignFleetAPI> checked = new ArrayList<>();
        List<FleetMemberAPI> toCheck = new ArrayList<>(Global.getCombatEngine().getFleetManager(FleetSide.ENEMY).getReservesCopy());
        toCheck.addAll(Global.getCombatEngine().getFleetManager(FleetSide.ENEMY).getDeployedCopy());

        for (FleetMemberAPI enemy : toCheck) {
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
        log.info("Removing plugin");
        Global.getCombatEngine().removePlugin(this);
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) engine = Global.getCombatEngine();
        if (engine == null) return;

        if (engine.isSimulation()) {
            removeSelf();
            return;
        }

        if (track != null && engine.isCombatOver()) {
            log.info("Terminating custom music playback");
            //Global.getSoundPlayer().setSuspendDefaultMusicPlayback(false);
            Global.getSoundPlayer().playCustomMusic(1, 0, null);
            removeSelf();
            return;
        }

        if (countdown == 0) {

            if (!playedMusic) {
                if (track == null) {
                    if (!needMod()) {
                        removeSelf();
                        return;
                    }

                    track = getTrack();
                    if (track == null) {
                        removeSelf();
                        return;
                    } else {
                        log.info("Found track " + track);
                    }
                }

                //Global.getSoundPlayer().setSuspendDefaultMusicPlayback(true);
                Global.getSoundPlayer().playCustomMusic(1, 0, track, true);
                playedMusic = true;
            }
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
