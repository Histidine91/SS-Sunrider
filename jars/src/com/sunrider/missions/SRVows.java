package com.sunrider.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.Sunrider_MiscFunctions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.sunrider.SRPeople;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.MathUtils;

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
	public static final Map<String, String> PLUSH_TO_SKILL = new HashMap<>();
	public static final String TESSERACT_CORE_ID = Commodities.ALPHA_CORE;
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
	protected String playerShipVariant;

	static {
		PLUSH_TO_SKILL.put("galleon", "sunrider_plushGalleon");
		PLUSH_TO_SKILL.put("dogoo", "sunrider_plushDogoo");
		PLUSH_TO_SKILL.put("rensouhou", "sunrider_plushRensouhou");
	}
	
	// runcode com.sunrider.missions.SRVows.debug()
	public static void debug() {
		SRVows mission = (SRVows)Global.getSector().getMemoryWithoutUpdate().get("$sunrider_missionVows_ref");
		SectorEntityToken beholder = Global.getSector().getEntityById("beholder_station");
		mission.makeImportant(beholder, "$sunrider_missionVows_shrine3", Stage.PARTY);

		PersonAPI person = mission.groom;
		person.getStats().setLevel(7);
		person.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
		person.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
		person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
		person.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
		person.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
		person.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 1);
		person.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 1);
	}
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		if (!setGlobalReference(MISSION_REF)) {
			return false;
		}
		
		SectorEntityToken beholder = Global.getSector().getEntityById("beholder_station");
		if (beholder == null) return false;

		// the curate's location isn't actually saved in vanilla, so we'll make something up
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

	public int getRandomSingingScore(MemoryAPI mem) {
		String song = mem.getString("$song");
		if (song == null) return 0;
		if (song.equals("armageddon")) return 0;
		return MathUtils.getRandomNumberInRange(1, 5);
	}

	protected void showTempHegOfficer(InteractionDialogAPI dialog) {
		PersonAPI officer = OfficerManagerEvent.createOfficer(hegOfficer.getFaction(), 7, OfficerManagerEvent.SkillPickPreference.ANY, this.genRandom);
		officer.getName().setFirst(Sunrider_MiscFunctions.getString("hegOfficerExtraNameFirst"));
		dialog.getVisualPanel().showSecondPerson(officer);
	}

	protected void showTempDM(InteractionDialogAPI dialog) {
		PersonAPI dm = bride.getFaction().createRandomPerson(FullName.Gender.MALE);
		dialog.getVisualPanel().showPersonInfo(dm, true);
	}

	protected void pickShip(InteractionDialogAPI dialog, MemoryAPI local) {
		playerShipVariant = local.getString("$rpgShip");
	}

	protected void addSkillFromPlush(InteractionDialogAPI dialog, MemoryAPI local) {
		String skill = PLUSH_TO_SKILL.get(local.getString("$prize"));
		Global.getSector().getCharacterData().getPerson().getStats().setSkillLevel(skill, 1);

		PersonAPI temp = Global.getSector().getPlayerFaction().createRandomPerson();
		temp.getStats().setSkillLevel(skill, 1);

		String skillName = Global.getSettings().getSkillSpec(skill).getName();
		dialog.getTextPanel().setFontSmallInsignia();
		dialog.getTextPanel().addPara(Sunrider_MiscFunctions.getString("missionVows_dialog_skillUnlockStr"), Misc.getHighlightColor(), skillName);
		dialog.getTextPanel().setFontInsignia();
		dialog.getTextPanel().addSkillPanel(temp, false);
	}

	protected void setTempFleetLocation(CampaignFleetAPI fleet) {
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		fleet.setContainingLocation(pf.getContainingLocation());
		fleet.setLocation(pf.getLocation().x, pf.getLocation().y);
	}

	protected CampaignFleetAPI createEnemyFleet() {
		CampaignFleetAPI fleet = FleetFactoryV3.createEmptyFleet(Factions.REMNANTS, FleetTypes.PATROL_SMALL, null);
		fleet.setNoFactionInName(true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY, true);

		FleetMemberAPI member = fleet.getFleetData().addFleetMember("tesseract_Attack");
		PersonAPI captain = Misc.getAICoreOfficerPlugin(TESSERACT_CORE_ID).createPerson(TESSERACT_CORE_ID, Factions.REMNANTS, this.genRandom);
		fleet.getFleetData().addOfficer(captain);
		fleet.setCommander(captain);
		member.setCaptain(captain);
		fleet.getFleetData().setFlagship(member);

		fleet.addTag("cbm_SunriderEpicBravery");
		fleet.getMemoryWithoutUpdate().set("$sunrider_music", "cbm_SunriderEpicBravery");

		return fleet;
	}

	protected CampaignFleetAPI createFriendlyFleet() {
		CampaignFleetAPI fleet = FleetFactoryV3.createEmptyFleet(Factions.LUDDIC_CHURCH, FleetTypes.PATROL_LARGE, null);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY, true);

		// you
		PersonAPI player = Global.getSector().getPlayerPerson();
		//fleet.getFleetData().addOfficer(player);
		FleetMemberAPI member = fleet.getFleetData().addFleetMember(playerShipVariant);
		member.setCaptain(player);
		fleet.getFleetData().setFlagship(member);
		fleet.setCommander(player);

		// Gabriel
		member = fleet.getFleetData().addFleetMember("invictus_Standard");
		//fleet.getFleetData().addOfficer(groom);
		member.setCaptain(groom);

		// le other NPCs
		fleet.getFleetData().addFleetMember("retribution_Standard");
		fleet.getFleetData().addFleetMember("mora_Strike");
		fleet.getFleetData().addFleetMember("condor_Support");
		fleet.getFleetData().addFleetMember("manticore_Balanced");
		fleet.getFleetData().addFleetMember("vanguard_Outdated");
		fleet.getFleetData().addFleetMember("lasher_luddic_church_Standard");

		fleet.getCargo().addCrew((int)fleet.getFleetData().getMinCrew());

		for (FleetMemberAPI curr : fleet.getFleetData().getMembersListCopy()) {
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}

		//fleet.getFleetData().sort();
		//fleet.getFleetData().setSyncNeeded();
		//fleet.getFleetData().syncIfNeeded();

		setTempFleetLocation(fleet);

		return fleet;
	}

	protected void createBattle(InteractionDialogAPI dialog, final Map<String, MemoryAPI> memoryMap) {
		final SectorEntityToken entity = dialog.getInteractionTarget();

		final CampaignFleetAPI playerFleetBackup = Global.getSector().getPlayerFleet();

		final CampaignFleetAPI enemyFleetTemp = createEnemyFleet();
		final CampaignFleetAPI playerFleetTemp = createFriendlyFleet();
		dialog.setInteractionTarget(enemyFleetTemp);
		Global.getSector().setPlayerFleet(playerFleetTemp);

		final FleetInteractionDialogPluginImpl.FIDConfig config = new FleetInteractionDialogPluginImpl.FIDConfig();
		config.leaveAlwaysAvailable = false;
		config.showCommLinkOption = false;
		config.showEngageText = false;
		config.showFleetAttitude = false;
		config.showTransponderStatus = false;
		config.showWarningDialogWhenNotHostile = false;
		config.alwaysAttackVsAttack = true;
		config.impactsAllyReputation = false;
		config.impactsEnemyReputation = false;
		config.pullInAllies = false;
		config.pullInEnemies = false;
		config.pullInStations = false;
		config.lootCredits = false;
		config.straightToEngage = true;

		config.dismissOnLeave = false;
		config.printXPToDialog = true;

		final RPGFIDP plugin = new RPGFIDP(config);

		final InteractionDialogPlugin originalPlugin = dialog.getPlugin();
		config.delegate = new FleetInteractionDialogPluginImpl.BaseFIDDelegate() {
			@Override
			public void notifyLeave(InteractionDialogAPI dialog) {
				enemyFleetTemp.despawn();
				playerFleetTemp.despawn();

				Global.getSector().setPlayerFleet(playerFleetBackup);

				dialog.setPlugin(originalPlugin);
				dialog.setInteractionTarget(Global.getSector().getEntityById("beholder_station"));

				EngagementResultAPI result = plugin.getLastResult();
				FleetEncounterContext context = (FleetEncounterContext) plugin.getContext();
				log.info("Checking outcome:");
				log.info(String.format("ERAPI: did player win? %s", result.didPlayerWin()));
				log.info(String.format("FEC: player won last %s, most recent %s, ultimate %s",
						context.didPlayerWinLastEngagement(), context.didPlayerWinMostRecentBattleOfEncounter(), context.didPlayerWinEncounterOutright()));
				if (result.didPlayerWin()) {
					memoryMap.get(MemKeys.PLAYER).set("$sunrider_missionVows_wonRPG", true);
				}

				FireBest.fire(null, dialog, memoryMap, "Sunrider_MissionVows_RPGResult");
			}
			@Override
			public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
				bcc.aiRetreatAllowed = false;
				bcc.enemyDeployAll = true;
			}
			@Override
			public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage) {
			}
		};

		dialog.setPlugin(plugin);
		plugin.init(dialog);
		plugin.optionSelected(null, FleetInteractionDialogPluginImpl.OptionId.CONTINUE_INTO_BATTLE);
	}
	
	@Override
	public boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) 
	{
		MemoryAPI local = memoryMap.get(MemKeys.LOCAL);
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
			case "genSingingResult":
				local.set("$songResult", getRandomSingingScore(local), 0);
				return true;
			case "pickShip":
				pickShip(dialog, local);
				return true;
			case "beginRPGBattle":
				createBattle(dialog, memoryMap);
				return true;
			case "showTempHegOfficer":
				showTempHegOfficer(dialog);
				return true;
			case "showTempDM":
				showTempDM(dialog);
				return true;
			case "addSkillFromPlush":
				addSkillFromPlush(dialog, local);
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
		set("$sunrider_missionVows_curateLast", curate.getName().getLast());
		boolean female = curate.getName().getGender() == FullName.Gender.FEMALE;
		set("$sunrider_missionVows_curateFatherOrMother", Global.getSettings().getString("sunrider", female ? "mother" : "father"));
		set("$sunrider_missionVows_curateHeOrShe", curate.getHeOrShe());
		set("$sunrider_missionVows_curateHisOrHer", curate.getHisOrHer());
		set("$sunrider_missionVows_curateHimOrHer", curate.getHimOrHer());

		set("$sunrider_missionVows_saintHeOrShe", hegOfficer.getHeOrShe());
		set("$sunrider_missionVows_saintHimOrHer", hegOfficer.getHimOrHer());
		set("$sunrider_missionVows_saintHisOrHer", hegOfficer.getHisOrHer());
		set("$sunrider_missionVows_SaintHeOrShe", Misc.ucFirst(hegOfficer.getHeOrShe()));
		set("$sunrider_missionVows_SaintHimOrHer", Misc.ucFirst(hegOfficer.getHimOrHer()));
		set("$sunrider_missionVows_SaintHisOrHer", Misc.ucFirst(hegOfficer.getHisOrHer()));
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
		else if (currentStage == Stage.FIND_CURATE)
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
