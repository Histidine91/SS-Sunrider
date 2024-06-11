/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sunrider.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.Sunrider_MiscFunctions;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.sunrider.SRPeople;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * The main code body for the fourth mission.
 * @author Histidine
 */
public class SRMission4 extends HubMissionWithSearch implements SunriderMissionInterface, FleetEventListener {
	
	public static Logger log = Global.getLogger(SRMission4.class);
	public static final String MISSION_REF = "$sunrider_mission4_ref";
	public static final float DELAY_BEFORE_AVAILABLE = 30;	// from completion of mission 2 (Origins)
	
	public static final List<String> ENEMY_SHIPS = Arrays.asList(
			"SR_ABB_2", "SR_ABB_2",
			"SR_PBB_1",
			"SR_PBB_2",
			"SR_PBB_3",
			"SR_AC_2", "SR_AC_2", "SR_AC_2",
			"SR_PC_1", "SR_PC_1",
			"SR_PC_2", "SR_PC_2", 
			"SR_Corvette_1",
			"SR_Corvette_2",
			"SR_Corvette_3",
			"SR_Corvette_4",
			"SR_Ironhog_1", 
			"SR_Ironhog_2",
			"SR_Ironhog_3",
			"SR_PF_1", "SR_PF_1",
			"SR_PF_2", "SR_PF_2"
	);

	public static enum Stage {
		GO_TO_SYSTEM,
		//SALVAGE,
		RETURN_TO_SPACER,
		COMPLETED,
		FAILED
	}
	
	protected PersonAPI drunk;
	protected StarSystemAPI targetSystem;
	protected MarketAPI drunkMarket;
	protected CampaignFleetAPI fleet;
	protected SectorEntityToken carrier;
	protected boolean wonBattle;
	protected boolean haveDrunkCommsID;
	
	// runcode com.sunrider.missions.SRMission4.debug()
	public static void debug() {
		SRMission4 mission = (SRMission4)Global.getSector().getMemoryWithoutUpdate().get("$sunrider_mission4_ref");
	}
	
	// runcode com.sunrider.missions.SRMission4.enableMission()
	public static void enableMission() {
		Global.getSector().getMemoryWithoutUpdate().set("$sunrider_mission2_doneOrSkipped", true);
		Global.getSector().getMemoryWithoutUpdate().unset("$sunrider_mission4_delay");		
	}
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		if (!setGlobalReference(MISSION_REF)) {
			return false;
		}
		
		requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_UNSAFE, Tags.THEME_CORE_POPULATED, 
				Tags.TRANSIENT, Tags.SYSTEM_CUT_OFF_FROM_HYPER, Tags.THEME_HIDDEN);
		requireSystemNotHasPulsar();
		requireSystemNotBlackHole();
		// try at least 15 LY but not more than 22
		preferSystemOutsideRangeOf(createdAt.getLocationInHyperspace(), 15);
		preferSystemWithinRangeOf(createdAt.getLocationInHyperspace(), 22);
		
		targetSystem = pickSystem();
		if (targetSystem == null) return false;		
		
		personOverride = Global.getSector().getImportantPeople().getPerson(SRPeople.AVA_ID);	
		drunkMarket = createdAt;
		
		setStoryMission();
		setStartingStage(Stage.GO_TO_SYSTEM);
		addSuccessStages(Stage.COMPLETED);
		addFailureStages(Stage.FAILED);
		
		setRepPersonChangesMedium();
		setRepFactionChangesNone();
		
		drunk = SRPeople.createDrunk();		
		makeImportant(targetSystem.getHyperspaceAnchor(), "$sunrider_mission4_targetSystem_important", Stage.GO_TO_SYSTEM);
		makeImportant(drunk, "$sunrider_mission4_drunk_important", Stage.RETURN_TO_SPACER);
		
		beginWithinHyperspaceRangeTrigger(targetSystem, 2, false, Stage.GO_TO_SYSTEM);
		triggerRunScriptAfterDelay(0.1f, new SpawnEnemyFleetScript(this));
		endTrigger();
		
		/*
		beginStageTrigger(Stage.COMPLETED);
		triggerSetGlobalMemoryValuePermanent("$sunrider_mission4_missionCompleted", true);
		triggerSetGlobalMemoryValuePermanent("$sunrider_mission4_doneOrSkipped", true);
		endTrigger();
		*/
		
		return true;
	}
	
	@Override
	public void acceptImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {		
		drunkMarket.addPerson(drunk);
		Misc.makeStoryCritical(drunkMarket, "sunrider_mission4");		
		SRPeople.createSalvagerSon();
		SRPeople.createPrototype();
	}
	
	protected void spawnFleet() {
		// TODO
		fleet = FleetFactoryV3.createEmptyFleet(Factions.INDEPENDENT, FleetTypes.PATROL_LARGE, null);
		for (String variantId : ENEMY_SHIPS) {
			FleetMemberAPI member = fleet.getFleetData().addFleetMember(variantId);
		}
		int fp = fleet.getFleetPoints();
		FleetParamsV3 params = new FleetParamsV3(targetSystem.getLocation(),
					Factions.SCAVENGERS,
					null,
					FleetTypes.PATROL_LARGE,
					fp, // combatPts
					0, // freighterPts
					0, // tankerPts
					0, // transportPts
					0, // linerPts
					0, // utilityPts
					0);
		params.qualityOverride = 1f;
		params.ignoreMarketFleetSizeMult = true;
		params.averageSMods = 1;
		FleetFactoryV3.addCommanderAndOfficersV2(fleet, params, genRandom);
		
		fleet.getFlagship().setFlagship(false);		
		FleetMemberAPI flag = fleet.getFleetData().addFleetMember("SR_ABB_3");
		PersonAPI son = SRPeople.createSalvagerSon();
		flag.setCaptain(son);
		fleet.setCommander(son);
		
		for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {			
			member.getCrewComposition().addCrew(member.getHullSpec().getMinCrew());
			float maxCR = member.getRepairTracker().getMaxCR();
			//log.info(String.format("Member %s max CR is %s", member.getShipName(), maxCR));
			member.getRepairTracker().setCR(maxCR);
			//log.info(String.format("Member %s current CR is %s", member.getShipName(), member.getRepairTracker().getCR()));
		}
		
		fleet.setName(Sunrider_MiscFunctions.getString("mission4_fleetName"));
		
		fleet.getFleetData().sort();
		fleet.getFleetData().setSyncNeeded();
		fleet.getFleetData().syncIfNeeded();
		
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALWAYS_PURSUE, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PURSUE_PLAYER, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE_ONE_BATTLE_ONLY, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_LOW_REP_IMPACT, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true);
		fleet.getMemoryWithoutUpdate().set("$genericHail", true);
		fleet.getMemoryWithoutUpdate().set("$genericHail_openComms", "Sunrider_M4Hail");
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN, 
					new NoRetreatFIDConfigGen(true));
		fleet.getMemoryWithoutUpdate().set("$sunrider_mission4_fleet", true);
		fleet.addTag("cbm_Epic_Action_Hero");
		fleet.getMemoryWithoutUpdate().set("$sunrider_music", "Epic_Action_Hero");
		
		makeImportant(fleet, "$sunrider_mission4_fleet_imp", Stage.GO_TO_SYSTEM);
		Misc.addDefeatTrigger(fleet, "Sunrider_Mission4_FleetDefeated");
		
		LocData loc = new LocData(EntityLocationType.HIDDEN_NOT_NEAR_STAR, null, Global.getSector().getCurrentLocation());
		loc.loc = new BaseThemeGenerator.EntityLocation();
		loc.type = EntityLocationType.ORBITING_PLANET_OR_STAR;
		loc.updateLocIfNeeded(this, null);
		
		SectorEntityToken token = spawnEntityToken(loc);
		targetSystem.addEntity(fleet);
		fleet.setLocation(token.getLocation().x, token.getLocation().y);
		fleet.addEventListener(this);
		targetSystem.removeEntity(token);
	}
	
	public void reportWonBattle() {
		wonBattle = true;
		spawnCarrier();
		Global.getSector().addScript(new SRMissionUtils.OpenDialogScript("Sunrider_Mission4_PostEncounterDialogStart"));
	}
	
	protected void spawnCarrier() {
		String variantId = "SunriderPactBattleship_Hull";
		
		DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(
				new ShipRecoverySpecial.PerShipData(variantId, 
						ShipRecoverySpecial.ShipCondition.PRISTINE, 0f), false);

		carrier = BaseThemeGenerator.addSalvageEntity(targetSystem, 
				Entities.WRECK, Factions.NEUTRAL, params);
		carrier.setDiscoverable(true);
		carrier.setLocation(fleet.getLocation().x, fleet.getLocation().y);
		makeImportant(carrier, "$sunrider_mission4_carrier_impFlag", Stage.GO_TO_SYSTEM);
		carrier.getMemoryWithoutUpdate().set("$sunrider_mission4_carrier", true);
	}
	
	protected void addPACTSupports(InteractionDialogAPI dialog) {
		CargoAPI loot = Global.getFactory().createCargo(true);
		loot.addFighters("SunriderPactSupportA", 1);		
		CargoStackAPI pactA = loot.getStacksCopy().get(0);
		loot.clear();
		loot.addFighters("SunriderPactSupportB", 1);
		CargoStackAPI pactB = loot.getStacksCopy().get(0);
		
		CargoAPI player = Global.getSector().getPlayerFleet().getCargo();
		player.addFromStack(pactA);
		player.addFromStack(pactB);
		player.addCommodity(Commodities.METALS, 300);
		player.addCommodity(Commodities.SUPPLIES, 200);
		player.addCommodity(Commodities.FUEL, 200);
		player.addCommodity(Commodities.HEAVY_MACHINERY, 50);
		
		TextPanelAPI text = dialog.getTextPanel();
		AddRemoveCommodity.addStackGainText(pactA, text);
		AddRemoveCommodity.addStackGainText(pactB, text);
		AddRemoveCommodity.addCommodityGainText(Commodities.METALS, 300, text);
		AddRemoveCommodity.addCommodityGainText(Commodities.SUPPLIES, 200, text);
		AddRemoveCommodity.addCommodityGainText(Commodities.FUEL, 200, text);
		AddRemoveCommodity.addCommodityGainText(Commodities.HEAVY_MACHINERY, 50, text);
	}
	
	public void levelUpAva(InteractionDialogAPI dialog) {
		PersonAPI ava = SRPeople.createAvaIfNeeded();
		ava.getStats().setLevel(ava.getStats().getLevel() + 1);
		ava.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
		
		String str = Sunrider_MiscFunctions.getString("mission4_str_levelUp");
		TextPanelAPI text = dialog.getTextPanel();
		Color hl = Misc.getHighlightColor();
		text.setFontSmallInsignia();
		text.addPara(str, hl, ava.getNameString(), ava.getStats().getLevel() + "");
		str = Sunrider_MiscFunctions.getString("mission4_str_learnedSkill");
		SkillSpecAPI spec = Global.getSettings().getSkillSpec(Skills.ENERGY_WEAPON_MASTERY);
		text.addPara(str, spec.getGoverningAptitudeColor(), spec.getName());	
		text.setFontInsignia();
	}
	
	protected void setupPostEncounterDialog() {
		//FleetInteractionDialogPluginImpl fidpi = (FleetInteractionDialogPluginImpl)Global.getSector().getCampaignUI().getCurrentInteractionDialog();
		
	}
	
	public void complete(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		Misc.makeNonStoryCritical(drunkMarket, "sunrider_mission4");
		setCurrentStage(Stage.COMPLETED, dialog, memoryMap);
		//drunkMarket.removePerson(drunk);
		drunkMarket.getCommDirectory().removePerson(drunk);
		Global.getSector().getMemoryWithoutUpdate().set("$sunrider_mission4_missionCompleted", true);
		Global.getSector().getMemoryWithoutUpdate().set("$sunrider_mission4_doneOrSkipped", true);
		Global.getSector().getMemoryWithoutUpdate().set("$sunrider_missionVows_delay", true, SRVows.DELAY_BEFORE_AVAILABLE);
	}
	
	@Override
	public boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) 
	{		
		switch (action) {
			case "accept":
				accept(dialog, memoryMap);
				return true;
			case "wonEncounter":
				//setupPostEncounterDialog();
				reportWonBattle();
				return true;
			case "postEncounterDialog":
				return true;
			case "afterPostEncounterDialog":
				return true;
			case "addPACTSupports":
				addPACTSupports(dialog);
				return true;
			case "completeSalvage":
				Misc.fadeAndExpire(carrier);
				levelUpAva(dialog);
				setCurrentStage(Stage.RETURN_TO_SPACER, dialog, memoryMap);
				return true;
			case "addToCommDirectory":
				drunkMarket.getCommDirectory().addPerson(drunk);
				this.haveDrunkCommsID = true;
				return true;
			case "complete":
				complete(dialog, memoryMap);
				return true;
		}
		return false;
	}
	
	@Override
	protected void updateInteractionDataImpl() {
		//set("$sunrider_mission4_system", targetSystem.getBaseName());	// extra key because I cba to hunt down all the times I used this key in rules
		set("$sunrider_mission4_targetSystem", targetSystem.getBaseName());
		set("$sunrider_mission4_drunkMarketName", drunkMarket.getName());
		set("$sunrider_mission4_drunkMarketId", drunkMarket.getId());
		set("$sunrider_mission4_haveDrunkCommsID", haveDrunkCommsID);
	}
	
	// intel text in intel screen description
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		
		if (currentStage == Stage.GO_TO_SYSTEM) 
		{
			info.addPara(Sunrider_MiscFunctions.getString("mission4_goToSystemDesc"), opad, h, 
					targetSystem.getNameWithLowercaseTypeShort());
		} 
		else if (currentStage == Stage.RETURN_TO_SPACER) 
		{
			info.addPara(Sunrider_MiscFunctions.getString("mission4_returnDesc"), opad, h, 
					drunkMarket.getName(), drunk.getNameString());
		} 
	}
	
	// intel text in message popups, or intel list on left side of screen
	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color hl = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_SYSTEM) 
		{
			info.addPara(Sunrider_MiscFunctions.getString("mission4_goToSystemNextStep"), 
					pad, tc, hl, targetSystem.getNameWithLowercaseTypeShort());
			return true;
		}
		else if (currentStage == Stage.RETURN_TO_SPACER) 
		{
			info.addPara(Sunrider_MiscFunctions.getString("mission4_returnNextStep"), 
					pad, tc, hl, drunkMarket.getName(), drunk.getNameString());
			return true;
		}
		return false;
	}
	
	@Override
	public String getBaseName() {
		return Sunrider_MiscFunctions.getString("mission4_name");
	}	
	

	@Override
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
		if (fleet == this.fleet && !wonBattle) {
			reportWonBattle();
		}
	}

	@Override
	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		//if (primaryWinner)
	}
	
	protected static class SpawnEnemyFleetScript implements Script {
		
		protected final SRMission4 mission;

		public SpawnEnemyFleetScript(SRMission4 mission) {
			this.mission = mission;
		}		

		@Override
		public void run() {
			mission.spawnFleet();
		}
	}
	
	public static class NoRetreatFIDConfigGen implements FleetInteractionDialogPluginImpl.FIDConfigGen {

		protected boolean haveObjectives;

		public NoRetreatFIDConfigGen(boolean haveObjectives) {
			this.haveObjectives = haveObjectives;
		}

		public FleetInteractionDialogPluginImpl.FIDConfig createConfig() {
			FleetInteractionDialogPluginImpl.FIDConfig config = new FleetInteractionDialogPluginImpl.FIDConfig();
			config.pullInEnemies = false;
			config.delegate = new FleetInteractionDialogPluginImpl.BaseFIDDelegate() {
				public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage) {
					
				}
				public void notifyLeave(InteractionDialogAPI dialog) {		
					// unreliable and won't work if fleet is killed with campaign console; just use the EveryFrameScript
					/*
					dialog.setInteractionTarget(Global.getSector().getPlayerFleet());
					RuleBasedInteractionDialogPluginImpl plugin = new RuleBasedInteractionDialogPluginImpl();
					dialog.setPlugin(plugin);
					plugin.init(dialog);
					plugin.fireBest("Sunrider_Mission4_PostEncounterDialogStart");
					*/
				}
				
				public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
					bcc.aiRetreatAllowed = false;
					bcc.fightToTheLast = true;
					bcc.objectivesAllowed = haveObjectives;
				}
			};
			return config;
		}
	}
}
