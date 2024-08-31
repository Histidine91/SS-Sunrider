package com.sunrider.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.CoreInteractionListener;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DevMenuOptions;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.Sunrider_MiscFunctions;
import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;

/**
 * "RPG Fleet Interaction Dialog Plugin", used for the RPG battle in mission 'Vows'.<br/>
 * Used to create a minimal, in-and-out battle.
 */
public class RPGFIDP extends FleetInteractionDialogPluginImpl {
    public static Logger log = Global.getLogger(RPGFIDP.class);

    public RPGFIDP(FIDConfig params)
    {
        super(params);
        this.shownTooLargeToRetreatMessage = true;  // stop unwanted message from appearing
    }

    // probably unused
    public EngagementResultAPI getLastResult() {
        return lastResult;
    }

    @Override
    public void backFromEngagement(EngagementResultAPI result)
    {
        textPanel.addPara(Sunrider_MiscFunctions.getString("missionVows_dialog_postBattleDesc"));
        // Do nothing, just print some stuff
        options.clearOptions();

        result.setBattle(context.getBattle());
        lastResult = result;
        context.setEngagedInHostilities(true);


        showFleetInfo();
        options.addOption(Sunrider_MiscFunctions.getString("missionVows_dialog_endBattle"), OptionId.GO_TO_MAIN, null);
        options.setShortcut(OptionId.GO_TO_MAIN, Keyboard.KEY_ESCAPE, false, false, false, true);
        options.setShortcut(OptionId.GO_TO_MAIN, Keyboard.KEY_RETURN, false, false, false, true);
    }

    // haven't externalized the strings here but they shouldn't appear in current implementation
    @Override
    protected void updatePreCombat()
    {
        options.clearOptions();
        options.addOption("Continue into battle", OptionId.CONTINUE_INTO_BATTLE, null);

        options.addOption("Go back", OptionId.GO_TO_MAIN, null);
        options.setShortcut(OptionId.GO_TO_MAIN, Keyboard.KEY_ESCAPE, false, false, false, true);

        if (Global.getSettings().isDevMode())
        {
            DevMenuOptions.addOptions(dialog);
        }
    }
}
