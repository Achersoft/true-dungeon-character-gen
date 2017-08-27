package com.achersoft.tdcc.party.dto;

import com.achersoft.tdcc.enums.Difficulty;
import com.achersoft.tdcc.party.dao.*;

public class PartyDetailsDTO {
    private String id;
    private String name;
    private Difficulty difficulty;
    private int initiative = 0;  
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
    
    public static PartyDetailsDTO fromDAO(PartyDetails createParty) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
