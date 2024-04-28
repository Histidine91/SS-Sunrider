package com.sunrider.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch;
import com.fs.starfarer.api.impl.campaign.plog.PlaythroughLog;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import static com.fs.starfarer.api.impl.campaign.rulecmd.missions.Sunrider_MiscFunctions.getString;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.sunrider.SRPeople;
import java.awt.Color;
import java.util.List;
import java.util.Map;

/**
 * The Java class for the second mission. 
 * Most of the logic is done in rules.csv, this is mostly for data holding and the intel item.
 * @author Histidine
 */
public class SRMission2 extends HubMissionWithSearch implements SunriderMissionInterface 
{
	public static final String MISSION_REF = "$sunrider_mission2_ref";
	public static final int RECOVER_COST = 15000;
	public static final float DELAY_BEFORE_AVAILABLE = 120;	// from completion of mission 1

	public static enum Stage {
		RECOVER_DATA,
		COMPLETED,
		FAILED
	}
		
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		if (!setGlobalReference(MISSION_REF)) {
			return false;
		}
		
		personOverride = Global.getSector().getImportantPeople().getPerson(SRPeople.AVA_ID);		
		
		setStoryMission();
		setStartingStage(Stage.RECOVER_DATA);
		addSuccessStages(Stage.COMPLETED);
		addFailureStages(Stage.FAILED);
		
		setRepPersonChangesMedium();
		setRepFactionChangesNone();
		
		// don't use a completion stage trigger, it can't be trusted https://fractalsoftworks.com/forum/index.php?topic=5061.msg392175#msg392175
		beginStageTrigger(Stage.COMPLETED);
		triggerSetGlobalMemoryValuePermanent("$sunrider_wolololo", true);
		endTrigger();
		
		setStageOnMemoryFlag(Stage.FAILED, Global.getSector().getMemoryWithoutUpdate(), "$sunrider_avaLeft");
		
		return true;
	}
	
	@Override
	protected void notifyEnding() {
		
	}
	
	@Override
	public boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {		
		switch (action) {
			case "addBlueprint":
				SpecialItemData special = new SpecialItemData("weapon_bp", "SunriderSavior");
				CargoAPI pc = Global.getSector().getPlayerFleet().getCargo();
				CargoStackAPI stack = Global.getFactory().createCargoStack(CargoAPI.CargoItemType.SPECIAL, special, pc);
				stack.setSize(1);
				pc.addFromStack(stack);
				AddRemoveCommodity.addStackGainText(stack, dialog.getTextPanel(), false);
				return true;
			
			case "complete":
				completeMission(dialog, params, memoryMap);
				return true;
		}
		return false;
	}
	
	// intel text in intel screen description
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float pad = 3;
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		
		if (currentStage == Stage.RECOVER_DATA) {
			info.addPara(getString("mission2_recoverDataDesc"), opad);
			bullet(info);
			
			LabelAPI label = info.addPara(getString("mission2_recoverDataDesc1"), pad);
			String highlightRaw = getString("mission2_recoverDataDesc1Highlight");
			String[] highlights = highlightRaw.split(";");
			label.setHighlight(highlights);
			label.setHighlightColors(Global.getSector().getFaction(Factions.TRITACHYON).getBaseUIColor(), 
					Global.getSector().getFaction(Factions.PLAYER).getBaseUIColor());
			
			if (haveGargoyle()) {
				label = info.addPara(getString("mission2_recoverDataDescGargoyle"), pad);
				String highlight = getString("mission2_recoverDataDescGargoyleHighlight");
				label.setHighlight(highlight);
			}
			if (haveKween()) {
				label = info.addPara(getString("mission2_recoverDataDescKween"), pad);
				String highlight = getString("mission2_recoverDataDescKweenHighlight");
				label.setHighlight(highlight);
				label.setHighlightColor(Global.getSector().getFaction(Factions.DIKTAT).getBaseUIColor());
			}
			unindent(info);
		}
	}
	
	// intel text in message popups, or intel list on left side of screen
	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.RECOVER_DATA) {
			info.addPara(getString("mission2_recoverDataNextStep"), pad, tc, h);
			return true;
		}
		return false;
	}
	
	@Override
	public String getBaseName() {
		return getString("mission2_name");
	}
	
	protected boolean haveGargoyle() {
		PersonAPI garg = Global.getSector().getImportantPeople().getPerson(People.GARGOYLE);
		if (garg == null) return false;
		SectorEntityToken galatia = Global.getSector().getEntityById("station_galatia_academy");
		if (galatia == null) return false;
		return galatia.getMarket() == garg.getMarket();
	}
	
	protected boolean haveKween() {
		PersonAPI kween = Global.getSector().getImportantPeople().getPerson("sfckween");
		if (kween == null) return false;
		return Global.getSector().getFaction(Factions.DIKTAT).getRelToPlayer().isAtWorst(RepLevel.INHOSPITABLE);
	}
	
	protected void completeMission(InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) 
	{
		setCurrentStage(Stage.COMPLETED, dialog, memoryMap);
		PlaythroughLog.getInstance().addEntry(getString("findSunrider_playthroughLogText"), true);
		Global.getSector().getMemoryWithoutUpdate().set("$sunrider_mission4_delay", true, SRMission4.DELAY_BEFORE_AVAILABLE);
		Global.getSector().getMemoryWithoutUpdate().set("$sunrider_mission2_missionCompleted", true);
		Global.getSector().getMemoryWithoutUpdate().set("$sunrider_mission2_doneOrSkipped", true);	
	}
	
	@Override
	protected void updateInteractionDataImpl() {
		set("$sunrider_mission2_recoverCreditsStr", Misc.getWithDGS(RECOVER_COST));
		set("$sunrider_mission2_recoverCredits", RECOVER_COST);
	}
}
