package com.sunrider.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class PlushDogoo {

    public static final float REPAIR_TIME_MULT = 0.9f;
    public static final float OVERLOAD_DURATION_MULT = 0.9f;

    public static class Level1 implements ShipSkillEffect {
        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {
            stats.getCombatEngineRepairTimeMult().modifyMult(id, REPAIR_TIME_MULT);
            stats.getCombatWeaponRepairTimeMult().modifyMult(id, REPAIR_TIME_MULT);
        }

        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
            stats.getCombatEngineRepairTimeMult().unmodify(id);
            stats.getCombatWeaponRepairTimeMult().unmodify(id);
        }

        public String getEffectDescription(float level) {
            return String.format(Global.getSettings().getString("sunrider", "skill_plushDogoo_desc1"), (int)(100 - REPAIR_TIME_MULT * 100));
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }

    public static class Level2 implements ShipSkillEffect {
        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {
            stats.getOverloadTimeMod().modifyMult(id, OVERLOAD_DURATION_MULT);
        }

        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
            stats.getOverloadTimeMod().unmodify(id);
        }

        public String getEffectDescription(float level) {
            return String.format(Global.getSettings().getString("sunrider", "skill_plushDogoo_desc2"), (int)(100 - OVERLOAD_DURATION_MULT * 100));
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }
}
