package com.sunrider.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch;
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.Sunrider_MiscFunctions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.sunrider.SRPeople;
import java.awt.Color;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * The main code body for the fifth mission.
 * @author Histidine
 */
public class SRVows extends HubMissionWithSearch implements SunriderMissionInterface {
	
	public static Logger log = Global.getLogger(SRVows.class);
	public static final String MISSION_REF = "$sunrider_missionVows_ref";
	public static final float DELAY_BEFORE_AVAILABLE = 30;	// from completion of mission 4 (Sons of Inquity)
	public static final float TIME_LIMIT = 30;
	public static final float CREDIT_REWARD = 6000;
	//public static final Set<String> VALID_CURATE_MARKETS = new HashSet<>(Arrays.asList("gilead", "volturn", "hesperus", "jangala"));
	
	public enum Stage {
		GO_TO_BEHOLDER,
		FIND_CURATE,
		RETURN_TO_BEHOLDER,
		PARTY,
		COMPLETED,
		FAILED
	}
	
	// needed people: bride, groom, heg officer, church officer
	// needed temp people: DJ etc.?
	
	//protected MarketAPI origin;
	protected PersonAPI bride = SRPeople.getOrCreateBride();
	protected PersonAPI groom = SRPeople.getOrCreateGroom();
	protected PersonAPI curate = Global.getSector().getImportantPeople().getPerson(People.SHRINE_CURATE);
	protected PersonAPI hegOfficer = SRPeople.getOrCreateHegOfficer();
	protected PersonAPI churchOfficer = SRPeople.getOrCreateChurchOfficer();
	protected MarketAPI curateLoc;
	
	// runcode com.sunrider.missions.SRMission4.debug()
	public static void debug() {
		SRVows mission = (SRVows)Global.getSector().getMemoryWithoutUpdate().get("$sunrider_missionVows_ref");
	}
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		if (!setGlobalReference(MISSION_REF)) {
			return false;
		}
		
		SectorEntityToken beholder = Global.getSector().getEntityById("beholder_station");
		if (beholder == null) return false;
		
		requireMarketIsNot(createdAt);
		requireMarketIsNot(Global.getSector().getEconomy().getMarket("volturn"));
		search.marketReqs.add(new HaveShrineRequirement());
		preferSystemWithinRangeOf(createdAt.getLocationInHyperspace(), 12);
		curateLoc = pickMarket();
		if (curateLoc == null) return false;		
		
		setStartingStage(Stage.GO_TO_BEHOLDER);
		setSuccessStage(Stage.COMPLETED);
		setFailureStage(Stage.FAILED);
		setStoryMission();
		
		makeImportant(beholder, "$sunrider_missionVows_shrine", Stage.GO_TO_BEHOLDER);
		makeImportant(curateLoc, "$sunrider_missionVows_curateLoc", Stage.FIND_CURATE);
		makeImportant(beholder, "$sunrider_missionVows_shrine2", Stage.RETURN_TO_BEHOLDER);
		makeImportant(beholder, "$sunrider_missionVows_shrine3", Stage.PARTY);
		
		setCreditReward(0);	// handled manually
		
		setTimeLimit(Stage.FAILED, TIME_LIMIT, null, Stage.PARTY);		
		connectWithDaysElapsed(Stage.PARTY, Stage.COMPLETED, 5);	// 5 days to return to Beholder once party starts
		
