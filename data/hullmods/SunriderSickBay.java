package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.BaseHullMod;

public class SunriderSickBay extends BaseHullMod {
	
    public static final float CREW_LOSSES = 50f;
//  public static final float CREW_RECOVERY = 25f;
    
    public void applyEffectsBeforeShipCreation(final ShipAPI.HullSize hullSize, final MutableShipStatsAPI stats, final String id) {
        stats.getDynamic().getStat("replacement_rate_decrease_mult").modifyMult(id, 1.0f - CREW_LOSSES / 100.0f);
//      stats.getDynamic().getStat("replacement_rate_increase_mult").modifyPercent(id, CREW_RECOVERY);
    }
    
    public String getDescriptionParam(final int index, final ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int) CREW_LOSSES + "%";
//		if (index == 1) return "" + (int) CREW_RECOVERY + "%";
        return null;
    }
	
	public boolean isApplicableToShip(final ShipAPI ship) {
        final int baysModified = (int)ship.getMutableStats().getNumFighterBays().getModifiedValue();
        if (baysModified <= 0) {
            return false;
        }
        final int bays = (int)ship.getMutableStats().getNumFighterBays().getBaseValue();
        return ship != null && bays > 0;
    }
    
    public String getUnapplicableReason(final ShipAPI ship) {
        return "Ship does not have standard fighter bays";
    }
	
	
}

