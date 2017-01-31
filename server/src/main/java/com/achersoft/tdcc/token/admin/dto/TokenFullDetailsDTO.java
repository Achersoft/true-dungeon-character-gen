package com.achersoft.tdcc.token.admin.dto;

import com.achersoft.tdcc.enums.Rarity;
import com.achersoft.tdcc.enums.Slot;
import com.achersoft.tdcc.enums.UsableBy;
import com.achersoft.tdcc.token.admin.dao.TokenFullDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    public @NotNull List<UsableBy> usableBy;
    public Integer str;
    public Integer dex;
    public Integer con;
    public Integer intel;
    public Integer wis;
    public Integer cha;
    public Integer health;
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
    public Integer treasure;  
    
    public TokenFullDetails toDAO() {
        TokenFullDetails build = TokenFullDetails.builder()
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
                .treasure(treasure)
                .build();
        
        if(usableBy.contains(UsableBy.ALL)){
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
            if(usableBy.contains(UsableBy.BARBARIAN))
                build.setUsableByBarbarian(true);
            if(usableBy.contains(UsableBy.BARD))
                build.setUsableByBard(true);
            if(usableBy.contains(UsableBy.CLERIC))
                build.setUsableByCleric(true);
            if(usableBy.contains(UsableBy.DRUID))
                build.setUsableByDruid(true);
            if(usableBy.contains(UsableBy.FIGHTER))
                build.setUsableByFighter(true);
            if(usableBy.contains(UsableBy.MONK))
                build.setUsableByMonk(true);
            if(usableBy.contains(UsableBy.PALADIN))
                build.setUsableByPaladin(true);
            if(usableBy.contains(UsableBy.RANGER))
                build.setUsableByRanger(true);
            if(usableBy.contains(UsableBy.ROGUE))
                build.setUsableByRogue(true);
            if(usableBy.contains(UsableBy.WIZARD))
                build.setUsableByWizard(true);  
        }

        return build;
    }
}
