package com.achersoft.tdcc.character;

import com.achersoft.tdcc.character.dao.CharacterDetails;

public interface CharacterService {
    public CharacterDetails getCharacter(String id);
    public CharacterDetails setTokenSlot(String id, String soltId, String tokenId);
}
