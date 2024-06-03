package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class SunriderShieldAlwaysOn extends BaseHullMod {

	public static final float OVERLOAD_DURATION_PERCENT_MOD = 50f;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getOverloadTimeMod().modifyPercent(id, OVERLOAD_DURATION_PERCENT_MOD);
	}
		
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}
	
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		super.advanceInCombat(ship, amount);

		if (!ship.isAlive()) return;
		
		// so the ship AI can't control it
		ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
		ship.blockCommandForOneFrame(ShipCommand.VENT_FLUX);
		
		String id = "sunrider_shield_always_on";
		
		if (ship.getFluxTracker().isOverloadedOrVenting()) {
			//ship.getMutableStats().getFluxDissipation().modifyMult(id, 10f);
		} else {
			if (!ship.getShield().isOn()) {
				ship.getShield().toggleOn();
			}
		}		
	}
}