package com.sunrider.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.LevelBasedEffect;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class PlushGalleon {

    public static final float DAMAGE_MULT = 0.9f;

    public static class Level1 implements ShipSkillEffect {
        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {
            stats.getShieldDamageTakenMult().modifyMult(id, DAMAGE_MULT);
            stats.getArmorDamageTakenMult().modifyMult(id, DAMAGE_MULT);
            stats.getHullDamageTakenMult().modifyMult(id, DAMAGE_MULT);
        }

        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
            stats.getShieldDamageTakenMult().unmodify(id);
            stats.getArmorDamageTakenMult().unmodify(id);
            stats.getHullDamageTakenMult().unmodify(id);
        }

        public String getEffectDescription(float level) {
            return String.format(Global.getSettings().getString("sunrider", "skill_plushGalleon_desc1"), (int)(100 - DAMAGE_MULT * 100));
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }
}
