package com.sunrider.shipsystems.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.sunrider.shipsystems.SunriderSeraphimAwakenStats;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;

public class SeraphimAwakenAI implements ShipSystemAIScript {
	
	public static Logger log = Global.getLogger(SeraphimAwakenAI.class);
	
	protected ShipAPI ship;
	protected CombatEngineAPI engine;
	protected ShipwideAIFlags flags;
	protected ShipSystemAPI system;
	
	protected IntervalUtil tracker = new IntervalUtil(0.2f, 0.25f);
	
	@Override
	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
		//log.info("Awaken AI init");
		this.ship = ship;
		this.flags = flags;
		this.engine = engine;
		this.system = system;
	}
	
	@Override
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {		
		tracker.advance(amount);			
		if (!tracker.intervalElapsed()) return;
		
		float fluxLevel = ship.getFluxLevel();	
		if (fluxLevel > 0.9f) return;
		
		//log.info("Checking Awaken use");
		
		boolean isUseful = false;
		float range = 0;
		if (system.getSpecAPI().getWeaponTypes() == null) {
			isUseful = true;
		} else {
			for (WeaponAPI w : ship.getAllWeapons()) {
				if (!system.getSpecAPI().getWeaponTypes().contains(w.getType())) {
					//log.info("Forbidden type");
					continue;
				}
				if (w.isDisabled() || w.getCooldownRemaining() > 1f) {
					//log.info("Disabled or cooldown too long");
					continue;
				}
				if (w.isFiring()) {
					//log.info("Already firing");
					//continue;
				}
				//log.info("Found weapon");
				isUseful = true;
				range = Math.max(range, w.getRange());
			}
		}
		// don't activate unless we're ready to fire
		if (!isUseful) return;
				
		float attackRange = range + SunriderSeraphimAwakenStats.RANGE_INCREASE;
		
		// Are we known to be in range of a desired target?
		boolean haveRange = false;
		if (target == null) {
			target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), ShipAPI.HullSize.FRIGATE, 
					attackRange, true, null);
			//log.info("Trying target: " + target);
			haveRange = true;
		} else {
			//log.info("Using existing target: " + target);
		}
		if (target == null) {
			return;
		}
		
		if (!haveRange) {
			float rangeToTarget = Misc.getDistance(ship.getLocation(), target.getLocation());
			float radSum = (ship.getCollisionRadius() + target.getCollisionRadius()) * 0.75f;
			haveRange = attackRange + radSum > rangeToTarget;
		}		

		if (haveRange) {
			ship.giveCommand(ShipCommand.USE_SYSTEM, null, 0);
		}
	}
}
