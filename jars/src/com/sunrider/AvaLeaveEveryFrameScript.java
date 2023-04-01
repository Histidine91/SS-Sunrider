package com.sunrider;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.Sunrider_MiscFunctions;

/**
 * Handles the situation where player sat bombs a market then goes to the location of Sunrider wreck. 
 * Pops up a dialog where Ava is revealed to have escaped.
 * @author Histidine
 */
public class AvaLeaveEveryFrameScript implements EveryFrameScript {
	public static final float DELAY = 0.2f;
	
	protected boolean isDone = false;
	protected float timer = 0;
	// we have cleaner ways of handling Ava's mutiny (when she's already in fleet) than popping up only after current dialog
	@Deprecated protected boolean isMutiny;
	
	public static void addScript(boolean mutiny) {
		// already present
		if (Global.getSector().hasScript(AvaLeaveEveryFrameScript.class)) return;
		
		AvaLeaveEveryFrameScript script = new AvaLeaveEveryFrameScript();
		//script.isMutiny = mutiny;
		Global.getSector().addScript(script);
	}

	@Override
	public boolean isDone()
	{
		return isDone;
	}

	@Override
	public boolean runWhilePaused()
	{
		return true;
	}
	
	@Override
	public void advance(float amount)
	{
		// Ava was already removed somehow, like we dismissed her before dialog could pop up
		if (!Sunrider_MiscFunctions.isAvaInParty()) 
		{
			isDone = true;
			return;
		}
		
		// Don't do anything while in a menu/dialog
		CampaignUIAPI ui = Global.getSector().getCampaignUI();
		if (Global.getSector().isInNewGameAdvance() || ui.isShowingDialog() || Global.getCurrentState() == GameState.TITLE)
		{
			return;
		}
		
		timer += Global.getSector().getClock().convertToDays(amount);
		if (timer < DELAY) return;
		
		if (!isDone)
		{
			showDialog();
			isDone = true;
		}
	}
	
	protected void showDialog() {
		if (isMutiny) {
			// check if mutiny conditions are still valid
			if (!Sunrider_MiscFunctions.avaCanMutiny()) return;
		}
		
		RuleBasedInteractionDialogPluginImpl plugin = new RuleBasedInteractionDialogPluginImpl();
		SectorEntityToken token = Global.getSector().getPlayerFleet().getContainingLocation().createToken(0, 0);
		Global.getSector().getCampaignUI().showInteractionDialog(plugin, token);
		plugin.getMemoryMap().get(MemKeys.LOCAL).set("$option", isMutiny ? "sunrider_avaMutiny1" : "sunrider_avaLeaveEnterSystem_start");
		plugin.fireBest("DialogOptionSelected");
	}
}
