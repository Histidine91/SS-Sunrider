package com.sunrider.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.TargetingFeedStats;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.*;
import java.util.List;

public class SunriderEmeraldAssaultStats extends TargetingFeedStats {
    public static final float DAMAGE_INCREASE_PERCENT = 30;
    public static final float SPEED_INCREASE_PERCENT = 30;

    public static final Color JITTER_UNDER_COLOR = new Color(50,255,100,125);
    public static final Color JITTER_COLOR = new Color(50,255,100,75);

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        if (effectLevel > 0) {
            float jitterLevel = effectLevel;
            float maxRangeBonus = 5f;
            float jitterRangeBonus = jitterLevel * maxRangeBonus;
            for (ShipAPI fighter : getFighters(ship)) {
                if (fighter.isHulk()) continue;
                MutableShipStatsAPI fStats = fighter.getMutableStats();

                fStats.getBallisticWeaponDamageMult().modifyPercent(id, DAMAGE_INCREASE_PERCENT * effectLevel);
                fStats.getEnergyWeaponDamageMult().modifyPercent(id, DAMAGE_INCREASE_PERCENT * effectLevel);
                fStats.getMissileWeaponDamageMult().modifyPercent(id, DAMAGE_INCREASE_PERCENT * effectLevel);
                fStats.getMaxSpeed().modifyPercent(id, SPEED_INCREASE_PERCENT * effectLevel);

                if (jitterLevel > 0) {
                    // too bright on Troopers
                    //fighter.setWeaponGlow(effectLevel, Misc.setAlpha(JITTER_UNDER_COLOR, 255), EnumSet.allOf(WeaponAPI.WeaponType.class));

                    fighter.setJitterUnder(KEY_JITTER, JITTER_COLOR, jitterLevel, 5, 0f, jitterRangeBonus);
                    fighter.setJitter(KEY_JITTER, JITTER_UNDER_COLOR, jitterLevel, 2, 0f, 0 + jitterRangeBonus * 1f);
                    Global.getSoundPlayer().playLoop("system_targeting_feed_loop", ship, 1f, 1f, fighter.getLocation(), fighter.getVelocity());
                }
            }
        }
    }

    protected List<ShipAPI> getFighters(ShipAPI carrier) {
        List<ShipAPI> result = new ArrayList<ShipAPI>();

//		this didn't catch fighters returning for refit
//		for (FighterLaunchBayAPI bay : carrier.getLaunchBaysCopy()) {
//			if (bay.getWing() == null) continue;
//			result.addAll(bay.getWing().getWingMembers());
//		}
        Set<ShipAPI> parents = new HashSet<>();
        parents.add(carrier);
        parents.addAll(carrier.getChildModulesCopy());

        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            if (!ship.isFighter()) continue;
            if (ship.getWing() == null) continue;
            if (parents.contains(ship.getWing().getSourceShip())) {
                result.add(ship);
            }
        }

        return result;
    }


    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        for (ShipAPI fighter : getFighters(ship)) {
            if (fighter.isHulk()) continue;
            MutableShipStatsAPI fStats = fighter.getMutableStats();
            fStats.getBallisticWeaponDamageMult().unmodify(id);
            fStats.getEnergyWeaponDamageMult().unmodify(id);
            fStats.getMissileWeaponDamageMult().unmodify(id);
        }
    }


    public StatusData getStatusData(int index, State state, float effectLevel) {
        int percent = Math.round(DAMAGE_INCREASE_PERCENT * effectLevel);
        String str = Global.getSettings().getString("sunrider", "systemEmeraldAssaultDesc1");
        str = String.format(str, percent);
        if (index == 0) {
            return new StatusData(str, false);
        }
        return null;
    }
}