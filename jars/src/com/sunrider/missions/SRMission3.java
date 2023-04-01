package com.sunrider.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.List;
import java.util.Map;

/**
 * The Java class for the third 'mission', really just a holder for the button to open dialog so player can view it when they want.
 * @author Histidine
 */
public class SRMission3 extends HubMissionWithSearch implements SunriderMissionInterface 
{
	public static final String MISSION_REF = "$sunrider_mission3_ref";
	public static final float DELAY_BEFORE_AVAILABLE = 40;	// from completion of mission 1
	public static final Object BUTTON_TALK = new Object();
	
	protected transient boolean shownDialog = false;

	public static enum Stage {
		RUNNING,
		COMPLETED,
		FAILED
	}
		
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		if (!setGlobalReference(MISSION_REF)) {
			return false;
		}
		
		personOverride = Global.getSector().getImportantPeople().getPerson(FindSunrider.AVA_ID);
		setRepFactionChangesNone();
		setRepPersonChangesNone();
		
		setStoryMission();
		setStartingStage(Stage.RUNNING);
		addSuccessStages(Stage.COMPLETED);
		addFailureStages(Stage.FAILED);
		
		beginStageTrigger(Stage.COMPLETED);
		triggerSetGlobalMemoryValue("$sunrider_mission3_missionCompleted", true);
		triggerSetGlobalMemoryValue("$sunrider_mission3_doneOrSkipped", true);
		
		endTrigger();
		
		setStageOnMemoryFlag(Stage.FAILED, Global.getSector().getMemoryWithoutUpdate(), "$sunrider_avaLeft");
		
		return true;
	}
	
	@Override
	protected void notifyEnding() {
		
	}
	
	@Override
	public boolean callEvent(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
		String action = params.get(0).getString(memoryMap);
		
		switch (action) {
			case "showNightmare":
				FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "SunriderNMsim");
				member.setShipName(member.getHullSpec().getHullName());
				dialog.getVisualPanel().showFleetMemberInfo(member, false);
				return true;
			
			case "complete":
				setCurrentStage(Stage.COMPLETED, dialog, memoryMap);
				return true;
		}
		return super.callEvent(ruleId, dialog, params, memoryMap);
	}
	
	@Override
	public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
		if (buttonId == BUTTON_TALK) {
			shownDialog = true;
			ui.updateUIForItem(this);
			RuleBasedInteractionDialogPluginImpl plugin = new RuleBasedInteractionDialogPluginImpl();
			ui.showDialog(Global.getSector().getPlayerFleet(), plugin);
			plugin.getMemoryMap().get(MemKeys.LOCAL).set("$option", "sunrider_mission3_convo1");
			plugin.fireBest("DialogOptionSelected");	
			return;
		}
		super.buttonPressConfirmed(buttonId, ui);
	}
	
	// intel text in intel screen description
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float pad = 3;
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		
		info.addPara(getString("mission3_desc"), opad);
		if (!shownDialog) {
			info.addButton(getString("mission3_button"), BUTTON_TALK, width, 24, opad);
		}		
	}
	
	// intel text in message popups, or intel list on left side of screen
	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		info.addPara(getString("mission3_nextStep"), pad, tc, h);
		return true;
	}
	
	@Override
	public String getBaseName() {
		return getString("mission3_name");
	}
	
	// =========================================================================
	// =========================================================================
	
	public static String getString(String id) {
		return Global.getSettings().getString("sunrider_missions", id);
	}
}
