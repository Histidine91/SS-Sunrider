package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.magiclib.util.MagicIncompatibleHullmods;

public class SunriderIronhogMod extends BaseHullMod {
	
	public static final String MOD_ID = "SunriderIronhogMod";
	
	public static final Set INCOMPATIBLE_HULLMODS = new HashSet(Arrays.asList(
		"missleracks"
	));	
	
	public static final float BALLISTIC_ROF_MOD = 20f;   // 20% higher ROF
	public static final float MISSILE_DAMAGE_MOD = 20f;   // 20% higher damage
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	
		stats.getBallisticRoFMult().modifyPercent(id, BALLISTIC_ROF_MOD);
		stats.getMissileWeaponDamageMult().modifyPercent(id, MISSILE_DAMAGE_MOD);		
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
		if (index == 0) return "" + (int) Math.round(BALLISTIC_ROF_MOD*100f) + "%";
		if (index == 1) return "" + (int) Math.round(MISSILE_DAMAGE_MOD*100f) + "%";
		return null;
	}
	

}