package com.achersoft.tdcc.character.create;

import com.achersoft.exception.InvalidDataException;
import com.achersoft.security.providers.UserPrincipalProvider;
import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.character.dao.CharacterDetails;
import com.achersoft.tdcc.character.dao.CharacterItem;
import com.achersoft.tdcc.character.persistence.CharacterMapper;
import com.achersoft.tdcc.enums.Slot;
import com.achersoft.tdcc.enums.SlotStatus;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import org.springframework.transaction.annotation.Transactional;

@Transactional(rollbackFor = Exception.class)
public class CharacterCreatorServiceImpl implements CharacterCreatorService {
    
    private @Inject CharacterMapper mapper;
    private @Inject UserPrincipalProvider userPrincipalProvider;
    
    @Override
    public CharacterDetails createCharacter(CharacterClass characterClass, String name) {
        String userId = userPrincipalProvider.getUserPrincipal().getSub();
        
        if(userId == null || userId.isEmpty())
            throw new InvalidDataException("User is not valid."); 
        
        if(characterClass == CharacterClass.BARBARIAN)
            return createBarbarian(userId, name);
        if(characterClass == CharacterClass.BARD)
            return createBard(userId, name);
        if(characterClass == CharacterClass.CLERIC)
            return createCleric(userId, name);
        if(characterClass == CharacterClass.DRUID)
            return createDruid(userId, name);
        if(characterClass == CharacterClass.DWARF_FIGHTER)
            return createDwarfFighter(userId, name);
        if(characterClass == CharacterClass.ELF_WIZARD)
            return createElfWizard(userId, name);
        if(characterClass == CharacterClass.FIGHTER)
            return createFighter(userId, name);
        if(characterClass == CharacterClass.MONK)
            return createMonk(userId, name);
        if(characterClass == CharacterClass.PALADIN)
            return createPaladin(userId, name);
        if(characterClass == CharacterClass.RANGER)
            return createRanger(userId, name);
        if(characterClass == CharacterClass.ROGUE)
            return createRogue(userId, name);
        if(characterClass == CharacterClass.WIZARD)
            return createWizard(userId, name);

        throw new InvalidDataException("Character class requested is not valid."); 
    }
    
    private CharacterDetails createBarbarian(String userId, String name) {
        CharacterDetails characterDetails = CharacterDetails.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .name(name)
                .characterClass(CharacterClass.BARBARIAN)
                .createdOn(new Date())
                .build();

        mapper.addCharacter(characterDetails);
        
        characterDetails.setStats(mapper.getStartingStats(CharacterClass.BARBARIAN, 4));
        characterDetails.getStats().setCharacterId(characterDetails.getId());;
        mapper.addCharacterStats(characterDetails.getStats());
        
        characterDetails.setItems(createDefaultItems(characterDetails.getId()));
        mapper.addCharacterItems(characterDetails.getItems());

        return characterDetails;
    }
    
