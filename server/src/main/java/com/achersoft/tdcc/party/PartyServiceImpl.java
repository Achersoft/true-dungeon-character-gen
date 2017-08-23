package com.achersoft.tdcc.party;

import com.achersoft.security.providers.UserPrincipalProvider;
import com.achersoft.tdcc.party.dao.Party;
import com.achersoft.tdcc.party.dao.PartyDetails;
import com.achersoft.tdcc.party.persistence.PartyMapper;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import org.springframework.transaction.annotation.Transactional;

@Transactional(rollbackFor = Exception.class)
public class PartyServiceImpl implements PartyService {
    
    private @Inject PartyMapper mapper;
    private @Inject UserPrincipalProvider userPrincipalProvider;
    
    @Override
    public PartyDetails createParty(Party party) {
        party.setId(UUID.randomUUID().toString());
        party.setCreatedOn(new Date());
        party.setSize(0);
        mapper.createParty(party);
        return PartyDetails.builder().id(party.getId()).name(party.getName()).difficulty(party.getDifficulty()).initiative(0).build();
    }

    @Override
    public PartyDetails getParty(String id) {
        return null;
    }

    @Override
    public List<Party> getParties() {
        return mapper.getParties(userPrincipalProvider.getUserPrincipal().getSub());
    }

    @Override
    public List<Party> deleteParty(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

 
}