		return true;
	}
	
	@Override
	public void acceptImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {		
		Misc.makeStoryCritical(curateLoc, "sunrider_misisonVows");
	}
		
	public void complete(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		setCurrentStage(Stage.COMPLETED, dialog, memoryMap);
		Global.getSector().getMemoryWithoutUpdate().set("$sunrider_missionVows_missionCompleted", true);
		Global.getSector().getMemoryWithoutUpdate().set("$sunrider_missionVows_doneOrSkipped", true);
	}
	
	public void fail(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		setCurrentStage(Stage.FAILED, dialog, memoryMap);
		Global.getSector().getMemoryWithoutUpdate().set("$sunrider_missionVows_doneOrSkipped", true);
	}
	
	@Override
	public boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) 
	{		
		switch (action) {
			case "findCurate":
				setCurrentStage(Stage.FIND_CURATE, dialog, memoryMap);
				setTimeLimit(Stage.FAILED, TIME_LIMIT * 2, null, Stage.PARTY);
				return true;
			case "returnToShrine":
				setCurrentStage(Stage.RETURN_TO_BEHOLDER, dialog, memoryMap);
				return true;
			case "enableWedding":
				setCurrentStage(Stage.PARTY, dialog, memoryMap);
				return true;
			case "complete":
				complete(dialog, memoryMap);
				return true;
			case "fail":
				fail(dialog, memoryMap);
				return true;
			case "isMissionRunning":
				// if you got here the mission is indeed running
				return true;
		}
		return false;
	}
	
	@Override
	protected void updateInteractionDataImpl() {
		set("$sunrider_missionVows_curateLoc", curateLoc);
		set("$sunrider_missionVows_curateLocName", curateLoc.getName());
		set("$sunrider_missionVows_curateLocOnOrAt", curateLoc.getOnOrAt());
		set("$sunrider_missionVows_reward", CREDIT_REWARD);
		set("$sunrider_missionVows_rewardStr", Misc.getWithDGS(CREDIT_REWARD));
		
		set("$sunrider_missionVows_groomName", groom.getNameString());
		set("$sunrider_missionVows_groomFirst", groom.getName().getFirst());
		set("$sunrider_missionVows_brideName", bride.getNameString());
		set("$sunrider_missionVows_brideFirst", bride.getName().getFirst());
		set("$sunrider_missionVows_curateName", curate.getNameString());
		set("$sunrider_missionVows_curateHeOrShe", curate.getHeOrShe());
		set("$sunrider_missionVows_curateHimOrHer", curate.getHimOrHer());

		set("$sunrider_missionVows_saintHeOrShe", hegOfficer.getHeOrShe());
		set("$sunrider_missionVows_saintHimOrHer", hegOfficer.getHimOrHer());
		set("$sunrider_missionVows_SaintHeOrShe", Misc.ucFirst(hegOfficer.getHeOrShe()));
		set("$sunrider_missionVows_SaintHimOrHer", Misc.ucFirst(hegOfficer.getHimOrHer()));
	}
	
	// intel text in intel screen description
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		Color church = Global.getSector().getFaction(Factions.LUDDIC_CHURCH).getBaseUIColor();
		
		if (currentStage == Stage.GO_TO_BEHOLDER) 
		{
			info.addPara(Sunrider_MiscFunctions.getString("missionVows_goToBeholderDesc"), opad, church, 
					Global.getSector().getEntityById("beholder_station").getName());
		}
		if (currentStage == Stage.FIND_CURATE) 
		{
			info.addPara(Sunrider_MiscFunctions.getString("missionVows_findCurateDesc"), opad, church, 
					curateLoc.getName());
		} 
		else if (currentStage == Stage.RETURN_TO_BEHOLDER) 
		{
			info.addPara(Sunrider_MiscFunctions.getString("missionVows_returnToBeholderDesc"), opad, church, 
					Global.getSector().getEntityById("beholder_station").getName());
		}
		else if (currentStage == Stage.PARTY) 
		{
			info.addPara(Sunrider_MiscFunctions.getString("missionVows_partyDesc"), opad, church, 
					Global.getSector().getEntityById("beholder_station").getName());
		}
	}
	
	// intel text in message popups, or intel list on left side of screen
	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color hl = Misc.getHighlightColor();
		Color church = Global.getSector().getFaction(Factions.LUDDIC_CHURCH).getBaseUIColor();
		
		if (currentStage == Stage.GO_TO_BEHOLDER) 
		{
			info.addPara(Sunrider_MiscFunctions.getString("missionVows_goToBeholderNextStep"), 
					pad, tc, church, Global.getSector().getEntityById("beholder_station").getName());
			return true;
		}
		else if (currentStage == Stage.FIND_CURATE) 
		{
			info.addPara(Sunrider_MiscFunctions.getString("missionVows_findCurateNextStep"), 
					pad, tc, church, curateLoc.getName());
			return true;
		}
		else if (currentStage == Stage.RETURN_TO_BEHOLDER) 
		{
			info.addPara(Sunrider_MiscFunctions.getString("missionVows_returnToBeholderNextStep"), 
					pad, tc, church, Global.getSector().getEntityById("beholder_station").getName());
			return true;
		}
		else if (currentStage == Stage.PARTY) 
		{
			info.addPara(Sunrider_MiscFunctions.getString("missionVows_partyNextStep"), 
					pad, tc, church, Global.getSector().getEntityById("beholder_station").getName());
			return true;
		}
		return false;
	}
	
	@Override
	public String getBaseName() {
		return Sunrider_MiscFunctions.getString("missionVows_name");
	}	
	
	@Override
	protected void notifyEnding() {
		Misc.makeNonStoryCritical(curateLoc, "sunrider_misisonVows");
	}
	
	public static class HaveShrineRequirement implements MarketRequirement {

		@Override
		public boolean marketMatchesRequirement(MarketAPI market) {
			return market.hasTag(Tags.LUDDIC_SHRINE);
		}		
	}
}
