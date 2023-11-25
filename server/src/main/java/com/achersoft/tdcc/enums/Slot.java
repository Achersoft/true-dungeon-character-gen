package com.achersoft.tdcc.enums;

public enum Slot {
    MAINHAND("Melee Weapon", 1),
    OFFHAND("Melee Offhand", 1),
    RANGE_MAINHAND("Range Weapon", 1),
    RANGE_OFFHAND("Range Offhand",1),
    INSTRUMENT("Instrument", 0),
    HEAD("Head",1),
    BEAD("Bead",3),
    EYES("Eyes",1),
    EAR("Ear", 2),
    NECK("Neck", 1),
    AOW("AoW", 1),
    TORSO("Torso", 1),
    SHIRT("Shirt", 1),
    BACK("Back", 1),
    WRIST("Wrist",1),
    HANDS("Hands", 1),
    FINGER("Finger", 2),
    WAIST("Waist", 1),
    LEGS("Legs", 1),
    SHINS("Shins", 1),
    FEET("Feet", 1),
    CHARM("Charm", 3),
    IOUNSTONE("Ioun Stone", 5),
    FIGURINE("Figurine", 1),
    SLOTLESS("Slotless", 1),
    RUNESTONE("Runestone", 1),
    POLYMORPH("Polymorph", 1),
    SAFEHOLD("Safehold", 1),
    HIRELING("Hireling", 0),
    UNDERLING("Underling", 0),
    FOLLOWER("Follower", 0);
    
    public final String text;
    public final int defaultSize;

    Slot(String text, int defaultSize) {
        this.text = text;
        this.defaultSize = defaultSize;
    }
    
    public String text() {
        return text;
    }
    public int defaultSize() { return this.defaultSize; }
}