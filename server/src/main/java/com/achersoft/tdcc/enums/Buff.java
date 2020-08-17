package com.achersoft.tdcc.enums;

import com.achersoft.tdcc.vtd.dao.VtdBuffEffect;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public enum Buff {
    BULLS_STRENGTH("Bull's Strength", "+4 Strength", true, CharacterClass.ALL, false, Collections.singletonList(VtdBuffEffect.builder().stat(Stat.STR).modifier(4).build())),
    CATS_GRACE("Cat's Grace", "+4 Dexterity", true, CharacterClass.ALL, false, Collections.singletonList(VtdBuffEffect.builder().stat(Stat.DEX).modifier(4).build())),
    POTION_BRAWN("Potion Brawn", "+2 Strength", true, CharacterClass.ALL, false, Collections.singletonList(VtdBuffEffect.builder().stat(Stat.STR).modifier(2).build())),
    POTION_DEFTNESS("Potion Deftness", "+2 Dexterity", true, CharacterClass.ALL, false, Collections.singletonList(VtdBuffEffect.builder().stat(Stat.DEX).modifier(2).build())),
    RAGE("Rage", "+4 damage with melee weapons", false, CharacterClass.BARBARIAN, false, Collections.singletonList(VtdBuffEffect.builder().stat(Stat.MELEE_DMG).modifier(4).build())),
    GREATER_RAGE("Greater Rage", "+6 damage with melee weapons", false, CharacterClass.BARBARIAN, false, Collections.singletonList(VtdBuffEffect.builder().stat(Stat.MELEE_DMG).modifier(6).build())),
    BOLSTER("Bolster", "+1 to AC", true, CharacterClass.ALL, false, Arrays.asList(VtdBuffEffect.builder().stat(Stat.MELEE_AC).modifier(1).build(), VtdBuffEffect.builder().stat(Stat.RANGE_AC).modifier(1).build())),
    RESISTANCE("Resistance", "+1 to all saves", true, CharacterClass.ALL, false, Arrays.asList(VtdBuffEffect.builder().stat(Stat.FORT).modifier(1).build(), VtdBuffEffect.builder().stat(Stat.REFLEX).modifier(1).build(), VtdBuffEffect.builder().stat(Stat.WILL).modifier(1).build())),
    SONG_OF_HEROISM("Song of Heroism", "+2 to AC and all saves", true, CharacterClass.ALL, false, Arrays.asList(VtdBuffEffect.builder().stat(Stat.MELEE_AC).modifier(2).build(), VtdBuffEffect.builder().stat(Stat.RANGE_AC).modifier(2).build(), VtdBuffEffect.builder().stat(Stat.FORT).modifier(2).build(), VtdBuffEffect.builder().stat(Stat.REFLEX).modifier(2).build(), VtdBuffEffect.builder().stat(Stat.WILL).modifier(2).build())),
    GUIDANCE("Guidance", "+1 to attack slide", true, CharacterClass.ALL, false,  Arrays.asList(VtdBuffEffect.builder().stat(Stat.MELEE_HIT).modifier(1).build(), VtdBuffEffect.builder().stat(Stat.MELEE_POLY_HIT).modifier(1).build(), VtdBuffEffect.builder().stat(Stat.RANGE_HIT).modifier(1).build())),
    BLESS("Bless", "+1 to attack slide & +1 to saves verse Fear. ", true, CharacterClass.ALL, false,  Arrays.asList(VtdBuffEffect.builder().stat(Stat.MELEE_HIT).modifier(1).build(), VtdBuffEffect.builder().stat(Stat.MELEE_POLY_HIT).modifier(1).build(), VtdBuffEffect.builder().stat(Stat.RANGE_HIT).modifier(1).build())),
    PRAYER("Prayer", "+2 to attack slide & Fear immunity. ", true, CharacterClass.ALL, false,  Arrays.asList(VtdBuffEffect.builder().stat(Stat.MELEE_HIT).modifier(2).build(), VtdBuffEffect.builder().stat(Stat.MELEE_POLY_HIT).modifier(2).build(), VtdBuffEffect.builder().stat(Stat.RANGE_HIT).modifier(2).build())),
    ALERTNESS("Alertness", "+10 initiative", false, CharacterClass.ELF_WIZARD, false, Collections.singletonList(VtdBuffEffect.builder().stat(Stat.INITIATIVE).modifier(10).build())),
    IRONSKIN("Ironskin", "+5 DR all", true, CharacterClass.ALL, false, Arrays.asList(VtdBuffEffect.builder().stat(Stat.DR_MELEE).modifier(5).build(), VtdBuffEffect.builder().stat(Stat.DR_RANGE).modifier(5).build(), VtdBuffEffect.builder().stat(Stat.DR_SPELL).modifier(5).build(), VtdBuffEffect.builder().stat(Stat.REFLEX).modifier(2).build(), VtdBuffEffect.builder().stat(Stat.WILL).modifier(2).build())),
    BARKSKIN("Barkskin", "+2 to AC", true, CharacterClass.ALL, false, Arrays.asList(VtdBuffEffect.builder().stat(Stat.MELEE_AC).modifier(2).build(), VtdBuffEffect.builder().stat(Stat.RANGE_AC).modifier(2).build())),
    BARDSONG_1_1("Bardsong +1/+1", "", false, CharacterClass.ALL, true, Arrays.asList(VtdBuffEffect.builder().stat(Stat.MELEE_HIT).modifier(1).build(), VtdBuffEffect.builder().stat(Stat.MELEE_POLY_HIT).modifier(1).build(), VtdBuffEffect.builder().stat(Stat.RANGE_HIT).modifier(1).build(),
            VtdBuffEffect.builder().stat(Stat.MELEE_DMG).modifier(1).build(), VtdBuffEffect.builder().stat(Stat.MELEE_POLY_DMG).modifier(1).build(), VtdBuffEffect.builder().stat(Stat.RANGE_DMG).modifier(1).build())),
    BARDSONG_1_2("Bardsong +1/+2", "", false, CharacterClass.ALL, true, Arrays.asList(VtdBuffEffect.builder().stat(Stat.MELEE_HIT).modifier(1).build(), VtdBuffEffect.builder().stat(Stat.MELEE_POLY_HIT).modifier(1).build(), VtdBuffEffect.builder().stat(Stat.RANGE_HIT).modifier(1).build(),
            VtdBuffEffect.builder().stat(Stat.MELEE_DMG).modifier(2).build(), VtdBuffEffect.builder().stat(Stat.MELEE_POLY_DMG).modifier(2).build(), VtdBuffEffect.builder().stat(Stat.RANGE_DMG).modifier(2).build())),
    BARDSONG_2_1("Bardsong +2/+1", "", false, CharacterClass.ALL, true, Arrays.asList(VtdBuffEffect.builder().stat(Stat.MELEE_HIT).modifier(2).build(), VtdBuffEffect.builder().stat(Stat.MELEE_POLY_HIT).modifier(2).build(), VtdBuffEffect.builder().stat(Stat.RANGE_HIT).modifier(2).build(),
            VtdBuffEffect.builder().stat(Stat.MELEE_DMG).modifier(1).build(), VtdBuffEffect.builder().stat(Stat.MELEE_POLY_DMG).modifier(1).build(), VtdBuffEffect.builder().stat(Stat.RANGE_DMG).modifier(1).build())),
    BARDSONG_1_3("Bardsong +1/+3", "", false, CharacterClass.ALL, true, Arrays.asList(VtdBuffEffect.builder().stat(Stat.MELEE_HIT).modifier(1).build(), VtdBuffEffect.builder().stat(Stat.MELEE_POLY_HIT).modifier(1).build(), VtdBuffEffect.builder().stat(Stat.RANGE_HIT).modifier(1).build(),
            VtdBuffEffect.builder().stat(Stat.MELEE_DMG).modifier(3).build(), VtdBuffEffect.builder().stat(Stat.MELEE_POLY_DMG).modifier(3).build(), VtdBuffEffect.builder().stat(Stat.RANGE_DMG).modifier(3).build())),
    BARDSONG_2_2("Bardsong +2/+2", "", false, CharacterClass.ALL, true, Arrays.asList(VtdBuffEffect.builder().stat(Stat.MELEE_HIT).modifier(2).build(), VtdBuffEffect.builder().stat(Stat.MELEE_POLY_HIT).modifier(2).build(), VtdBuffEffect.builder().stat(Stat.RANGE_HIT).modifier(2).build(),
            VtdBuffEffect.builder().stat(Stat.MELEE_DMG).modifier(2).build(), VtdBuffEffect.builder().stat(Stat.MELEE_POLY_DMG).modifier(2).build(), VtdBuffEffect.builder().stat(Stat.RANGE_DMG).modifier(2).build())),
    BARDSONG_2_3("Bardsong +2/+3", "", false, CharacterClass.ALL, true, Arrays.asList(VtdBuffEffect.builder().stat(Stat.MELEE_HIT).modifier(2).build(), VtdBuffEffect.builder().stat(Stat.MELEE_POLY_HIT).modifier(2).build(), VtdBuffEffect.builder().stat(Stat.RANGE_HIT).modifier(2).build(),
            VtdBuffEffect.builder().stat(Stat.MELEE_DMG).modifier(3).build(), VtdBuffEffect.builder().stat(Stat.MELEE_POLY_DMG).modifier(3).build(), VtdBuffEffect.builder().stat(Stat.RANGE_DMG).modifier(3).build())),
    BARDSONG_2_4("Bardsong +2/+4", "", false, CharacterClass.ALL, true, Arrays.asList(VtdBuffEffect.builder().stat(Stat.MELEE_HIT).modifier(2).build(), VtdBuffEffect.builder().stat(Stat.MELEE_POLY_HIT).modifier(2).build(), VtdBuffEffect.builder().stat(Stat.RANGE_HIT).modifier(2).build(),
            VtdBuffEffect.builder().stat(Stat.MELEE_DMG).modifier(4).build(), VtdBuffEffect.builder().stat(Stat.MELEE_POLY_DMG).modifier(4).build(), VtdBuffEffect.builder().stat(Stat.RANGE_DMG).modifier(4).build())),
    BARDSONG_3_2("Bardsong +3/+2", "", false, CharacterClass.ALL, true, Arrays.asList(VtdBuffEffect.builder().stat(Stat.MELEE_HIT).modifier(3).build(), VtdBuffEffect.builder().stat(Stat.MELEE_POLY_HIT).modifier(3).build(), VtdBuffEffect.builder().stat(Stat.RANGE_HIT).modifier(3).build(),
            VtdBuffEffect.builder().stat(Stat.MELEE_DMG).modifier(2).build(), VtdBuffEffect.builder().stat(Stat.MELEE_POLY_DMG).modifier(2).build(), VtdBuffEffect.builder().stat(Stat.RANGE_DMG).modifier(2).build())),
    BARDSONG_3_3("Bardsong +3/+3", "", false, CharacterClass.ALL, true, Arrays.asList(VtdBuffEffect.builder().stat(Stat.MELEE_HIT).modifier(3).build(), VtdBuffEffect.builder().stat(Stat.MELEE_POLY_HIT).modifier(3).build(), VtdBuffEffect.builder().stat(Stat.RANGE_HIT).modifier(3).build(),
            VtdBuffEffect.builder().stat(Stat.MELEE_DMG).modifier(3).build(), VtdBuffEffect.builder().stat(Stat.MELEE_POLY_DMG).modifier(3).build(), VtdBuffEffect.builder().stat(Stat.RANGE_DMG).modifier(3).build())),
    BARDSONG_4_4("Bardsong +4/+4", "", false, CharacterClass.ALL, true, Arrays.asList(VtdBuffEffect.builder().stat(Stat.MELEE_HIT).modifier(4).build(), VtdBuffEffect.builder().stat(Stat.MELEE_POLY_HIT).modifier(4).build(), VtdBuffEffect.builder().stat(Stat.RANGE_HIT).modifier(4).build(),
            VtdBuffEffect.builder().stat(Stat.MELEE_DMG).modifier(4).build(), VtdBuffEffect.builder().stat(Stat.MELEE_POLY_DMG).modifier(4).build(), VtdBuffEffect.builder().stat(Stat.RANGE_DMG).modifier(4).build()));

    private @Getter final String name;
    private @Getter final String displayText;
    private @Getter final boolean selectable;
    private @Getter final CharacterClass characterClass;
    private @Getter final boolean bardsong;
    private @Getter final List<VtdBuffEffect> effects;

    Buff(String name, String displayText, boolean selectable, CharacterClass characterClass, boolean isBardsong, List<VtdBuffEffect> effects) {
        this.name = name;
        this.displayText = displayText;
        this.selectable = selectable;
        this.characterClass = characterClass;
        this.bardsong = isBardsong;
        this.effects = effects;
    }

    public static List<Buff> getSelectableBuffs() {
        return Arrays.stream(Buff.values()).filter(Buff::isSelectable).collect(Collectors.toList());
    }

    public static List<Buff> getBardsongBuff() {
        return Arrays.stream(Buff.values()).filter(Buff::isBardsong).collect(Collectors.toList());
    }

    public static Buff getBuff(String name) {
        return Arrays.stream(Buff.values()).filter(buff -> buff.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}


