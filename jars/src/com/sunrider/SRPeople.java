package com.sunrider;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Voices;
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.Sunrider_MiscFunctions;

public class SRPeople {	
	
	public static final String AVA_ID = "sunrider_ava";
	public static final String SALVAGER_ID = "sunrider_suzuki";
	public static final String DRUNK_ID = "sunrider_bumble";
	public static final String SALVAGER_SON_ID = "sunrider_yoshio";
	public static final String PROTOTYPE_ID = "sunrider_celia";
	
	public static PersonAPI createAvaIfNeeded() {
		PersonAPI person = Global.getSector().getImportantPeople().getPerson(AVA_ID);
		if (person != null) return person;
		
		person = Global.getFactory().createPerson();
		person.setId(AVA_ID);
		person.setVoice(Voices.SOLDIER);
		person.setFaction(Factions.INDEPENDENT);
		person.setGender(FullName.Gender.FEMALE);
		person.setRankId(Ranks.SPACE_COMMANDER);
		person.setPostId(Ranks.POST_OFFICER);
		person.getName().setFirst(Sunrider_MiscFunctions.getString("avaNameFirst"));
		person.getName().setLast(Sunrider_MiscFunctions.getString("avaNameLast"));
		person.setPortraitSprite("graphics/portraits/Portrait_Ava.png");
		person.getMemoryWithoutUpdate().set("$chatterChar", "sunrider_ava");
		person.getMemoryWithoutUpdate().set("$nex_noOfficerDeath", true);	// waifus do not die when killed
		
		// set skills (8 combat skills)
		person.getStats().setLevel(8);
		person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
		person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
		person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
		person.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
		person.getStats().setSkillLevel(Skills.BALLISTIC_MASTERY, 2);
		person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
		//person.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 2);
		person.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
		person.getStats().setSkillLevel("sunrider_SunridersMother", 2);
		person.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);	// bonus
		
		Global.getSector().getImportantPeople().addPerson(person);
		return person;
	}
	
	public static PersonAPI createSalvager() {
		PersonAPI person = Global.getSector().getImportantPeople().getPerson(SALVAGER_ID);
		if (person != null) return person;
		
		person = Global.getFactory().createPerson();
		person.setId(SALVAGER_ID);
		person.setVoice(Voices.SPACER);
		person.setFaction(Factions.INDEPENDENT);
		person.setGender(FullName.Gender.MALE);
		person.setRankId(Ranks.SPACE_CAPTAIN);
		person.setPostId(Ranks.POST_SPACER);
		person.getName().setFirst(Sunrider_MiscFunctions.getString("salvagerNameFirst"));
		person.getName().setLast(Sunrider_MiscFunctions.getString("salvagerNameLast"));
		person.setPortraitSprite("graphics/portraits/portrait31.png");
		Global.getSector().getImportantPeople().addPerson(person);
		return person;
	}
	
	public static PersonAPI createDrunk() {
		PersonAPI person = Global.getSector().getImportantPeople().getPerson(DRUNK_ID);
		if (person != null) return person;
		
		person = Global.getFactory().createPerson();
		person.setId(DRUNK_ID);
		person.setVoice(Voices.SPACER);
		person.setFaction(Factions.INDEPENDENT);
		person.setGender(FullName.Gender.MALE);
		person.setRankId(Ranks.CITIZEN);
		person.setPostId(Ranks.POST_CITIZEN);
		person.getName().setFirst(Sunrider_MiscFunctions.getString("drunkNameFirst"));
		person.getName().setLast(Sunrider_MiscFunctions.getString("drunkNameLast"));
		person.setPortraitSprite("graphics/portraits/portrait25.png");
		Global.getSector().getImportantPeople().addPerson(person);
		return person;
	}
	
	public static PersonAPI createSalvagerSon() {
		PersonAPI person = Global.getSector().getImportantPeople().getPerson(SALVAGER_SON_ID);
		if (person != null) return person;
		
		person = Global.getFactory().createPerson();
		person.setId(SALVAGER_SON_ID);
		person.setVoice(Voices.VILLAIN);
		person.setFaction(Factions.INDEPENDENT);
		person.setGender(FullName.Gender.MALE);
		person.setRankId(Ranks.SPACE_CAPTAIN);
		person.setPostId(Ranks.POST_SPACER);
		person.getName().setFirst(Sunrider_MiscFunctions.getString("salvagerSonNameFirst"));
		person.getName().setLast(Sunrider_MiscFunctions.getString("salvagerNameLast"));
		person.setPortraitSprite("graphics/portraits/portrait40.png");
		
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
		
		Global.getSector().getImportantPeople().addPerson(person);
		return person;
	}
	
	public static PersonAPI createPrototype() {
		PersonAPI person = Global.getSector().getImportantPeople().getPerson(PROTOTYPE_ID);
		if (person != null) return person;
		
		person = Global.getFactory().createPerson();
		person.setId(PROTOTYPE_ID);
		person.setVoice(Voices.SCIENTIST);
		person.setFaction(Factions.INDEPENDENT);
		person.setGender(FullName.Gender.FEMALE);
		person.setRankId(Ranks.PILOT);
		person.setPostId(Ranks.POST_PATROL_COMMANDER);
		person.getName().setFirst(Sunrider_MiscFunctions.getString("prototypeName"));
		person.getName().setLast("");
		person.setPortraitSprite("graphics/portraits/SunriderPrototype.png");
		
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
}
