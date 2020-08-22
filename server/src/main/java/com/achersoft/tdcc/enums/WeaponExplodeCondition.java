package com.achersoft.tdcc.enums;

public enum WeaponExplodeCondition {
    NONE("None"),
    TRIPPLE_FIRST_20("Natural 20"),
    NATURAL_20("Natural 20"),
    CRIT("Crit");

    private  final String displayText;

    WeaponExplodeCondition(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() { return displayText; }
}


