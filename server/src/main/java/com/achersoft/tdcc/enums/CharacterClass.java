package com.achersoft.tdcc.enums;

import lombok.Getter;

public enum CharacterClass {
    ALL("All"),
    BARBARIAN("Barbarian"),
    BARD("Bard"),
    CLERIC("Cleric"),
    DRUID("Druid"),
    FIGHTER("Fighter"),
    DWARF_FIGHTER("Dwarf Fighter"),
    ELF_WIZARD("Elf Wizard"),
    WIZARD("Wizard"),
    MONK("Monk"),
    PALADIN("Paladin"),
    RANGER("Ranger"),
    ROGUE("Rogue");

    private @Getter final String displayText;

    CharacterClass(String displayText) {
        this.displayText = displayText;
    }
}


