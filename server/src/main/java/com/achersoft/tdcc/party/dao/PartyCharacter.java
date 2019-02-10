package com.achersoft.tdcc.party.dao;

import com.achersoft.tdcc.character.dao.CharacterDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class PartyCharacter {
    private String id;
    private String name;
    private String userName;
    private int level = 0;
    private int health = 0;
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
    private int psychicLevel = 0;
    private int spellDmg = 0;
    private int spellHeal = 0;
    private int spellResist = 0;  
    private boolean commonScroll = false;
    private boolean uncommonScroll = false;
    private boolean rareScroll = false;
    private int treasure = 0;   
    private boolean hasCoGF = false;
    private boolean hasCoS = false;
    private boolean hasGoC = false;
    private boolean hasBoC = false;
    private boolean hasCoC = false;
    private boolean hasEotP = false;
    private boolean hasSotP = false;
    
    public static PartyCharacter fromCharacterDetails(CharacterDetails cd) {
        return PartyCharacter.builder()
                .id(cd.getId())
                .name(cd.getName())
                .level(cd.getStats().getLevel())
                .health(cd.getStats().getHealth())
                .meleeHit(cd.getStats().getMeleeHit())
                .meleeDmg(cd.getStats().getMeleeDmg())
                .meleeFire(cd.getStats().isMeleeFire())
                .meleeCold(cd.getStats().isMeleeCold()) 
                .meleeShock(cd.getStats().isMeleeShock()) 
                .meleeSonic(cd.getStats().isMeleeSonic())
                .meleeEldritch(cd.getStats().isMeleeEldritch()) 
                .meleePoison(cd.getStats().isMeleePoison())
                .meleeDarkrift(cd.getStats().isMeleeDarkrift())
                .meleeSacred(cd.getStats().isMeleeSacred())
                .meleeAC(cd.getStats().getMeleeAC())
                .rangeHit(cd.getStats().getRangeHit())
                .rangeDmg(cd.getStats().getRangeDmg())
                .rangeFire(cd.getStats().isRangeFire())
                .rangeCold(cd.getStats().isRangeCold())
                .rangeShock(cd.getStats().isRangeShock())
                .rangeSonic(cd.getStats().isRangeSonic())
                .rangeEldritch(cd.getStats().isRangeEldritch())
                .rangePoison(cd.getStats().isRangePoison())
                .rangeDarkrift(cd.getStats().isRangeDarkrift())
                .rangeSacred(cd.getStats().isRangeSacred())
                .rangeAC(cd.getStats().getRangeAC())
                .rangeMissileAC(cd.getStats().getRangeMissileAC())
                .fort(cd.getStats().getFort())
                .reflex(cd.getStats().getReflex())
                .will(cd.getStats().getWill())
                .retDmg(cd.getStats().getRetDmg())
                .retFire(cd.getStats().isRetFire())
                .retCold(cd.getStats().isRetCold())
                .retShock(cd.getStats().isRetShock())
                .retSonic(cd.getStats().isRetSonic())
                .retEldritch(cd.getStats().isRetEldritch())
                .retPoison(cd.getStats().isRetPoison())
                .retDarkrift(cd.getStats().isRetDarkrift())
                .retSacred(cd.getStats().isRetSacred())
                .cannotBeSuprised(cd.getStats().isCannotBeSuprised())
                .freeMovement(cd.getStats().isFreeMovement())
                .psychic(cd.getStats().isPsychic())
                .psychicLevel(cd.getStats().getPsychicLevel())
                .spellDmg(cd.getStats().getSpellDmg())
                .spellHeal(cd.getStats().getSpellHeal())
                .spellResist(cd.getStats().getSpellResist())
                .treasure(cd.getStats().getTreasureMin())
                .build();
    }
}
