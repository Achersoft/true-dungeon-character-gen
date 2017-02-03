package com.achersoft.tdcc.character.create;

import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.character.dao.CharacterDetails;
import com.achersoft.tdcc.character.dao.CharacterItem;
import com.achersoft.tdcc.character.dao.CharacterStats;
import com.achersoft.tdcc.character.persistence.CharacterMapper;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class CharacterCreatorServiceImpl implements CharacterCreatorService {
    
    private @Inject CharacterMapper mapper;
    
    @Override
    public CharacterDetails createCharacter(CharacterClass characterClass, String name) {
        
        
        return createBarbarian(name);
    }
    
    private CharacterDetails createBarbarian(String name) {
        CharacterDetails characterDetails = CharacterDetails.builder()
                .name(name)
                .characterClass(CharacterClass.BARBARIAN)
                .build();
        
        mapper.addCharacter(characterDetails);
        
        characterDetails.setStats(mapper.getStartingStats(CharacterClass.BARBARIAN, 4));
        characterDetails.getStats().setCharacterId(characterDetails.getId());
        mapper.addCharacterStats(characterDetails.getStats());
        
        List<CharacterItem> items = new ArrayList();
        items
        
        
        return characterDetails;
    }
}
