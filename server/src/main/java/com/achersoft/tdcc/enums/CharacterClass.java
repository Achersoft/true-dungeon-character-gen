package com.achersoft.tdcc.enums;

import lombok.Getter;

public enum CharacterClass {
    ALL("All", "ALL"),
    BARBARIAN("Barbarian", "barbarian"),
    BARD("Bard", "bard"),
    CLERIC("Cleric", "cleric"),
    DRUID("Druid", "druid"),
    FIGHTER("Fighter", "fighter"),
    DWARF_FIGHTER("Dwarf Fighter", "dwarf"),
    ELF_WIZARD("Elf Wizard", "elf"),
    WIZARD("Wizard", "wizard"),
    MONK("Monk", "monk"),
    PALADIN("Paladin", "paladin"),
    RANGER("Ranger", "ranger"),
    ROGUE("Rogue", "rogue");

    private @Getter final String displayText;
    private @Getter final String rollerText;

    CharacterClass(String displayText, String rollerText) {
        this.displayText = displayText;
        this.rollerText = rollerText;
    }

    public String getDtoValue(boolean isPrestige) {
        if (!isPrestige)
            return this.name();
        else {
            switch (this) {
                case ALL:
                    return ALL.name();
                case BARBARIAN:
                    return "BARBARIAN_BERSERKER";
                case BARD:
                    return "BARD_TROUBADOUR";
                case CLERIC:
                    return "CLERIC_TEMPLAR";
                case DRUID:
                    return "DRUID_SUMMONER";
                case FIGHTER:
                    return "FIGHTER_DUELIST";
                case DWARF_FIGHTER:
                    return "DWARF_FIGHTER_DEFENDER";
                case ELF_WIZARD:
                    return "ELF_WIZARD_ILLUSIONIST";
                case WIZARD:
                    return "WIZARD_SORCERER";
                case MONK:
                    return "MONK_DRUNKEN_MASTER";
                case PALADIN:
                    return "PALADIN_CAVALIER";
                case RANGER:
                    return "RANGER_WARDEN";
                case ROGUE:
                    return "ROGUE_ASSASSIN";
            }
        }
        return "";
    }
}


