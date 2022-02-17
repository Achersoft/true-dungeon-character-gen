package com.achersoft.tdcc.vtd.dao;

import com.achersoft.tdcc.enums.*;
import lombok.*;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
@EqualsAndHashCode
public class QueuedSkill {
    private String id;
    private String skillId;
    private String skillName;
    private boolean selfTarget;
    private int selfHeal;
    private boolean madEvoker;
    private int lohNumber;
    private InGameEffect inGameEffect;
    private boolean markUse;
    private boolean ignoreUse;
    private int damage;
}
