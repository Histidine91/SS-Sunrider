package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.GameState;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import com.fs.starfarer.api.mission.FleetSide;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.combat.DamageType;
import org.lazywizard.lazylib.combat.CombatUtils;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.combat.CollisionClass;


public class SunriderVanguard implements EveryFrameWeaponEffectPlugin {
		
	private static final Color COLOR = new Color(40, 50, 100, 255);
	DamagingProjectileAPI Proj;
    
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		weapon.getSpec().setUnaffectedByProjectileSpeedBonuses(true);
        if (engine.isPaused()) {return;}
		if (Global.getSettings().getCurrentState() == GameState.COMBAT){
			
			for (DamagingProjectileAPI p : engine.getProjectiles())
				if (p.getWeapon() == weapon){
					Proj = p;
				}
		
			if (Proj != null && !Proj.isFading() && Proj.getElapsed() < 2f){
				Vector2f point = Proj.getLocation();
				float Damage = Proj.getDamageAmount() * 1f;
				
				DamagingExplosionSpec PierceEffect = new DamagingExplosionSpec(
					0.05f,				//duration
					30f,				//radius
					30f, 				//coreRadius
					Damage,				//maxDamage
					Damage,				//minDamage
					CollisionClass.HITS_SHIPS_ONLY_NO_FF,	//collisionClass
					CollisionClass.HITS_SHIPS_AND_ASTEROIDS,	//collisionClassByFighter
					12f,				//particleSizeMin,
					20f,				//particleSizeRange
					0.5f,				//particleDuration
					10, 				//particleCount
					COLOR,
					COLOR);
					
					PierceEffect.setShowGraphic(false); 
					PierceEffect.setDamageType(DamageType.ENERGY);
					
				engine.spawnDamagingExplosion(PierceEffect, Proj.getSource(), point, false);	
			}
		}
	}
}
