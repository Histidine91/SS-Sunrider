package data.missions.sunrider_shmup;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets so we can add ships and fighter wings to them.
		// In this scenario, the fleets are attacking each other, but
		// in other scenarios, a fleet may be defending or trying to escape
		api.initFleet(FleetSide.PLAYER, "", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "ISS", FleetGoal.ATTACK, true);

//		api.getDefaultCommander(FleetSide.PLAYER).getStats().setSkillLevel(Skills.COORDINATED_MANEUVERS, 3);
//		api.getDefaultCommander(FleetSide.PLAYER).getStats().setSkillLevel(Skills.ELECTRONIC_WARFARE, 3);
		
		// Set a small blurb for each fleet that shows up on the mission detail and
		// mission results screens to identify each side.
		api.setFleetTagline(FleetSide.PLAYER, "Seraphim");
		api.setFleetTagline(FleetSide.ENEMY, "Target Dummies");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Test the Seraphim's combat AI");
		
//			FleetMemberAPI member = api.addToFleet(FleetSide.PLAYER, "omen_PD", FleetMemberType.SHIP, "Milk Run", true);
//			member.getCaptain().getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
//			member.getCaptain().getStats().setSkillLevel(Skills.SHIELD_MODULATION, 2);
//			member.getCaptain().getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
			
			api.addToFleet(FleetSide.PLAYER, "heron_Seraphim", FleetMemberType.SHIP, "Seraphim Carrier", true);
			//api.addToFleet(FleetSide.PLAYER, "SunriderSeraphim_variant", FleetMemberType.SHIP, "Seraphim", false);
	//		PersonAPI person = new AICoreOfficerPluginImpl().createPerson(Commodities.ALPHA_CORE, null, null);
	//		member.setCaptain(person);
			
			api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, null, false);
			api.addToFleet(FleetSide.ENEMY, "enforcer_Escort", FleetMemberType.SHIP, null, false);
			api.addToFleet(FleetSide.ENEMY, "venture_Balanced", FleetMemberType.SHIP, null, false);
		
		
		// Set up the map.
		float width = 12000f;
		float height = 12000f;
		
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// Add an asteroid field
		api.addAsteroidField(minX, minY + height / 2, 0, 8000f,
							 20f, 70f, 100);
		
		api.addPlanet(0, 0, 50f, StarTypes.RED_GIANT, 250f, true);		
	}

}
