package com.achersoft.tdcc.vtd.dao;

import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.enums.SkillLevel;
import com.achersoft.tdcc.enums.SkillType;
import lombok.*;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
@EqualsAndHashCode
public class CharacterSkill {
    private String id;
    private String characterId;
    private CharacterClass characterClass;
    private int characterLevel;
    private String name;
    private String details;
    private SkillLevel skillLevel;
    private SkillType skillType;
    private boolean expendable;
    private boolean used;
    private int minEffect;
    private int maxEffect;
}
