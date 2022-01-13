package com.achersoft.tdcc.enums;

import lombok.Getter;

public enum ConditionalUse {
    NONE(false),
    DEXTERITY_18(false),
    DEXTERITY_20(false),
    INTELLECT_20(false),
    WISDOM_20(false),
    WEAPON_2H(false),
    WEAPON_1H(false),
    WEAPON_RANGED(false),
    WEAPON_RANGED_2H(false),
    BOW(false),
    CROSSBOW(false),
    MAY_NOT_USE_SHIELDS(false),
    STRENGTH_24(false),
    NO_OTHER_TREASURE(false),
    NO_OTHER_HAVOC(false),
    NOT_WITH_COA(false),
    NOT_WITH_COS_COA(false),
    NOT_WITH_PRO_SCROLL(false),
    NOT_WITH_OTHER_WONDER(false),
    IRON_WEAPON(false),
    SLING(false),
    THRALL_WEAPON(false),
    NOT_WITH_ROSP(false),
    NOT_WITH_SOC(false),
    NOT_WITH_ROSS(false),
    ONE_OTHER_UR_TREASURE(false), 
    DIRK_WEAPON(false),
    COMMON_WEAPON(false),
    COMMON_WEAPON_MELEE(false),
    COMMON_WEAPON_RANGE(false),
    UNCOMMON_WEAPON(false),
    UNCOMMON_WEAPON_MELEE(false),
    UNCOMMON_WEAPON_RANGE(false),
    RARE_WEAPON(false),
    RARE_WEAPON_MELEE(false),
    RARE_WEAPON_RANGE(false),
    UNCOMMON_OR_BELOW_WEAPON(false),
    UNCOMMON_OR_BELOW_WEAPON_MELEE(false),
    UNCOMMON_OR_BELOW_WEAPON_RANGE(false),
    NO_OTHER_IOUN_STONE(false),
    MISSILE_ATTACK(false),
    NOT_RARE_PLUS_TORSO(false),
    NOT_UR_PLUS_RING(false),
    NO_OTHER_SIXTH_LEVEL_REWARD(false),
    GOBLIN_WEAPON(false),
    DWARF_WEAPON(false),
    PLUS_1_AC_GOBLIN_WEAPON(false),
    LESS_THAN_10_HIT_MELEE(true);

    private @Getter final boolean post;

    ConditionalUse(boolean isPost) {
        this.post = isPost;
    }
}