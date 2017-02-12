package com.achersoft.tdcc.character;

import com.achersoft.tdcc.character.dao.CharacterDetails;
import com.achersoft.tdcc.character.dao.CharacterName;
import java.util.List;

public interface CharacterService {
    public CharacterDetails getCharacter(String id);
    public List<CharacterName> getCharacters();
    public CharacterDetails setTokenSlot(String id, String soltId, String tokenId);
    public CharacterDetails unequipTokenSlot(String id, String soltId);
    public List<CharacterName> deleteCharacter(String id);
}
