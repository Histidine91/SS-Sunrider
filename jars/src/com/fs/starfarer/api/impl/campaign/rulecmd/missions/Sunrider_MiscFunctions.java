package com.fs.starfarer.api.impl.campaign.rulecmd.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMission;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import static com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin.getEntityMemory;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.loading.PersonMissionSpec;
import com.fs.starfarer.api.util.DelayedActionScript;
import com.fs.starfarer.api.util.Misc;
import com.sunrider.NexUtils;
import com.sunrider.SRPeople;
import com.sunrider.missions.FindSunrider;
import com.sunrider.missions.SunriderMissionInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.log4j.Logger;

/**
 * Miscellaneous functions for Sunrider mission and sat bomb handling.
 * @author Histidine
 */
public class Sunrider_MiscFunctions extends BaseCommandPlugin {
	
	public static Logger log = Global.getLogger(Sunrider_MiscFunctions.class);
    
	public static final String MEM_KEY_AVA_WANT_LEAVE = "$sunrider_avaWantLeave";
	
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        String arg = params.get(0).getString(memoryMap);
		
		switch (arg) {
			case "haveSunrider":
				return haveSunrider();
			case "isAvaInParty":
				return isAvaInParty();
			case "createMission":
				return createMission(dialog, params.get(1).getString(memoryMap));
			case "declineMission":
				addBountyDelayedScript(getBountyDelayTime());
				break;
			case "canShowMission2":
				return canShowMission2(dialog, memoryMap);
			case "canShowMission3":
				return canShowMission3(dialog, memoryMap);
			case "mission2CanScan":
				return mission2CanScan(dialog, memoryMap, params.get(1).getBoolean(memoryMap));	
			case "avaLeave":
				removeAva(dialog, memoryMap, params.get(1).getBoolean(memoryMap), true);
				//updateSunriderMissions(dialog, memoryMap);	// no need, we're already hard-killing them anyway
				break;
			case "avaWantLeave":
				return doesAvaWantToLeave();
			case "avaMutiny":
				return avaMutiny(dialog, memoryMap);
			case "blockSPDisengage":
				blockSPDisengage(dialog);
				return true;
			// have you considered remembering which one it is and sticking to it? no
			case "haveItem":
			case "hasItem":
				String itemId = params.get(1).getString(memoryMap);
				int needed = 1;
				if (params.size() >= 3)
					needed = params.get(2).getInt(memoryMap);
				
				Global.getLogger(this.getClass()).info("wololo " + itemId + " " + needed);
				return Global.getSector().getPlayerFleet().getCargo().getCommodityQuantity(itemId) >= needed;
		}
		
