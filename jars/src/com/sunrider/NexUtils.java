package com.sunrider;

import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import exerelin.campaign.battle.NexFleetInteractionDialogPluginImpl;

/**
 * Separate class for any constructor that uses stuff from Nexerelin. 
 * Needed because class loader seems to throw an exception if it contains constructors 
 * of a class from an absent mod, even if that constructor is never called.
 * @author Histidine
 */
public class NexUtils {
	
	public static FleetInteractionDialogPluginImpl getNexFIDPI(FleetInteractionDialogPluginImpl.FIDConfig config) 
	{
		return new NexFleetInteractionDialogPluginImpl(config);
	}
}
