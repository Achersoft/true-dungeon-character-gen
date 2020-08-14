package com.achersoft.tdcc.vtd.dto;

import com.achersoft.tdcc.character.dao.CharacterStats;
import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.enums.SkillLevel;
import com.achersoft.tdcc.vtd.dao.CharacterSkill;
import com.achersoft.tdcc.vtd.dao.VtdDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class VtdDetailsDTO {
    public String id;
    public String name;
    public String username;
    public CharacterClass characterClass;
    public CharacterStats stats;
    public List<String> alwaysInEffect;
    public List<String> oncePerRound;
    public List<String> oncePerRoom;
    public List<String> oncePerGame;
    public List<CharacterSkill> characterSkills;
    public List<CharacterSkill> zeroSkills;
    public List<CharacterSkill> oneSkills;
    public List<CharacterSkill> twoSkills;
    public List<CharacterSkill> threeSkills;
    public List<CharacterSkill> fourSkills;
    public List<Integer> meleeDmgRange;
    public List<Integer> meleeOffhandDmgRange;
    public List<Integer> meleePolyDmgRange;
    public List<Integer> rangeDmgRange;
    public List<Integer> rangeOffhandDmgRange;
    public Integer meleeCritMin;
    public Integer meleePolyCritMin;
    public Integer rangeCritMin;
    public Integer currentHealth;
    public Boolean splitHeal;
    public Boolean madEvoker;
    
    public static VtdDetailsDTO fromDAO(VtdDetails dao) {
        VtdDetailsDTO build = VtdDetailsDTO.builder()
                .id(dao.getCharacterId())
                .name(dao.getName())
                .characterClass(dao.getCharacterClass())
                .stats(dao.getStats())
                .alwaysInEffect(new ArrayList<>())
                .oncePerRound(new ArrayList<>())
                .oncePerRoom(new ArrayList<>())
                .oncePerGame(new ArrayList<>())
                .characterSkills(new ArrayList<>())
                .zeroSkills(new ArrayList<>())
                .oneSkills(new ArrayList<>())
                .twoSkills(new ArrayList<>())
                .threeSkills(new ArrayList<>())
                .fourSkills(new ArrayList<>())
                .meleeCritMin(dao.getMeleeCritMin())
                .meleePolyCritMin(dao.getMeleePolyCritMin())
                .rangeCritMin(dao.getRangeCritMin())
                .currentHealth(dao.getCurrentHealth())
                .meleeDmgRange(dao.getMeleeDmgRange())
                .meleePolyDmgRange(dao.getMeleePolyDmgRange())
                .rangeDmgRange(dao.getRangeDmgRange())
                .splitHeal(true)
                .madEvoker(true)
                .build();

        if (dao.getCharacterSkills() != null) {
            final List<CharacterSkill> usedable = new ArrayList<>();

            dao.getCharacterSkills().forEach(skill -> {
                if (skill.getSkillLevel() == SkillLevel.NA) {
                    if (skill.getUsableNumber() > 0)
                        usedable.add(skill);
                    else
                        build.getCharacterSkills().add(skill);
                } else if (skill.getSkillLevel() == SkillLevel.ZERO) {
                    build.getZeroSkills().add(skill);
                } else if (skill.getSkillLevel() == SkillLevel.ONE) {
                    build.getOneSkills().add(skill);
                } else if (skill.getSkillLevel() == SkillLevel.TWO) {
                    build.getTwoSkills().add(skill);
                } else if (skill.getSkillLevel() == SkillLevel.THREE) {
                    build.getThreeSkills().add(skill);
                } else if (skill.getSkillLevel() == SkillLevel.FOUR) {
                    build.getFourSkills().add(skill);
                }
            });

            build.getCharacterSkills().addAll(usedable);
        }
        
        if(dao.getNotes() != null) {
            dao.getNotes().forEach((note) -> {
                if(note.isAlwaysInEffect())
                    build.alwaysInEffect.add(note.getNote());
                if(note.isOncePerRound())
                    build.oncePerRound.add(note.getNote());
                if(note.isOncePerRoom())
                    build.oncePerRoom.add(note.getNote());
                if(note.isOncePerGame())
                    build.oncePerGame.add(note.getNote());
            });
        }
        
        return build;
    }
}
