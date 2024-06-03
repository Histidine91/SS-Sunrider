package com.sunrider;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Voices;
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.Sunrider_MiscFunctions;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class SRPeople {	
	
	public static final String AVA_ID = "sunrider_ava";
	public static final String SALVAGER_ID = "sunrider_suzuki";
	public static final String DRUNK_ID = "sunrider_bumble";
	public static final String SALVAGER_SON_ID = "sunrider_yoshio";
	public static final String PROTOTYPE_ID = "sunrider_celia";
	public static final String BRIDE_ID = "sunrider_grandorder";
	public static final String GROOM_ID = "sunrider_santos";
	public static final String HEG_OFFICER_ID = "sunrider_saint";
	public static final String CHURCH_OFFICER_ID = "sunrider_holyoak";
	
	
	public static PersonAPI getOrCreatePerson(String personId, @Nullable Integer officerLevel, String factionId, String firstName, String lastName,
											  @Nullable FullName.Gender gender, @Nullable String portrait, @Nullable String rankId, @Nullable String postId,
											  @Nullable String personality, @Nullable String voice, @Nullable PersonImportance importance, @Nullable Random random)
	{
		PersonAPI person = Global.getSector().getImportantPeople().getPerson(personId);
		if (person != null) return person;

		FactionAPI faction = Global.getSector().getFaction(factionId);
		if (officerLevel != null) {
			person = OfficerManagerEvent.createOfficer(faction, officerLevel, OfficerManagerEvent.SkillPickPreference.ANY, random);
		} else {
			person = faction.createRandomPerson();
		}
		person.setId(personId);
		if (gender != null) person.getName().setGender(gender);
		person.getName().setFirst(firstName);
		person.getName().setLast(lastName);
		if (portrait != null) person.setPortraitSprite(portrait);
		if (rankId != null) person.setRankId(rankId);
		if (postId != null) person.setPostId(postId);
		if (personality != null) person.setPersonality(personality);
		if (voice != null) person.setVoice(voice);
		if (importance != null) person.setImportance(importance);

		Global.getSector().getImportantPeople().addPerson(person);

		return person;
	}
	
	public static PersonAPI createAvaIfNeeded() {
		PersonAPI person = Global.getSector().getImportantPeople().getPerson(AVA_ID);
		if (person != null) return person;
		
		person = getOrCreatePerson(AVA_ID, null, Factions.INDEPENDENT, 
				Sunrider_MiscFunctions.getString("avaNameFirst"), Sunrider_MiscFunctions.getString("avaNameLast"),
				FullName.Gender.FEMALE, "graphics/portraits/Portrait_Ava.png",
				Ranks.SPACE_COMMANDER, Ranks.POST_OFFICER, Personalities.STEADY, Voices.SOLDIER, null, null
		);
		
		person.getMemoryWithoutUpdate().set("$chatterChar", "sunrider_ava");
		person.getMemoryWithoutUpdate().set("$nex_noOfficerDeath", true);	// waifus do not die when killed
		
		// set skills (8 combat skills)
		person.getStats().setLevel(8);
		person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
		person.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
		person.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
		person.getStats().setSkillLevel(Skills.BALLISTIC_MASTERY, 2);
		person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
		person.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
		person.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
		person.getStats().setSkillLevel("sunrider_SunridersMother", 2);
		//person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
		//person.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 2);
		//person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
		person.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);	// bonus
		
		return person;
	}
	
	public static PersonAPI createSalvager() {
		PersonAPI person = Global.getSector().getImportantPeople().getPerson(SALVAGER_ID);
		if (person != null) return person;
		
		person = getOrCreatePerson(SALVAGER_ID, null, Factions.INDEPENDENT, 
				Sunrider_MiscFunctions.getString("salvagerNameFirst"), Sunrider_MiscFunctions.getString("salvagerNameLast"),
				FullName.Gender.MALE, "graphics/portraits/portrait31.png",
				Ranks.SPACE_CAPTAIN, Ranks.POST_SPACER, Personalities.STEADY, Voices.SPACER, null, null
		);
		return person;
	}
	
	public static PersonAPI createDrunk() {
		PersonAPI person = Global.getSector().getImportantPeople().getPerson(DRUNK_ID);
		if (person != null) return person;
		
		person = person = getOrCreatePerson(DRUNK_ID, null, Factions.INDEPENDENT, 
				Sunrider_MiscFunctions.getString("drunkNameFirst"), Sunrider_MiscFunctions.getString("drunkNameLast"),
				FullName.Gender.MALE, "graphics/portraits/portrait25.png",
				Ranks.CITIZEN, Ranks.POST_CITIZEN, Personalities.STEADY, Voices.SPACER, null, null
		);
		return person;
	}
	
	public static PersonAPI createSalvagerSon() {
		PersonAPI person = Global.getSector().getImportantPeople().getPerson(SALVAGER_SON_ID);
		if (person != null) return person;
		
		person = getOrCreatePerson(SALVAGER_SON_ID, null, Factions.INDEPENDENT, 
				Sunrider_MiscFunctions.getString("salvagerSonNameFirst"), Sunrider_MiscFunctions.getString("salvagerNameLast"),
				FullName.Gender.MALE, "graphics/portraits/portrait40.png",
				Ranks.SPACE_CAPTAIN, Ranks.POST_SPACER, Personalities.STEADY, Voices.VILLAIN, null, null
		);
		
		// skills
		person.getStats().setLevel(6);
		person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
		person.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
		person.getStats().setSkillLevel(Skills.BALLISTIC_MASTERY, 1);
		person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 1);
		person.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 1);
		person.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 1);
		person.getStats().setSkillLevel(Skills.CONTAINMENT_PROCEDURES, 1);
		person.getStats().setSkillLevel(Skills.MAKESHIFT_EQUIPMENT, 1);
		person.getStats().setSkillLevel(Skills.SALVAGING, 1);
		
		return person;
	}
	
	public static PersonAPI createPrototype() {
		PersonAPI person = Global.getSector().getImportantPeople().getPerson(PROTOTYPE_ID);
		if (person != null) return person;
		
		person = getOrCreatePerson(PROTOTYPE_ID, null, Factions.INDEPENDENT, 
				Sunrider_MiscFunctions.getString("prototypeName"), "",
				FullName.Gender.FEMALE, "graphics/portraits/SunriderPrototype.png",
				Ranks.PILOT, Ranks.POST_PATROL_COMMANDER, Personalities.STEADY, Voices.SCIENTIST, null, null
		);
		
		// skills
		person.getStats().setLevel(7);
		person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
		person.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
		person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
		person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
		person.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
		person.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
		person.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
		person.getStats().setSkillLevel(Skills.CYBERNETIC_AUGMENTATION, 1);
		person.getStats().setSkillLevel(Skills.ELECTRONIC_WARFARE, 1);
		person.getStats().setSkillLevel(Skills.NEURAL_LINK, 1);
		
		Global.getSector().getImportantPeople().addPerson(person);
		return person;
	}
	
	public static PersonAPI getOrCreateBride() {
		PersonAPI person = Global.getSector().getImportantPeople().getPerson(BRIDE_ID);
		if (person != null) return person;
		
		person = Global.getSector().getFaction(Factions.LUDDIC_CHURCH).createRandomPerson(FullName.Gender.FEMALE);
		person.setId(BRIDE_ID);
		person.setVoice(Voices.FAITHFUL);	// I guess
		person.setFaction(Factions.LUDDIC_CHURCH);
		person.setRankId(Ranks.CITIZEN);
		person.setPostId(Ranks.CITIZEN);
		
		long seed = Sunrider_MiscFunctions.getSectorSeed();
		boolean altName = new Random(seed).nextBoolean();
		
		person.getName().setFirst(Sunrider_MiscFunctions.getString("brideNameFirst" + (altName ? "2" : "")));
		person.getName().setLast(Sunrider_MiscFunctions.getString("brideNameLast"));
		person.setPortraitSprite("graphics/portraits/portrait_luddic03.png");	// TODO
		
		Global.getSector().getImportantPeople().addPerson(person);
		return person;
	}
	
	public static PersonAPI getOrCreateGroom() {
		PersonAPI person = Global.getSector().getImportantPeople().getPerson(GROOM_ID);
		if (person != null) return person;
		
		person = Global.getSector().getFaction(Factions.LUDDIC_CHURCH).createRandomPerson(FullName.Gender.MALE);
		person.setId(GROOM_ID);
		person.setVoice(Voices.FAITHFUL);
		person.setFaction(Factions.LUDDIC_CHURCH);
		person.setRankId(Ranks.CITIZEN);
		person.setPostId(Ranks.CITIZEN);
		person.getName().setFirst(Sunrider_MiscFunctions.getString("groomNameFirst"));
		person.getName().setLast(Sunrider_MiscFunctions.getString("groomNameLast"));
		person.setPortraitSprite("graphics/portraits/portrait_luddic00.png");
		
		Global.getSector().getImportantPeople().addPerson(person);
		return person;
	}
	
	public static PersonAPI getOrCreateHegOfficer() {
		PersonAPI person = Global.getSector().getImportantPeople().getPerson(HEG_OFFICER_ID);
		if (person != null) return person;
		
		long seed = Sunrider_MiscFunctions.getSectorSeed();
		
		person = getOrCreatePerson(HEG_OFFICER_ID, 6, Factions.HEGEMONY, 
				Sunrider_MiscFunctions.getString("hegOfficerNameFirst"), Sunrider_MiscFunctions.getString("hegOfficerNameLast"),
				null, null,
				Ranks.SPACE_CAPTAIN, Ranks.POST_AGENT, null, Voices.SOLDIER, null, new Random(seed)
		);
		return person;
	}
	
	public static PersonAPI getOrCreateChurchOfficer() {
		PersonAPI person = Global.getSector().getImportantPeople().getPerson(CHURCH_OFFICER_ID);
		if (person != null) return person;
		
		long seed = Sunrider_MiscFunctions.getSectorSeed();
		
		person = getOrCreatePerson(CHURCH_OFFICER_ID, 5, Factions.LUDDIC_CHURCH, 
				Sunrider_MiscFunctions.getString("churchOfficerNameFirst"), Sunrider_MiscFunctions.getString("churchOfficerNameLast"),
				null, null,
				Ranks.KNIGHT_CAPTAIN, Ranks.POST_AGENT, null, Voices.FAITHFUL, null, new Random(seed)
		);
		return person;
	}
	
	public static void updateAvaSkills() {
		PersonAPI ava = Global.getSector().getImportantPeople().getPerson(AVA_ID);
		if (ava == null) return;	// no need to do anything
		
		int ta = (int)ava.getStats().getSkillLevel(Skills.TARGET_ANALYSIS);
		int sysEx = (int)ava.getStats().getSkillLevel(Skills.SYSTEMS_EXPERTISE);
		int ce = (int)ava.getStats().getSkillLevel(Skills.COMBAT_ENDURANCE);
		int fm = (int)ava.getStats().getSkillLevel(Skills.FIELD_MODULATION);
		
		if (ta == 0 && sysEx == 0 && ce == 2 && fm == 2) {
			ava.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
			ava.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
			ava.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 0);
			ava.getStats().setSkillLevel(Skills.FIELD_MODULATION, 0);
		}
		
		if (ava.getStats().getSkillLevel("sunrider_SunridersMother") == 0) {
			ava.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 0);
			ava.getStats().setSkillLevel("sunrider_SunridersMother", 2);
		}	
	}
}
