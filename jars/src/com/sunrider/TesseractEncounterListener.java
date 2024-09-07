package com.sunrider;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;

import java.util.ArrayList;
import java.util.List;

public class TesseractEncounterListener implements FleetEventListener {

    public static final String MEMFLAG_ENCOUNTERED_OMEGA = "$sunrider_encounteredOmega";

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {

    }

    // unlock on encountering any Facet or larger omega ship as an enemy
    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        if (!battle.isPlayerInvolved()) return;

        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        for (CampaignFleetAPI otherFleet : battle.getNonPlayerSideSnapshot()) {
            boolean remnant = otherFleet.getFaction().getId().equals(Factions.REMNANTS);
            boolean omega = otherFleet.getFaction().getId().equals(Factions.OMEGA);
            if (remnant || omega) {
                List<FleetMemberAPI> toCheck = new ArrayList<>(otherFleet.getFleetData().getMembersListCopy());
                toCheck.addAll(Misc.getSnapshotMembersLost(otherFleet));
                for (FleetMemberAPI member : toCheck) {

                    if (member.getHullSpec().hasTag(Tags.OMEGA) && member.getHullSpec().getHullSize().compareTo(ShipAPI.HullSize.DESTROYER) >= 0)
                    {
                        saveMemkey();
                        Global.getSector().getListenerManager().removeListener(this);
                        return;
                    }
                }
            }
        }
    }

    protected static void saveMemkey() {
        Global.getSector().getCharacterData().getMemoryWithoutUpdate().set(MEMFLAG_ENCOUNTERED_OMEGA, true);
    }

    public static boolean hasMemkey() {
        if (Global.getSector().getCharacterData().getMemoryWithoutUpdate().getBoolean(MEMFLAG_ENCOUNTERED_OMEGA)) return true;
        if (Global.getSector().getCharacterData().getMemoryWithoutUpdate().getBoolean("$nex_defeatedTesseract")) return true;
        return false;
    }
}
