package com.achersoft.tdcc.token.admin.dto;

import com.achersoft.tdcc.enums.Rarity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenFullDetailsDTO {
    private String id;
    private String name;
    private String text;
    private Rarity rarity;
    private boolean usableByBarbarian;
    private boolean usableByBard;
    private boolean usableByCleric;
    private boolean usableByDruid;
    private boolean usableByFighter;
    private boolean usableByWizard;
    private boolean usableByMonk;
    private boolean usableByPaladin;
    private boolean usableByRanger;
    private boolean usableByRogue;
    private Integer str;
    private Integer dex;
    private Integer con;
    private Integer intel;
    private Integer wis;
    private Integer cha;
    private Integer health;
    private Integer meleeHit;
    private Integer meleeDmg;
    private Boolean meleeFire;
    private Boolean meleeCold;
    private Boolean meleeShock;
    private Boolean meleeSonic;
    private Boolean meleeEldritch;
    private Boolean meleePosion;
    private Boolean meleeDarkrift;
    private Boolean meleeSacred;
    private Integer meleeAC;
    private Integer rangeHit;
    private Integer rangeDmg;
    private Boolean rangeFire;
    private Boolean rangeCold;
    private Boolean rangeShock;
    private Boolean rangeSonic;
    private Boolean rangeEldritch;
    private Boolean rangePosion;
    private Boolean rangeDarkrift;
    private Boolean rangeSacred;
    private Integer rangeAC;
    private Integer rangeMissleAC;
    private Integer fort;
    private Integer reflex;
    private Integer will;
    private Integer retDmg;
    private Boolean retFire;
    private Boolean retCold;
    private Boolean retShock;
    private Boolean retSonic;
    private Boolean retEldritch;
    private Boolean retPosion;
    private Boolean retDarkrift;
    private Boolean retSacred;
    private Boolean cannotBeSuprised;
    private Boolean freeMovement;
    private Boolean psychic;
    private Integer spellDmg;
    private Integer spellHeal;
    private Integer spellResist;
    private Integer treasure;  
}
