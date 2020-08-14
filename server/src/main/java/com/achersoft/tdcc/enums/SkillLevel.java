package com.achersoft.tdcc.enums;

import lombok.Getter;

public enum SkillLevel {
    NA("N/A"),
    ZERO("0"),
    ONE("1"),
    TWO("2"),
    THREE("3"),
    FOUR("4");

    private @Getter final String displayText;

    SkillLevel(String displayText) {
        this.displayText = displayText;
    }
}


