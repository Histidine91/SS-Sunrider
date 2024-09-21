package com.sunrider.missions;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.ShowImageVisual;
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.Sunrider_MiscFunctions;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DelayedActionScript;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.sunrider.SRPeople;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SRShowdown extends HubMissionWithSearch implements SunriderMissionInterface, FleetEventListener {

    public enum Stage {
        FIRST_SYSTEM,
        SECOND_SYSTEM,
        REPORT_BACK,
        COMPLETED,
        FAILED
    }

    public static final float FALCON_DECRYPT_DAYS = 10;

    /*
        TODO:
           Code to handle mission completion from dialog, bla bla
     */

    protected StarSystemAPI system;
    protected StarSystemAPI system2;
    protected CampaignFleetAPI fleet;
    protected boolean wonBattle;
    protected SectorEntityToken falconWreck;

    @Override
    protected boolean create(MarketAPI createdAt, boolean barEvent) {
        if (!setGlobalReference("$sunrider_missionShowdown_ref")) {
            return false;
        }

        SectorEntityToken beholder = Global.getSector().getEntityById("beholder_station");

        requireSystemTags(ReqMode.ALL, Tags.THEME_REMNANT_MAIN);
        requireSystemTags(ReqMode.NOT_ANY, Tags.TRANSIENT, Tags.SYSTEM_CUT_OFF_FROM_HYPER, Tags.THEME_HIDDEN);
        requireSystemOutsideRangeOf(beholder.getLocationInHyperspace(), 15);
        preferSystemWithinRangeOf(beholder.getLocationInHyperspace(), 20);
        preferSystemUnexplored();
        system2 = pickSystem();
        if (system2 == null) {
            Global.getLogger(this.getClass()).info("Could not pick system 2");
            return false;
        }

        requireSystemNotHasPulsar();
        //requireSystemNotBlackHole();
        requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_UNSAFE, Tags.THEME_CORE, Tags.THEME_REMNANT,
                Tags.TRANSIENT, Tags.SYSTEM_CUT_OFF_FROM_HYPER, Tags.THEME_HIDDEN);
        preferSystemWithinRangeOf(system2.getLocation(), 15);
        preferSystemUnexplored();

        system = pickSystem();
        if (system == null) {
            Global.getLogger(this.getClass()).info("Could not pick system 1");
            return false;
        }

        setStoryMission();

        setStartingStage(Stage.FIRST_SYSTEM);
        setSuccessStage(Stage.COMPLETED);
        setFailureStage(Stage.FAILED);

        setCreditReward(5000000);
        setRepFactionChangesVeryHigh();
        setRepPersonChangesVeryHigh();

        makeImportant(system.getHyperspaceAnchor(), "$sunrider_missionShowdown_system", Stage.FIRST_SYSTEM);
        makeImportant(system2.getHyperspaceAnchor(), "$sunrider_missionShowdown_system2", Stage.SECOND_SYSTEM);

        return true;
    }

    @Override
    public void acceptImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        beginWithinHyperspaceRangeTrigger(system.getHyperspaceAnchor(), 3, false, Stage.FIRST_SYSTEM);
        triggerRunScriptAfterDelay(0, new ShowdownEventScript(this, Stage.FIRST_SYSTEM));
        endTrigger();

        beginWithinHyperspaceRangeTrigger(system2.getHyperspaceAnchor(), 3, false, Stage.SECOND_SYSTEM);
        triggerRunScriptAfterDelay(0, new ShowdownEventScript(this, Stage.SECOND_SYSTEM));
        endTrigger();

        beginStageTrigger(Stage.REPORT_BACK);
        triggerSetMemoryValueAfterDelay(0, Global.getSector().getCharacterData().getMemoryWithoutUpdate(), "$sunrider_missionShowdown_reportBack", true);
        endTrigger();
    }

    @Override
    protected void updateInteractionDataImpl() {
        set("$sunrider_missionShowdown_system", system.getNameWithLowercaseType());
        set("$sunrider_missionShowdown_system2", system2.getNameWithLowercaseType());
    }

    protected void spawnHegemonyDerelict() {
        String variantId = getRandomVariantId("enforcer_xiv");

        DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(
                new ShipRecoverySpecial.PerShipData(variantId,
                        ShipRecoverySpecial.ShipCondition.WRECKED, 0f), false);

        SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
        ship.setDiscoverable(true);
        SectorEntityToken orbitFocus = getClosestJumpPoint(system);
        ship.setCircularOrbit(orbitFocus, genRandom.nextFloat() * 360f, 150f, 365f);
        makeImportant(ship, "$sunrider_missionShowdown_important", Stage.FIRST_SYSTEM);
        ship.getMemoryWithoutUpdate().set("$sunrider_missionShowdown_hegemonyWreck", true);
    }

    protected FleetMemberAPI addFleetMember(String variantId, String aiCoreId) {
        FleetMemberAPI member = fleet.getFleetData().addFleetMember(variantId);
        PersonAPI captain = Misc.getAICoreOfficerPlugin(aiCoreId).createPerson(aiCoreId, Factions.REMNANTS, this.genRandom);
        member.setCaptain(captain);

        float maxCR = member.getRepairTracker().getMaxCR();
        member.getRepairTracker().setCR(maxCR);

        return member;
    }

    protected void spawnFleet() {
        fleet = FleetFactoryV3.createEmptyFleet(Factions.REMNANTS, FleetTypes.PATROL_LARGE, null);
        FleetMemberAPI flag = addFleetMember("SR_RF_Boss", Commodities.OMEGA_CORE);

        fleet.setCommander(flag.getCaptain());  // needs to be done before setFlagship, which forces the fleet commander to ride the flagship
        fleet.getFleetData().setFlagship(flag);

        //member.setFlagship(true); // see if this is needed first

        for (int i=0; i<4; i++) {
            addFleetMember("SunriderNMhard", Commodities.ALPHA_CORE);
        }
        addFleetMember("SR_RC_1", Commodities.GAMMA_CORE);
        addFleetMember("SR_RC_1", Commodities.GAMMA_CORE);
        addFleetMember("SR_RC_1", Commodities.GAMMA_CORE);

        addFleetMember("SR_RC_2", Commodities.BETA_CORE);
        addFleetMember("SR_RC_2", Commodities.BETA_CORE);

        addFleetMember("SR_RC_3", Commodities.BETA_CORE);
        addFleetMember("SR_RC_3", Commodities.BETA_CORE);

        fleet.setName(Sunrider_MiscFunctions.getString("missionShowdown_fleetName"));

        fleet.getFleetData().sort();
        fleet.getFleetData().setSyncNeeded();
        fleet.getFleetData().syncIfNeeded();
        fleet.inflateIfNeeded();

        MemoryAPI mem = fleet.getMemoryWithoutUpdate();

        mem.set(MemFlags.MEMORY_KEY_MAKE_ALWAYS_PURSUE, true);
        mem.set(MemFlags.MEMORY_KEY_PURSUE_PLAYER, true);
        mem.set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
        mem.set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
        mem.set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE_ONE_BATTLE_ONLY, true);
        mem.set(MemFlags.MEMORY_KEY_LOW_REP_IMPACT, true);
        mem.set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true);
        mem.set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        mem.set(MemFlags.FLEET_DO_NOT_IGNORE_PLAYER, true);
        mem.set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, true);
        mem.set("$sunrider_missionShowdown_fleet", true);
        mem.set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN,
                new SRMission4.NoRetreatFIDConfigGen(false));


        mem.set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "Fallen_Angel_Pt3");
        fleet.addTag("cbm_sunrider_senritsu");   // for Custom Battle Music mod
        mem.set("$sunrider_music", "Sora_no_Senritsu");

        fleet.setNoFactionInName(true);

        fleet.inflateIfNeeded();
        ShipVariantAPI var = flag.getVariant().clone();
        var.setSource(VariantSource.REFIT);
        flag.setVariant(var, false, false);
        flag.getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);

        makeImportant(fleet, "$sunrider_missionShowdown_fleet_imp", Stage.SECOND_SYSTEM);
        Misc.addDefeatTrigger(fleet, "Sunrider_MissionShowdown_FleetDefeated");

        LocData loc = new LocData(EntityLocationType.ORBITING_PLANET_OR_STAR, null, system2);

        SectorEntityToken token = spawnEntityToken(loc);
        system2.addEntity(fleet);
        fleet.setLocation(token.getLocation().x, token.getLocation().y);
        fleet.addEventListener(this);
        system2.removeEntity(token);
        //Global.getLogger(this.getClass()).info("Ryuvian fleet created in " + fleet.getContainingLocation().getName());
    }

    public String getRandomVariantId(String hullId) {
        List<String> variants = Global.getSettings().getHullIdToVariantListMap().get(hullId);
        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>();
        picker.addAll(variants);
        String variantId = picker.pick();
        if (variantId == null) variantId = hullId + "_Hull";
        return variantId;
    }

    public SectorEntityToken getClosestJumpPoint(StarSystemAPI system) {
        return Misc.getDistressJumpPoint(system);
    }

    @Override
    public boolean callAction(String action, String ruleId, final InteractionDialogAPI dialog, List<Misc.Token> params, final Map<String, MemoryAPI> memoryMap)
    {
        switch (action) {
            case "printGoodLuckText":
                // maybe I don't want this
                //dialog.getTextPanel().setFontVictor();
                return true;
            case "advanceToStage2":
                setCurrentStage(Stage.SECOND_SYSTEM, dialog, memoryMap);
                return true;
            case "delayedSetEncounterVisual":
                Global.getSector().addTransientScript(new EveryFrameScript() {
                    protected boolean done;

                    @Override
                    public boolean isDone() {
                        return done;
                    }

                    @Override
                    public boolean runWhilePaused() {
                        return true;
                    }

                    @Override
                    public void advance(float amount) {
                        List<Misc.Token> params = new ArrayList<>();
                        params.add(new Misc.Token("SunriderShowdown", Misc.TokenType.LITERAL));
                        new ShowImageVisual().execute(null, Global.getSector().getCampaignUI().getCurrentInteractionDialog(),
                                params, memoryMap);
                        done = true;
                    }
                });
                return true;
            case "wonEncounter":
                reportWonBattle(dialog, memoryMap);
                return true;
            case "startFalconDecrypt":
                // add an everyframe that triggers dialog after timer
                // (we could also set two memkeys to trigger dialog on dock, but this way makes more sense)
                Global.getSector().addScript(new FalconDecryptScript(FALCON_DECRYPT_DAYS));
                return true;
            case "complete":
                completeMission(dialog, memoryMap);
                return true;
        }
        return false;
    }

    public void reportWonBattle(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        wonBattle = true;
        setCurrentStage(Stage.REPORT_BACK, dialog, memoryMap);
        //spawnFalcon();
        Global.getSector().addScript(new SRMissionUtils.OpenDialogScript("Sunrider_MissionShowdown_PostEncounterDialogStart"));
    }

    public void completeMission(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        // temporary code, remove later
        setCreditReward(5000000);
        setRepFactionChangesVeryHigh();
        setRepPersonChangesVeryHigh();

        PersonAPI hegOfficer = SRPeople.getOrCreateHegOfficer();

        CoreReputationPlugin.MissionCompletionRep completionRepPerson = new CoreReputationPlugin.MissionCompletionRep(
                getRepRewardSuccessPerson(), getRewardLimitPerson(),
                -getRepPenaltyFailurePerson(), getPenaltyLimitPerson());
        CoreReputationPlugin.MissionCompletionRep completionRepFaction = new CoreReputationPlugin.MissionCompletionRep(
                getRepRewardSuccessFaction(), getRewardLimitFaction(),
                -getRepPenaltyFailureFaction(), getPenaltyLimitFaction());

        TextPanelAPI text = dialog != null ? dialog.getTextPanel() : null;
        boolean withMessage = text != null;

        // rep with Hegemony and its officer, Church is handled automatically since Holyoak is the mission giver
        CoreReputationPlugin.RepActions action = CoreReputationPlugin.RepActions.MISSION_SUCCESS;
        Global.getSector().adjustPlayerReputation(
                new CoreReputationPlugin.RepActionEnvelope(action, completionRepPerson,
                        dialog.getTextPanel(), true, withMessage),
                hegOfficer);
        Global.getSector().adjustPlayerReputation(
                new CoreReputationPlugin.RepActionEnvelope(action, completionRepFaction,
                        dialog.getTextPanel(), true, withMessage),
                hegOfficer.getFaction().getId());

        setCurrentStage(Stage.COMPLETED, dialog, memoryMap);
        Global.getSector().getCharacterData().getMemoryWithoutUpdate().set("$sunrider_missionShowdown_missionCompleted", true);
        Global.getSector().getCharacterData().getMemoryWithoutUpdate().unset("$sunrider_missionShowdown_reportBack");
    }

    @Deprecated
    protected void spawnFalcon() {
        String variantId = "SR_RF_Boss";    // FIXME correct?

        DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(
                new ShipRecoverySpecial.PerShipData(variantId,
                        ShipRecoverySpecial.ShipCondition.PRISTINE, 0f), false);

        falconWreck = BaseThemeGenerator.addSalvageEntity(system2,
                Entities.WRECK, Factions.NEUTRAL, params);
        falconWreck.setDiscoverable(true);
        falconWreck.setLocation(fleet.getLocation().x, fleet.getLocation().y);
        makeImportant(falconWreck, "$sunrider_missionShowdown_falcon_impFlag", Stage.SECOND_SYSTEM, Stage.REPORT_BACK);
        falconWreck.getMemoryWithoutUpdate().set("$sunrider_missionShowdown_falcon", true);
    }

    @Override
    public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();

        if (currentStage == Stage.FIRST_SYSTEM)
        {

            info.addPara(Sunrider_MiscFunctions.getString("missionShowdown_firstSystemDesc"), opad, h,
                    system.getBaseName());
        }
        if (currentStage == Stage.SECOND_SYSTEM)
        {
            info.addPara(Sunrider_MiscFunctions.getString("missionShowdown_secondSystemDesc"), opad, h,
                    system2.getBaseName());
        }
        else if (currentStage == Stage.REPORT_BACK)
        {
            FactionAPI heg = Global.getSector().getFaction(Factions.HEGEMONY);
            FactionAPI church = Global.getSector().getFaction(Factions.LUDDIC_CHURCH);
            LabelAPI label = info.addPara(Sunrider_MiscFunctions.getString("missionShowdown_reportBackDesc"), opad, h,
                    heg.getDisplayName(), church.getDisplayName());
            label.setHighlightColors(heg.getBaseUIColor(), church.getBaseUIColor());
        }
    }

    // intel text in message popups, or intel list on left side of screen
    @Override
    public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
        Color hl = Misc.getHighlightColor();

        if (currentStage == Stage.FIRST_SYSTEM)
        {
            info.addPara(this.getGoToSystemTextShort(system),
                    pad, tc, hl, system.getNameWithLowercaseTypeShort());
            return true;
        }
        else if (currentStage == Stage.SECOND_SYSTEM)
        {
            info.addPara(this.getGoToSystemTextShort(system2),
                    pad, tc, hl, system2.getNameWithLowercaseTypeShort());
            return true;
        }
        else if (currentStage == Stage.REPORT_BACK)
        {
            FactionAPI heg = Global.getSector().getFaction(Factions.HEGEMONY);
            FactionAPI church = Global.getSector().getFaction(Factions.LUDDIC_CHURCH);
            LabelAPI label = info.addPara(Sunrider_MiscFunctions.getString("missionShowdown_reportBackNextStep"), pad, tc,
                    heg.getDisplayName(), church.getDisplayName());
            label.setHighlightColors(heg.getBaseUIColor(), church.getBaseUIColor());
            return true;
        }
        return false;
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map, Object currentStage) {
        // stupid hack to make showMap work correctly after accepting
        if (this.currentStage == Stage.FIRST_SYSTEM) {
            return system.getHyperspaceAnchor();
        }
        return super.getMapLocation(map, currentStage);
    }

    @Override
    public String getBaseName() {
        return Sunrider_MiscFunctions.getString("missionShowdown_name");
    }

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
        if (fleet == this.fleet && !wonBattle) {
            reportWonBattle(null, null);
        }
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {

    }

    public static class FalconDecryptScript extends DelayedActionScript {


        public FalconDecryptScript(float daysLeft) {
            super(daysLeft);
        }

        @Override
        public void doAction() {
            Global.getSector().addScript(new SRMissionUtils.OpenDialogScript("Sunrider_MissionShowdown_FalconDecryptDialogStart"));
        }
    }

    public static class ShowdownEventScript implements Script {

        SRShowdown mission;
        Stage stage;

        public ShowdownEventScript(SRShowdown mission, Stage stage) {
            this.mission = mission;
            this.stage = stage;
        }

        @Override
        public void run() {
            //Global.getLogger(this.getClass()).info("Executing event script for stage "+ stage);
            switch (stage) {
                case FIRST_SYSTEM:
                    mission.spawnHegemonyDerelict();
                    return;
                case SECOND_SYSTEM:
                    mission.spawnFleet();
                    return;
            }
        }
    }
}
