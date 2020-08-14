package com.achersoft.tdcc.enums;

public enum SkillType {
    DAMAGE("Damage"),
    DAMAGE_RANGE_AC_15("Damage on AC 15"),
    HEAL("Heal"),
    BUFF("Buff"),
    EFFECT("Effect");

    private  final String displayText;

    SkillType(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() { return displayText; }
}


