package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.BaseHullMod;

public class SpaceWhaleRyuvianEngineering extends BaseHullMod {

	private static final float WEAPON_HEALTH = 4000f;
	private static final float ENGINE_HEALTH = 5000f;
	private static final float ENERGY_DAMAGE_TAKEN = 0f;
	

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getWeaponHealthBonus().modifyPercent(id, WEAPON_HEALTH);
		stats.getEngineHealthBonus().modifyPercent(id, ENGINE_HEALTH);	
		stats.getEnergyShieldDamageTakenMult().modifyMult(id, ENERGY_DAMAGE_TAKEN);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) WEAPON_HEALTH + "%";
		if (index == 1) return "" + (int) ENGINE_HEALTH + "%";
		if (index == 2) return "immune";
		return null;
	}



}


