package com.achersoft.tdcc.token.admin.dto;

import com.achersoft.tdcc.enums.ConditionalUse;
import com.achersoft.tdcc.enums.Rarity;
import com.achersoft.tdcc.enums.Slot;
import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.token.admin.dao.TokenFullDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenFullDetailsDTO {
    public String id;
    public @NotNull @NotEmpty String name;
    public String text;
    public @NotNull Rarity rarity;
    public @NotNull Slot slot;
    public @NotNull List<CharacterClass> usableBy;
    public Integer str;
    public Integer dex;
    public Integer con;
    public Integer intel;
    public Integer wis;
    public Integer cha;
    public Integer health;
    public Integer regen;
    public Boolean oneHanded;
    public Boolean twoHanded;
    public Boolean shield;
    public Boolean thrown;
    public Boolean rangerOffhand;
    public Boolean instrument;
    public Integer meleeHit;
    public Integer meleeDmg;
    public Boolean meleeFire;
    public Boolean meleeCold;
    public Boolean meleeShock;
    public Boolean meleeSonic;
    public Boolean meleeEldritch;
    public Boolean meleePoison;
    public Boolean meleeDarkrift;
    public Boolean meleeSacred;
    public Integer meleeAC;
    public Boolean rangedWeapon;
    public Integer rangeHit;
    public Integer rangeDmg;
    public Boolean rangeFire;
    public Boolean rangeCold;
    public Boolean rangeShock;
    public Boolean rangeSonic;
    public Boolean rangeEldritch;
    public Boolean rangePoison;
    public Boolean rangeDarkrift;
    public Boolean rangeSacred;
    public Integer rangeAC;
    public Integer rangeMissileAC;
    public Integer fort;
    public Integer reflex;
    public Integer will;
    public Integer retDmg;
    public Boolean retFire;
    public Boolean retCold;
    public Boolean retShock;
    public Boolean retSonic;
    public Boolean retEldritch;
    public Boolean retPoison;
    public Boolean retDarkrift;
    public Boolean retSacred;
    public Boolean cannotBeSuprised;
    public Boolean freeMovement;
    public Boolean psychic;
    public Integer spellDmg;
    public Integer spellHeal;
    public Integer spellResist;
    public Integer initiative;  
    public Integer treasureMin;  
    public Integer treasureMax;
    public Integer drMelee;
    public Integer drRange;
    public Integer drSpell;
    public Integer drFire;
    public Integer drCold;
    public Integer drShock;
    public Integer drSonic;
    public Integer drEldritch;
    public Integer drPoison;
    public Integer drDarkrift;
    public Integer drSacred;
    public ConditionalUse conditionalUse;
    public Boolean alwaysInEffect;
    public Boolean oncePerRound;
    public Boolean oncePerRoom;
    public Boolean oncePerGame;
    public String specialText;
    
    public static TokenFullDetailsDTO fromDAO(TokenFullDetails tokenFullDetails) {
        TokenFullDetailsDTO build = TokenFullDetailsDTO.builder()
                .id(tokenFullDetails.getId())
                .name(tokenFullDetails.getName())
                .text(tokenFullDetails.getText())
                .rarity(tokenFullDetails.getRarity())
                .slot(tokenFullDetails.getSlot())
                .str(tokenFullDetails.getStr())
                .dex(tokenFullDetails.getDex())
                .con(tokenFullDetails.getCon())
                .intel(tokenFullDetails.getIntel())
                .wis(tokenFullDetails.getWis())
                .cha(tokenFullDetails.getCha())
                .health(tokenFullDetails.getHealth())
                .regen(tokenFullDetails.getRegen())
                .oneHanded(tokenFullDetails.isOneHanded())
                .twoHanded(tokenFullDetails.isTwoHanded())
                .shield(tokenFullDetails.isShield())
                .thrown(tokenFullDetails.isThrown())
                .rangerOffhand(tokenFullDetails.isRangerOffhand())
                .instrument(tokenFullDetails.isInstrument())
                .meleeHit(tokenFullDetails.getMeleeHit())
                .meleeDmg(tokenFullDetails.getMeleeDmg())
                .meleeFire(tokenFullDetails.isMeleeFire())
                .meleeCold(tokenFullDetails.isMeleeCold())
                .meleeShock(tokenFullDetails.isMeleeShock())
                .meleeSonic(tokenFullDetails.isMeleeSonic())
                .meleeEldritch(tokenFullDetails.isMeleeEldritch())
                .meleePoison(tokenFullDetails.isMeleePoison())
                .meleeDarkrift(tokenFullDetails.isMeleeDarkrift())
                .meleeSacred(tokenFullDetails.isMeleeSacred())
                .meleeAC(tokenFullDetails.getMeleeAC())
                .rangedWeapon(tokenFullDetails.isRangedWeapon())
                .rangeHit(tokenFullDetails.getRangeHit())
                .rangeDmg(tokenFullDetails.getRangeDmg())
                .rangeFire(tokenFullDetails.isRangeFire())
                .rangeCold(tokenFullDetails.isRangeCold())
                .rangeShock(tokenFullDetails.isRangeShock())
                .rangeSonic(tokenFullDetails.isRangeSonic())
                .rangeEldritch(tokenFullDetails.isRetEldritch())
                .rangePoison(tokenFullDetails.isRetPoison())
                .rangeDarkrift(tokenFullDetails.isRangeDarkrift())
                .rangeSacred(tokenFullDetails.isRangeSacred())
                .rangeAC(tokenFullDetails.getRangeAC())
                .rangeMissileAC(tokenFullDetails.getRangeMissileAC())
                .fort(tokenFullDetails.getFort())
                .reflex(tokenFullDetails.getReflex())
                .will(tokenFullDetails.getWill())
                .retDmg(tokenFullDetails.getRetDmg())
                .retFire(tokenFullDetails.isRetFire())
                .retCold(tokenFullDetails.isRetCold())
                .retShock(tokenFullDetails.isRetShock())
                .retSonic(tokenFullDetails.isRetSonic())
                .retEldritch(tokenFullDetails.isRetEldritch())
                .retPoison(tokenFullDetails.isRetPoison())
                .retDarkrift(tokenFullDetails.isRetDarkrift())
                .retSacred(tokenFullDetails.isRetSacred())
                .cannotBeSuprised(tokenFullDetails.isCannotBeSuprised())
                .freeMovement(tokenFullDetails.isFreeMovement())
                .psychic(tokenFullDetails.isPsychic())
                .spellDmg(tokenFullDetails.getSpellDmg())
                .spellHeal(tokenFullDetails.getSpellHeal())
                .spellResist(tokenFullDetails.getSpellResist())
                .initiative(tokenFullDetails.getInitiative())
                .treasureMin(tokenFullDetails.getTreasureMin())
                .treasureMax(tokenFullDetails.getTreasureMax())
                .drMelee(tokenFullDetails.getDrMelee())
                .drRange(tokenFullDetails.getDrRange())
                .drSpell(tokenFullDetails.getDrSpell())
                .drFire(tokenFullDetails.getDrFire())
                .drCold(tokenFullDetails.getDrCold())
                .drShock(tokenFullDetails.getDrShock())
                .drSonic(tokenFullDetails.getDrSonic())
                .drEldritch(tokenFullDetails.getDrEldritch())
                .drPoison(tokenFullDetails.getDrPoison())
                .drDarkrift(tokenFullDetails.getDrDarkrift())
                .drSacred(tokenFullDetails.getDrSacred())
                .conditionalUse(tokenFullDetails.getConditionalUse())
                .alwaysInEffect(tokenFullDetails.isAlwaysInEffect())
                .oncePerRound(tokenFullDetails.isOncePerRound())
                .oncePerRoom(tokenFullDetails.isOncePerRoom())
                .oncePerGame(tokenFullDetails.isOncePerGame())
                .specialText(tokenFullDetails.getSpecialText())
                .usableBy(new ArrayList())
                .build();
        
        if(tokenFullDetails.isUsableByBarbarian() && tokenFullDetails.isUsableByBard()&&
           tokenFullDetails.isUsableByCleric()&& tokenFullDetails.isUsableByDruid()&&
           tokenFullDetails.isUsableByFighter()&& tokenFullDetails.isUsableByMonk() &&
           tokenFullDetails.isUsableByPaladin()&& tokenFullDetails.isUsableByRanger()&&
           tokenFullDetails.isUsableByRogue()&& tokenFullDetails.isUsableByWizard())
            build.usableBy.add(CharacterClass.ALL);
        else {
            if(tokenFullDetails.isUsableByBarbarian()) 
                build.usableBy.add(CharacterClass.BARBARIAN);
            if(tokenFullDetails.isUsableByBard()) 
                build.usableBy.add(CharacterClass.BARD);
            if(tokenFullDetails.isUsableByCleric()) 
                build.usableBy.add(CharacterClass.CLERIC);
            if(tokenFullDetails.isUsableByDruid()) 
                build.usableBy.add(CharacterClass.DRUID);
            if(tokenFullDetails.isUsableByFighter()) 
                build.usableBy.add(CharacterClass.FIGHTER);
            if(tokenFullDetails.isUsableByMonk()) 
                build.usableBy.add(CharacterClass.MONK);
            if(tokenFullDetails.isUsableByPaladin()) 
                build.usableBy.add(CharacterClass.PALADIN);
            if(tokenFullDetails.isUsableByRanger()) 
                build.usableBy.add(CharacterClass.RANGER);
            if(tokenFullDetails.isUsableByRogue()) 
                build.usableBy.add(CharacterClass.ROGUE);
            if(tokenFullDetails.isUsableByWizard()) 
                build.usableBy.add(CharacterClass.WIZARD);
        }

        return build;
    }
    
    public TokenFullDetails toDAO() {
        TokenFullDetails build = TokenFullDetails.builder()
                .id(id)
                .name(name)
                .text(text)
                .rarity(rarity)
                .slot(slot)
                .str(str)
                .dex(dex)
                .con(con)
                .intel(intel)
                .wis(wis)
                .cha(cha)
                .health(health)
                .regen(regen)
                .oneHanded(oneHanded)
                .twoHanded(twoHanded)
                .shield(shield)
                .thrown(thrown)
                .rangerOffhand(rangerOffhand)
                .instrument(instrument)
                .meleeHit(meleeHit)
                .meleeDmg(meleeDmg)
                .meleeFire(meleeFire)
                .meleeCold(meleeCold)
                .meleeShock(meleeShock)
                .meleeSonic(meleeSonic)
                .meleeEldritch(meleeEldritch)
                .meleePoison(meleePoison)
                .meleeDarkrift(meleeDarkrift)
                .meleeSacred(meleeSacred)
                .meleeAC(meleeAC)
                .rangedWeapon(rangedWeapon)
                .rangeHit(rangeHit)
                .rangeDmg(rangeDmg)
                .rangeFire(rangeFire)
                .rangeCold(rangeCold)
                .rangeShock(rangeShock)
                .rangeSonic(rangeSonic)
                .rangeEldritch(rangeEldritch)
                .rangePoison(rangePoison)
                .rangeDarkrift(rangeDarkrift)
                .rangeSacred(rangeSacred)
                .rangeAC(rangeAC)
                .rangeMissileAC(rangeMissileAC)
                .fort(fort)
                .reflex(reflex)
                .will(will)
                .retDmg(retDmg)
                .retFire(retFire)
                .retCold(retCold)
                .retShock(retShock)
                .retSonic(retSonic)
                .retEldritch(retEldritch)
                .retPoison(retPoison)
                .retDarkrift(retDarkrift)
                .retSacred(retSacred)
                .cannotBeSuprised(cannotBeSuprised)
                .freeMovement(freeMovement)
                .psychic(psychic)
                .spellDmg(spellDmg)
                .spellHeal(spellHeal)
                .spellResist(spellResist)
                .initiative(initiative)
                .treasureMin(treasureMin)
                .treasureMax(treasureMax)
                .drMelee(drMelee)
                .drRange(drRange)
                .drSpell(drSpell)
                .drFire(drFire)
                .drCold(drCold)
                .drShock(drShock)
                .drSonic(drSonic)
                .drEldritch(drEldritch)
                .drPoison(drPoison)
                .drDarkrift(drDarkrift)
                .drSacred(drSacred)
                .conditionalUse((conditionalUse != null)?conditionalUse:ConditionalUse.NONE)
                .alwaysInEffect(alwaysInEffect)
                .oncePerRound(oncePerRound)
                .oncePerRoom(oncePerRoom)
                .oncePerGame(oncePerGame)
                .specialText(specialText)
                .build();
        
        if(usableBy.contains(CharacterClass.ALL)){
            build.setUsableByBarbarian(true);
            build.setUsableByBard(true);
            build.setUsableByCleric(true);
            build.setUsableByDruid(true);
            build.setUsableByFighter(true);
            build.setUsableByMonk(true);
            build.setUsableByPaladin(true);
            build.setUsableByRanger(true);
            build.setUsableByRogue(true);
            build.setUsableByWizard(true);
        } else if(usableBy != null && !usableBy.isEmpty()) {
            if(usableBy.contains(CharacterClass.BARBARIAN))
                build.setUsableByBarbarian(true);
            if(usableBy.contains(CharacterClass.BARD))
                build.setUsableByBard(true);
            if(usableBy.contains(CharacterClass.CLERIC))
                build.setUsableByCleric(true);
            if(usableBy.contains(CharacterClass.DRUID))
                build.setUsableByDruid(true);
            if(usableBy.contains(CharacterClass.FIGHTER))
                build.setUsableByFighter(true);
            if(usableBy.contains(CharacterClass.MONK))
                build.setUsableByMonk(true);
            if(usableBy.contains(CharacterClass.PALADIN))
                build.setUsableByPaladin(true);
            if(usableBy.contains(CharacterClass.RANGER))
                build.setUsableByRanger(true);
            if(usableBy.contains(CharacterClass.ROGUE))
                build.setUsableByRogue(true);
            if(usableBy.contains(CharacterClass.WIZARD))
                build.setUsableByWizard(true);  
        }

        return build;
    }
}
