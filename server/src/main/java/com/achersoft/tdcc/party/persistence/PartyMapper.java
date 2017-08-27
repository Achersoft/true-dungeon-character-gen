package com.achersoft.tdcc.party.persistence;

import com.achersoft.tdcc.party.dao.Party;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PartyMapper {   
    public void createParty(Party party);
    public Party getParty(@Param("id") String id);
    public List<Party> getParties(@Param("userId") String userId);
    public void deleteParty(@Param("id") String id, @Param("userId") String userId);
}

