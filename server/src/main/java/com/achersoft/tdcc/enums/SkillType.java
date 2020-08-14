package com.achersoft.tdcc.enums;

public enum SkillType {
    DAMAGE("Damage"),
    HEAL("Heal"),
    BUFF("Buff"),
    EFFECT("Effect");

    private  final String displayText;

    SkillType(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() { return displayText; }
}


