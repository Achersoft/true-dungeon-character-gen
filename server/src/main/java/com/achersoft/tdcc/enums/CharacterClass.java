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


