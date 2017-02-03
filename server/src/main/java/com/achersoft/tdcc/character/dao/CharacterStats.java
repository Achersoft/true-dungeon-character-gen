package com.achersoft.tdcc.character.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class CharacterStats {
    private String characterId = null;
    private int str = 0;
    private int dex = 0;
    private int con = 0;
    private int intel = 0;
    private int wis = 0;
    private int cha = 0;
    private int health = 0;
    private int regen = 0; 
    private int meleeHit = 0;
    private int meleeDmg = 0;
    private boolean meleeFire = false;
    private boolean meleeCold = false;
    private boolean meleeShock = false;
    private boolean meleeSonic = false;
    private boolean meleeEldritch = false;
    private boolean meleePoison = false;
    private boolean meleeDarkrift = false;
    private boolean meleeSacred = false;
    private int meleeAC = 0;
    private int rangeHit = 0;
    private int rangeDmg = 0;
    private boolean rangeFire = false;
    private boolean rangeCold = false;
    private boolean rangeShock = false;
    private boolean rangeSonic = false;
    private boolean rangeEldritch = false;
    private boolean rangePoison = false;
    private boolean rangeDarkrift = false;
    private boolean rangeSacred = false;
    private int rangeAC = 0;
    private int rangeMissileAC = 0;
    private int fort = 0;
    private int reflex = 0;
    private int will = 0;
    private int retDmg = 0;
    private boolean retFire = false;
    private boolean retCold = false;
    private boolean retShock = false;
    private boolean retSonic = false;
    private boolean retEldritch = false;
    private boolean retPoison = false;
    private boolean retDarkrift = false;
    private boolean retSacred = false;
    private boolean cannotBeSuprised = false;
    private boolean freeMovement = false;
    private boolean psychic = false;
    private int spellDmg = 0;
    private int spellHeal = 0;
    private int spellResist = 0;
    private int initiative = 0;  
    private int treasureMin = 0;  
    private int treasureMax = 0; 
    private int level = 0;
    private int drMelee = 0;
    private int drRange = 0;
    private int drSpell = 0;
    private int drFire = 0;
    private int drCold = 0;
    private int drShock = 0;
    private int drSonic = 0;
    private int drEldritch = 0;
    private int drPoison = 0;
    private int drDarkrift = 0;
    private int drSacred = 0;
}
