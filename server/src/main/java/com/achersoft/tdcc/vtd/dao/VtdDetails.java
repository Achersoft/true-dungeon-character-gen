package com.achersoft.tdcc.vtd.dao;

import com.achersoft.tdcc.character.dao.CharacterItem;
import com.achersoft.tdcc.character.dao.CharacterNote;
import com.achersoft.tdcc.character.dao.CharacterStats;
import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.enums.DamageModEffect;
import com.achersoft.tdcc.enums.WeaponExplodeCondition;
import com.achersoft.tdcc.vtd.admin.dao.VtdRoom;
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
    private String characterOrigId;
    private String userId;
    private String adventureId;
    private String adventureName;
    private List<VtdMonster> monsters;
    private Date expires;
    private String name;
    private CharacterClass characterClass;
    private CharacterStats stats;
    private int currentHealth;
    private int rollerDifficulty;
    private int initBonus;
    private int healthBonus;
    private int glovesCabalBonus;
    private int braceletCabalBonus;
    private int charmCabalBonus;
    private int roomNumber = 0;
    private int meleeMainHit;
    private int meleeOffhandHit;
    private int rangeMainHit;
    private int rangeOffhandHit;
    private List<CharacterNote> notes;
    private List<CharacterItem> items;
    private List<CharacterSkill> characterSkills;
    private List<VtdBuff> buffs;
    private List<VtdPoly> polys;
    private String critTypes;
    private String availableEffects;
    private String meleeDmgRange;
    private String meleeOffhandDmgRange;
    private String meleePolyDmgRange;
    private String rangeDmgRange;
    private String rangeOffhandDmgRange;
    private String meleeWeaponExplodeRange;
    private String meleeWeaponSecondaryExplodeRange;
    private String meleeOffhandWeaponExplodeRange;
    private String rangeWeaponExplodeRange;
    private String rangeOffhandWeaponExplodeRange;
    private WeaponExplodeCondition meleeWeaponExplodeEffect;
    private WeaponExplodeCondition meleeWeaponSecondaryExplodeEffect;
    private WeaponExplodeCondition meleeOffhandWeaponExplodeEffect;
    private WeaponExplodeCondition rangeWeaponExplodeEffect;
    private WeaponExplodeCondition rangeOffhandWeaponExplodeEffect;
    private String meleeWeaponExplodeText;
    private String meleeWeaponSecondaryExplodeText;
    private String meleeOffhandWeaponExplodeText;
    private String rangeWeaponExplodeText;
    private String rangeOffhandWeaponExplodeText;
    private String meleeDmgEffects;
    private String meleeOffhandDmgEffects;
    private String meleePolyDmgEffects;
    private String rangeDmgEffects;
    private String rangeOffhandDmgEffects;
    private int meleeCritMin;
    private int meleeOffhandCritMin;
    private int meleePolyCritMin;
    private int rangeCritMin;
    private int meleeSneakHit;
    private int meleeSneakDamage;
    private int meleeSneakCritMin;
    private int rangeSneakHit;
    private int rangeSneakDamage;
    private int rangeSneakCritMin;
    private int unmodifiableSneakDamage;
    private boolean isSneakCanCrit;
    private boolean isSneakAtRange;
    private boolean splitHeal;
    private boolean madEvoker;
    private boolean swapElements;
    private boolean magePower;
    private boolean archMagePower;
    private boolean mightyWeapon;
    private boolean prestigeAvailable;
    private boolean prestigeActive;
}
