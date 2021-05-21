package com.achersoft.tdcc.enums;

import com.achersoft.tdcc.vtd.dao.VtdBuffEffect;
import com.achersoft.tdcc.vtd.dao.VtdDebuff;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public enum Debuff {
    DRAIN_LIFE("Drain Life", "Drains 10% max HP",  Collections.singletonList(VtdBuffEffect.builder().stat(Stat.PERCENT_HEALTH).modifier(-10).build())),
    DRAIN_LIFE_EPIC("Drain Life Epic", "Drains 20% max HP",  Collections.singletonList(VtdBuffEffect.builder().stat(Stat.PERCENT_HEALTH).modifier(-20).build()));

    private @Getter final String name;
    private @Getter final String displayText;
    private @Getter final List<VtdBuffEffect> effects;

    Debuff(String name, String displayText, List<VtdBuffEffect> effects) {
        this.name = name;
        this.displayText = displayText;
        this.effects = effects;
    }

    public static Debuff getDebuff(String name) {
        return Arrays.stream(Debuff.values()).filter(buff -> buff.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static List<VtdDebuff> getAll(String characterId) {
        return Arrays.stream(Debuff.values()).map(debuff -> VtdDebuff.builder().characterId(characterId).debuff(debuff).level(0).build()).collect(Collectors.toList());
    }
}


