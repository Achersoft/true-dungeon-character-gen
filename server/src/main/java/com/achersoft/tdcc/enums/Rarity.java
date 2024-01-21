package com.achersoft.tdcc.enums;

public enum Rarity {
    ALL(""),
    COMMON("black"),
    UNCOMMON("#41A317"),
    RARE("crimson"),
    ULTRARARE("purple"),
    ENHANCED("royalblue"),
    EXALTED("royalblue"),
    RELIC("darkblue"),
    RELIC_PLUS("darkblue"),
    LEGENDARY("#ff8800"),
    ELDRITCH("#165417"),
    PREMIUM("#3E7C3F"),
    ARTIFACT("blueviolet"),
    PLAYER_REWARD("#c4c4c4"),
    SAFEHOLD("#eaeaea");
    
    public final String htmlColor; 
    Rarity(String htmlColor) {
        this.htmlColor = htmlColor;
    }
    
    public String htmlColor() {
        return htmlColor;
    }
    
    public boolean isHigherThanUltraRare() {
        return this.ordinal() > ULTRARARE.ordinal();
    }

    public boolean isHigherThanExalted() {
        return this.ordinal() > EXALTED.ordinal();
    }
}


