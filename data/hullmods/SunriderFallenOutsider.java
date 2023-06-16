package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;

public class SunriderFallenOutsider extends BaseLogisticsHullMod {
	
	public static final float SHIELD_ENERGY_DAMAGE_TAKEN_MULTIPLIER = 0.8f;   //80% damage taken by energy->shield buff
	public static final float CORONA_EFFECT_MULTIPLIER = 0.6f;                //60% damage taken from corona buff
	public static final float VENT_RATE_MULTIPLIER = 1.1f;                    //110% ventrate (+10% buff)
	public static final float OVERLOAD_TIME_MULTIPLIER = 1.2f;                //120% overload time (+20% debuff)
	public static final float SUPPLY_USE_MULTIPLIER = 25f;                    //+25% supply use debuff
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	
		stats.getEnergyShieldDamageTakenMult().modifyMult(id, SHIELD_ENERGY_DAMAGE_TAKEN_MULTIPLIER);
		stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, CORONA_EFFECT_MULTIPLIER);
		stats.getVentRateMult().modifyMult(id, VENT_RATE_MULTIPLIER);
		stats.getOverloadTimeMod().modifyMult(id, OVERLOAD_TIME_MULTIPLIER);
		stats.getSuppliesPerMonth().modifyPercent(id, SUPPLY_USE_MULTIPLIER);
	}


	
	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) Math.round(100f - (SHIELD_ENERGY_DAMAGE_TAKEN_MULTIPLIER*100f)) + "%";
		if (index == 1) return "" + (int) Math.round(100f - (CORONA_EFFECT_MULTIPLIER*100f)) + "%";
		if (index == 2) return "" + (int) ((VENT_RATE_MULTIPLIER *100f) - 100f) + "%";
		if (index == 3) return "" + (int) ((OVERLOAD_TIME_MULTIPLIER *100f) - 100f) + "%";
		if (index == 4) return "" + (int) SUPPLY_USE_MULTIPLIER + "%";
		return null;
	}
	

}