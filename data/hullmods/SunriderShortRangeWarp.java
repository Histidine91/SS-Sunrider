package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.BaseHullMod;

public class SunriderShortRangeWarp extends BaseHullMod {
	
    public static final float GROUND_BONUS = 300.0f;
    
    public void applyEffectsBeforeShipCreation(final ShipAPI.HullSize hullSize, final MutableShipStatsAPI stats, final String id) {
        stats.getDynamic().getMod("ground_support").modifyFlat(id, GROUND_BONUS);
    }
    
    public String getDescriptionParam(final int index, final ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int) GROUND_BONUS;
        return null;
    }
}

