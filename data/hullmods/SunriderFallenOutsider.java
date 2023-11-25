package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.magiclib.util.MagicIncompatibleHullmods;

public class SunriderFallenOutsider extends BaseHullMod {
	
	public static final String MOD_ID = "SunriderFallenOutsider";
	
	public static final Set INCOMPATIBLE_HULLMODS = new HashSet(Arrays.asList(
		"solar_shielding"
	));
	
	public static final float SHIELD_ENERGY_DAMAGE_TAKEN_MULTIPLIER = 0.8f;   // 80% damage taken by energy->shield buff
	public static final float CORONA_EFFECT_MULTIPLIER = 0f;                // invulnerable to terrain damage
	//public static final float VENT_RATE_MULTIPLIER = 1.1f;                    // 110% ventrate (+10% buff)
	public static final float OVERLOAD_TIME_MULTIPLIER = 0.9f;                // 90% overload time
	public static final float SUPPLY_USE_MOD = 25f;                    // +25% supply use debuff
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	
		stats.getEnergyShieldDamageTakenMult().modifyMult(id, SHIELD_ENERGY_DAMAGE_TAKEN_MULTIPLIER);
		stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, CORONA_EFFECT_MULTIPLIER);
		//stats.getVentRateMult().modifyMult(id, VENT_RATE_MULTIPLIER);
		stats.getOverloadTimeMod().modifyMult(id, OVERLOAD_TIME_MULTIPLIER);
		stats.getSuppliesPerMonth().modifyPercent(id, SUPPLY_USE_MOD);		
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ShipVariantAPI variant = ship.getVariant();
		for (Object tmp : INCOMPATIBLE_HULLMODS) {
			String blocked = (String)tmp;
            if (variant.getHullMods().contains(blocked)) {
                MagicIncompatibleHullmods.removeHullmodWithWarning(variant, blocked, MOD_ID);
            }
        }
	}
	
	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) Math.round(100f - (SHIELD_ENERGY_DAMAGE_TAKEN_MULTIPLIER*100f)) + "%";
		if (index == 1) return "" + (int) Math.round(100f - (CORONA_EFFECT_MULTIPLIER*100f)) + "%";
		if (index == 2) return "" + (int) (100 - OVERLOAD_TIME_MULTIPLIER*100) + "%";
		if (index == 3) return "" + (int) SUPPLY_USE_MOD + "%";
		return null;
	}
	

}