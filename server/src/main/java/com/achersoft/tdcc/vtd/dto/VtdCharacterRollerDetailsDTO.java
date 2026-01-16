package com.achersoft.tdcc.vtd.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class VtdCharacterRollerDetailsDTO {

    public @Builder.Default VtdAppDataDTO appdata = VtdAppDataDTO.builder().build();
    public Integer meleeac;
    public String meleeweapon;
    public VtdWeaponDamageDTO meleedamage;
    public Integer rangedac;
    public String rangedweapon;
    public VtdWeaponDamageDTO rangeddamage;
    public Integer spelldamage;
    public VtdWeaponDamageDTO retributiondamage;
    public Integer maxhp;
    public Integer initiative;
    public Integer strength;
    public Integer dexterity;
    public Integer constitution;
    public Integer intelligence;
    public Integer wisdom;
    public Integer charisma;
    public Boolean freemovement;
    public Boolean nosurprise;
    public Boolean quickstrike;
    public Boolean vivs;
    public Boolean libramlooting;
    public @Builder.Default String notes = "";

    public static VtdCharacterRollerDetailsDTO from(VtdDetailsDTO dto) {
        return VtdCharacterRollerDetailsDTO.builder()
                .meleeac(dto.getStats().getMeleeAC())
                .meleeweapon(dto.getItems().meleeMainhand.name)
                .meleedamage(VtdWeaponDamageDTO.builder()
                        .total(dto.getStats().getMeleeDmg())
                        .fire(dto.getStats().getMFire())
                        .cold(dto.getStats().getMCold())
                        .shock(dto.getStats().getMShock())
                        .sonic(dto.getStats().getMSonic())
                        .eldritch(dto.getStats().getMEldritch())
                        .poison(dto.getStats().getMPoison())
                        .darkrift(dto.getStats().getMDarkrift())
                        .sacred(dto.getStats().getMSacred())
                        .acid(dto.getStats().getMAcid())
                        .build())
                .rangedac(dto.getStats().getRangeAC())
                .rangedweapon(dto.getItems().rangedMainhand.name)
                .rangeddamage(VtdWeaponDamageDTO.builder()
                        .total(dto.getStats().getRangeDmg())
                        .fire(dto.getStats().getRFire())
                        .cold(dto.getStats().getRCold())
                        .shock(dto.getStats().getRShock())
                        .sonic(dto.getStats().getRSonic())
                        .eldritch(dto.getStats().getREldritch())
                        .poison(dto.getStats().getRPoison())
                        .darkrift(dto.getStats().getRDarkrift())
                        .sacred(dto.getStats().getRSacred())
                        .acid(dto.getStats().getRAcid())
                        .build())
                .spelldamage(dto.getStats().getSpellDmg())
                .retributiondamage(VtdWeaponDamageDTO.builder()
                        .total(dto.getStats().getRetDmg())
                        .fire(dto.getStats().isRetFire()?1:0)
                        .cold(dto.getStats().isRetCold()?1:0)
                        .shock(dto.getStats().isRetShock()?1:0)
                        .sonic(dto.getStats().isRetSonic()?1:0)
                        .eldritch(dto.getStats().isRetEldritch()?1:0)
                        .poison(dto.getStats().isRetPoison()?1:0)
                        .darkrift(dto.getStats().isRetDarkrift()?1:0)
                        .sacred(dto.getStats().isRetSacred()?1:0)
                        .acid(0)
                        .build())
                .maxhp(dto.getStats().getHealth())
                .initiative(dto.initBonus)
                .strength(dto.getStats().getStr())
                .dexterity(dto.getStats().getDex())
                .constitution(dto.getStats().getCon())
                .intelligence(dto.getStats().getIntel())
                .wisdom(dto.getStats().getWis())
                .charisma(dto.getStats().getCha())
                .freemovement(dto.getStats().isFreeMovement())
                .nosurprise(dto.getStats().isCannotBeSuprised())
                .quickstrike(dto.items.getCharms() != null && !dto.items.getCharms().isEmpty() && dto.items.getCharms().stream().anyMatch(characterItemDTO -> characterItemDTO.name.equalsIgnoreCase("Charm of Quick Strike")))
                .vivs(dto.items.getNeck() != null && dto.items.getNeck().getName() != null && dto.items.getNeck().getName().equalsIgnoreCase("Viv’s Amulet of Noble Might"))
                .libramlooting(dto.items.getSlotless() != null && !dto.items.getSlotless().isEmpty() && dto.items.getSlotless().stream().anyMatch(characterItemDTO -> characterItemDTO.name.equalsIgnoreCase("Libram of Looting")))
                .build();
    }
}
