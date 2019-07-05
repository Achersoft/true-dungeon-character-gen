package com.achersoft.tdcc.character.dto;

import com.achersoft.tdcc.character.dao.CharacterItem;
import com.achersoft.tdcc.enums.Rarity;
import com.achersoft.tdcc.enums.Slot;
import com.achersoft.tdcc.enums.SlotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class CharacterItemDTO {
    public String id;
    public String itemId;
    public String imgName;
    public String characterId;
    public String name;
    public String text;
    public Slot slot;
    public int index;
    public Rarity rarity;
    public SlotStatus slotStatus;
    public String statusText;
    
    public static CharacterItemDTO fromDAO(CharacterItem dao) {
        return CharacterItemDTO.builder()
                .id(dao.getId())
                .itemId(dao.getItemId())
                .imgName(dao.getName().replaceAll("’","").replaceAll(",", "").replaceAll(":", "").replaceAll(" ","-"))
                .characterId(dao.getCharacterId())
                .name(dao.getName())
                .text(dao.getText())
                .slot(dao.getSlot())
                .index(dao.getIndex())
                .rarity(dao.getRarity())
                .slotStatus(dao.getSlotStatus())
                .statusText(dao.getStatusText())
                .build();
    }
}
