package com.achersoft.tdcc.character.dao;

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
public class CharacterItem {
    private String id;
    private String itemId;
    private String characterId;
    private String name;
    private String text;
    private Slot slot;
    private int index;
    private Rarity rarity;
    private SlotStatus slotStatus;
    private String statusText;
}
