package com.achersoft.tdcc.enums;

public enum WeaponExplodeEffect {
    NONE("None"),
    INSTANT_KILL("Self"),
    DAMAGE("Party"),
    HEAL("Monster");

    private  final String displayText;

    WeaponExplodeEffect(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() { return displayText; }
}


