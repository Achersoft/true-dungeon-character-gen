package com.achersoft.tdcc.party;

import com.achersoft.tdcc.party.dao.Party;
import com.achersoft.tdcc.party.dao.PartyDetails;
import java.util.List;

public interface PartyService {
    public PartyDetails createParty(Party party);
    public PartyDetails getParty(String id);
    public List<Party> getParties();
    public List<Party> deleteParty(String id);
}
