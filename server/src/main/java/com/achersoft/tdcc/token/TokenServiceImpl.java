package com.achersoft.tdcc.token;

import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.enums.Slot;
import com.achersoft.tdcc.token.dao.Token;
import com.achersoft.tdcc.token.persistence.TokenMapper;
import java.util.List;
import javax.inject.Inject;

public class TokenServiceImpl implements TokenService{

    private @Inject TokenMapper mapper;
    
    @Override
    public List<Token> getSlotItems(String characterId, CharacterClass characterClass, Slot slot) {
        return mapper.getNonWeaponSlotItems(characterId, characterClass, slot);
    }
    
}
