package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class SunriderFalconMod extends BaseHullMod {

	// yes the numbers are maximum cringe, don't look at me I don't balance this stuff - H.
	public static final float HULL_MOD = 900f;						// 10x health
	public static final float BALLISTIC_ENERGY_DAMAGE_MOD = 400f;	// 5x dam
	public static final float RANGE_MOD = 50f;
	public static final float RECOVER_CHANCE_MULT = 0f;
	public static final float SUPPLY_FUEL_COST_MULT = 100f;
	public static final float DP_COST_MULT = 100f;					// to block deployment unless no other ships are deployed; doesn't work
	

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getBallisticWeaponDamageMult().modifyPercent(id, BALLISTIC_ENERGY_DAMAGE_MOD);
		stats.getEnergyWeaponDamageMult().modifyPercent(id, BALLISTIC_ENERGY_DAMAGE_MOD);
		stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_MOD);
		stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_MOD);
		stats.getDynamic().getMod(Stats.SHIP_RECOVERY_MOD).modifyMult(id, RECOVER_CHANCE_MULT);
		stats.getSuppliesPerMonth().modifyMult(id, SUPPLY_FUEL_COST_MULT);
		stats.getFuelUseMod().modifyMult(id, SUPPLY_FUEL_COST_MULT);
		//stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyMult(id, DP_COST_MULT);
		stats.getHullBonus().modifyPercent(id, HULL_MOD);
		stats.getVariant().getHints().add(ShipHullSpecAPI.ShipTypeHints.UNBOARDABLE);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) HULL_MOD + "%";
		if (index == 1) return "" + (int) BALLISTIC_ENERGY_DAMAGE_MOD + "%";
		if (index == 2) return "" + (int) RANGE_MOD + "%";
		return null;
	}
}


