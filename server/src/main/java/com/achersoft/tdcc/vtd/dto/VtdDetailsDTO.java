package com.achersoft.tdcc.vtd.dto;

import com.achersoft.tdcc.character.dao.CharacterStats;
import com.achersoft.tdcc.enums.*;
import com.achersoft.tdcc.vtd.admin.dao.VtdRoom;
import com.achersoft.tdcc.vtd.dao.CharacterSkill;
import com.achersoft.tdcc.vtd.dao.VtdDetails;
import com.achersoft.tdcc.vtd.dao.VtdMonster;
import com.achersoft.tdcc.vtd.dao.VtdPoly;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class VtdDetailsDTO {
    public String id;
    public String name;
    public String adventureName;
    public CharacterClass characterClass;
    public String characterClassString;
    public CharacterStats stats;
    public Integer currentHealth;
    public int rollerDifficulty;
    public int initBonus;
    public int healthBonus;
    public int roomNumber;
    public List<VtdMonster> monsters;
    public List<InGameEffect> effects;
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
    public List<VtdPoly> availablePoly;
    public VtdPoly poly;
    public List<VtdPoly> availableAnimalCompanion;
    public VtdPoly animalCompanion;
    public List<Integer> meleeDmgRange;
    public List<Integer> meleeOffhandDmgRange;
    public List<Integer> meleePolyDmgRange;
    public List<Integer> meleeAnimalCompanionDmgRange;
    public List<Integer> rangeDmgRange;
    public List<Integer> rangeOffhandDmgRange;
    public List<Integer> meleeWeaponExplodeRange;
    public List<Integer> meleeWeaponSecondaryExplodeRange;
    public List<Integer> meleeOffhandWeaponExplodeRange;
    public List<Integer> rangeWeaponExplodeRange;
    public List<Integer> rangeOffhandWeaponExplodeRange;
    public WeaponExplodeCondition meleeWeaponExplodeEffect;
    public WeaponExplodeCondition meleeWeaponSecondaryExplodeEffect;
    public WeaponExplodeCondition meleeOffhandWeaponExplodeEffect;
    public WeaponExplodeCondition rangeWeaponExplodeEffect;
    public WeaponExplodeCondition rangeOffhandWeaponExplodeEffect;
    public String meleeWeaponExplodeText;
    public String meleeWeaponSecondaryExplodeText;
    public String meleeOffhandWeaponExplodeText;
    public String rangeWeaponExplodeText;
    public String rangeOffhandWeaponExplodeText;
    public List<DamageModEffect> meleeDmgEffects;
    public List<DamageModEffect> meleeOffhandDmgEffects;
    public List<DamageModEffect> meleePolyDmgEffects;
    public List<DamageModEffect> rangeDmgEffects;
    public List<DamageModEffect> rangeOffhandDmgEffects;
    public Integer meleeCritMin;
    public Integer meleeOffhandCritMin;
    public Integer meleePolyCritMin;
    public Integer rangeCritMin;
    public Integer meleeSneakHit;
    public Integer meleeSneakDamage;
    public Integer meleeSneakCritMin;
    public Integer rangeSneakHit;
    public Integer rangeSneakDamage;
    public Integer rangeSneakCritMin;
    public Integer unmodifiableSneakDamage;
    public boolean sneakCanCrit;
    public boolean sneakAtRange;
    public boolean splitHeal;
    public boolean madEvoker;
    public boolean mightyWeapon;
    public boolean prestigeAvailable;
    public boolean prestigeActive;
    
    public static VtdDetailsDTO fromDAO(VtdDetails dao) {
        VtdDetailsDTO build = VtdDetailsDTO.builder()
                .id(dao.getCharacterId())
                .name(dao.getName())
                .adventureName(dao.getAdventureName())
                .currentHealth(dao.getCurrentHealth())
                .characterClass(dao.getCharacterClass())
                .characterClassString(dao.getCharacterClass().getDtoValue(dao.isPrestigeActive()))
                .stats(dao.getStats())
                .rollerDifficulty(dao.getRollerDifficulty())
                .initBonus(dao.getInitBonus())
                .healthBonus(dao.getHealthBonus())
                .roomNumber(dao.getRoomNumber())
                .monsters(dao.getMonsters())
                .effects(new ArrayList<>())
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
                .availablePoly(new ArrayList<>())
                .availableAnimalCompanion(new ArrayList<>())
                .meleeDmgRange(new ArrayList<>())
                .meleeOffhandDmgRange(new ArrayList<>())
                .meleePolyDmgRange(new ArrayList<>())
                .meleeAnimalCompanionDmgRange(new ArrayList<>())
                .rangeDmgRange(new ArrayList<>())
                .rangeOffhandDmgRange(new ArrayList<>())
                .meleeWeaponExplodeRange(new ArrayList<>())
                .meleeWeaponSecondaryExplodeRange(new ArrayList<>())
                .meleeOffhandDmgRange(new ArrayList<>())
                .rangeWeaponExplodeRange(new ArrayList<>())
                .rangeOffhandWeaponExplodeRange(new ArrayList<>())
                .meleeWeaponExplodeEffect(dao.getMeleeWeaponExplodeEffect())
                .meleeWeaponSecondaryExplodeEffect(dao.getMeleeWeaponSecondaryExplodeEffect())
                .meleeOffhandWeaponExplodeEffect(dao.getMeleeOffhandWeaponExplodeEffect())
                .rangeWeaponExplodeEffect(dao.getRangeWeaponExplodeEffect())
                .rangeOffhandWeaponExplodeEffect(dao.getRangeOffhandWeaponExplodeEffect())
                .meleeWeaponExplodeText(dao.getMeleeWeaponExplodeText())
                .meleeWeaponSecondaryExplodeText(dao.getMeleeWeaponSecondaryExplodeText())
                .meleeOffhandWeaponExplodeText(dao.getMeleeOffhandWeaponExplodeText())
                .rangeWeaponExplodeText(dao.getRangeWeaponExplodeText())
                .rangeOffhandWeaponExplodeText(dao.getRangeOffhandWeaponExplodeText())
                .meleeDmgEffects(new ArrayList<>())
                .meleeOffhandDmgEffects(new ArrayList<>())
                .meleePolyDmgEffects(new ArrayList<>())
                .rangeDmgEffects(new ArrayList<>())
                .rangeOffhandDmgEffects(new ArrayList<>())
                .meleeCritMin(dao.getMeleeCritMin())
                .meleeOffhandCritMin(dao.getMeleeOffhandCritMin())
                .meleePolyCritMin(dao.getMeleePolyCritMin())
                .rangeCritMin(dao.getRangeCritMin())
                .meleeSneakHit(dao.getMeleeSneakHit())
                .meleeSneakDamage(dao.getMeleeSneakDamage())
                .meleeSneakCritMin(dao.getMeleeSneakCritMin())
                .rangeSneakHit(dao.getRangeSneakHit())
                .rangeSneakDamage(dao.getRangeSneakDamage())
                .rangeSneakCritMin(dao.getRangeSneakCritMin())
                .unmodifiableSneakDamage(dao.getUnmodifiableSneakDamage())
                .sneakCanCrit(dao.isSneakCanCrit())
                .sneakAtRange(dao.isSneakAtRange())
                .splitHeal(dao.isSplitHeal())
                .madEvoker(dao.isMadEvoker())
                .prestigeAvailable(dao.isPrestigeAvailable())
                .prestigeActive(dao.isPrestigeActive())
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

        if (dao.getAvailableEffects() != null && !dao.getAvailableEffects().isEmpty())
            build.setEffects(Arrays.stream(dao.getAvailableEffects().split(",")).map(InGameEffect::valueOf).collect(Collectors.toList()));

        build.setAvailableBuffs(Buff.getSelectableBuffs().stream().map(BuffDTO::fromDAO).collect(Collectors.toList()));
        build.setAvailableBardsong(Buff.getBardsongBuff().stream().map(BuffDTO::fromDAO).collect(Collectors.toList()));

        if (dao.getBuffs() != null) {
            build.setBuffs(dao.getBuffs().stream().map(vtdBuff -> BuffDTO.fromDAO(vtdBuff.getBuff())).collect(Collectors.toList()));
            build.getAvailableBuffs().removeAll(build.getBuffs());

            if (build.getBuffs().stream().filter(BuffDTO::isBardsong).count() > 0)
                build.setAvailableBardsong(new ArrayList<>());
        }

        VtdPoly activePoly = null;
        if (dao.getPolys() != null) {
            VtdPoly nonePoly = null;
            VtdPoly noneAnimalPoly = null;
            for (VtdPoly poly : dao.getPolys()) {
                if (poly.isCompanion()) {
                    if (noneAnimalPoly == null && poly.getName().equalsIgnoreCase("None")) {
                        noneAnimalPoly = poly;
                        if (poly.isActive()) {
                            build.setAnimalCompanion(poly);
                            activePoly = poly;
                        }
                    } else {
                        if (poly.isActive()) {
                            build.setAnimalCompanion(poly);
                            activePoly = poly;
                        } else
                            build.getAvailableAnimalCompanion().add(poly);
                    }
                } else {
                    if (nonePoly == null && poly.getName().equalsIgnoreCase("None")) {
                        nonePoly = poly;
                        if (poly.isActive())
                            build.setPoly(poly);
                    } else {
                        if (poly.isActive())
                            build.setPoly(poly);
                        else
                            build.getAvailablePoly().add(poly);
                    }
                }
            }
            build.getAvailablePoly().sort(Comparator.comparing(vtdPoly -> vtdPoly.getName().toLowerCase()));
            build.getAvailablePoly().add(0, nonePoly);
            build.getAvailableAnimalCompanion().sort(Comparator.comparing(vtdPoly -> vtdPoly.getName().toLowerCase()));
            build.getAvailableAnimalCompanion().add(0, noneAnimalPoly);
        }

        if (dao.getMeleeDmgRange() != null && !dao.getMeleeDmgRange().isEmpty())
            build.setMeleeDmgRange(Arrays.stream(dao.getMeleeDmgRange().split(",")).map(Integer::valueOf).collect(Collectors.toList()));

        if (dao.getMeleeOffhandDmgRange() != null && !dao.getMeleeOffhandDmgRange().isEmpty())
            build.setMeleeOffhandDmgRange(Arrays.stream(dao.getMeleeOffhandDmgRange().split(",")).map(Integer::valueOf).collect(Collectors.toList()));

        if (dao.getMeleePolyDmgRange() != null && !dao.getMeleePolyDmgRange().isEmpty())
            build.setMeleePolyDmgRange(Arrays.stream(dao.getMeleePolyDmgRange().split(",")).map(Integer::valueOf).collect(Collectors.toList()));

        if (activePoly != null && activePoly.getDmgRange() != null && !activePoly.getDmgRange().isEmpty())
            build.setMeleeAnimalCompanionDmgRange(Arrays.stream(activePoly.getDmgRange().split(",")).map(Integer::valueOf).collect(Collectors.toList()));

        if (dao.getRangeDmgRange() != null && !dao.getRangeDmgRange().isEmpty())
            build.setRangeDmgRange(Arrays.stream(dao.getRangeDmgRange().split(",")).map(Integer::valueOf).collect(Collectors.toList()));

        if (dao.getRangeOffhandDmgRange() != null && !dao.getRangeOffhandDmgRange().isEmpty())
            build.setRangeOffhandDmgRange(Arrays.stream(dao.getRangeOffhandDmgRange().split(",")).map(Integer::valueOf).collect(Collectors.toList()));

        if (dao.getMeleeWeaponExplodeRange() != null && !dao.getMeleeWeaponExplodeRange().isEmpty())
            build.setMeleeWeaponExplodeRange(Arrays.stream(dao.getMeleeWeaponExplodeRange().split(",")).map(Integer::valueOf).collect(Collectors.toList()));

        if (dao.getMeleeWeaponSecondaryExplodeRange() != null && !dao.getMeleeWeaponSecondaryExplodeRange().isEmpty())
            build.setMeleeWeaponSecondaryExplodeRange(Arrays.stream(dao.getMeleeWeaponSecondaryExplodeRange().split(",")).map(Integer::valueOf).collect(Collectors.toList()));

        if (dao.getMeleeOffhandWeaponExplodeRange() != null && !dao.getMeleeOffhandWeaponExplodeRange().isEmpty())
            build.setMeleeOffhandDmgRange(Arrays.stream(dao.getMeleeOffhandWeaponExplodeRange().split(",")).map(Integer::valueOf).collect(Collectors.toList()));

        if (dao.getRangeWeaponExplodeRange() != null && !dao.getRangeWeaponExplodeRange().isEmpty())
            build.setRangeWeaponExplodeRange(Arrays.stream(dao.getRangeWeaponExplodeRange().split(",")).map(Integer::valueOf).collect(Collectors.toList()));

        if (dao.getMeleeDmgEffects() != null && !dao.getMeleeDmgEffects().isEmpty())
            build.setMeleeDmgEffects(Arrays.stream(dao.getMeleeDmgEffects().split(",")).map(DamageModEffect::valueOf).collect(Collectors.toList()));

        if (dao.getMeleeOffhandDmgEffects() != null && !dao.getMeleeOffhandDmgEffects().isEmpty())
            build.setMeleeOffhandDmgEffects(Arrays.stream(dao.getMeleeOffhandDmgEffects().split(",")).map(DamageModEffect::valueOf).collect(Collectors.toList()));

        if (dao.getMeleePolyDmgEffects() != null && !dao.getMeleePolyDmgEffects().isEmpty())
            build.setMeleePolyDmgEffects(Arrays.stream(dao.getMeleePolyDmgEffects().split(",")).map(DamageModEffect::valueOf).collect(Collectors.toList()));

        if (dao.getRangeDmgEffects() != null && !dao.getRangeDmgEffects().isEmpty())
            build.setRangeDmgEffects(Arrays.stream(dao.getRangeDmgEffects().split(",")).map(DamageModEffect::valueOf).collect(Collectors.toList()));

        if (dao.getRangeOffhandDmgEffects() != null && !dao.getRangeOffhandDmgEffects().isEmpty())
            build.setRangeOffhandDmgEffects(Arrays.stream(dao.getRangeOffhandDmgEffects().split(",")).map(DamageModEffect::valueOf).collect(Collectors.toList()));

        return build;
    }
}
