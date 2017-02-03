package com.achersoft.tdcc.character.dao;

import com.achersoft.tdcc.enums.CharacterClass;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class CharacterDetails {
    private String id;
    private String name;
    private CharacterClass characterClass;
    private CharacterStats stats;
    private List<CharacterItem> items;
}
