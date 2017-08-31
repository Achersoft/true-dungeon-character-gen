package com.achersoft.tdcc.party;

import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.party.dao.Party;
import com.achersoft.tdcc.party.dao.PartyDetails;
import com.achersoft.tdcc.party.dao.SelectableCharacters;
import java.util.List;

public interface PartyService {
    public PartyDetails createParty(Party party);
    public PartyDetails getParty(String id);
    public List<Party> getParties();
    public SelectableCharacters getSelectableCharacters(String userid, CharacterClass cClass);
    public PartyDetails addPartyCharacter(String id, String characterId);
    public PartyDetails removePartyCharacter(String id, CharacterClass cClass);
    public List<Party> deleteParty(String id);
}
