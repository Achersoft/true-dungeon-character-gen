package com.achersoft.tdcc.vtd.dao;

import com.achersoft.tdcc.character.dao.CharacterNote;
import com.achersoft.tdcc.character.dao.CharacterStats;
import com.achersoft.tdcc.enums.CharacterClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class VtdDetails {
    private String characterId;
    private Date expires;
    private String name;
    private CharacterClass characterClass;
    private CharacterStats stats;
    private List<CharacterNote> notes;
    private List<CharacterSkill> characterSkills;
    private List<Integer> meleeDmgRange;
    private List<Integer> meleeOffhandDmgRange;
    private List<Integer> meleePolyDmgRange;
    private List<Integer> rangeDmgRange;
    private List<Integer> rangeOffhandDmgRange;
    private Integer meleeCritMin;
    private Integer meleePolyCritMin;
    private Integer rangeCritMin;
    private Integer currentHealth;
}
