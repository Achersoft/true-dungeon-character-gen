package com.achersoft.tdcc.character.create;

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
        
        return createBarbarian(userId, name);
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
        characterDetails.getStats().setCharacterId(characterDetails.getId());
        System.err.println(characterDetails.getStats());
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
