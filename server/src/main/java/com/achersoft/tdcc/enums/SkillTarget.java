package com.achersoft.tdcc.enums;

public enum SkillTarget {
    NONE("None"),
    SELF("Self"),
    PARTY("Party"),
    MONSTER("Monster"),
    ANY("ANY");

    private  final String displayText;

    SkillTarget(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() { return displayText; }
}


