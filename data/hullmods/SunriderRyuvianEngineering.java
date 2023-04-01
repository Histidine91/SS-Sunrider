package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.BaseHullMod;

public class SunriderRyuvianEngineering extends BaseHullMod {

	private static final float WEAPON_HEALTH = 1500f;
	private static final float ENGINE_HEALTH = 2000f;
	

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getWeaponHealthBonus().modifyPercent(id, WEAPON_HEALTH);
		stats.getEngineHealthBonus().modifyPercent(id, ENGINE_HEALTH);	
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) WEAPON_HEALTH + "%";
		if (index == 1) return "" + (int) ENGINE_HEALTH + "%";			
		return null;
	}



}


