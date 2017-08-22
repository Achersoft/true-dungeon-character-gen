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
    public boolean editable;
    public String name;
    public CharacterClass characterClass;
    public CharacterStats stats;
    public CharacterItemDTO head;
    public CharacterItemDTO eyes;
    public CharacterItemDTO leftEar;
    public CharacterItemDTO rightEar;
    public CharacterItemDTO neck;
    public CharacterItemDTO aow;
    public CharacterItemDTO torso;
    public CharacterItemDTO wrists;
    public CharacterItemDTO hands;
    public CharacterItemDTO meleeMainhand;
    public CharacterItemDTO meleeOffhand;
    public List<CharacterItemDTO> instruments;
    public CharacterItemDTO rangedMainhand;
    public CharacterItemDTO rangedOffhand;
    public List<CharacterItemDTO> backs;
    public List<CharacterItemDTO> rings;
    public CharacterItemDTO waist;
    public CharacterItemDTO shirt;
    public CharacterItemDTO boots;
    public CharacterItemDTO shins;
    public CharacterItemDTO legs;
    public List<CharacterItemDTO> figurines;
    public List<CharacterItemDTO> charms;
    public List<CharacterItemDTO> iounStones;
    public List<CharacterItemDTO> slotless;
    public List<CharacterItemDTO> runestones;
    public List<String> alwaysInEffect;
    public List<String> oncePerRound;
    public List<String>  oncePerRoom;
    public List<String> oncePerGame;
    
    public static CharacterDetailsDTO fromDAO(CharacterDetails dao) {
        CharacterDetailsDTO build = CharacterDetailsDTO.builder()
                .id(dao.getId())
                .editable(dao.isEditable())
                .name(dao.getName())
                .characterClass(dao.getCharacterClass())
                .stats(dao.getStats())
                .instruments(new ArrayList())
                .backs(new ArrayList())
                .rings(new ArrayList())
                .charms(new ArrayList())
                .iounStones(new ArrayList())
                .slotless(new ArrayList())
                .figurines(new ArrayList())
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
                build.head = CharacterItemDTO.fromDAO(item);
            if(item.getSlot() == Slot.EYES)
                build.eyes = CharacterItemDTO.fromDAO(item);
            if(item.getSlot() == Slot.EAR) {
               if(item.getIndex() == 0)  
                   build.leftEar = CharacterItemDTO.fromDAO(item);
               if(item.getIndex() == 1)  
                   build.rightEar = CharacterItemDTO.fromDAO(item);
            }
            if(item.getSlot() == Slot.NECK)
                build.neck = CharacterItemDTO.fromDAO(item);
            if(item.getSlot() == Slot.AOW)
                build.aow = CharacterItemDTO.fromDAO(item);
            if(item.getSlot() == Slot.TORSO)
                build.torso = CharacterItemDTO.fromDAO(item);
            if(item.getSlot() == Slot.WRIST)
                build.wrists = CharacterItemDTO.fromDAO(item);
            if(item.getSlot() == Slot.HANDS)
                build.hands = CharacterItemDTO.fromDAO(item);
            if(item.getSlot() == Slot.MAINHAND)
                build.meleeMainhand = CharacterItemDTO.fromDAO(item);
            if(item.getSlot() == Slot.OFFHAND)
                build.meleeOffhand = CharacterItemDTO.fromDAO(item);
            if(item.getSlot() == Slot.INSTRUMENT)
                build.instruments.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.RANGE_MAINHAND)
                build.rangedMainhand = CharacterItemDTO.fromDAO(item);
            if(item.getSlot() == Slot.RANGE_OFFHAND)
                build.rangedOffhand = CharacterItemDTO.fromDAO(item);
            if(item.getSlot() == Slot.BACK)
                build.backs.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.FINGER)
                build.rings.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.WAIST)
                build.waist = CharacterItemDTO.fromDAO(item);
            if(item.getSlot() == Slot.SHIRT)
                build.shirt = CharacterItemDTO.fromDAO(item);
            if(item.getSlot() == Slot.FEET)
                build.boots = CharacterItemDTO.fromDAO(item);
            if(item.getSlot() == Slot.SHINS)
                build.shins = CharacterItemDTO.fromDAO(item);
            if(item.getSlot() == Slot.LEGS)
                build.legs = CharacterItemDTO.fromDAO(item);
            if(item.getSlot() == Slot.FIGURINE)
                build.figurines.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.CHARM)
                build.charms.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.IOUNSTONE)
                build.iounStones.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.SLOTLESS)
                build.slotless.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.RUNESTONE)
                build.runestones.add(CharacterItemDTO.fromDAO(item));
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
