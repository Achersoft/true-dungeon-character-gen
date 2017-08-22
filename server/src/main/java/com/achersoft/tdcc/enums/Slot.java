package com.achersoft.tdcc.enums;

public enum Slot {
    MAINHAND("Melee Weapon"),
    OFFHAND("Melee Offhand"),
    RANGE_MAINHAND("Range Weapon"),
    RANGE_OFFHAND("Range Offhand"),
    INSTRUMENT("Instrument"),
    HEAD("Head"),
    EYES("Eyes"),
    EAR("Ear"),
    NECK("Neck"),
    AOW("AoW"),
    TORSO("Torso"),
    SHIRT("Shirt"),
    BACK("Back"),
    WRIST("Wrist"),
    HANDS("Hands"),
    FINGER("Finger"),
    WAIST("Waist"),
    LEGS("Legs"),
    SHINS("Shins"),
    FEET("Feet"),
    CHARM("Charm"),
    IOUNSTONE("Ioun Stone"),
    FIGURINE("Figurine"),
    SLOTLESS("Slotless"),
    RUNESTONE("Runestone");
    
    public final String text; 
    Slot(String text) {
        this.text = text;
    }
    
    public String text() {
        return text;
    }
}