package com.achersoft.tdcc.vtd.dao;

import com.achersoft.tdcc.enums.*;
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
    private SkillTarget skillTarget;
    private SkillStatEffect skillStatEffect;
    private int usedNumber;
    private int usableNumber;
    private int minEffect;
    private int maxEffect;
}
