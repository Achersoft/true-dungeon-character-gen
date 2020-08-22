package com.achersoft.tdcc.vtd.dao;

import com.achersoft.tdcc.character.dao.CharacterNote;
import com.achersoft.tdcc.character.dao.CharacterStats;
import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.enums.WeaponExplodeCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class VtdDetails {
    private String characterId;
    private String userId;
    private Date expires;
    private String name;
    private CharacterClass characterClass;
    private CharacterStats stats;
    private int currentHealth;
    private int rollerDifficulty;
    private int initBonus;
    private int healthBonus;
    private int roomNumber = 0;
    private List<CharacterNote> notes;
    private List<CharacterSkill> characterSkills;
    private List<VtdBuff> buffs;
    private String meleeDmgRange;
    private String meleeOffhandDmgRange;
    private String meleePolyDmgRange;
    private String rangeDmgRange;
    private String rangeOffhandDmgRange;
    private String meleeWeaponExplodeRange;
    private String meleeOffhandWeaponExplodeRange;
    private String rangeWeaponExplodeRange;
    private WeaponExplodeCondition meleeWeaponExplodeEffect;
    private WeaponExplodeCondition meleeOffhandWeaponExplodeEffect;
    private WeaponExplodeCondition rangeWeaponExplodeEffect;
    private String meleeWeaponExplodeText;
    private String meleeOffhandWeaponExplodeText;
    private String rangeWeaponExplodeText;
    private int meleeCritMin;
    private int meleeOffhandCritMin;
    private int meleePolyCritMin;
    private int rangeCritMin;
    private int meleeSneakCritMin;
    private int rangeSneakCritMin;
    private boolean isSneakCanCrit;
    private boolean isSneakAtRange;
    private boolean splitHeal;
    private boolean madEvoker;
    private boolean mightyWeapon;
}