		return false;
    }
	
	// same as BeginMission except for not accepting the mission immediately
	protected static boolean createMission(InteractionDialogAPI dialog, String missionId) {
		
		PersonMissionSpec spec = Global.getSettings().getMissionSpec(missionId);
		if (spec == null) {
			throw new RuntimeException("Mission with spec [" + missionId + "] not found");
		}
		
		HubMission mission = spec.createMission();
		
		SectorEntityToken entity = dialog.getInteractionTarget();
		PersonAPI person = entity.getActivePerson();
		
		if (person == null) {
//			throw new RuntimeException("Attempting to BeginMission " + missionId + 
//									   " in interaction with entity.getActivePerson() == null");
//			String key = "$beginMission_seedExtra";
//			String extra = person.getMemoryWithoutUpdate().getString(key);
			String extra = "";
			long seed = BarEventManager.getInstance().getSeed(null, person, extra);
//			person.getMemoryWithoutUpdate().set(key, "" + seed); // so it's not the same seed for multiple missions
			mission.setGenRandom(new Random(seed));
			
		} else {
			mission.setPersonOverride(person);
			//mission.setGenRandom(new Random(Misc.getSalvageSeed(entity)));
			String key = "$beginMission_seedExtra";
			String extra = person.getMemoryWithoutUpdate().getString(key);
			long seed = BarEventManager.getInstance().getSeed(null, person, extra);
			person.getMemoryWithoutUpdate().set(key, "" + seed); // so it's not the same seed for multiple missions
			mission.setGenRandom(new Random(seed));
		}
		
		mission.createAndAbortIfFailed(entity.getMarket(), false);

		if (mission.isMissionCreationAborted()) {
			return false;
		}
		
		return true;
	}
	
	// runcode com.fs.starfarer.api.impl.campaign.rulecmd.missions.Sunrider_MiscFunctions.addBountyDelayedScript(0);
	/**
	 * Sets the memory flag to enable the Sunrider MagicBounty event, after a delay.
	 * @param time
	 */
	public static void addBountyDelayedScript(float time) {
		Global.getSector().addScript(new DelayedActionScript(time) {
			@Override
			public void doAction() {
				Global.getSector().getMemoryWithoutUpdate().set("$sunrider_bountyEnabled", true);
			}
		});
	}
	
	public static void addBountyDelayedScript() {
		addBountyDelayedScript(getBountyDelayTime());
	}
	
	public static boolean canShowMission2(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) 
	{
		if (!isAvaInParty()) return false;
		//if (!haveSunrider()) return false;		
		
		MarketAPI market = dialog.getInteractionTarget().getMarket();
		if (market == null) return false;
		
		// can't spawn in the same places the mission can be completed
		// force player to walk a bit
		if (Factions.TRITACHYON.equals(market.getFactionId()) || Factions.PLAYER.equals(market.getFactionId()))
		{
			return false;
		}
		
		boolean m1complete =  Global.getSector().getMemoryWithoutUpdate().getBoolean(FindSunrider.MEM_FLAG_COMPLETE_ALT);
		//Global.getLogger(Sunrider_MiscFunctions.class).info("wololo " + m1complete);
		return m1complete;
	}
	
	public static boolean canShowMission3(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) 
	{
		if (!isAvaInParty()) return false;
		//if (!haveSunrider()) return false;		
		
		MarketAPI market = dialog.getInteractionTarget().getMarket();
		if (market == null) return false;
		
		boolean m1complete =  Global.getSector().getMemoryWithoutUpdate().getBoolean(FindSunrider.MEM_FLAG_COMPLETE_ALT);
		//Global.getLogger(Sunrider_MiscFunctions.class).info("wololo " + m1complete);
		return m1complete;
	}
	
	public static boolean mission2CanScan(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap, boolean checkForAdmin) 
	{
		PersonAPI pers = dialog.getInteractionTarget().getActivePerson();
		if (pers == null) return false;
		
		if (checkForAdmin) {
			boolean admin = Ranks.POST_ADMINISTRATOR.equals(pers.getPostId()) || pers == dialog.getInteractionTarget().getMarket().getAdmin();
			if (!admin) return false;
		}		
		
		if (!isAvaInParty()) return false;
		if (doesAvaWantToLeave()) return false;
		
		return haveSunrider();
	}
	
	public static boolean haveSunrider() {
		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
			if (member.getHullId().equals("Sunridership")) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Informs all running Sunrider missions to check their stage changes and triggers.
	 * @param dialog
	 * @param memoryMap
	 */
	public static void updateSunriderMissions(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		for (IntelInfoPlugin info : Global.getSector().getIntelManager().getIntel(SunriderMissionInterface.class)) {
			if (info instanceof BaseHubMission) {
				BaseHubMission mission = (BaseHubMission)info;
				if (mission.isEnding() || mission.isEnded()) continue;
				mission.checkStageChangesAndTriggers(dialog, memoryMap);
			}
		}
	}
	
	protected static void removeAva(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap, boolean unsetCaptain, boolean repImpact) 
	{
		PersonAPI ava = Global.getSector().getImportantPeople().getPerson(SRPeople.AVA_ID);
		if (ava == null) return;
		
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		if (pf.getFleetData().getOfficerData(ava) != null) {
			pf.getFleetData().removeOfficer(ava);
			AddRemoveCommodity.addOfficerLossText(ava, dialog.getTextPanel());
			// remove Ava from any ship she's on
			if (unsetCaptain) {
				FleetMemberAPI member = getAvasFleetMember();
				if (member != null) member.setCaptain(null);
			}
		}
		
		if (repImpact) {
			CoreReputationPlugin.CustomRepImpact impact = new CoreReputationPlugin.CustomRepImpact();
			impact.delta = -0.1f;
			impact.ensureAtBest = RepLevel.HOSTILE;
			Global.getSector().adjustPlayerReputation(new RepActionEnvelope(RepActions.CUSTOM, impact,
													  null, dialog.getTextPanel(), true), ava);
		}		
		
		Global.getSector().getMemoryWithoutUpdate().set("$sunrider_avaLeft", true);
		terminateMissions(dialog, memoryMap);
	}
	
	public static boolean doesAvaWantToLeave() {
		if (!isAvaInParty()) return false;
		return Global.getSector().getMemoryWithoutUpdate().getBoolean(MEM_KEY_AVA_WANT_LEAVE);
	}
	
	public static float getBountyDelayTime() {
		return 365 * (float)(3 + Math.random());
	}
	
	public static boolean isAvaInParty() {
		PersonAPI ava = Global.getSector().getImportantPeople().getPerson(SRPeople.AVA_ID);
		if (ava == null) return false;
		
		boolean missionOngoing = Global.getSector().getMemoryWithoutUpdate().contains(FindSunrider.MISSION_REF);
		if (missionOngoing) return true;
		
		OfficerDataAPI avaOfficer = Global.getSector().getPlayerFleet().getFleetData().getOfficerData(ava);
		if (avaOfficer == null) return false;
		
		return true;
	}
	
	/**
	 * Adapted from {@code SalvageDefenderInteraction.java}; sets up the battle with Ava during the mutiny.
	 * @param dialog
	 * @param memoryMap
	 * @return
	 */
	public boolean avaMutiny(InteractionDialogAPI dialog, final Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		final SectorEntityToken entity = dialog.getInteractionTarget();
		final MemoryAPI memory = getEntityMemory(memoryMap);

		final CampaignFleetAPI avaFleet = generateAvaFleet(dialog, memoryMap);
		
		Misc.giveStandardReturnToSourceAssignments(avaFleet, true);
		
		dialog.setInteractionTarget(avaFleet);
		
		final FleetInteractionDialogPluginImpl.FIDConfig config = new FleetInteractionDialogPluginImpl.FIDConfig();
		config.showCommLinkOption = false;
		config.showEngageText = false;
		config.showFleetAttitude = false;
		config.showTransponderStatus = false;
		config.showWarningDialogWhenNotHostile = false;
		config.alwaysAttackVsAttack = true;
		config.impactsEnemyReputation = false;
		config.pullInAllies = true;
		config.pullInEnemies = false;
		config.pullInStations = false;
		
		config.dismissOnLeave = false;
		config.printXPToDialog = true;
		
		boolean nex = Global.getSettings().getModManager().isModEnabled("nexerelin");
		final FleetInteractionDialogPluginImpl plugin;
		if (nex) {
			plugin = NexUtils.getNexFIDPI(config);
		}			
		else {
			plugin = new FleetInteractionDialogPluginImpl(config);
		}
		
		final InteractionDialogPlugin originalPlugin = dialog.getPlugin();
		config.delegate = new FleetInteractionDialogPluginImpl.BaseFIDDelegate() {
			@Override
			public void notifyLeave(InteractionDialogAPI dialog) {
								
				dialog.setPlugin(originalPlugin);
				dialog.setInteractionTarget(entity);
				
				if (plugin.getContext() instanceof FleetEncounterContext) {
					FleetEncounterContext context = (FleetEncounterContext) plugin.getContext();
					if (context.didPlayerWinEncounterOutright()) {
						FireBest.fire(null, dialog, memoryMap, "Sunrider_BeatAvaContinue");
					} else {
						//Global.getLogger(this.getClass()).info("Attempting dialog dismiss");
						dialog.dismiss();
					}
				} else {					
					dialog.dismiss();
				}
			}
			@Override
			public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
				bcc.aiRetreatAllowed = false;
				bcc.objectivesAllowed = false;
				bcc.enemyDeployAll = true;
			}			
		};
		
		dialog.setPlugin(plugin);
		plugin.init(dialog);
		
		blockSPDisengage(dialog);
	
		return true;
	}
	
	public CampaignFleetAPI generateAvaFleet(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		CampaignFleetAPI fleet = FleetFactoryV3.createEmptyFleet(Factions.INDEPENDENT, FleetTypes.MERC_PRIVATEER, null);
		PersonAPI ava = Global.getSector().getImportantPeople().getPerson(SRPeople.AVA_ID);
		
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		FleetMemberAPI member = getAvasFleetMember();
		pf.getFleetData().removeFleetMember(member);
		fleet.getFleetData().addFleetMember(member);
		fleet.getFleetData().setFlagship(member);
		
		removeAva(dialog, memoryMap, false, false);
		fleet.getFleetData().addOfficer(ava);
		fleet.setCommander(ava);
		member.setCaptain(ava);
		fleet.setAIMode(true);
		fleet.setName(Global.getSettings().getString("sunrider", "mutinyFleetName"));
		fleet.setFaction(Factions.INDEPENDENT, true);
		fleet.setNoFactionInName(true);
				
		// not sure if needed but can't hurt, or can it?
		//fleet.setInflated(true);
		//fleet.setInflater(null);
		//fleet.inflateIfNeeded();
		fleet.getFleetData().setSyncNeeded();
		fleet.getFleetData().syncIfNeeded();
		//fleet.forceSync();
		
		pf.getContainingLocation().addEntity(fleet);
		fleet.setLocation(pf.getLocation().x, pf.getLocation().y);
		
		Misc.makeHostile(fleet);
		Misc.makeLowRepImpact(fleet, "avaRage");
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_IGNORE_PLAYER_COMMS, true);
		fleet.getMemoryWithoutUpdate().set("$sunrider_avaMutiny", true);
		
		return fleet;
	}
	
	public static boolean avaCanMutiny() {
		// Ava must be in party and in command of a ship
		if (!isAvaInParty()) {
			return false;
		}
		FleetMemberAPI member = getAvasFleetMember();
		if (member == null) {
			return false;
		}
		
		// don't mutiny if too beat up
		if (member.isMothballed()) return false;
		if (member.isCivilian()) return false;
		if (!member.canBeDeployedForCombat()) return false;
		if (member.getRepairTracker().getCR() < 0.3f) return false;
		if (member.getStatus().getHullFraction() < 0.4f) return false;
		
		// if Ava's ship is Sunrider, always mutiny
		if (member.getHullId().equals("Sunridership")) return true;
		
		// check our DP versus that of player's other ships; will fight at 2-1 odds
		float dp = member.getDeploymentPointsCost();
		float playerDP = 0;
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		for (FleetMemberAPI playerMember : pf.getFleetData().getMembersListCopy()) {
			if (playerMember == member) continue;
			if (playerMember.isCivilian()) continue;
			if (playerMember.isMothballed()) continue;
			if (!playerMember.canBeDeployedForCombat()) continue;
			playerDP += playerMember.getDeploymentPointsCost();
		}
		log.info(String.format("DP ratio: %s vs %s", dp, playerDP));
		
		if (playerDP > dp * 2) return false;
		
		return true;
	}
	
	/**
	 * Kill all ongoing Sunrider missions if Ava leaves. Needed because missions can run in parallel 
	 * and the first one to see the {@code $sunrider_avaLeft} memory key eats it.
	 * @param dialog
	 * @param memoryMap
	 */
	public static void terminateMissions(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		List<Misc.Token> params = new ArrayList<>();
		params.add(new Misc.Token("endFailure", Misc.TokenType.LITERAL));
		for (IntelInfoPlugin info : Global.getSector().getIntelManager().getIntel(SunriderMissionInterface.class)) 
		{
			BaseHubMission bhm = (BaseHubMission)info;
			if (bhm.isEnding() || bhm.isEnded()) continue;
			bhm.callEvent(null, dialog, params, memoryMap);
		}
	}
	
	/**
	 * Gets the ship in player fleet that Ava is on.
	 * @return
	 */
	public static FleetMemberAPI getAvasFleetMember() {
		PersonAPI ava = Global.getSector().getImportantPeople().getPerson(SRPeople.AVA_ID);
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		for (FleetMemberAPI member : pf.getFleetData().getMembersListCopy()) {
			if (member.getCaptain() == ava) {
				return member;
			}
		}
		return null;
	}
	
	public static void blockSPDisengage(InteractionDialogAPI dialog) {
		dialog.getOptionPanel().setEnabled(FleetInteractionDialogPluginImpl.OptionId.CLEAN_DISENGAGE, false);
        dialog.getOptionPanel().setTooltip(FleetInteractionDialogPluginImpl.OptionId.CLEAN_DISENGAGE, 
				Global.getSettings().getString("sunrider", "mutinyBlockSPDisengageTooltip"));
	}
	
	public static String getString(String id) {
		return Global.getSettings().getString("sunrider_missions", id);
	}
}
