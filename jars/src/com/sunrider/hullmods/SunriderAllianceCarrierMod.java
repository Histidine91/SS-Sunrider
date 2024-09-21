package com.sunrider.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.listeners.FighterOPCostModifier;
import com.fs.starfarer.api.impl.campaign.SurveyPluginImpl;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import org.magiclib.util.MagicIncompatibleHullmods;

public class SunriderAllianceCarrierMod extends BaseHullMod {
	
	public static final String MOD_ID = "SunriderAllianceCarrierMod";
	
	public static final Set<String> INCOMPATIBLE_HULLMODS = new HashSet(Arrays.asList(
		"unstable_injector", "defensive_targeting_array"
	));

	public static final String ENGINE_MODULE_ID = "SunriderACVengine";
	public static final String TROOPER_HULL_ID = "SunriderAllianceTrooper";
	public static final float FIGHTER_TARGETING_RANGE_BONUS = 30f;
	public static final float NO_ENGINE_PERFORMANCE_MULT = 0.25f;

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		if (ship.getOriginalOwner() == -1) {
			return; //suppress in refit
		}

		if (ship.getParentStation() == null) {
			linkVentOrOverload(ship);
		} else {
			linkCarrierOrder(ship);
		}
		checkEngines(ship);
		handleExtraFighters(ship);
	}

	protected void linkVentOrOverload(ShipAPI parent) {
		if (parent.getFluxTracker().isVenting()) {
			for (ShipAPI module : parent.getChildModulesCopy()) {
				if (module.getFluxTracker().isOverloadedOrVenting()) continue;
				module.giveCommand(ShipCommand.VENT_FLUX, null, 0);
			}
		} else if (parent.getFluxTracker().isOverloaded()) {
			float overloadDuration = parent.getFluxTracker().getOverloadTimeRemaining();
			for (ShipAPI module : parent.getChildModulesCopy()) {
				if (module.getFluxTracker().isOverloadedOrVenting()) continue;
				module.getFluxTracker().forceOverload(overloadDuration);
			}
		}
	}

	// from SWP's LinkedHull by DarkRevenant
	protected void linkCarrierOrder(ShipAPI child) {
		ShipAPI parent = child.getParentStation();
		if (parent == null || !parent.isAlive()) return;
		if (child.hasLaunchBays()) {
			if (child.isPullBackFighters() != parent.isPullBackFighters()) {
				child.giveCommand(ShipCommand.PULL_BACK_FIGHTERS, null, 0);
			}
			if (child.getAIFlags() != null) {
				if (((Global.getCombatEngine().getPlayerShip() == parent) || (parent.getAIFlags() == null))
						&& (parent.getShipTarget() != null)) {
					child.getAIFlags().setFlag(AIFlags.CARRIER_FIGHTER_TARGET, 1f, parent.getShipTarget());
				} else if ((parent.getAIFlags() != null)
						&& parent.getAIFlags().hasFlag(AIFlags.CARRIER_FIGHTER_TARGET)
						&& (parent.getAIFlags().getCustom(AIFlags.CARRIER_FIGHTER_TARGET) != null)) {
					child.getAIFlags().setFlag(AIFlags.CARRIER_FIGHTER_TARGET, 1f, parent.getAIFlags().getCustom(AIFlags.CARRIER_FIGHTER_TARGET));
				}
			}
		}
	}

	protected void checkEngines(ShipAPI ship) {
		// main body handling
		if (ship.getParentStation() == null) {
			boolean hasEngine = false;
			for (ShipAPI module : ship.getChildModulesCopy()) {
				if (module.getHullSpec().getHullId().equals(ENGINE_MODULE_ID) && module.isAlive()) {
					hasEngine = true;
					break;
				}
			}
			if (!hasEngine) {
				ship.getMutableStats().getAcceleration().modifyMult(MOD_ID, NO_ENGINE_PERFORMANCE_MULT);
				ship.getMutableStats().getDeceleration().modifyMult(MOD_ID, NO_ENGINE_PERFORMANCE_MULT);
				ship.getMutableStats().getTurnAcceleration().modifyMult(MOD_ID, NO_ENGINE_PERFORMANCE_MULT);
				ship.getMutableStats().getMaxTurnRate().modifyMult(MOD_ID, NO_ENGINE_PERFORMANCE_MULT);
				ship.getMutableStats().getMaxSpeed().modifyMult(MOD_ID, NO_ENGINE_PERFORMANCE_MULT);
				ship.getMutableStats().getZeroFluxSpeedBoost().modifyMult(MOD_ID, NO_ENGINE_PERFORMANCE_MULT);
			}
		}
		// engine block handling
		else {
			// from SWP's LinkedHull by DarkRevenant
			ShipAPI parent = ship.getParentStation();
			if (parent == null || !parent.isAlive()) return;
			ShipEngineControllerAPI ec = parent.getEngineController();
			if (ec != null) {
				if (parent.isAlive()) {
					if (ec.isAccelerating()) {
						ship.giveCommand(ShipCommand.ACCELERATE, null, 0);
					}
					if (ec.isAcceleratingBackwards()) {
						ship.giveCommand(ShipCommand.ACCELERATE_BACKWARDS, null, 0);
					}
					if (ec.isDecelerating()) {
						ship.giveCommand(ShipCommand.DECELERATE, null, 0);
					}
					if (ec.isStrafingLeft()) {
						ship.giveCommand(ShipCommand.STRAFE_LEFT, null, 0);
					}
					if (ec.isStrafingRight()) {
						ship.giveCommand(ShipCommand.STRAFE_RIGHT, null, 0);
					}
					if (ec.isTurningLeft()) {
						ship.giveCommand(ShipCommand.TURN_LEFT, null, 0);
					}
					if (ec.isTurningRight()) {
						ship.giveCommand(ShipCommand.TURN_RIGHT, null, 0);
					}
				}

				ShipEngineControllerAPI cec = ship.getEngineController();
				if (cec != null) {
					if ((ec.isFlamingOut() || ec.isFlamedOut()) && !cec.isFlamingOut() && !cec.isFlamedOut()) {
						ship.getEngineController().forceFlameout(true);
					}
				}
			}
		}
	}

	// taken from Diable Avionics' Wanzer Gantry; credits to Tartiflette and FlashFrozen as per DA's license
	protected void handleExtraFighters(ShipAPI parent) {
		if (parent.getMutableStats().getFighterRefitTimeMult().getPercentStatMod(MOD_ID) != null) {
			return;	// already executed
		}

		boolean allDeployed = true, ranOnce = false;

		for (FighterLaunchBayAPI bay : parent.getLaunchBaysCopy()) {
			if (bay.getWing() != null) {
				ranOnce = true;
				FighterWingSpecAPI spec = bay.getWing().getSpec();
				String hullId = spec.getVariant().getHullSpec().getHullId();
				if (!TROOPER_HULL_ID.equals(hullId)) continue;

				FighterWingSpecAPI wingSpec = bay.getWing().getSpec();
				int deployed = bay.getWing().getWingMembers().size();
				int maxTotal = wingSpec.getNumFighters() + 1;
				int actualAdd = maxTotal - deployed;

				if (actualAdd > 0) {
					bay.setExtraDeployments(actualAdd);
					bay.setExtraDeploymentLimit(maxTotal);
					bay.setExtraDuration(9999999);
					allDeployed = false;
				} else {
					bay.setExtraDeployments(0);
					bay.setExtraDeploymentLimit(0);
					bay.setFastReplacements(0);
				}

				if (parent.getMutableStats().getFighterRefitTimeMult().getPercentStatMod(MOD_ID) == null && actualAdd != 0) {
					//instantly add all the required fighters upon deployment
					bay.setFastReplacements(actualAdd);
				}
			}
		}

		if (parent.getMutableStats().getFighterRefitTimeMult().getPercentStatMod(MOD_ID) == null && allDeployed && ranOnce) {
			//used as a check to add all the extra fighters upon deployment
			parent.getMutableStats().getFighterRefitTimeMult().modifyPercent(MOD_ID, 0.01f);
		}
	}


	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getFighterWingRange().modifyPercent(id, FIGHTER_TARGETING_RANGE_BONUS);
		if (!stats.hasListenerOfClass(NoDroneListener.class)) stats.addListener(new NoDroneListener());
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
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {

	}

	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return (int)FIGHTER_TARGETING_RANGE_BONUS + "%";
		if (index == 1) return Global.getSettings().getHullSpec(TROOPER_HULL_ID).getHullName();
		if (index == 2) return "" + 1;
		return null;
	}

	public static class NoDroneListener implements FighterOPCostModifier {
		@Override
		public int getFighterOPCost(MutableShipStatsAPI stats, FighterWingSpecAPI fighter, int currCost) {
			if (fighter.hasTag(Tags.AUTOMATED_FIGHTER)) return 99999;
			return currCost;
		}
	}
}