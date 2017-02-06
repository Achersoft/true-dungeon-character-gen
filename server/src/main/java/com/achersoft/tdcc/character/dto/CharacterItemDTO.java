package com.achersoft.tdcc.character.dto;

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
    public String itemId;
    public String characterId;
    public String name;
    public String text;
    public int index;
    public Rarity rarity;
    public SlotStatus slotStatus;
    public String statusText;
}
