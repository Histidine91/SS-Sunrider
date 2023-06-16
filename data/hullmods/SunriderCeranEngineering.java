package data.hullmods;

import com.fs.starfarer.api.Global;
import java.util.Iterator;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.util.Misc;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;




public class SunriderCeranEngineering extends BaseHullMod {
	
    public static final float ECM_BONUS = 5f;	
	public static final float SHIELD_BONUSES = 250f;
	public static final float SPEED_BOOST_REDUCTION = 40f;
	public static final float FLUX_DISSIPATION_MULT = 25f;
	public static final float CR_BONUS = 0.3f;
	public static final float SUPPLY_USE_MULTIPLIER = 25f;

    public void applyEffectsBeforeShipCreation(final ShipAPI.HullSize hullSize, final MutableShipStatsAPI stats, final String id) {
        stats.getDynamic().getMod("electronic_warfare_flat").modifyFlat(id, ECM_BONUS);
		stats.getShieldTurnRateMult().modifyPercent(id, SHIELD_BONUSES);
		stats.getShieldUnfoldRateMult().modifyPercent(id, SHIELD_BONUSES);
		stats.getZeroFluxSpeedBoost().modifyMult(id, (100f-SPEED_BOOST_REDUCTION)/100f);
	    stats.getVentRateMult().modifyMult(id, 0f);
		stats.getFluxDissipation().modifyMult(id, ((100f+FLUX_DISSIPATION_MULT)/100f));
	    stats.getMaxCombatReadiness().modifyFlat(id, CR_BONUS);
		stats.getSuppliesPerMonth().modifyPercent(id, SUPPLY_USE_MULTIPLIER);
    }

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
        return (ship != null) && !ship.getVariant().hasHullMod("advancedshieldemitter");
	}
	

	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("advancedshieldemitter")) {
			return "Incompatible with Accelerated Shields.";
		}
		
		return null;
	}
    
    public String getDescriptionParam(final int index, final ShipAPI.HullSize hullSize) {
		if (index == 0) return "" + (int) (CR_BONUS * 100f) + "%";
	    if (index == 1) return "" + (int) ECM_BONUS + "%";
        if (index == 2) return "" + (int) SHIELD_BONUSES + "%";
	    if (index == 3) return "" + (int) FLUX_DISSIPATION_MULT + "%";
		if (index == 4) return "" + (int) SUPPLY_USE_MULTIPLIER + "%";
		if (index == 5) return "" + (int) -SPEED_BOOST_REDUCTION + "%";

			
        return null;
    }
}

