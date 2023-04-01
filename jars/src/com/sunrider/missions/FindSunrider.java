package com.sunrider.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.CurrentLocationChangedListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Voices;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.impl.campaign.plog.PlaythroughLog;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddShip;
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.Sunrider_MiscFunctions;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.sunrider.AvaLeaveEveryFrameScript;
import com.sunrider.SunriderSatBombListener;
import java.awt.Color;
import java.util.List;
import java.util.Map;

/**
 * The main code body for the first mission.
 * @author Histidine
 */
public class FindSunrider extends HubMissionWithSearch implements CurrentLocationChangedListener, SunriderMissionInterface 
{	
	public static final String AVA_ID = "sunrider_ava";
	public static final String SALVAGER_ID = "sunrider_suzuki";
	public static final String MISSION_REF = "$sunrider_findSunrider_ref";
	// I accidentally made it set both in different places, but not sure the non-alt version is actually being set?
	// even though it looks like it should be
	// ...maybe because it's set at mission generation and I just need to try with a new save
	@Deprecated public static final String MEM_FLAG_COMPLETE = "$sunrider_findSunrider_missionCompleted";
	public static final String MEM_FLAG_COMPLETE_ALT = "$sunrider_findSunrider_complete";
	
	public static final int HIRE_COST = 10000;

	public static enum Stage {
		TALK_TO_PIRATE,
		FIND_SCAVENGER,
		RECOVERY,
		COMPLETED,
		FAILED
	}
	
	protected PersonAPI salvager;
	protected PersonAPI ava;
	protected MarketAPI salvagerMarket;
	protected SectorEntityToken wreck;
	protected StarSystemAPI wreckLoc;
	
	// runcode com.sunrider.missions.FindSunrider.debug()
	public static void debug() {
		FindSunrider mission = (FindSunrider)Global.getSector().getMemoryWithoutUpdate().get(MISSION_REF);
		mission.addFailureStages(Stage.FAILED);
	}
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		if (!setGlobalReference(MISSION_REF)) {
			return false;
		}
		
		requireMarketIsNot(createdAt);
		requireMarketFaction(Factions.INDEPENDENT);
		requireMarketNotHidden();
		requireMarketNotInHyperspace();
		salvagerMarket = pickMarket();
		if (salvagerMarket == null) return false;
		
