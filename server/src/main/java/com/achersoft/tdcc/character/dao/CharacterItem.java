package com.achersoft.tdcc.character.dao;

import com.achersoft.tdcc.enums.Rarity;
import com.achersoft.tdcc.enums.Slot;
import com.achersoft.tdcc.enums.SlotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
@EqualsAndHashCode(exclude={"name", "text", "rarity", "slotStatus", "statusText"})
public class CharacterItem implements Comparable<CharacterItem>{
    private String id;
    private String itemId;
    private String characterId;
    private String name;
    private String text;
    private Slot slot;
    private int index;
    private Rarity rarity;
    private @Builder.Default Rarity maxRarity = Rarity.ALL;
    private SlotStatus slotStatus;
    private String statusText;
    
    @Override
    public int compareTo(CharacterItem ci) {
        if(ci.slot.ordinal() > this.slot.ordinal())
            return -1;
        return 1;
    }
}
