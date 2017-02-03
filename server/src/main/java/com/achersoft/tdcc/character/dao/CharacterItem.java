package com.achersoft.tdcc.character.dao;

import com.achersoft.tdcc.enums.Slot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class CharacterItem {
    private String characterId;
    private String itemId;
    private Slot slot;
    private int index;
}
