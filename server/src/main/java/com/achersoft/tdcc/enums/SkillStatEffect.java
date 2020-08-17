package com.achersoft.tdcc.enums;

public enum SkillStatEffect {
    NONE("None"),
    HIT("Damage"),
    RANGE_HIT("Range Hit"),
    DAMAGE("Damage"),
    MELEE_DAMAGE("Melee Damage"),
    RANGE_DAMAGE("Range Damage"),
    AC("AC"),
    SAVE("Save"),
    SAVE_AC("Save AC"),
    STR("STR"),
    DEX("DEX");

    private  final String displayText;

    SkillStatEffect(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() { return displayText; }
}


