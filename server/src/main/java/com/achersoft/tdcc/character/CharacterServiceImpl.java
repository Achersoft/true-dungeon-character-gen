package com.achersoft.tdcc.character;

import com.achersoft.security.providers.UserPrincipalProvider;
import com.achersoft.tdcc.character.dao.CharacterDetails;
import com.achersoft.tdcc.character.persistence.CharacterMapper;
import com.achersoft.tdcc.token.persistence.TokenMapper;
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
        return getCharacter(id);
    }
    
}
