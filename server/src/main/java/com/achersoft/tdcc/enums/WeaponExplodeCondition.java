package com.achersoft.tdcc.enums;

public enum WeaponExplodeCondition {
    NONE("None", CharacterClass.ALL, null),
    MISFIRE("Misfire", CharacterClass.ALL, DamageModEffect.MISFIRE),
    NO_DAMAGE_MOD("", CharacterClass.ALL, DamageModEffect.NO_DAMAGE_MOD),
    TRIPPLE_CRIT("Tripple Crit", CharacterClass.ALL, DamageModEffect.TRIPPLE_CRIT),
    TRIPPLE_CRIT_ON_20("", CharacterClass.ALL, DamageModEffect.TRIPPLE_CRIT_ON_20),
    TRIPPLE_CRIT_ON_FIRST_20("Natural 20", CharacterClass.ALL, DamageModEffect.TRIPPLE_CRIT_ON_FIRST_20),
    TRIPPLE_CRIT_ON_FIRST_20_BARB("Natural 20", CharacterClass.BARBARIAN, DamageModEffect.TRIPPLE_CRIT_ON_FIRST_20),
    TWO_DAMAGE_ON_SNEAK("Natural 20", CharacterClass.ROGUE, DamageModEffect.PLUS_2_SNEAK_DAMAGE),
    NATURAL_20("Natural 20", CharacterClass.ALL, null),
    CRIT("Crit", CharacterClass.ALL, DamageModEffect.ALWAYS_CRIT);

    private final String displayText;
    private final CharacterClass characterClass;
    private final DamageModEffect damageModEffect;

    WeaponExplodeCondition(String displayText, CharacterClass characterClass, DamageModEffect damageModEffect) {
        this.displayText = displayText;
        this.characterClass = characterClass;
        this.damageModEffect = damageModEffect;
    }

    public String getDisplayText() { return displayText; }

    public DamageModEffect getDamageModEffect(CharacterClass characterClass) {
        if (this.characterClass == CharacterClass.ALL || this.characterClass == characterClass)
            return damageModEffect;
        return null;
    }
}
