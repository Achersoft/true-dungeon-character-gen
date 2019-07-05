package com.achersoft.tdcc.party.dao;

import com.achersoft.tdcc.enums.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class PartyDetails {
    private String id;
    private Boolean editable;
    private String name;
    private Difficulty difficulty;
    private int initiative = 0;  
    private int size;
    private PartyCharacter barbarian;
    private PartyCharacter bard;
    private PartyCharacter cleric;
    private PartyCharacter druid;
    private PartyCharacter fighter;
    private PartyCharacter dwarfFighter;
    private PartyCharacter elfWizard;
    private PartyCharacter monk;
    private PartyCharacter paladin;
    private PartyCharacter ranger;
    private PartyCharacter rogue;
    private PartyCharacter wizard;
}
