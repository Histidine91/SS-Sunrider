package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.SurveyPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.magiclib.util.MagicIncompatibleHullmods;

public class SunriderMiningBBMod extends SurveyingEquipment {
	
	public static final String MOD_ID = "SunriderMiningBBMod";
	
	public static final Set INCOMPATIBLE_HULLMODS = new HashSet(Arrays.asList(
		"surveying_equipment"
	));	
	
	public static final float EMP_DAMAGE_MULT = 0.5f;   // 50% EMP damage resistance
	public static final float SURVEY_COST_MOD = 60;   // 60 fewer supplies and machinery
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	
		stats.getEmpDamageTakenMult().modifyMult(id, EMP_DAMAGE_MULT);
		stats.getDynamic().getMod(Stats.getSurveyCostReductionId(Commodities.HEAVY_MACHINERY)).modifyFlat(id, SURVEY_COST_MOD);
		stats.getDynamic().getMod(Stats.getSurveyCostReductionId(Commodities.SUPPLIES)).modifyFlat(id, SURVEY_COST_MOD);	
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ShipVariantAPI variant = ship.getVariant();
		for (Object tmp : INCOMPATIBLE_HULLMODS) {
			String blocked = (String)tmp;
            if (variant.getHullMods().contains(blocked)) {
                MagicIncompatibleHullmods.removeHullmodWithWarning(variant, blocked, MOD_ID);
            }
        }
	}
	
	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) Math.round(100 - EMP_DAMAGE_MULT*100f) + "%";
		if (index == 1) return "" + (int) SURVEY_COST_MOD;
		if (index == 2) return "" + (int) SurveyPluginImpl.MIN_SUPPLIES_OR_MACHINERY;
		return null;
	}
}