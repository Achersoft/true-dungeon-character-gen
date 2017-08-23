package com.achersoft.tdcc.party.dao;

import com.achersoft.tdcc.enums.Difficulty;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class Party {
    private String id;
    private String name;
    private Difficulty difficulty;
    private int size;
    private String barbarian;
    private String bard;
    private String cleric;
    private String druid;
    private String fighter;
    private String dwarfFighter;
    private String elfWizard;
    private String monk;
    private String paladin;
    private String ranger;
    private String rogue;
    private String wizard;
    private Date createdOn;
}
