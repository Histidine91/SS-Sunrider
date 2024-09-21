package data.missions.sunrider_test;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets so we can add ships and fighter wings to them.
		// In this scenario, the fleets are attacking each other, but
		// in other scenarios, a fleet may be defending or trying to escape
		api.initFleet(FleetSide.PLAYER, "", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "", FleetGoal.ATTACK, true);

//		api.getDefaultCommander(FleetSide.PLAYER).getStats().setSkillLevel(Skills.COORDINATED_MANEUVERS, 3);
//		api.getDefaultCommander(FleetSide.PLAYER).getStats().setSkillLevel(Skills.ELECTRONIC_WARFARE, 3);
		
		// Set a small blurb for each fleet that shows up on the mission detail and
		// mission results screens to identify each side.
		api.setFleetTagline(FleetSide.PLAYER, "Player");
		api.setFleetTagline(FleetSide.ENEMY, "Enemy");
		
		List<String> ships = new ArrayList<>(Arrays.asList(new String[] {
			"Sunridership",
			"SunriderNightmare",
			"SunriderRyuvianCruiser",
			"SunriderPactBattleship",
			"SunriderPactCruiser",
			"SunriderPactDestroyer",
			"SunriderPactFrigate",
			"SunriderAllianceCarrier",
			"SunriderAllianceBattleship",
			"SunriderAllianceCruiser",
			"SunriderMiningFrigate",
			"SunriderMiningBattleship",
			"SunriderPirateFrigate",
			"SunriderIronhog",
			"SunriderGunboat",
		}));
		
		for (String hullId : ships) {
			List<String> variants = Global.getSettings().getHullIdToVariantListMap().get(hullId);
			String variantId = variants.isEmpty() ? hullId + "_Hull" : variants.get(0);
			addShip(api, FleetSide.PLAYER, variantId, null);
			addShip(api, FleetSide.ENEMY, variantId, null);
		}
		
		// Set up the map.
		float width = 16000f;
		float height = 16000f;
		
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// Add an asteroid field
		api.addAsteroidField(minX, minY + height / 2, 0, 8000f,
							 20f, 70f, 100);	
	}
	
	protected void addShip(MissionDefinitionAPI api, FleetSide side, String variant, String name) {
		api.addToFleet(side, variant, FleetMemberType.SHIP, name, false);
	}
}