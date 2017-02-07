package com.achersoft.tdcc.character;

import com.achersoft.security.providers.UserPrincipalProvider;
import com.achersoft.tdcc.character.dao.CharacterDetails;
import com.achersoft.tdcc.character.dao.CharacterItem;
import com.achersoft.tdcc.character.persistence.CharacterMapper;
import com.achersoft.tdcc.enums.Slot;
import com.achersoft.tdcc.enums.SlotStatus;
import com.achersoft.tdcc.token.persistence.TokenMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class CharacterServiceImpl implements CharacterService {
    
    private @Inject CharacterMapper mapper;
    private @Inject TokenMapper tokenMapper;
    private @Inject UserPrincipalProvider userPrincipalProvider;

    @Override
    public CharacterDetails getCharacter(String id) {
        CharacterDetails characterDetails = mapper.getCharacter(id, userPrincipalProvider.getUserPrincipal().getSub());
        
        if(characterDetails == null)
            throw new UnsupportedOperationException("Not supported yet."); 
        
        characterDetails.setStats(mapper.getCharacterStats(id));
        characterDetails.setItems(mapper.getCharacterItems(id));
        
        return characterDetails;
    }

    @Override
    public CharacterDetails setTokenSlot(String id, String soltId, String tokenId) {
        tokenMapper.setTokenSlot(soltId, tokenId);
        CharacterDetails characterDetails =getCharacter(id);
        
        validateCharacterItems(characterDetails);
        
        mapper.deleteCharacterItems(id);
        mapper.addCharacterItems(characterDetails.getItems());
        
        return characterDetails;
    }
    
    @Override
    public CharacterDetails unequipTokenSlot(String id, String soltId) {
        tokenMapper.unequipTokenSlot(soltId);
        CharacterDetails characterDetails =getCharacter(id);
        
        validateCharacterItems(characterDetails);
        
        mapper.deleteCharacterItems(id);
        mapper.addCharacterItems(characterDetails.getItems());
        
        return characterDetails;
    }
    
    private void validateCharacterItems(CharacterDetails characterDetails) {
        //create map of items
        final Map<String, CharacterItem> itemsMap = new HashMap();
        characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null).forEach((item) -> {
            itemsMap.put(item.getItemId(), item);
        });
        //check if 2h wep and remove shueld
        checkSlotModItems(itemsMap, characterDetails);
        
    }
    
    private void checkSlotModItems(Map<String, CharacterItem> itemsMap, CharacterDetails characterDetails) {
        long fingerCount = 0;
        // Check for Medalion of Heroism
        if(itemsMap.containsKey("d4674a1b2bea57e8b11676fed2bf81bd4c48ac78"))
            characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> item.getSlot()!=Slot.FINGER).collect(Collectors.toList()));
        else {
            fingerCount = characterDetails.getItems().stream().filter((item) -> item.getSlot()==Slot.FINGER).count();
            if(fingerCount == 0) {
                characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.FINGER).index(0).slotStatus(SlotStatus.OK).build());
                characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.FINGER).index(1).slotStatus(SlotStatus.OK).build());
                fingerCount = 2;
            }
        }
        
        // Check for Hand of Glory & Gloves of Glory
        if(itemsMap.containsKey("83f0fa64bcdfcdd374b83a6189d786471d8e4213") || itemsMap.containsKey("68ca031f95876ef5b3e9099894a7ffeb02a73142")) {
            if(fingerCount == 2)
                characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.FINGER).index(2).slotStatus(SlotStatus.OK).build());
        } else {
            if(fingerCount > 2)
                characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> !(item.getSlot()==Slot.FINGER && item.getIndex() > 1)).collect(Collectors.toList()));
        }
        
        // Check for Charm Braclets & Charm Necklace
        long charmCount = characterDetails.getItems().stream().filter((item) -> item.getSlot()==Slot.CHARM).count();
        if(itemsMap.containsKey("0f19e0a1be0f5494e71765590d86390d40f98177") || itemsMap.containsKey("572fb173c78ab1963747b00f22cc42a89c74b7b4")) {
            if(itemsMap.containsKey("0f19e0a1be0f5494e71765590d86390d40f98177")) {
                if(charmCount == 3) {
                    charmCount = 5;
                    characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.CHARM).index(3).slotStatus(SlotStatus.OK).build());
                    characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.CHARM).index(4).slotStatus(SlotStatus.OK).build());
                } else if(charmCount == 6) {
                    charmCount = 8;
                    characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.CHARM).index(6).slotStatus(SlotStatus.OK).build());
                    characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.CHARM).index(7).slotStatus(SlotStatus.OK).build());
                }
            } else {
                if(charmCount == 8) {
                    charmCount = 6;
                    characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> !(item.getSlot()==Slot.CHARM && item.getIndex() > 5)).collect(Collectors.toList()));
                }
            }
            if(itemsMap.containsKey("572fb173c78ab1963747b00f22cc42a89c74b7b4")) {
                if(charmCount == 3) {
                    characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.CHARM).index(3).slotStatus(SlotStatus.OK).build());
                    characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.CHARM).index(4).slotStatus(SlotStatus.OK).build());
                    characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.CHARM).index(5).slotStatus(SlotStatus.OK).build());
                } else if(charmCount == 5) {
                    characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.CHARM).index(5).slotStatus(SlotStatus.OK).build());
                    characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.CHARM).index(6).slotStatus(SlotStatus.OK).build());
                    characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.CHARM).index(7).slotStatus(SlotStatus.OK).build());
                }
            } else {
                if(charmCount == 8) 
                    characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> !(item.getSlot()==Slot.CHARM && item.getIndex() > 4)).collect(Collectors.toList()));
            }
        } else {
            if(charmCount > 3) 
                characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> !(item.getSlot()==Slot.CHARM && item.getIndex() > 2)).collect(Collectors.toList()));
        }
        
        // Check for Earcuff of Orbits
        long iounCount = characterDetails.getItems().stream().filter((item) -> item.getSlot()==Slot.IOUNSTONE).count();
        if(itemsMap.containsKey("a898af8cb818c2f1e56acd8ddb78f0de9d4901e0")) {
            if(iounCount == 5) {
                characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.IOUNSTONE).index(5).slotStatus(SlotStatus.OK).build());
                characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.IOUNSTONE).index(6).slotStatus(SlotStatus.OK).build());
            }
        } else {
            if(iounCount > 5)
                characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> !(item.getSlot()==Slot.IOUNSTONE && item.getIndex() > 4)).collect(Collectors.toList()));
        }
        
        // Check for Charm of Brooching
        long backCount = characterDetails.getItems().stream().filter((item) -> item.getSlot()==Slot.BACK).count();
        if(characterDetails.getItems().stream().filter((item) -> (item.getItemId()!=null&&item.getItemId().equals("2ea75650daa8b7025cd7887c87ccd16f6a6ca369"))).count() > 0) {
            if(backCount == 1)
                characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.BACK).index(1).slotStatus(SlotStatus.OK).build());
        } else {
            if(backCount > 1)
                characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> !(item.getSlot()==Slot.BACK && item.getIndex() > 0)).collect(Collectors.toList()));
        }
    }
}
