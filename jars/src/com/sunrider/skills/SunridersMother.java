package com.sunrider.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.LevelBasedEffect;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;

public class SunridersMother {
	
	public static Logger log = Global.getLogger(SunridersMother.class);
	
	public static final String HULL_ID = "Sunridership";
	public static final Set<String> FIGHTER_IDS = new HashSet<>();
	
	public static final float FLUX_DISSIPATION_BONUS = 15f;
	@Deprecated public static final float FLUX_DISSIPATION_MULT = 1.15f;
	public static final float SYSTEM_FLUX_COST_MULT = 0;
	public static final float FIGHTER_DAMAGE_REDUCTION = 20f;
	public static final float FIGHTER_SPEED_BONUS = 20f;
	public static final float SUPPLY_USE_REDUCTION_MULT = 0.25f;
	
	static {
		FIGHTER_IDS.addAll(Arrays.asList(
			"SunriderBianca",
			"SunriderBlackJack",
			"SunriderHavoc",
			"SunriderLiberty",
			"SunriderPaladin",
			"SunriderPhoenix",
			"SunriderSeraphim"
		));
	}
	
	protected static String getHullId(MutableShipStatsAPI stats) {
		String hullId = null;
		if (stats.getVariant() != null) {
			ShipHullSpecAPI spec = stats.getVariant().getHullSpec();
			hullId = spec.getBaseHullId();
			if (hullId == null) hullId = spec.getHullId();
		}
		return hullId;
	}
	
	protected static boolean isValidShip(MutableShipStatsAPI stats) {
		String hullId = getHullId(stats);
		return HULL_ID.equals(hullId);
	}
	
	protected static boolean isValidFighter(MutableShipStatsAPI stats) {
		String hullId = getHullId(stats);
		//log.info("Fighter hull ID is " + hullId);
		return FIGHTER_IDS.contains(hullId);
	}
	
		
	public static class Level1 implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			if (isValidShip(stats)) {
				//Global.getLogger(this.getClass()).info("wololo");
				//stats.getFluxDissipation().modifyMult(id, FLUX_DISSIPATION_MULT);
				stats.getFluxDissipation().modifyPercent(id, FLUX_DISSIPATION_BONUS);
			}
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getFluxDissipation().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return String.format(Global.getSettings().getString("sunrider", "skill_sunridersMother_desc1"), (int)Math.round(FLUX_DISSIPATION_BONUS));
			//return "-" + (int)Math.round(FLUX_DISSIPATION_MULT * 100f) + "% flux dissipation  (Sunrider only)";
			
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	public static class Level2 implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			//stats.getShieldDamageTakenMult().modifyMult(id, 0);	// TESTING ONLY
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			//stats.getShieldDamageTakenMult().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return String.format(Global.getSettings().getString("sunrider", "skill_sunridersMother_desc2"));
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public LevelBasedEffect.ScopeDescription getScopeDescription() {
			return LevelBasedEffect.ScopeDescription.PILOTED_SHIP;
		}
	}
	
	public static class Level3 implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			if (isValidFighter(stats)) {
				stats.getHullDamageTakenMult().modifyMult(id, 1f - FIGHTER_DAMAGE_REDUCTION / 100f);
				stats.getArmorDamageTakenMult().modifyMult(id, 1f - FIGHTER_DAMAGE_REDUCTION / 100f);
				stats.getShieldDamageTakenMult().modifyMult(id, 1f - FIGHTER_DAMAGE_REDUCTION / 100f);
			}
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getHullDamageTakenMult().unmodify(id);
			stats.getArmorDamageTakenMult().unmodify(id);
			stats.getShieldDamageTakenMult().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return String.format(Global.getSettings().getString("sunrider", "skill_sunridersMother_desc3"), (int)(FIGHTER_DAMAGE_REDUCTION));
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.SHIP_FIGHTERS;
		}
	}
	
	public static class Level4 implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			if (isValidFighter(stats)) {
				stats.getMaxSpeed().modifyPercent(id, FIGHTER_SPEED_BONUS);
				stats.getAcceleration().modifyPercent(id, FIGHTER_SPEED_BONUS);
				//log.info(String.format("Applying %s percent speed boost to fighter %s", FIGHTER_SPEED_BONUS, stats.getVariant().getHullSpec().getHullId()));
			}	
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getMaxSpeed().unmodify(id);
			stats.getAcceleration().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return String.format(Global.getSettings().getString("sunrider", "skill_sunridersMother_desc4"), (int)(FIGHTER_DAMAGE_REDUCTION));
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public LevelBasedEffect.ScopeDescription getScopeDescription() {
			return LevelBasedEffect.ScopeDescription.SHIP_FIGHTERS;
		}
	}
	
	public static class SupplyLevel1 implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			if (isApplicable(stats)) {
				stats.getSuppliesPerMonth().modifyMult(id, 1 - SUPPLY_USE_REDUCTION_MULT);
			}
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getSuppliesPerMonth().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return String.format(Global.getSettings().getString("sunrider", "skill_sunridersMother_descSupply1"), (int)Math.round(SUPPLY_USE_REDUCTION_MULT * 100));			
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public LevelBasedEffect.ScopeDescription getScopeDescription() {
			return LevelBasedEffect.ScopeDescription.PILOTED_SHIP;
		}
		
		protected boolean isApplicable(MutableShipStatsAPI stats) {
			return isValidShip(stats);
		}
	}
	
	public static class SupplyLevel2 extends SupplyLevel1 {		
		protected boolean isApplicable(MutableShipStatsAPI stats) {
			String hullId = getHullId(stats);
			return hullId.startsWith("Sunrider");
		}
		
		public String getEffectDescription(float level) {
			return String.format(Global.getSettings().getString("sunrider", "skill_sunridersMother_descSupply2"), (int)Math.round(SUPPLY_USE_REDUCTION_MULT * 100));			
		}
	}
}