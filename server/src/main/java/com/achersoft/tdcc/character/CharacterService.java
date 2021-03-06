package com.achersoft.tdcc.character;

import com.achersoft.tdcc.character.dao.CharacterDetails;
import com.achersoft.tdcc.character.dao.CharacterName;
import com.achersoft.tdcc.party.dao.SelectableCharacters;
import java.util.List;
import javax.ws.rs.core.StreamingOutput;

public interface CharacterService {
    void recalculateCharacters();
    CharacterDetails getCharacter(String id);
    List<CharacterName> getCharacters();
    SelectableCharacters getSelectableCharacters(String userId);
    CharacterDetails setTokenSlot(String id, String soltId, String tokenId);
    CharacterDetails unequipTokenSlot(String id, String soltId);
    List<CharacterName> deleteCharacter(String id);
    StreamingOutput exportCharacterPdf(String id);
    String exportCharacterHTML(String id);
    CharacterDetails validateCharacterItems(String id);
    CharacterDetails getCharacterMaxLevel(String id);
    
}
