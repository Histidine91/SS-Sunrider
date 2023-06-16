package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.BaseHullMod;

public class SunriderRyderResilience extends BaseHullMod {

	private static final float WEAPON_AND_ENGINE_HEALTH = 50f;         //+50% HP buff
	private static final float WEAPON_AND_ENGINE_REPAIR_SPEED = 40f;   //-40% time to repair buff
	

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getWeaponHealthBonus().modifyPercent(id, WEAPON_AND_ENGINE_HEALTH);
		stats.getEngineHealthBonus().modifyPercent(id, WEAPON_AND_ENGINE_HEALTH);	
		stats.getCombatEngineRepairTimeMult().modifyMult(id, 1f - WEAPON_AND_ENGINE_REPAIR_SPEED * 0.01f);
		stats.getCombatWeaponRepairTimeMult().modifyMult(id, 1f - WEAPON_AND_ENGINE_REPAIR_SPEED * 0.01f);
		
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) WEAPON_AND_ENGINE_HEALTH + "%";
		if (index == 1) return "" + (int) WEAPON_AND_ENGINE_REPAIR_SPEED + "%";			
		return null;
	}



}
