package com.sunrider.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;

public class SunriderSeraphimAwakenStats extends BaseShipSystemScript {
			
	public static final Object KEY_SHIP = new Object();
	public static final float DAMAGE_BONUS_PERCENT = 100f;
	public static final float RANGE_INCREASE = 1100f;
	public static final float PROJ_SPEED_BONUS = 100f;
	public static final float AUTOFIRE_ACC_BONUS = 100f;
	
	public static String awakenText = null;	//Global.getSettings().getString("sunrider", "systemAwakenFloaty");
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		CombatEntityAPI entity = stats.getEntity();
		if (entity != null) {
			if (state == State.IN && awakenText != null) {
				Global.getCombatEngine().addFloatingText(entity.getLocation(), awakenText, 30, Color.CYAN, entity, 0.5f, 2.5f);
			}
			((ShipAPI)entity).setJitterUnder(KEY_SHIP, new Color(168,168,255,192), effectLevel, 15, 0f, 15f);
		}
		float damBonus = DAMAGE_BONUS_PERCENT * effectLevel;
		float rangeBonus = RANGE_INCREASE * effectLevel;
		float speedBonus = PROJ_SPEED_BONUS * effectLevel;
		stats.getEnergyWeaponDamageMult().modifyPercent(id, damBonus);
		stats.getEnergyWeaponRangeBonus().modifyFlat(id, rangeBonus);
		stats.getProjectileSpeedMult().modifyPercent(id, speedBonus);
		stats.getAutofireAimAccuracy().modifyFlat(id, AUTOFIRE_ACC_BONUS * 0.01f);
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getEnergyWeaponDamageMult().unmodify(id);
		stats.getEnergyWeaponRangeBonus().unmodify(id);
		stats.getProjectileSpeedMult().unmodify(id);
		stats.getAutofireAimAccuracy().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float bonusPercent = DAMAGE_BONUS_PERCENT * effectLevel;
		if (index == 0) {
			return new StatusData("+" + (int) bonusPercent + "% energy weapon damage" , false);
		}
		if (index == 1) {
			return new StatusData("energy range increase +" + Misc.getRoundedValueMaxOneAfterDecimal(RANGE_INCREASE * effectLevel) + "su", false);
		}
		if (index == 0) {
			return new StatusData("+" + (int) AUTOFIRE_ACC_BONUS + "% autofire target leading" , false);
		}
		return null;
	}
}
