package com.achersoft.tdcc.character.dao;

import com.achersoft.tdcc.enums.CharacterClass;
import java.util.Date;
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
    private String userId;
    private String name;
    private CharacterClass characterClass;
    private Date createdOn;
    private CharacterStats stats;
    private List<CharacterItem> items;
    private List<CharacterNote> notes;
}
