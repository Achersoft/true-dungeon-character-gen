package com.achersoft.tdcc.vtd.dao;

import com.achersoft.tdcc.character.dao.CharacterStats;
import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.enums.WeaponExplodeCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class temp {
    String characterId;
    String userId;
    Date expires;
    String name;
    CharacterClass characterClass;
    CharacterStats stats;
    int currentHealth;
    int rollerDifficulty;
    int initBonus;
    int roomNumber;
    String meleeDmgRange;
    String meleeOffhandDmgRange;
    String meleePolyDmgRange;
    String rangeDmgRange;
    String rangeOffhandDmgRange;
    String meleeWeaponExplodeRange;
    String rangeWeaponExplodeRange;
    WeaponExplodeCondition meleeWeaponExplodeEffect;
    WeaponExplodeCondition rangeWeaponExplodeEffect;
    String meleeWeaponExplodeText;
    String rangeWeaponExplodeText;
    int meleeCritMin;
    int meleeOffhandCritMin;
    int meleePolyCritMin;
    int rangeCritMin;
    int meleeSneakCritMin;
    int rangeSneakCritMin;
    boolean isSneakCanCrit;
    boolean isSneakAtRange;
    boolean splitHeal;
    boolean madEvoker;
    boolean mightyWeapon;
}
