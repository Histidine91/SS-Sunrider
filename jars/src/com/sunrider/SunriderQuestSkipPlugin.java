package com.sunrider;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.sunrider.console.EnableMission;
import com.sunrider.missions.SRVows;
import exerelin.campaign.ExerelinSetupData;
import exerelin.campaign.questskip.BaseQuestSkipPlugin;

import java.util.Map;
import java.util.Random;

public class SunriderQuestSkipPlugin extends BaseQuestSkipPlugin {

    @Override
    public void onNewGameAfterTimePass() {
        if (!ExerelinSetupData.getInstance().corvusMode) return;

        PersonAPI ava = null;
        Map<String, Boolean> quests = chain.getEnabledQuestMap();
        if (chain.isQuestEnabled("sunrider_firstArrival", quests)) {
            EnableMission.addAvaIfNeeded();
            ava = SRPeople.createAvaIfNeeded();
            addSunrider();

            ava.getRelToPlayer().adjustRelationship(0.05f, RepLevel.WELCOMING); // handholding
        }
        if (chain.isQuestEnabled("sunrider_origins", quests)) {
            ava.getRelToPlayer().adjustRelationship(0.03f, RepLevel.WELCOMING); // praise; no lewdness in bar
            addSaviorBlueprint();
        }

        if (chain.isQuestEnabled("sunrider_nightmareBreakdown", quests)) {
            ava.getRelToPlayer().adjustRelationship(0.02f, RepLevel.WELCOMING); // comfort about work
        }

        if (chain.isQuestEnabled("sunrider_iniquity", quests)) {
            addPACTSupports();
            ava.getStats().setLevel(ava.getStats().getLevel() + 1);
            ava.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);

            ava.getRelToPlayer().adjustRelationship(0.25f, RepLevel.FRIENDLY); // confess
        }

        if (chain.isQuestEnabled("sunrider_vows", quests)) {
            pickRandomPlush();

            PersonAPI bride = SRPeople.getOrCreateBride();
            PersonAPI groom = SRPeople.getOrCreateGroom();
            float amount = (5 + 2 + 10 + 10 + 5)/100f;  // all positive options taken
            bride.getRelToPlayer().adjustRelationship(amount, RepLevel.COOPERATIVE);
            groom.getRelToPlayer().adjustRelationship(amount, RepLevel.COOPERATIVE);
        }

        if (chain.isQuestEnabled("sunrider_showdown", quests)) {
            Global.getSector().getPlayerFleet().getCargo().addSpecial(new SpecialItemData("ship_bp", "SunriderFalcon"), 1);
        }
    }

    protected void addSunrider() {
        FleetMemberAPI sunrider = addShipToPlayerFleet("Sunridership_Hull");
        sunrider.setShipName(sunrider.getHullSpec().getHullName());
        sunrider.setVariant(sunrider.getVariant().clone(), false, false);
        sunrider.getVariant().setSource(VariantSource.REFIT);
        sunrider.getVariant().addPermaMod(HullMods.COMP_STRUCTURE);
        sunrider.getVariant().addPermaMod(HullMods.FAULTY_GRID);
        sunrider.getVariant().addPermaMod("damaged_mounts");
    }

    protected void addSaviorBlueprint() {
        SpecialItemData special = new SpecialItemData("weapon_bp", "SunriderSavior");
        CargoAPI pc = Global.getSector().getPlayerFleet().getCargo();
        CargoStackAPI stack = Global.getFactory().createCargoStack(CargoAPI.CargoItemType.SPECIAL, special, pc);
        stack.setSize(1);
        pc.addFromStack(stack);
    }

    protected void addPACTSupports() {
        CargoAPI loot = Global.getFactory().createCargo(true);
        loot.addFighters("SunriderPactSupportA", 1);
        CargoStackAPI pactA = loot.getStacksCopy().get(0);
        loot.clear();
        loot.addFighters("SunriderPactSupportB", 1);
        CargoStackAPI pactB = loot.getStacksCopy().get(0);

        CargoAPI player = Global.getSector().getPlayerFleet().getCargo();
        player.addFromStack(pactA);
        player.addFromStack(pactB);
    }

    protected void pickRandomPlush() {
        String seedStr = Global.getSector().getSeedString().replaceAll("[^0-9]", "");
        Long seed = Long.parseLong(seedStr);

        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>(new Random(seed));
        picker.addAll(SRVows.PLUSH_TO_SKILL.keySet());
        String plush = picker.pick();
        Global.getSector().getCharacterData().getMemoryWithoutUpdate().set("$sunrider_vowsPrize", plush);
        String skill = SRVows.PLUSH_TO_SKILL.get(plush);
        Global.getSector().getCharacterData().getPerson().getStats().setSkillLevel(skill, 1);
    }

    @Override
    public boolean shouldShow() {
        return false;   // disabled for now
    }
}
