package com.achersoft.tdcc.vtd.dto;

import com.achersoft.tdcc.character.dao.CharacterStats;
import com.achersoft.tdcc.enums.Buff;
import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.enums.SkillLevel;
import com.achersoft.tdcc.enums.WeaponExplodeEffect;
import com.achersoft.tdcc.vtd.dao.CharacterSkill;
import com.achersoft.tdcc.vtd.dao.VtdBuff;
import com.achersoft.tdcc.vtd.dao.VtdDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class VtdDetailsDTO {
    public String id;
    public String name;
    public CharacterClass characterClass;
    public CharacterStats stats;
    public Integer currentHealth;
    public int rollerDifficulty;
    public int initBonus;
    public int roomNumber;
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
    public List<BuffDTO> buffs;
    public List<BuffDTO> availableBuffs;
    public List<BuffDTO> availableBardsong;
    public List<Integer> meleeDmgRange;
    public List<Integer> meleeOffhandDmgRange;
    public List<Integer> meleePolyDmgRange;
    public List<Integer> rangeDmgRange;
    public List<Integer> rangeOffhandDmgRange;
    public List<Integer> meleeWeaponExplodeRange;
    public List<Integer> rangeWeaponExplodeRange;
    public WeaponExplodeEffect meleeWeaponExplodeEffect;
    public WeaponExplodeEffect rangeWeaponExplodeEffect;
    public String meleeWeaponExplodeText;
    public String rangeWeaponExplodeText;
    public Integer meleeCritMin;
    public Integer meleeOffhandCritMin;
    public Integer meleePolyCritMin;
    public Integer rangeCritMin;
    public Integer meleeSneakCritMin;
    public Integer rangeSneakCritMin;
    public boolean isSneakCanCrit;
    public boolean isSneakAtRange;
    public boolean splitHeal;
    public boolean madEvoker;
    public boolean mightyWeapon;
    
    public static VtdDetailsDTO fromDAO(VtdDetails dao) {
        VtdDetailsDTO build = VtdDetailsDTO.builder()
                .id(dao.getCharacterId())
                .name(dao.getName())
                .currentHealth(dao.getCurrentHealth())
                .characterClass(dao.getCharacterClass())
                .stats(dao.getStats())
                .rollerDifficulty(dao.getRollerDifficulty())
                .initBonus(dao.getInitBonus())
                .roomNumber(dao.getRoomNumber())
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
                .buffs(new ArrayList<>())
                .meleeDmgRange(new ArrayList<>())
                .meleeOffhandDmgRange(new ArrayList<>())
                .meleePolyDmgRange(new ArrayList<>())
                .rangeDmgRange(new ArrayList<>())
                .rangeOffhandDmgRange(new ArrayList<>())
                .meleeWeaponExplodeRange(new ArrayList<>())
                .rangeWeaponExplodeRange(new ArrayList<>())
                .meleeWeaponExplodeEffect(dao.getMeleeWeaponExplodeEffect())
                .rangeWeaponExplodeEffect(dao.getRangeWeaponExplodeEffect())
                .meleeWeaponExplodeText(dao.getMeleeWeaponExplodeText())
                .rangeWeaponExplodeText(dao.getRangeWeaponExplodeText())
                .meleeCritMin(dao.getMeleeCritMin())
                .meleeOffhandCritMin(dao.getMeleeOffhandCritMin())
                .meleePolyCritMin(dao.getMeleePolyCritMin())
                .rangeCritMin(dao.getRangeCritMin())
                .meleeSneakCritMin(dao.getMeleeSneakCritMin())
                .rangeSneakCritMin(dao.getRangeSneakCritMin())
                .isSneakCanCrit(dao.isSneakCanCrit())
                .isSneakAtRange(dao.isSneakAtRange())
                .splitHeal(dao.isSplitHeal())
                .madEvoker(dao.isMadEvoker())
                .mightyWeapon(dao.isMightyWeapon())
                .build();

        if (dao.getCharacterSkills() != null) {
            final List<CharacterSkill> usable = new ArrayList<>();

            dao.getCharacterSkills().forEach(skill -> {
                if (skill.getSkillLevel() == SkillLevel.NA) {
                    if (skill.getUsableNumber() > 0)
                        usable.add(skill);
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

            build.getCharacterSkills().addAll(usable);
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

        build.setAvailableBuffs(Buff.getSelectableBuffs().stream().map(BuffDTO::fromDAO).collect(Collectors.toList()));
        build.setAvailableBardsong(Buff.getBardsongBuff().stream().map(BuffDTO::fromDAO).collect(Collectors.toList()));

        if (dao.getBuffs() != null) {
            build.setBuffs(dao.getBuffs().stream().map(vtdBuff -> BuffDTO.fromDAO(vtdBuff.getBuff())).collect(Collectors.toList()));
            build.getAvailableBuffs().removeAll(build.getBuffs());

            if (build.getBuffs().stream().filter(BuffDTO::isBardsong).count() > 0)
                build.setAvailableBardsong(new ArrayList<>());
        }

        if (dao.getMeleeDmgRange() != null && !dao.getMeleeDmgRange().isEmpty())
            build.setMeleeDmgRange(Arrays.stream(dao.getMeleeDmgRange().split(",")).map(Integer::valueOf).collect(Collectors.toList()));

        if (dao.getMeleeOffhandDmgRange() != null && !dao.getMeleeOffhandDmgRange().isEmpty())
            build.setMeleeOffhandDmgRange(Arrays.stream(dao.getMeleeOffhandDmgRange().split(",")).map(Integer::valueOf).collect(Collectors.toList()));

        if (dao.getMeleePolyDmgRange() != null && !dao.getMeleePolyDmgRange().isEmpty())
            build.setMeleePolyDmgRange(Arrays.stream(dao.getMeleePolyDmgRange().split(",")).map(Integer::valueOf).collect(Collectors.toList()));

        if (dao.getRangeDmgRange() != null && !dao.getRangeDmgRange().isEmpty())
            build.setRangeDmgRange(Arrays.stream(dao.getRangeDmgRange().split(",")).map(Integer::valueOf).collect(Collectors.toList()));

        if (dao.getRangeOffhandDmgRange() != null && !dao.getRangeOffhandDmgRange().isEmpty())
            build.setRangeOffhandDmgRange(Arrays.stream(dao.getRangeOffhandDmgRange().split(",")).map(Integer::valueOf).collect(Collectors.toList()));

        if (dao.getMeleeWeaponExplodeRange() != null && !dao.getMeleeWeaponExplodeRange().isEmpty())
            build.setMeleeWeaponExplodeRange(Arrays.stream(dao.getMeleeWeaponExplodeRange().split(",")).map(Integer::valueOf).collect(Collectors.toList()));

        if (dao.getRangeWeaponExplodeRange() != null && !dao.getRangeWeaponExplodeRange().isEmpty())
            build.setRangeWeaponExplodeRange(Arrays.stream(dao.getRangeWeaponExplodeRange().split(",")).map(Integer::valueOf).collect(Collectors.toList()));

        return build;
    }
}