		this.requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_UNSAFE, Tags.THEME_CORE_POPULATED);
		requireSystemNotHasPulsar();
		// try at least 25 LY but not more than 40
		preferSystemOutsideRangeOf(salvagerMarket.getContainingLocation().getLocation(), 25);
		preferSystemWithinRangeOf(salvagerMarket.getContainingLocation().getLocation(), 40);
		wreckLoc = pickSystem();
		if (wreckLoc == null) return false;
		
		//createAvaIfNeeded();	// mission isn't generated until after the bar encounter, so we need to make her on game load
		ava = Global.getSector().getImportantPeople().getPerson(AVA_ID);
		salvager = createSalvager();
		Misc.makeStoryCritical(salvagerMarket, "sunrider_findSunrider");
		
		personOverride = ava;
		setRepPersonChangesMedium();
		
		setStoryMission();
		setStartingStage(Stage.TALK_TO_PIRATE);
		addSuccessStages(Stage.COMPLETED);
		addFailureStages(Stage.FAILED);
		
		beginStageTrigger(Stage.COMPLETED);
		triggerSetGlobalMemoryValue(MEM_FLAG_COMPLETE, true);
		endTrigger();
		
		setStageOnMemoryFlag(Stage.FAILED, Global.getSector().getMemoryWithoutUpdate(), "$sunrider_avaLeft");
		
		Global.getSector().getListenerManager().addListener(this);
		SunriderSatBombListener.addIfNeeded();
		
		return true;
	}
	
	@Override
	protected void notifyEnding() {
		// unprotect salvagerMarket and remove Ava and Suzuki from important people here?
		// maybe keep them as important people in case we need to reference them again
		
		Misc.makeNonStoryCritical(salvagerMarket, "sunrider_findSunrider");
		salvagerMarket.removePerson(salvager);
		salvagerMarket.getCommDirectory().removePerson(salvager);
		
		// cleanup stuff
		if (wreck != null && wreck.isAlive()) {
			wreck.getContainingLocation().removeEntity(wreck);
		}
		Global.getSector().getListenerManager().removeListener(this);
		
		if (currentStage != Stage.COMPLETED)
			Sunrider_MiscFunctions.addBountyDelayedScript();
	}	
	
	/*
		actions done before getting here:
		- generate Ava on game load
		- generate mission after finishing convo with Ava
		needed actions here:
		- set stage after bribing pirate, add/unhide Suzuki
		- set stage after talking to Suzuki, hide Suzuki, spawn the Sunrider derelict
		- end of salvage interaction: show Ava's skills
		- end of salvage interaction: hire Ava, add Sunrider to fleet, end mission
	*/
	@Override
	public boolean callEvent(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
		String action = params.get(0).getString(memoryMap);
		
		switch (action) {
			case "talkedToPirate":
				salvagerMarket.addPerson(salvager);
				salvagerMarket.getCommDirectory().addPerson(salvager);
				makeImportant(salvager, "$sunrider_findSunrider_talkToSalvager", Stage.FIND_SCAVENGER);
				setCurrentStage(Stage.FIND_SCAVENGER, dialog, memoryMap);
				return true;
			case "talkedToSalvager":
				salvagerMarket.removePerson(salvager);
				salvagerMarket.getCommDirectory().removePerson(salvager);
				createSunrider();
				setCurrentStage(Stage.RECOVERY, dialog, memoryMap);
				return true;
			case "printSkills":
				TextPanelAPI text = dialog.getTextPanel();
				text.addSkillPanel(ava, false);
				
				text.setFontSmallInsignia();
				MutableCharacterStatsAPI stats = ava.getStats();
				String personality = Misc.lcFirst(ava.getPersonalityAPI().getDisplayName());
				text.addPara(getString("findSunrider_personalityAndLevel"), Misc.getHighlightColor(),
						personality, stats.getLevel() + "");
				text.highlightInLastPara(Misc.getHighlightColor(), personality, "" + stats.getLevel());
				text.addParagraph(ava.getPersonalityAPI().getDescription());
				text.setFontInsignia();
				
				return true;
			case "complete":
				completeMission(dialog, params, memoryMap);
				return true;
		}
		return super.callEvent(ruleId, dialog, params, memoryMap);
	}
	
	// intel text in intel screen description
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		
		if (currentStage == Stage.TALK_TO_PIRATE) {
			info.addPara(getString("findSunrider_talkToPirateDesc"), opad, h, salvager.getNameString());
		}
		else if (currentStage == Stage.FIND_SCAVENGER) {
			String pers = salvager.getNameString();
			String mark = salvagerMarket.getName();
			String str = getString("findSunrider_talkToSalvagerDesc");
			str = str.replace("$onOrAt", salvagerMarket.getOnOrAt());
			LabelAPI label = info.addPara(str, opad, h, pers, mark);
			label.setHighlightColors(h, salvagerMarket.getTextColorForFactionOrPlanet());			
		}
		else if (currentStage == Stage.RECOVERY) {
			info.addPara(getString("findSunrider_recoverDesc"), opad, h, wreckLoc.getNameWithLowercaseType());
		}
	}
	
	// intel text in message popups, or intel list on left side of screen
	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.TALK_TO_PIRATE) {
			info.addPara(getString("findSunrider_talkToPirateNextStep"), pad, tc, h, salvager.getNameString());
			return true;
		}
		else if (currentStage == Stage.FIND_SCAVENGER) {
			String pers = salvager.getNameString();
			String mark = salvagerMarket.getName();
			String str = getString("findSunrider_talkToSalvagerNextStep");
			str = str.replace("$onOrAt", salvagerMarket.getOnOrAt());
			LabelAPI label = info.addPara(str, pad, tc, h, pers, mark);
			label.setHighlightColors(h, salvagerMarket.getTextColorForFactionOrPlanet());
		}
		else if (currentStage == Stage.RECOVERY) {
			info.addPara(getString("findSunrider_recoverNextStep"), pad, tc, h, wreckLoc.getNameWithLowercaseType());
		}
		return false;
	}
	
	@Override
	public String getBaseName() {
		return getString("findSunrider_name");
	}
	
	/**
	 * Creates the Sunrider wreck in the selected system and sets its defenders.
	 */
	protected void createSunrider() {
		String variantId = "Sunridership_Hull";	// FIXME needs actual variant?
		
		DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(new ShipRecoverySpecial.PerShipData(variantId,
				DerelictShipEntityPlugin.pickBadCondition(null)), false);
		LocData loc = new LocData(EntityLocationType.UNCOMMON, null, wreckLoc);
		wreck = spawnDerelict(params, loc);
		setEntityMissionRef(wreck, MISSION_REF);
		makeImportant(wreck, "$sunrider_findSunrider_target", Stage.RECOVERY);
		
		CampaignFleetAPI defender = Global.getFactory().createEmptyFleet(Factions.OMEGA, "Nightmares", true);
		defender.getFleetData().addFleetMember("SunriderNMsim");
		defender.getFleetData().addFleetMember("SunriderNMsim");
		defender.addTag("cbm_SunriderBoss");
		for (FleetMemberAPI member : defender.getFleetData().getMembersListWithFightersCopy()) {
			member.setVariant(member.getVariant().clone(), false, false);
			member.getVariant().setSource(VariantSource.REFIT);
			member.getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);
			member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR());
		}
		wreck.getMemoryWithoutUpdate().set("$hasDefenders", defender);
		wreck.getMemoryWithoutUpdate().set("$defenderFleet", defender);
	}
	
	/**
	 * Mission completion: configure the Sunrider, add it and Ava to player fleet.
	 * @param dialog
	 * @param params
	 * @param memoryMap
	 */
	protected void completeMission(InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) 
	{
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		
		FleetMemberAPI sunrider = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "Sunridership_Hull");
		sunrider.setShipName(sunrider.getHullSpec().getHullName());
		
		sunrider.setVariant(sunrider.getVariant().clone(), false, false);
		sunrider.getVariant().setSource(VariantSource.REFIT);
		sunrider.getVariant().addPermaMod(HullMods.COMP_STRUCTURE);
		sunrider.getVariant().addPermaMod(HullMods.FAULTY_GRID);
		sunrider.getVariant().addPermaMod("damaged_mounts");
		
		float minCR = playerFleet.getStats().getDynamic().getMod(Stats.RECOVERED_HULL_MIN).computeEffective(0);
		float maxCR = playerFleet.getStats().getDynamic().getMod(Stats.RECOVERED_HULL_MIN).computeEffective(0);
		float cr = (float)(minCR + Math.random() * (maxCR - minCR));
		float hull = (float)(minCR + Math.random() * (maxCR - minCR));
		sunrider.getRepairTracker().setCR(cr);
		sunrider.getStatus().setHullFraction(hull);		
		
		playerFleet.getFleetData().addFleetMember(sunrider);
		AddShip.addShipGainText(sunrider, dialog.getTextPanel());
		
		playerFleet.getFleetData().addOfficer(ava);
		AddRemoveCommodity.addOfficerGainText(ava, dialog.getTextPanel());
		//sunrider.setCaptain(ava);	// don't auto assign since it allows use-while-over-officer-limit exploit
		
		AddRemoveCommodity.addCreditsLossText(HIRE_COST, dialog.getTextPanel());
		playerFleet.getCargo().getCredits().subtract(HIRE_COST);
		float debt = -playerFleet.getCargo().getCredits().get();
		if (debt > 0) {
			int lastDebt = SharedData.getData().getPreviousReport().getDebt();
			SharedData.getData().getPreviousReport().setDebt((int)(debt + lastDebt));
		}
		
		Global.getSector().getPlayerStats().addStoryPoints(1, dialog.getTextPanel(), false);
		setRepFactionChangesNone();	// since Ava is now player faction, no need to set rep
		setCurrentStage(Stage.COMPLETED, dialog, memoryMap);
		Misc.fadeAndExpire(wreck);
		
		PlaythroughLog.getInstance().addEntry(getString("findSunrider_playthroughLogText"), true);
		
		Global.getSector().getMemoryWithoutUpdate().set(MEM_FLAG_COMPLETE_ALT, true);
		Global.getSector().getMemoryWithoutUpdate().set("$sunrider_mission2_delay", true, SRMission2.DELAY_BEFORE_AVAILABLE);
		Global.getSector().getMemoryWithoutUpdate().set("$sunrider_mission3_delay", true, SRMission3.DELAY_BEFORE_AVAILABLE);
	}
	
	@Override
	protected void updateInteractionDataImpl() {
		set("$sunrider_findSunrider_salvagerMarket", salvagerMarket.getName());
		set("$sunrider_findSunrider_salvagerOnOrAt", salvagerMarket.getOnOrAt());
		set("$sunrider_findSunrider_salvagerSystem", salvagerMarket.getContainingLocation().getNameWithLowercaseTypeShort());
		set("$sunrider_findSunrider_systemName", wreckLoc.getNameWithLowercaseType());
	}	

	// If we enter the Sunrider's system, and Ava wants to leave due to sat bombing, set up the script to display the message
	@Override
	public void reportCurrentLocationChanged(LocationAPI prev, LocationAPI curr) {
		if (curr == wreckLoc && Sunrider_MiscFunctions.doesAvaWantToLeave()) {
			AvaLeaveEveryFrameScript.addScript(false);
		}
	}
	
	// =========================================================================
	// =========================================================================
	
	public static String getString(String id) {
		return Global.getSettings().getString("sunrider_missions", id);
	}
	
	public static PersonAPI createAvaIfNeeded() {
		PersonAPI person = Global.getSector().getImportantPeople().getPerson(AVA_ID);
		if (person != null) return person;
		
		person = Global.getFactory().createPerson();
		person.setId(AVA_ID);
		person.setVoice(Voices.SOLDIER);
		person.setFaction(Factions.INDEPENDENT);
		person.setGender(FullName.Gender.FEMALE);
		person.setRankId(Ranks.SPACE_COMMANDER);
		person.setPostId(Ranks.POST_OFFICER);
		person.getName().setFirst(getString("avaNameFirst"));
		person.getName().setLast(getString("avaNameLast"));
		person.setPortraitSprite("graphics/portraits/Portrait_Ava.png");
		person.getMemoryWithoutUpdate().set("$chatterChar", "sunrider_ava");
		person.getMemoryWithoutUpdate().set("$nex_noOfficerDeath", true);	// waifus do not die when killed
		
		// set skills (8 combat skills)
		person.getStats().setLevel(8);
		person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
		person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
		person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
		person.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
		person.getStats().setSkillLevel(Skills.BALLISTIC_MASTERY, 2);
		person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
		//person.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 2);
		person.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
		person.getStats().setSkillLevel("sunrider_SunridersMother", 2);
		person.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);	// bonus
		
		Global.getSector().getImportantPeople().addPerson(person);
		return person;
	}
	
	public static PersonAPI createSalvager() {
		PersonAPI person = Global.getSector().getImportantPeople().getPerson(SALVAGER_ID);
		if (person != null) return person;
		
		person = Global.getFactory().createPerson();
		person.setId(SALVAGER_ID);
		person.setVoice(Voices.SPACER);
		person.setFaction(Factions.INDEPENDENT);
		person.setGender(FullName.Gender.MALE);
		person.setRankId(Ranks.SPACE_CAPTAIN);
		person.setPostId(Ranks.POST_SPACER);
		person.getName().setFirst(getString("salvagerNameFirst"));
		person.getName().setLast(getString("salvagerNameLast"));
		person.setPortraitSprite("graphics/portraits/portrait31.png");
		Global.getSector().getImportantPeople().addPerson(person);
		return person;
	}
}
