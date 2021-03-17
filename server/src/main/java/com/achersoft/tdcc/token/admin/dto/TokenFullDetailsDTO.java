package com.achersoft.tdcc.token.admin.dto;

import com.achersoft.tdcc.enums.*;
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
    public String damageRange;
    public String damageExplodeRange;
    public WeaponExplodeCondition weaponExplodeCondition;
    public String weaponExplodeText;
    public Integer critMin;
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
    public Boolean buckler;
    public Boolean mug;
    public Boolean thrown;
    public Boolean rangerOffhand;
    public Boolean instrument;
    public Boolean monkOffhand;
    public Boolean shuriken;
    public Boolean bracerWeapon;
    public Boolean holyWeapon;
    public Integer meleeHit;
    public Integer meleeDmg;
    public Integer meleePolyHit;
    public Integer meleePolyDmg;
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
    public Boolean possession;
    public Boolean addLevel;
    public Boolean wonderEffect;
    public Boolean setRingsThree;
    public Boolean noRings;
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
    public Integer drForce;
    public Integer headSlots;
    public Integer backSlots;
    public Integer stoneSlots;
    public Integer charmSlots;
    public Integer eyeSlots;
    public Integer figurineSlots;
    public Integer rareEyeSlots;
    public Integer rareFeetSlots;
    public Integer rareLegSlots;
    public Integer rareShirtSlots;
    public Integer rareWaistSlots;
    public ConditionalUse conditionalUse;
    public Boolean alwaysInEffect;
    public Boolean oncePerRound;
    public Boolean oncePerRoom;
    public Boolean oncePerGame;
    public String specialText;
    public Integer mFire;
    public Integer mCold;
    public Integer mShock;
    public Integer mSonic;
    public Integer mEldritch;
    public Integer mPoison;
    public Integer mDarkrift;
    public Integer mSacred;
    public Integer mForce;
    public Integer mAcid;
    public Integer rFire;
    public Integer rCold;
    public Integer rShock;
    public Integer rSonic;
    public Integer rEldritch;
    public Integer rPoison;
    public Integer rDarkrift;
    public Integer rSacred;
    public Integer rForce;
    public Integer rAcid;
    
    public static TokenFullDetailsDTO fromDAO(TokenFullDetails tokenFullDetails) {
        TokenFullDetailsDTO build = TokenFullDetailsDTO.builder()
                .id(tokenFullDetails.getId())
                .name(tokenFullDetails.getName())
                .text(tokenFullDetails.getText())
                .rarity(tokenFullDetails.getRarity())
                .slot(tokenFullDetails.getSlot())
                .damageRange(tokenFullDetails.getDamageRange())
                .damageExplodeRange(tokenFullDetails.getDamageExplodeRange())
                .weaponExplodeCondition(tokenFullDetails.getWeaponExplodeCondition())
                .weaponExplodeText(tokenFullDetails.getWeaponExplodeText())
                .critMin(tokenFullDetails.getCritMin())
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
                .buckler(tokenFullDetails.isBuckler())
                .mug(tokenFullDetails.isMug())
                .thrown(tokenFullDetails.isThrown())
                .rangerOffhand(tokenFullDetails.isRangerOffhand())
                .instrument(tokenFullDetails.isInstrument())
                .bracerWeapon(tokenFullDetails.isBracerWeapon())
                .monkOffhand(tokenFullDetails.isMonkOffhand())
                .shuriken(tokenFullDetails.isShuriken())
                .meleeHit(tokenFullDetails.getMeleeHit())
                .meleeDmg(tokenFullDetails.getMeleeDmg())
                .meleePolyHit(tokenFullDetails.getMeleePolyHit())
                .meleePolyDmg(tokenFullDetails.getMeleePolyDmg())
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
                .possession(tokenFullDetails.isPossession())
                .addLevel(tokenFullDetails.isAddLevel())
                .wonderEffect(tokenFullDetails.isWonderEffect())
                .setRingsThree(tokenFullDetails.isSetRingsThree())
                .noRings(tokenFullDetails.isNoRings())
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
                .drForce(tokenFullDetails.getDrForce())
                .headSlots(tokenFullDetails.getHeadSlots())
                .backSlots(tokenFullDetails.getBackSlots())
                .charmSlots(tokenFullDetails.getCharmSlots())
                .stoneSlots(tokenFullDetails.getStoneSlots())
                .eyeSlots(tokenFullDetails.getEyeSlots())
                .figurineSlots(tokenFullDetails.getFigurineSlots())
                .rareEyeSlots(tokenFullDetails.getRareEyeSlots())
                .rareFeetSlots(tokenFullDetails.getRareFeetSlots())
                .rareLegSlots(tokenFullDetails.getRareLegSlots())
                .rareShirtSlots(tokenFullDetails.getRareShirtSlots())
                .rareWaistSlots(tokenFullDetails.getRareWaistSlots())
                .conditionalUse(tokenFullDetails.getConditionalUse())
                .alwaysInEffect(tokenFullDetails.isAlwaysInEffect())
                .oncePerRound(tokenFullDetails.isOncePerRound())
                .oncePerRoom(tokenFullDetails.isOncePerRoom())
                .oncePerGame(tokenFullDetails.isOncePerGame())
                .specialText(tokenFullDetails.getSpecialText())
                .usableBy(new ArrayList())
                .mFire(tokenFullDetails.getMFire())
                .mCold(tokenFullDetails.getMCold())
                .mShock(tokenFullDetails.getMShock())
                .mSonic(tokenFullDetails.getMSonic())
                .mEldritch(tokenFullDetails.getMEldritch())
                .mPoison(tokenFullDetails.getMPoison())
                .mDarkrift(tokenFullDetails.getMDarkrift())
                .mSacred(tokenFullDetails.getMSacred())
                .mForce(tokenFullDetails.getMForce())
                .mAcid(tokenFullDetails.getMAcid())
                .rFire(tokenFullDetails.getRFire())
                .rCold(tokenFullDetails.getRCold())
                .rShock(tokenFullDetails.getRShock())
                .rSonic(tokenFullDetails.getRSonic())
                .rEldritch(tokenFullDetails.getREldritch())
                .rPoison(tokenFullDetails.getRPoison())
                .rDarkrift(tokenFullDetails.getRDarkrift())
                .rSacred(tokenFullDetails.getRSacred())
                .rForce(tokenFullDetails.getRForce())
                .rAcid(tokenFullDetails.getRAcid())
                .build();
        
        if(tokenFullDetails.isUsableByBarbarian() && tokenFullDetails.isUsableByBard()&&
           tokenFullDetails.isUsableByCleric()&& tokenFullDetails.isUsableByDruid()&&
           tokenFullDetails.isUsableByDwarfFighter()&& tokenFullDetails.isUsableByElfWizard()&&     
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
            if(tokenFullDetails.isUsableByDwarfFighter()) 
                build.usableBy.add(CharacterClass.DWARF_FIGHTER);
            if(tokenFullDetails.isUsableByElfWizard()) 
                build.usableBy.add(CharacterClass.ELF_WIZARD);
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
                .damageRange(damageRange)
                .damageExplodeRange(damageExplodeRange)
                .weaponExplodeCondition(weaponExplodeCondition)
                .weaponExplodeText(weaponExplodeText)
                .critMin(critMin)
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
                .buckler(buckler)
                .mug(mug)
                .thrown(thrown)
                .rangerOffhand(rangerOffhand)
                .instrument(instrument)
                .monkOffhand(monkOffhand)
                .shuriken(shuriken)
                .bracerWeapon(bracerWeapon)
                .meleeHit(meleeHit)
                .meleeDmg(meleeDmg)
                .meleePolyHit(meleePolyHit)
                .meleePolyDmg(meleePolyDmg)
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
                .possession(possession)
                .addLevel(addLevel)
                .wonderEffect(wonderEffect)
                .setRingsThree(setRingsThree)
                .noRings(noRings)
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
                .drForce(drForce)
                .headSlots(headSlots)
                .backSlots(backSlots)
                .charmSlots(charmSlots)
                .stoneSlots(stoneSlots)
                .figurineSlots(figurineSlots)
                .eyeSlots(eyeSlots)
                .rareEyeSlots(this.getRareEyeSlots() == null ? 0 : this.getRareEyeSlots())
                .rareFeetSlots(this.getRareFeetSlots() == null ? 0 : this.getRareFeetSlots())
                .rareLegSlots(this.getRareLegSlots() == null ? 0 : this.getRareLegSlots())
                .rareShirtSlots(this.getRareShirtSlots() == null ? 0 : this.getRareShirtSlots())
                .rareWaistSlots(this.getRareWaistSlots() == null ? 0 : this.getRareWaistSlots())
                .conditionalUse((conditionalUse != null)?conditionalUse:ConditionalUse.NONE)
                .alwaysInEffect(alwaysInEffect)
                .oncePerRound(oncePerRound)
                .oncePerRoom(oncePerRoom)
                .oncePerGame(oncePerGame)
                .specialText(specialText)
                .mFire(this.getMFire())
                .mCold(this.getMCold())
                .mShock(this.getMShock())
                .mSonic(this.getMSonic())
                .mEldritch(this.getMEldritch())
                .mPoison(this.getMPoison())
                .mDarkrift(this.getMDarkrift())
                .mSacred(this.getMSacred())
                .mForce(this.getMForce())
                .mAcid(this.getMAcid())
                .rFire(this.getRFire())
                .rCold(this.getRCold())
                .rShock(this.getRShock())
                .rSonic(this.getRSonic())
                .rEldritch(this.getREldritch())
                .rPoison(this.getRPoison())
                .rDarkrift(this.getRDarkrift())
                .rSacred(this.getRSacred())
                .rForce(this.getRForce())
                .rAcid(this.getRAcid())
                .build();
        
        if(usableBy.contains(CharacterClass.ALL)){
            build.setUsableByBarbarian(true);
            build.setUsableByBard(true);
            build.setUsableByCleric(true);
            build.setUsableByDruid(true);
            build.setUsableByDwarfFighter(true);
            build.setUsableByElfWizard(true);
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
            if(usableBy.contains(CharacterClass.DWARF_FIGHTER))
                build.setUsableByDwarfFighter(true);
            if(usableBy.contains(CharacterClass.ELF_WIZARD))
                build.setUsableByElfWizard(true);
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
