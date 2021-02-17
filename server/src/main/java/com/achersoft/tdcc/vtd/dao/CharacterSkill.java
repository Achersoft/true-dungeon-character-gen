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
    private boolean aoe;
    private int usedNumber;
    private int usableNumber;
    private int minEffect;
    private int maxEffect;
    private boolean oncePerRoom;
    private boolean isCold;
    private boolean isFire;
    private boolean isShock;
    private boolean isSonic;
    private boolean isPoison;
    private boolean isSacred;
    private boolean isDarkrift;
    private boolean isAcid;
    private @Builder.Default int coldBonus = 0;
    private @Builder.Default int fireBonus = 0;
    private @Builder.Default int shockBonus = 0;
    private @Builder.Default int sonicBonus = 0;
    private @Builder.Default int poisonBonus = 0;
    private @Builder.Default int sacredBonus = 0;
    private @Builder.Default int darkriftBonus = 0;
    private @Builder.Default int acidBonus = 0;
}
