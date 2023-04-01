package com.sunrider.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.DamperFieldStats;
import static com.fs.starfarer.api.impl.combat.DamperFieldStats.getDamper;
import java.awt.Color;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Machiavelli's ship system.
 * @author Histidine
 */
public class SunriderIronWillStats extends DamperFieldStats {
	
	protected static Map mag = new HashMap();
	static {
		mag.put(ShipAPI.HullSize.FIGHTER, 0.5f);
		mag.put(ShipAPI.HullSize.FRIGATE, 0.5f);
		mag.put(ShipAPI.HullSize.DESTROYER, 0.5f);
		mag.put(ShipAPI.HullSize.CRUISER, 0.6f);
		mag.put(ShipAPI.HullSize.CAPITAL_SHIP, 0.6f);
	}
	
	public static final float ROF_BONUS = 0.6f;
	public static final float FLUX_REDUCTION = 37.5f;
	public static final Color GLOW_COLOR = new Color(255,200,0,155);
	
	protected Object STATUSKEY2 = new Object();
	protected Object STATUSKEY3 = new Object();
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		//effectLevel = 1f;
		
		float mult = (Float) mag.get(ShipAPI.HullSize.CRUISER);
		if (stats.getVariant() != null) {
			mult = (Float) mag.get(stats.getVariant().getHullSize());
		}
		stats.getHullDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
		stats.getArmorDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
		stats.getEmpDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
		
		float multROF = 1f + ROF_BONUS * effectLevel;
		stats.getBallisticRoFMult().modifyMult(id, multROF);
		stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f - (FLUX_REDUCTION * 0.01f));
		
		
		ShipAPI ship = null;
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
		}
		if (player) {
			ShipSystemAPI system = getDamper(ship);
			if (system != null) {
				float percent = (1f - mult) * effectLevel * 100;
				Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1,
					system.getSpecAPI().getIconSpriteName(), system.getDisplayName(),
					String.format(getString("systemIronWillDesc1"), (int)Math.round(percent)), false);
				percent = (multROF - 1) * 100;
				Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY2,
					system.getSpecAPI().getIconSpriteName(), system.getDisplayName(),
					String.format(getString("systemIronWillDesc2"), (int)Math.round(percent)), false);
				Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY3,
					system.getSpecAPI().getIconSpriteName(), system.getDisplayName(),
					String.format(getString("systemIronWillDesc3"), (int)FLUX_REDUCTION), false);
			}
		}
		
		if (ship != null) {
			ship.setWeaponGlow(effectLevel, GLOW_COLOR, EnumSet.of(WeaponAPI.WeaponType.BALLISTIC));
		}
	}
	
	public static String getString(String id) {
		return Global.getSettings().getString("sunrider", id);
	}
}
