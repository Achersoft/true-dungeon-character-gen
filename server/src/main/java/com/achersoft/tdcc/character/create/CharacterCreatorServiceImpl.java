package com.achersoft.tdcc.character.create;

import com.achersoft.exception.AuthenticationException;
import com.achersoft.exception.InvalidDataException;
import com.achersoft.security.providers.UserPrincipalProvider;
import com.achersoft.tdcc.character.CharacterService;
import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.character.dao.CharacterDetails;
import com.achersoft.tdcc.character.dao.CharacterItem;
import com.achersoft.tdcc.character.persistence.CharacterMapper;
import com.achersoft.tdcc.enums.Slot;
import com.achersoft.tdcc.enums.SlotStatus;
import com.achersoft.tdcc.token.admin.dao.TokenFullDetails;
import com.achersoft.tdcc.token.admin.persistence.TokenAdminMapper;
import com.achersoft.tdcc.token.persistence.TokenMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.springframework.transaction.annotation.Transactional;

@Transactional(rollbackFor = Exception.class)
public class CharacterCreatorServiceImpl implements CharacterCreatorService {
    
    private @Inject CharacterMapper mapper;
    private @Inject UserPrincipalProvider userPrincipalProvider;
    private @Inject CharacterService characterService; 
    private @Inject TokenMapper tokenMapper;
    private @Inject TokenAdminMapper tokenAdminMapper;
    
    @Override
    public CharacterDetails createCharacter(CharacterClass characterClass, String name) throws Exception {
        String userId = userPrincipalProvider.getUserPrincipal().getSub();
        
        if(name == null || name.isEmpty() || name.equals("null"))
            throw new InvalidDataException("Character name cannot be null."); 
        
        if(characterClass == null)
            throw new InvalidDataException("A character must have a class."); 
        
        if(userId == null || userId.isEmpty())
            throw new AuthenticationException("User is not valid."); 
        
        if(mapper.getCharacterCount(userId) >= 100)
            throw new InvalidDataException("Maximum character limit exceeded. Due to storage capabilities of the serer each account is limited to 100 characters. In order to create additional characters you must delete one or more existing characters");
        
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
    
    @Override
    public CharacterDetails renameCharacter(String id, String name) throws Exception {
        CharacterDetails characterDetails = characterService.getCharacter(id);
        
        if(name == null || name.isEmpty() || name.equals("null"))
            throw new InvalidDataException("Character name cannot be null."); 
        
        characterDetails.setName(name);
        mapper.updateCharacterName(id, name);
        
        return characterDetails;
    }
    
    @Override
    public CharacterDetails copyCharacter(CharacterClass characterClass, String name, String cloneId) throws Exception {
        AtomicReference<CharacterDetails> characterDetails = new AtomicReference<>(createCharacter(characterClass, name));
        
        try {
            List<CharacterItem> items = characterService.getCharacter(cloneId).getItems();
            List<CharacterItem> regularItems = new ArrayList<>();
            List<CharacterItem> lastItems = new ArrayList<>();

            for (CharacterItem item : items) {
                if (item.getItemId() == null || item.getItemId().isEmpty())
                    continue;
                if (!tokenMapper.itemUsableByClass(item.getItemId(), characterClass.name()))
                    continue;

                final TokenFullDetails tokenDetails = tokenAdminMapper.getTokenDetails(item.getItemId());
                tokenDetails.setSlotModifiers(tokenAdminMapper.getSlotModifiers(tokenDetails.getId()));

                if (tokenDetails.getSlotModifiers() != null && !tokenDetails.getSlotModifiers().isEmpty()) {
                    CharacterItem charItem = characterDetails.get().getItems().stream().filter(i -> (i.getItemId()==null || i.getItemId().isEmpty()) && i.getSlot().equals(item.getSlot()))
                            .min(Comparator.comparing(CharacterItem::getIndex)).orElse(null);
                    if (charItem != null)
                        characterDetails.set(characterService.setTokenSlot(characterDetails.get().getId(), charItem.getId(), item.getItemId()));
                } else if (tokenDetails.getSlot() == Slot.FIGURINE) {
                    lastItems.add(item);
                } else {
                    regularItems.add(item);
                }
            }

            for (CharacterItem item : regularItems) {
                CharacterItem charItem = characterDetails.get().getItems().stream().filter(i -> (i.getItemId()==null || i.getItemId().isEmpty()) && i.getSlot().equals(item.getSlot()))
                        .min(Comparator.comparing(CharacterItem::getIndex)).orElse(null);
                if (charItem != null)
                    characterDetails.set(characterService.setTokenSlot(characterDetails.get().getId(), charItem.getId(), item.getItemId()));
            }

            for (CharacterItem item : lastItems) {
                CharacterItem charItem = characterDetails.get().getItems().stream().filter(i -> (i.getItemId()==null || i.getItemId().isEmpty()) && i.getSlot().equals(item.getSlot()))
                        .min(Comparator.comparing(CharacterItem::getIndex)).orElse(null);
                if (charItem != null)
                    characterDetails.set(characterService.setTokenSlot(characterDetails.get().getId(), charItem.getId(), item.getItemId()));
            }

        } catch(Exception e) { }

        return characterService.getCharacter(characterDetails.get().getId());
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
        for (Slot value : Slot.values()) {
            for (int i=0; i<value.defaultSize; i++)
                items.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterId).slot(value).index(i).slotStatus(SlotStatus.OK).build());
        }

        return items;
    }
}
