package com.achersoft.tdcc.token.admin.dao;

import com.achersoft.tdcc.enums.ConditionalUse;
import com.achersoft.tdcc.enums.Rarity;
import com.achersoft.tdcc.enums.Slot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class TokenFullDetails {
    private String id;
    private String name;
    private String text;
    private Rarity rarity;
    private Slot slot;
    private String damageRange;
    private boolean usableByBarbarian;
    private boolean usableByBard;
    private boolean usableByCleric;
    private boolean usableByDruid;
    private boolean usableByDwarfFighter;
    private boolean usableByElfWizard;
    private boolean usableByFighter;
    private boolean usableByWizard;
    private boolean usableByMonk;
    private boolean usableByPaladin;
    private boolean usableByRanger;
    private boolean usableByRogue;
    private int str;
    private int dex;
    private int con;
    private int intel;
    private int wis;
    private int cha;
    private int health;
    private int regen; 
    private boolean oneHanded;
    private boolean twoHanded;
    private boolean shield;
    private boolean buckler;
    private boolean mug;
    private boolean thrown;
    private boolean monkOffhand;
    private boolean rangerOffhand;
    private boolean instrument;
    private boolean shuriken;
    private boolean bracerWeapon;
    private int meleeHit;
    private int meleeDmg;
    private int meleePolyHit;
    private int meleePolyDmg;
    private boolean meleeFire;
    private boolean meleeCold;
    private boolean meleeShock;
    private boolean meleeSonic;
    private boolean meleeEldritch;
    private boolean meleePoison;
    private boolean meleeDarkrift;
    private boolean meleeSacred;
    private int meleeAC;
    private boolean rangedWeapon;
    private int rangeHit;
    private int rangeDmg;
    private boolean rangeFire;
    private boolean rangeCold;
    private boolean rangeShock;
    private boolean rangeSonic;
    private boolean rangeEldritch;
    private boolean rangePoison;
    private boolean rangeDarkrift;
    private boolean rangeSacred;
    private int rangeAC;
    private int rangeMissileAC;
    private int fort;
    private int reflex;
    private int will;
    private int retDmg;
    private boolean retFire;
    private boolean retCold;
    private boolean retShock;
    private boolean retSonic;
    private boolean retEldritch;
    private boolean retPoison;
    private boolean retDarkrift;
    private boolean retSacred;
    private boolean cannotBeSuprised;
    private boolean freeMovement;
    private boolean psychic;
    private boolean possession;
    private boolean addLevel;
    private boolean wonderEffect;
    private boolean setRingsThree;
    private boolean noRings;
    private int spellDmg;
    private int spellHeal;
    private int spellResist;
    private int initiative;   
    private int treasureMin;  
    private int treasureMax; 
    private int drMelee;
    private int drRange;
    private int drSpell;
    private int drFire;
    private int drCold;
    private int drShock;
    private int drSonic;
    private int drEldritch;
    private int drPoison;
    private int drDarkrift;
    private int drSacred;
    private int drForce;
    private int headSlots;
    private int backSlots;
    private int stoneSlots;
    private int charmSlots;
    private ConditionalUse conditionalUse;
    private boolean alwaysInEffect;
    private boolean oncePerRound;
    private boolean oncePerRoom;
    private boolean oncePerGame;
    private String specialText;
}
