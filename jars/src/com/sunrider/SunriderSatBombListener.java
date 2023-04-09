package com.sunrider;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyPlayerHostileActListener;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.Sunrider_MiscFunctions;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.Nex_MarketCMD.NexTempData;
import com.fs.starfarer.api.util.DelayedActionScript;
import com.sunrider.missions.FindSunrider;
import java.util.Map;

/**
 * Detects when a sat bomb occurs.
 * Normally (check the constant) this listener is only present if the 'Find Sunrider' mission is ongoing, or Ava is in party.
 * @author Histidine
 */
public class SunriderSatBombListener implements ColonyPlayerHostileActListener {
	
	public static final boolean ALWAYS_RUN = true;
	
	public static void addIfNeeded() {
		// already added?
		if (Global.getSector().getListenerManager().hasListenerOfClass(SunriderSatBombListener.class))
			return;
		
		if (!ALWAYS_RUN) {
			MemoryAPI mem = Global.getSector().getMemoryWithoutUpdate();
			// found sunrider but have no Ava, do not add
			boolean foundSunrider = mem.getBoolean(FindSunrider.MEM_FLAG_COMPLETE);
			if (foundSunrider) {
				if (!Sunrider_MiscFunctions.isAvaInParty()) return;
			} 
			// not yet found Sunrider and mission not ongoing, do not add
			else if (!mem.contains(FindSunrider.MISSION_REF)) {
				return;
			}
		}	
		
		Global.getSector().getListenerManager().addListener(new SunriderSatBombListener(), true);
	}

	public void reportSaturationBombardmentFinished(
			InteractionDialogAPI dialog, final MarketAPI market, MarketCMD.TempData actionData) {
		
		Global.getLogger(this.getClass()).info("Sat bomb occured!");
		
		
		if (Global.getSettings().getModManager().isModEnabled("nexerelin") && actionData instanceof NexTempData) {
			NexTempData ntd = (NexTempData)actionData;
			if (ntd.satBombLimitedHatred) return;	// do nothing if the sat bomb didn't enrage everyone
		}
		
		if (actionData != null) {
			for (FactionAPI angery : actionData.willBecomeHostile) {
				// FIXME: Hivers will always be here since they care about atrocities
				if (angery.getId().equals("HIVER") || angery.getId().equals("tahlan_legioinfernalis"))
					return;
			}
		}
		
		// not yet encountered Ava bar event?
		// block it in rules.csv and prepare to enable the bounty
		if (!Global.getSector().getMemoryWithoutUpdate().getBoolean("$sunrider_findSunrider_seenMission")) {
			Global.getSector().getMemoryWithoutUpdate().set("$sunrider_findSunrider_blockMission", true);
			Sunrider_MiscFunctions.addBountyDelayedScript(Sunrider_MiscFunctions.getBountyDelayTime());
			return;
		}
		
		// check if Ava should mutiny now
		// if yes, add the every frame script and let stuff happen
		// if not, just set the flag and wait for her to find an opportunity to leave
		if (Sunrider_MiscFunctions.avaCanMutiny()) {
			Global.getLogger(this.getClass()).info("Preparing for mutiny");
			
			// need to set non-zero expiration since bombardment code advances the market with 0/very small elapsed time
			// a few times, so if set to 0 the value would be removed from memory
			market.getMemoryWithoutUpdate().set("$sunrider_avaMutiny", true, 1f);
			
			// special case: when station gets destroyed by the sat bomb
			if (dialog != null) {	// can be null if something manually calls the listener without a dialog (e.g. TASC planetkiller)
				MarketAPI alt = dialog.getInteractionTarget().getMarket();
				if (alt != market) {
					alt.getMemoryWithoutUpdate().set("$sunrider_avaMutiny", true, 1f);
					// fixes a minor NPE
					dialog.getInteractionTarget().getMemoryWithoutUpdate().set("$tradeMode", CoreUITradeMode.OPEN, 1f);
				}
			}
			// special case: no dialog, open our own (e.g. TASC planetkiller)
			else {
				Global.getLogger(this.getClass()).info("Opening dialog");
				Global.getSector().addScript(new DelayedActionScript(0.1f) {
					@Override
					public void doAction() {
						RuleBasedInteractionDialogPluginImpl plugin = new RuleBasedInteractionDialogPluginImpl();
						Global.getSector().getCampaignUI().showInteractionDialog(plugin, market.getPrimaryEntity());
						plugin.getMemoryMap().get(MemKeys.LOCAL).set("$option", "sunrider_avaMutiny_alt");
						plugin.fireBest("DialogOptionSelected");
					}
				});				
			}
			Global.getSector().getMemoryWithoutUpdate().set(Sunrider_MiscFunctions.MEM_KEY_AVA_WANT_LEAVE, true);
			return;
		}
		
		Global.getSector().getMemoryWithoutUpdate().set(Sunrider_MiscFunctions.MEM_KEY_AVA_WANT_LEAVE, true);
		Map<String, MemoryAPI> memoryMap = null;
		if (dialog != null) memoryMap = dialog.getPlugin().getMemoryMap();
		Sunrider_MiscFunctions.terminateMissions(dialog, memoryMap);
	}	
	
	// unused
	public void reportRaidToDisruptFinished(InteractionDialogAPI dialog,
							MarketAPI market, MarketCMD.TempData actionData, Industry industry) {}
		
	public void reportRaidForValuablesFinishedBeforeCargoShown(InteractionDialogAPI dialog, 
								MarketAPI market, MarketCMD.TempData actionData,
								CargoAPI cargo) {}
	
	public void reportTacticalBombardmentFinished(InteractionDialogAPI dialog,
			MarketAPI market, MarketCMD.TempData actionData) {}
}
