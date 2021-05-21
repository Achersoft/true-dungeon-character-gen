package com.achersoft.tdcc.vtd.dto;

import com.achersoft.tdcc.character.dao.CharacterDetails;
import com.achersoft.tdcc.character.dao.CharacterItem;
import com.achersoft.tdcc.character.dao.CharacterStats;
import com.achersoft.tdcc.character.dto.CharacterItemDTO;
import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.enums.Slot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class VtdItemsDTO {
    public @Builder.Default boolean editable = false;
    public List<CharacterItemDTO> heads;
    public List<CharacterItemDTO> eyes;
    public CharacterItemDTO leftEar;
    public CharacterItemDTO rightEar;
    public List<CharacterItemDTO> bead;
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
    public List<CharacterItemDTO> waist;
    public List<CharacterItemDTO> shirt;
    public List<CharacterItemDTO> boots;
    public CharacterItemDTO shins;
    public List<CharacterItemDTO> legs;
    public List<CharacterItemDTO> figurines;
    public List<CharacterItemDTO> charms;
    public List<CharacterItemDTO> iounStones;
    public List<CharacterItemDTO> slotless;
    public List<CharacterItemDTO> runestones;
    public List<String> alwaysInEffect;
    public List<String> oncePerRound;
    public List<String>  oncePerRoom;
    public List<String> oncePerGame;
    
    public static VtdItemsDTO fromDAO(List<CharacterItem> items) {
        VtdItemsDTO build = VtdItemsDTO.builder()
                .editable(false)
                .boots(new ArrayList<>())
                .shirt(new ArrayList<>())
                .waist(new ArrayList<>())
                .legs(new ArrayList<>())
                .instruments(new ArrayList<>())
                .heads(new ArrayList<>())
                .eyes(new ArrayList<>())
                .backs(new ArrayList<>())
                .rings(new ArrayList<>())
                .charms(new ArrayList<>())
                .iounStones(new ArrayList<>())
                .slotless(new ArrayList<>())
                .figurines(new ArrayList<>())
                .runestones(new ArrayList<>())
                .alwaysInEffect(new ArrayList<>())
                .oncePerRound(new ArrayList<>())
                .oncePerRoom(new ArrayList<>())
                .oncePerGame(new ArrayList<>())
                .bead(new ArrayList<>())
                .build();

        items.forEach((item) -> {
            if(item.getName() == null)
                item.setName("Empty");
            if(item.getSlot() == Slot.HEAD)
                build.heads.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.EYES)
                build.eyes.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.EAR) {
               if(item.getIndex() == 0)  
                   build.leftEar = CharacterItemDTO.fromDAO(item);
               if(item.getIndex() == 1)  
                   build.rightEar = CharacterItemDTO.fromDAO(item);
            }
            if(item.getSlot() == Slot.NECK)
                build.neck = CharacterItemDTO.fromDAO(item);
            if(item.getSlot() == Slot.BEAD)
                build.bead.add(CharacterItemDTO.fromDAO(item));
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
                build.waist.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.SHIRT)
                build.shirt.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.FEET)
                build.boots.add(CharacterItemDTO.fromDAO(item));
            if(item.getSlot() == Slot.SHINS)
                build.shins = CharacterItemDTO.fromDAO(item);
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
        });
        
        return build;
    }
}
