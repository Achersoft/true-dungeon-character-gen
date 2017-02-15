package com.achersoft.tdcc.character.dto;

import com.achersoft.tdcc.character.dao.*;
import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.enums.Slot;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class CharacterDetailsDTO {
    public String id;
    public String name;
    public CharacterClass characterClass;
    public CharacterStats stats;
    public CharacterItem head;
    public CharacterItem eyes;
    public CharacterItem leftEar;
    public CharacterItem rightEar;
    public CharacterItem neck;
    public CharacterItem torso;
    public CharacterItem wrists;
    public CharacterItem hands;
    public CharacterItem meleeMainhand;
    public CharacterItem meleeOffhand;
    public List<CharacterItem> instruments;
    public CharacterItem rangedMainhand;
    public CharacterItem rangedOffhand;
    public List<CharacterItem> backs;
    public List<CharacterItem> rings;
    public CharacterItem waist;
    public CharacterItem shirt;
    public CharacterItem boots;
    public CharacterItem legs;
    public CharacterItem figurine;
    public List<CharacterItem> charms;
    public List<CharacterItem> iounStones;
    public List<CharacterItem> slotless;
    public List<CharacterItem> runestones;
    public List<String> alwaysInEffect;
    public List<String> oncePerRound;
    public List<String>  oncePerRoom;
    public List<String> oncePerGame;
    
    public static CharacterDetailsDTO fromDAO(CharacterDetails dao) {
        CharacterDetailsDTO build = CharacterDetailsDTO.builder()
                .id(dao.getId())
                .name(dao.getName())
                .characterClass(dao.getCharacterClass())
                .stats(dao.getStats())
                .instruments(new ArrayList())
                .backs(new ArrayList())
                .rings(new ArrayList())
                .charms(new ArrayList())
                .iounStones(new ArrayList())
                .slotless(new ArrayList())
                .runestones(new ArrayList())
                .alwaysInEffect(new ArrayList())
                .oncePerRound(new ArrayList())
                .oncePerRoom(new ArrayList())
                .oncePerGame(new ArrayList())
                .build();
      
        dao.getItems().stream().forEach((item) -> {
            if(item.getName() == null)
                item.setName("Empty");
            if(item.getSlot() == Slot.HEAD)
                build.head = item;
            if(item.getSlot() == Slot.EYES)
                build.eyes = item;
            if(item.getSlot() == Slot.EAR) {
               if(item.getIndex() == 0)  
                   build.leftEar = item;
               if(item.getIndex() == 1)  
                   build.rightEar = item;
            }
            if(item.getSlot() == Slot.NECK)
                build.neck = item;
            if(item.getSlot() == Slot.TORSO)
                build.torso = item;
            if(item.getSlot() == Slot.WRIST)
                build.wrists = item;
            if(item.getSlot() == Slot.HANDS)
                build.hands = item;
            if(item.getSlot() == Slot.MAINHAND)
                build.meleeMainhand = item;
            if(item.getSlot() == Slot.OFFHAND)
                build.meleeOffhand = item;
            if(item.getSlot() == Slot.INSTRUMENT)
                build.instruments.add(item);
            if(item.getSlot() == Slot.RANGE_MAINHAND)
                build.rangedMainhand = item;
            if(item.getSlot() == Slot.RANGE_OFFHAND)
                build.rangedOffhand = item;
            if(item.getSlot() == Slot.BACK)
                build.backs.add(item);
            if(item.getSlot() == Slot.FINGER)
                build.rings.add(item);
            if(item.getSlot() == Slot.WAIST)
                build.waist = item;
            if(item.getSlot() == Slot.SHIRT)
                build.shirt = item;
            if(item.getSlot() == Slot.FEET)
                build.boots = item;
            if(item.getSlot() == Slot.LEGS)
                build.legs = item;
            if(item.getSlot() == Slot.FIGURINE)
                build.figurine = item;
            if(item.getSlot() == Slot.CHARM)
                build.charms.add(item);
            if(item.getSlot() == Slot.IOUNSTONE)
                build.iounStones.add(item);
            if(item.getSlot() == Slot.SLOTLESS)
                build.slotless.add(item);
            if(item.getSlot() == Slot.RUNESTONE)
                build.runestones.add(item);
        });
        
        if(dao.getNotes() != null) {
            dao.getNotes().stream().forEach((note) -> {
                if(note.isAlwaysInEffect())
                    build.alwaysInEffect.add(note.getNote());
                if(note.isOncePerRound())
                    build.oncePerRound.add(note.getNote());
                if(note.isOncePerRoom())
                    build.oncePerRoom.add(note.getNote());
                if(note.isOncePerGame())
                    build.oncePerGame.add(note.getNote());
            });
        }
        
        return build;
    }
}
