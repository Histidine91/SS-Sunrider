package com.sunrider.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.mission.FleetSide;

public class SunriderShortRangeWarpStats extends BaseShipSystemScript {
	
	public static final float FLUX_COST = 5000;
	protected boolean enteringWarp;
	
	@Override
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		//Global.getLogger(this.getClass()).info("yolo " + state + ", " + effectLevel);
		if (!enteringWarp && state == State.IN) {
			FleetMemberAPI member = stats.getFleetMember();
			if (member == null) return;
			PersonAPI captain = member != null ? member.getCaptain() : null;
			if (haveSunriderMommy(captain)) {
				// have skill, do nothing
			} else 	{
				ShipAPI ship = Global.getCombatEngine().getFleetManager(FleetSide.PLAYER).getShipFor(member);
				if (ship == null) ship = Global.getCombatEngine().getFleetManager(FleetSide.ENEMY).getShipFor(member);
				if (ship == null) return;
				
				ship.getFluxTracker().increaseFlux(FLUX_COST, false);
			}
			enteringWarp = true;
		}		
	}
	
	@Override
	public void unapply(MutableShipStatsAPI stats, String id) {
		enteringWarp = false;
	}
	
	protected boolean haveSunriderMommy(PersonAPI captain) {
		return captain != null && captain.getStats().getSkillLevel("sunrider_SunridersMother") >= 2;
	}
	
	@Override
	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		if (haveSunriderMommy(ship.getCaptain())) return true;
		float curr = ship.getFluxTracker().getCurrFlux();
		float max = ship.getFluxTracker().getMaxFlux();
		//Global.getLogger(this.getClass()).info(String.format("Flux current %s, max %s", curr, max));
		return max - curr >= FLUX_COST;
	}
}
