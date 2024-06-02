package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class Sunrider_PlayInteractionMusic extends BaseCommandPlugin {
	
	public static Logger log = Global.getLogger(Sunrider_PlayInteractionMusic.class);
	public static final String MEMORY_KEY_NEED_RESET = "$sunrider_wantResetMusic";
	
	@Override
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
		SectorEntityToken entity = dialog.getInteractionTarget();
		
		if (params.isEmpty()) {
			log.info("Unsetting music for " + entity.getName());
			if (entity.getMemoryWithoutUpdate().getBoolean(MEMORY_KEY_NEED_RESET)) {
				entity.getMemoryWithoutUpdate().unset(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY);
			}
			
			playMusic(null);
			return true;
		}
		
        String track = params.get(0).getString(memoryMap);
		boolean persistent = false;
		if (params.size() > 1)
			persistent = params.get(1).getBoolean(memoryMap);
        
		
		if (persistent) {
			log.info("Setting music for " + entity.getName() + ": " + track);
			entity.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, track);
			entity.getMemoryWithoutUpdate().set(MEMORY_KEY_NEED_RESET, true);
			playMusic(track);	// make double sure we're playing the right stuff
		} else {
			playMusic(track);
		}
		return true;
    }
	
	public void playMusic(String track) {
		//log.info("Playing track " + track);
		Global.getSoundPlayer().playCustomMusic(1, 1, track, true);
	}
}
