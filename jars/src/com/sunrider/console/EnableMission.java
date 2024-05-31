package com.sunrider.console;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMission;
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.Sunrider_MiscFunctions;
import com.fs.starfarer.api.loading.PersonMissionSpec;
import com.sunrider.SRPeople;
import java.util.Random;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;

public class EnableMission implements BaseCommand {
	
	@Override
	public CommandResult runCommand(String args, CommandContext context) {
		if (!context.isInCampaign()) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }
        
        if (args.isEmpty())
        {
            return CommandResult.BAD_SYNTAX;
        }

        String[] tmp = args.split(" ");

        if (tmp.length == 0)
        {
            return CommandResult.BAD_SYNTAX;
        }
		
		InteractionDialogAPI dialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
		/*
		if (dialog == null) {
			Console.showMessage("Must have an interaction dialog open");
            return CommandResult.WRONG_CONTEXT;
        }
		*/
		
		try {
			int missionNum = Integer.parseInt(tmp[0]);
			boolean result = launchMission(missionNum);
			if (result) return CommandResult.SUCCESS;
			else return CommandResult.ERROR;
			
		} catch (NumberFormatException ex) {
			Console.showMessage("Invalid mission number specified");
			return CommandResult.BAD_SYNTAX;
		}
	}
	
	public static boolean launchMission(int num) {
		switch (num) {
			case 1:
				Global.getSector().getMemoryWithoutUpdate().unset("$sunrider_findSunrider_seenMission");
				Global.getSector().getMemoryWithoutUpdate().unset("$sunrider_findSunrider_blockMission");
				Global.getSector().getMemoryWithoutUpdate().set("$daysSinceStart", 271);
				Console.showMessage("Variables set for showing mission 1 (including, for the duration of the current dialog, the days since start)");
				//startMissionWithId("sunrider_findSunrider", SRPeople.createAvaIfNeeded());
				return true;
			case 2:
				addAvaIfNeeded();
				Global.getSector().getMemoryWithoutUpdate().set("$sunrider_findSunrider_complete", true);
				Global.getSector().getMemoryWithoutUpdate().unset("$sunrider_seenMission2");
				Global.getSector().getMemoryWithoutUpdate().unset("$sunrider_mission2_delay");
				Console.showMessage("Variables set for showing mission 2");
				//startMissionWithId("sunrider_findSunrider", SRPeople.createAvaIfNeeded());
				return true;
			case 3:
				addAvaIfNeeded();
				Global.getSector().getMemoryWithoutUpdate().set("$sunrider_findSunrider_complete", true);
				Global.getSector().getMemoryWithoutUpdate().unset("$sunrider_seenMission3");
				Global.getSector().getMemoryWithoutUpdate().unset("$sunrider_mission3_delay");
				Console.showMessage("Variables set for showing mission 3");
				return true;
			case 4:
				addAvaIfNeeded();
				Global.getSector().getMemoryWithoutUpdate().set("$sunrider_findSunrider_complete", true);
				Global.getSector().getMemoryWithoutUpdate().set("$sunrider_mission2_doneOrSkipped", true);
				Global.getSector().getMemoryWithoutUpdate().unset("$sunrider_seenMission4");
				Global.getSector().getMemoryWithoutUpdate().unset("$sunrider_mission4_delay");
				Console.showMessage("Variables set for showing mission 4");
				return true;
			case 5:
				addAvaIfNeeded();				
				Global.getSector().getMemoryWithoutUpdate().set("$sunrider_mission3_doneOrSkipped", true);
				Global.getSector().getMemoryWithoutUpdate().set("$sunrider_mission4_doneOrSkipped", true);
				Global.getSector().getMemoryWithoutUpdate().unset("$sunrider_missionVows_doneOrSkipped");
				Global.getSector().getMemoryWithoutUpdate().unset("$sunrider_missionVows_delay");
				Console.showMessage("Variables set for showing mission 5, Vows of the Cosmos");
				return true;	
			default:
				Console.showMessage("Invalid mission ID!");
				return false;
		}
	}
	
	public static void addAvaIfNeeded() {
		if (!Sunrider_MiscFunctions.isAvaInParty()) {
			PersonAPI ava = SRPeople.createAvaIfNeeded();
			Global.getSector().getPlayerFleet().getFleetData().addOfficer(ava);
			Console.showMessage("Added Ava to player fleet");
		}
	}
	
	public static void startMissionWithId(String id, PersonAPI person) {
		PersonMissionSpec spec = Global.getSettings().getMissionSpec(id);
		HubMission mission = spec.createMission();
		mission.setPersonOverride(person);
		String extra = "";
		long seed = BarEventManager.getInstance().getSeed(null, person, extra);
		mission.setGenRandom(new Random(seed));
	}
}
