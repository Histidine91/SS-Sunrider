package com.sunrider.missions;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.ids.Factions;

public class SRMissionUtils {

    // runcode com.sunrider.missions.SRMissionUtils.testSkills();
    public static void testSkills() {
        PersonAPI pers = OfficerManagerEvent.createOfficer(Global.getSector().getPlayerFaction(), 1);
        pers.getStats().setSkillLevel("sunrider_plushGalleon", 1);
        pers.getStats().setSkillLevel("sunrider_plushDogoo", 1);
        pers.getStats().setSkillLevel("sunrider_plushRensouhou", 1);
        Global.getSector().getPlayerFleet().getFleetData().addOfficer(pers);
    }

    public static class OpenDialogScript implements EveryFrameScript {

        public final String ruleTrigger;
        protected boolean done;

        public OpenDialogScript(String ruleTrigger) {
            this.ruleTrigger = ruleTrigger;
        }

        @Override
        public boolean isDone() {
            return done;
        }

        @Override
        public boolean runWhilePaused() {
            return true;
        }

        @Override
        public void advance(float time) {
            CampaignUIAPI ui = Global.getSector().getCampaignUI();
            if (ui.isShowingDialog() || ui.isShowingMenu()) return;

            RuleBasedInteractionDialogPluginImpl plugin = new RuleBasedInteractionDialogPluginImpl();
            ui.showInteractionDialog(plugin, Global.getSector().getPlayerFleet());
            plugin.fireBest(ruleTrigger);
            done = true;
        }
    }
}
