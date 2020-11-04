package com.achersoft.tdcc.enums;

public enum SkillStatEffect {
    NONE("None"),
    BARDSONG("Bardsong"),
    HIT("Hit"),
    MELEE_HIT("Melee Hit"),
    RANGE_HIT("Range Hit"),
    DAMAGE("Damage"),
    MELEE_DAMAGE("Melee Damage"),
    RANGE_DAMAGE("Range Damage"),
    SET_AC("AC"),
    AC("AC"),
    SAVE("Save"),
    INIT("init"),
    SPELL("Spell"),
    SPELL_MULT("Spell"),
    SAVE_AC("Save AC"),
    STR("STR"),
    DEX("DEX");

    private  final String displayText;

    SkillStatEffect(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() { return displayText; }
}


