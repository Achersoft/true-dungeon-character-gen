package com.achersoft.tdcc.token.admin.dao;

import com.achersoft.tdcc.enums.ConditionalUse;
import com.achersoft.tdcc.enums.Rarity;
import com.achersoft.tdcc.enums.Slot;
import com.achersoft.tdcc.enums.WeaponExplodeCondition;
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
    private String damageExplodeRange;
    private WeaponExplodeCondition weaponExplodeCondition;
    private String weaponExplodeText;
    private int critMin;
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
    private boolean holyWeapon;
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
    private int eyeSlots;
    private int figurineSlots;
    private ConditionalUse conditionalUse;
    private boolean alwaysInEffect;
    private boolean oncePerRound;
    private boolean oncePerRoom;
    private boolean oncePerGame;
    private String specialText;
    private int mFire = 0;
    private int mCold = 0;
    private int mShock = 0;
    private int mSonic = 0;
    private int mEldritch = 0;
    private int mPoison = 0;
    private int mDarkrift = 0;
    private int mSacred = 0;
    private int mForce = 0;
    private int mAcid = 0;
    private int rFire = 0;
    private int rCold = 0;
    private int rShock = 0;
    private int rSonic = 0;
    private int rEldritch = 0;
    private int rPoison = 0;
    private int rDarkrift = 0;
    private int rSacred = 0;
    private int rForce = 0;
    private int rAcid = 0;
}