    private CharacterDetails createBard(String userId, String name) {
        CharacterDetails characterDetails = CharacterDetails.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .name(name)
                .characterClass(CharacterClass.BARD)
                .createdOn(new Date())
                .build();

        mapper.addCharacter(characterDetails);
        
        characterDetails.setStats(mapper.getStartingStats(CharacterClass.BARD, 4));
        characterDetails.getStats().setCharacterId(characterDetails.getId());;
        mapper.addCharacterStats(characterDetails.getStats());
        
        characterDetails.setItems(createDefaultItems(characterDetails.getId()));
        characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.INSTRUMENT).index(0).slotStatus(SlotStatus.OK).build());
        mapper.addCharacterItems(characterDetails.getItems());

        return characterDetails;
    }
    
    private CharacterDetails createCleric(String userId, String name) {
        CharacterDetails characterDetails = CharacterDetails.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .name(name)
                .characterClass(CharacterClass.CLERIC)
                .createdOn(new Date())
                .build();

        mapper.addCharacter(characterDetails);
        
        characterDetails.setStats(mapper.getStartingStats(CharacterClass.CLERIC, 4));
        characterDetails.getStats().setCharacterId(characterDetails.getId());;
        mapper.addCharacterStats(characterDetails.getStats());
        
        characterDetails.setItems(createDefaultItems(characterDetails.getId()));
        mapper.addCharacterItems(characterDetails.getItems());

        return characterDetails;
    }
    
    private CharacterDetails createDruid(String userId, String name) {
        CharacterDetails characterDetails = CharacterDetails.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .name(name)
                .characterClass(CharacterClass.DRUID)
                .createdOn(new Date())
                .build();

        mapper.addCharacter(characterDetails);
        
        characterDetails.setStats(mapper.getStartingStats(CharacterClass.DRUID, 4));
        characterDetails.getStats().setCharacterId(characterDetails.getId());;
        mapper.addCharacterStats(characterDetails.getStats());
        
        characterDetails.setItems(createDefaultItems(characterDetails.getId()));
        mapper.addCharacterItems(characterDetails.getItems());

        return characterDetails;
    }
    
    private CharacterDetails createDwarfFighter(String userId, String name) {
        CharacterDetails characterDetails = CharacterDetails.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .name(name)
                .characterClass(CharacterClass.DWARF_FIGHTER)
                .createdOn(new Date())
                .build();

        mapper.addCharacter(characterDetails);
        
        characterDetails.setStats(mapper.getStartingStats(CharacterClass.DWARF_FIGHTER, 4));
        characterDetails.getStats().setCharacterId(characterDetails.getId());;
        mapper.addCharacterStats(characterDetails.getStats());
        
        characterDetails.setItems(createDefaultItems(characterDetails.getId()));
        mapper.addCharacterItems(characterDetails.getItems());

        return characterDetails;
    }
    
    private CharacterDetails createElfWizard(String userId, String name) {
        CharacterDetails characterDetails = CharacterDetails.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .name(name)
                .characterClass(CharacterClass.ELF_WIZARD)
                .createdOn(new Date())
                .build();

        mapper.addCharacter(characterDetails);
        
        characterDetails.setStats(mapper.getStartingStats(CharacterClass.ELF_WIZARD, 4));
        characterDetails.getStats().setCharacterId(characterDetails.getId());;
        mapper.addCharacterStats(characterDetails.getStats());
        
        characterDetails.setItems(createDefaultItems(characterDetails.getId()));
        mapper.addCharacterItems(characterDetails.getItems());

        return characterDetails;
    }
    
    private CharacterDetails createFighter(String userId, String name) {
        CharacterDetails characterDetails = CharacterDetails.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .name(name)
                .characterClass(CharacterClass.FIGHTER)
                .createdOn(new Date())
                .build();

        mapper.addCharacter(characterDetails);
        
        characterDetails.setStats(mapper.getStartingStats(CharacterClass.FIGHTER, 4));
        characterDetails.getStats().setCharacterId(characterDetails.getId());;
        mapper.addCharacterStats(characterDetails.getStats());
        
        characterDetails.setItems(createDefaultItems(characterDetails.getId()));
        mapper.addCharacterItems(characterDetails.getItems());

        return characterDetails;
    }
    
    private CharacterDetails createMonk(String userId, String name) {
        CharacterDetails characterDetails = CharacterDetails.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .name(name)
                .characterClass(CharacterClass.MONK)
                .createdOn(new Date())
                .build();

        mapper.addCharacter(characterDetails);
        
        characterDetails.setStats(mapper.getStartingStats(CharacterClass.MONK, 4));
        characterDetails.getStats().setCharacterId(characterDetails.getId());;
        mapper.addCharacterStats(characterDetails.getStats());
        
        characterDetails.setItems(createDefaultItems(characterDetails.getId()));
        mapper.addCharacterItems(characterDetails.getItems());

        return characterDetails;
    }
    
    private CharacterDetails createPaladin(String userId, String name) {
        CharacterDetails characterDetails = CharacterDetails.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .name(name)
                .characterClass(CharacterClass.PALADIN)
                .createdOn(new Date())
                .build();

        mapper.addCharacter(characterDetails);
        
        characterDetails.setStats(mapper.getStartingStats(CharacterClass.PALADIN, 4));
        characterDetails.getStats().setCharacterId(characterDetails.getId());;
        mapper.addCharacterStats(characterDetails.getStats());
        
        characterDetails.setItems(createDefaultItems(characterDetails.getId()));
        mapper.addCharacterItems(characterDetails.getItems());

        return characterDetails;
    }
    
    private CharacterDetails createRanger(String userId, String name) {
        CharacterDetails characterDetails = CharacterDetails.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .name(name)
                .characterClass(CharacterClass.RANGER)
                .createdOn(new Date())
                .build();

        mapper.addCharacter(characterDetails);
        
        characterDetails.setStats(mapper.getStartingStats(CharacterClass.RANGER, 4));
        characterDetails.getStats().setCharacterId(characterDetails.getId());;
        mapper.addCharacterStats(characterDetails.getStats());
        
        characterDetails.setItems(createDefaultItems(characterDetails.getId()));
        mapper.addCharacterItems(characterDetails.getItems());

        return characterDetails;
    }
    
    private CharacterDetails createRogue(String userId, String name) {
        CharacterDetails characterDetails = CharacterDetails.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .name(name)
                .characterClass(CharacterClass.ROGUE)
                .createdOn(new Date())
                .build();

        mapper.addCharacter(characterDetails);
        
        characterDetails.setStats(mapper.getStartingStats(CharacterClass.ROGUE, 4));
        characterDetails.getStats().setCharacterId(characterDetails.getId());;
        mapper.addCharacterStats(characterDetails.getStats());
        
        characterDetails.setItems(createDefaultItems(characterDetails.getId()));
        mapper.addCharacterItems(characterDetails.getItems());

        return characterDetails;
    }
    
    private CharacterDetails createWizard(String userId, String name) {
        CharacterDetails characterDetails = CharacterDetails.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .name(name)
                .characterClass(CharacterClass.WIZARD)
                .createdOn(new Date())
                .build();

        mapper.addCharacter(characterDetails);
        
        characterDetails.setStats(mapper.getStartingStats(CharacterClass.WIZARD, 4));
        characterDetails.getStats().setCharacterId(characterDetails.getId());;
        mapper.addCharacterStats(characterDetails.getStats());
        
        characterDetails.setItems(createDefaultItems(characterDetails.getId()));
        mapper.addCharacterItems(characterDetails.getItems());

        return characterDetails;
    }
    
    private List<CharacterItem> createDefaultItems(String characterId) {
        List<CharacterItem> items = new ArrayList();
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.HEAD).index(0).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.EYES).index(0).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.EAR).index(0).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.EAR).index(1).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.NECK).index(0).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.TORSO).index(0).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.WRIST).index(0).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.HANDS).index(0).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.MAINHAND).index(0).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.OFFHAND).index(0).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.RANGE_MAINHAND).index(0).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.RANGE_OFFHAND).index(0).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.BACK).index(0).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.FINGER).index(0).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.FINGER).index(1).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.FIGURINE).index(0).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.WAIST).index(0).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.SHIRT).index(0).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.FEET).index(0).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.LEGS).index(0).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.CHARM).index(0).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.CHARM).index(1).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.CHARM).index(2).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.IOUNSTONE).index(0).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.IOUNSTONE).index(1).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.IOUNSTONE).index(2).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.IOUNSTONE).index(3).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.IOUNSTONE).index(4).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.SLOTLESS).index(0).slotStatus(SlotStatus.OK).build());
        items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(Slot.RUNESTONE).index(0).slotStatus(SlotStatus.OK).build());
        
        return items;
    }
}