package data.hullmods;

import com.fs.starfarer.api.Global;
import java.util.Iterator;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.util.HashMap;
import java.util.Map;



public class SunriderRyuvianAfterburners extends BaseHullMod {
	
	private static Map speed = new HashMap();
	static {
		speed.put(HullSize.FRIGATE, 130f);
		speed.put(HullSize.DESTROYER, 110f);
		speed.put(HullSize.CRUISER, 50f);
		speed.put(HullSize.CAPITAL_SHIP, 30f);
	}
		

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, 2f);
		stats.getZeroFluxSpeedBoost().modifyFlat(id , (Float) speed.get(hullSize));
	    stats.getVentRateMult().modifyMult(id, 0f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) speed.get(HullSize.FRIGATE)).intValue();
		if (index == 1) return "" + ((Float) speed.get(HullSize.DESTROYER)).intValue();
		if (index == 2) return "" + ((Float) speed.get(HullSize.CRUISER)).intValue();
		if (index == 3) return "" + ((Float) speed.get(HullSize.CAPITAL_SHIP)).intValue();
					
		return null;
	}
	

	@Override
	public boolean isApplicableToShip(final ShipAPI ship) {
        return (ship != null) && !ship.getVariant().hasHullMod("safetyoverrides");
	}
	

	public String getUnapplicableReason(final ShipAPI ship) {
		if (ship.getVariant().hasHullMod("safetyoverrides")) {
			return "Incompatible with Safety Overrides.";
		}
		
		return null;
	}
}


