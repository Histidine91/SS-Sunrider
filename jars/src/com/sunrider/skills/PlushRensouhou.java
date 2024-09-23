package com.sunrider.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class PlushRensouhou {

    public static final float ROF_PERCENT = 5;
    public static final float FLUX_PERCENT = 5;

    public static class Level1 implements ShipSkillEffect {
        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {
            float rofMod = ROF_PERCENT;
            float fluxMod = FLUX_PERCENT * 0.01f;
            if (hullSize == ShipAPI.HullSize.FRIGATE || hullSize == ShipAPI.HullSize.DESTROYER) {
                rofMod *= 2;
                fluxMod *= 2;
            }

            stats.getBallisticRoFMult().modifyPercent(id, rofMod);
            stats.getEnergyRoFMult().modifyPercent(id, rofMod);
            stats.getMissileRoFMult().modifyPercent(id, rofMod);
            stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1 - fluxMod);
            stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1 - fluxMod);
            stats.getMissileWeaponFluxCostMod().modifyMult(id, 1 - fluxMod);
        }

        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
            stats.getBallisticRoFMult().unmodify(id);
            stats.getEnergyRoFMult().unmodify(id);
            stats.getMissileRoFMult().unmodify(id);
            stats.getBallisticWeaponFluxCostMod().unmodify(id);
            stats.getEnergyWeaponFluxCostMod().unmodify(id);
            stats.getMissileWeaponFluxCostMod().unmodify(id);
        }

        public String getEffectDescription(float level) {
            return String.format(Global.getSettings().getString("sunrider", "skill_plushRensouhou_desc1"),
                    (int)ROF_PERCENT, (int)FLUX_PERCENT, Global.getSettings().getString("sunrider", "skill_plushRensouhou_desc1Highlight"));
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }
}
