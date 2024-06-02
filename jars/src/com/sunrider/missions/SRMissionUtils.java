package com.sunrider.missions;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;

public class SRMissionUtils {

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
