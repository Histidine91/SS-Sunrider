package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class SunriderGunboatMod extends BaseHullMod {
	
	public static final float BALLISTIC_DAMAGE_MOD = 20f;   // +20% ballistic damage
	public static final float BALLISTIC_VELOCITY_MOD = 25f;   // +25% ballistic projectile velocity
	public static final float FRIGATE_FIGHTER_DAMAGE_MOD = 10f;   // +10% damage to frigates and fighters
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {	
		stats.getBallisticWeaponDamageMult().modifyPercent(id, BALLISTIC_DAMAGE_MOD);
		stats.getBallisticProjectileSpeedMult().modifyPercent(id, BALLISTIC_VELOCITY_MOD);
		stats.getDamageToFighters().modifyPercent(id, FRIGATE_FIGHTER_DAMAGE_MOD);
	}
	
	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) Math.round(BALLISTIC_DAMAGE_MOD) + "%";
		if (index == 1) return "" + (int) Math.round(BALLISTIC_VELOCITY_MOD) + "%";
		if (index == 2) return "" + (int) Math.round(FRIGATE_FIGHTER_DAMAGE_MOD) + "%";
		return null;
	}
}