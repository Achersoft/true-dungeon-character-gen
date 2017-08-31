package com.achersoft.tdcc.party.persistence;

import com.achersoft.tdcc.party.dao.Party;
import com.achersoft.user.dao.User;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PartyMapper {   
    public void createParty(Party party);
    public Party getParty(@Param("id") String id);
    public List<Party> getParties(@Param("userId") String userId);
    public List<User> getUsers();
    public void addBarbarian(@Param("partyId") String partyId, @Param("characterId") String characterId);
    public void addBard(@Param("partyId") String partyId, @Param("characterId") String characterId);
    public void addCleric(@Param("partyId") String partyId, @Param("characterId") String characterId);
    public void addDruid(@Param("partyId") String partyId, @Param("characterId") String characterId);
    public void addDwarf(@Param("partyId") String partyId, @Param("characterId") String characterId);
    public void addElf(@Param("partyId") String partyId, @Param("characterId") String characterId);
    public void addFighter(@Param("partyId") String partyId, @Param("characterId") String characterId);
    public void addMonk(@Param("partyId") String partyId, @Param("characterId") String characterId);
    public void addPaladin(@Param("partyId") String partyId, @Param("characterId") String characterId);
    public void addRanger(@Param("partyId") String partyId, @Param("characterId") String characterId);
    public void addRogue(@Param("partyId") String partyId, @Param("characterId") String characterId);
    public void addWizard(@Param("partyId") String partyId, @Param("characterId") String characterId);
    public void removeBarbarian(@Param("partyId") String partyId);
    public void removeBard(@Param("partyId") String partyId);
    public void removeCleric(@Param("partyId") String partyId);
    public void removeDruid(@Param("partyId") String partyId);
    public void removeDwarf(@Param("partyId") String partyId);
    public void removeElf(@Param("partyId") String partyId);
    public void removeFighter(@Param("partyId") String partyId);
    public void removeMonk(@Param("partyId") String partyId);
    public void removePaladin(@Param("partyId") String partyId);
    public void removeRanger(@Param("partyId") String partyId);
    public void removeRogue(@Param("partyId") String partyId);
    public void removeWizard(@Param("partyId") String partyId);
    public void deleteParty(@Param("id") String id, @Param("userId") String userId);
}

