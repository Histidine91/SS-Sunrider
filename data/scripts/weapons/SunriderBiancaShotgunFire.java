package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.MathUtils;

/**
 * VIC's Besomar on fire script shamelessly stolen by Histidine
 * @author PureTilt
 */
public class SunriderBiancaShotgunFire implements OnFireEffectPlugin {

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		// uncomment if you want a sound that fires for each projectile
        //Global.getSoundPlayer().playSound("SunridersShotgunFire", 1, 1f, weapon.getLocation(), weapon.getShip().getVelocity());
        projectile.getVelocity().scale(MathUtils.getRandomNumberInRange(0.9f, 1.1f));
    }
}