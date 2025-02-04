package com.achersoft.tdcc.character.dto;

import com.achersoft.tdcc.character.dao.*;
import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.enums.Slot;

import java.util.*;

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
    public String username;
    public Date lastModified;
    public CharacterClass characterClass;
    public CharacterStats stats;
    public Set<CharacterItemDTO> heads;
    public Set<CharacterItemDTO> eyes;
    public Set<CharacterItemDTO> ears;
    public Set<CharacterItemDTO> bead;
    public Set<CharacterItemDTO> necks;
    public Set<CharacterItemDTO> torsos;
    public Set<CharacterItemDTO> wrists;
    public Set<CharacterItemDTO> hands;
    public CharacterItemDTO aow;
    public CharacterItemDTO meleeMainhand;
    public CharacterItemDTO meleeOffhand;
    public Set<CharacterItemDTO> instruments;
    public CharacterItemDTO rangedMainhand;
    public CharacterItemDTO rangedOffhand;
    public Set<CharacterItemDTO> backs;
    public Set<CharacterItemDTO> rings;
    public Set<CharacterItemDTO> waist;
    public Set<CharacterItemDTO> shirt;
    public Set<CharacterItemDTO> boots;
    public Set<CharacterItemDTO> shins;
    public Set<CharacterItemDTO> legs;
    public Set<CharacterItemDTO> figurines;
    public Set<CharacterItemDTO> charms;
    public Set<CharacterItemDTO> iounStones;
    public Set<CharacterItemDTO> slotless;
    public Set<CharacterItemDTO> runestones;
    public Set<CharacterItemDTO> safeholds;
    public Set<CharacterItemDTO> hirelings;
    public Set<CharacterItemDTO> underlings;
    public Set<CharacterItemDTO> followers;
    public Set<CharacterItemDTO> sidekicks;
    public Set<CharacterItemDTO> mark;
    public Set<CharacterItemDTO> pets;
    public List<String> alwaysInEffect;
    public List<String> oncePerRound;
    public List<String>  oncePerRoom;
    public List<String> oncePerGame;
    
    public static CharacterDetailsDTO fromDAO(CharacterDetails dao) {
        CharacterDetailsDTO build = CharacterDetailsDTO.builder()
                .id(dao.getId())
                .editable(dao.isEditable())
                .name(dao.getName())
                .username(dao.getUsername())
                .lastModified(dao.getLastModified())
                .characterClass(dao.getCharacterClass())
                .stats(dao.getStats())
                .instruments(new TreeSet<>())
                .heads(new TreeSet<>())
                .eyes(new TreeSet<>())
                .ears(new TreeSet<>())
                .bead(new TreeSet<>())
                .necks(new TreeSet<>())
                .torsos(new TreeSet<>())
                .wrists(new TreeSet<>())
                .hands(new TreeSet<>())
                .backs(new TreeSet<>())
                .boots(new TreeSet<>())
                .shins(new TreeSet<>())
                .legs(new TreeSet<>())
                .shirt(new TreeSet<>())
                .waist(new TreeSet<>())
                .rings(new TreeSet<>())
                .charms(new TreeSet<>())
                .iounStones(new TreeSet<>())
                .slotless(new TreeSet<>())
                .figurines(new TreeSet<>())
                .runestones(new TreeSet<>())
                .safeholds(new TreeSet<>())
                .hirelings(new TreeSet<>())
                .underlings(new TreeSet<>())
                .followers(new TreeSet<>())
                .sidekicks(new TreeSet<>())
                .mark(new TreeSet<>())
                .pets(new TreeSet<>())
                .alwaysInEffect(new ArrayList<>())
                .oncePerRound(new ArrayList<>())
                .oncePerRoom(new ArrayList<>())
                .oncePerGame(new ArrayList<>())
                .bead(new TreeSet<>())
                .build();
      
        dao.getItems().stream().forEach((item) -> {
            if(item.getName() == null)
                item.setName("Empty");
            if(item.getSlot() == Slot.HEAD)
                build.heads.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.EYES)
                build.eyes.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.EAR)
                build.ears.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.NECK)
                build.necks.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.BEAD)
                build.bead.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.AOW)
                build.aow = CharacterItemDTO.fromDAO(item);
            if(item.getSlot() == Slot.TORSO)
                build.torsos.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.WRIST)
                build.wrists.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.HANDS)
                build.hands.add(CharacterItemDTO.fromDAO(item));
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
                build.waist.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.SHIRT)
                build.shirt.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.FEET)
                build.boots.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.SHINS)
                build.shins.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.LEGS)
                build.legs.add(CharacterItemDTO.fromDAO(item));
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
            if(item.getSlot() == Slot.SAFEHOLD)
                build.safeholds.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.HIRELING)
                build.hirelings.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.UNDERLING)
                build.underlings.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.FOLLOWER)
                build.followers.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.SIDEKICK)
                build.sidekicks.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.MARK)
                build.mark.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.PET)
                build.pets.add(CharacterItemDTO.fromDAO(item));
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
