package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.sunrider.SRPeople;
import com.sunrider.SunriderSatBombListener;

public class SunriderModPlugin extends BaseModPlugin {
	
	@Override
	public void onGameLoad(boolean newGame) {
		reverseCompatibility();
		SRPeople.createAvaIfNeeded();
		SunriderSatBombListener.addIfNeeded();
	}
	
	public void reverseCompatibility() {
		MemoryAPI mem = Global.getSector().getMemoryWithoutUpdate();
		if (mem.getBoolean("$sunrider_findSunrider_seenMission") && !mem.getBoolean("$sunrider_findSunrider_complete")) {
			for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
				if (member.getHullId().equals("Sunridership")) {
					mem.set("$sunrider_findSunrider_complete", true);
					break;
				}
			}
		}
		
		SRPeople.updateAvaSkills();
	}
}
