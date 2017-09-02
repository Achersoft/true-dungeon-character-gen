package com.achersoft.rest.services;

import com.achersoft.security.annotations.RequiresPrivilege;
import com.achersoft.security.type.Privilege;
import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.enums.Difficulty;
import com.achersoft.tdcc.party.PartyService;
import com.achersoft.tdcc.party.dao.Party;
import com.achersoft.tdcc.party.dao.PartyDetails;
import com.achersoft.tdcc.party.dao.SelectableCharacters;
import java.util.List;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.hibernate.validator.constraints.NotEmpty;

@Path("/party")
public class PartyRestService {

    private @Inject PartyService partyService; 
    
    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @PUT 
    @Path("/create")
    @Produces({MediaType.APPLICATION_JSON})	
    public PartyDetails createParty(@QueryParam("name") String name) throws Exception {
        return partyService.createParty(Party.builder().name(name).build());
    }

    @GET 
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})	
    public PartyDetails getParty(@PathParam("id") String id) throws Exception {
        return partyService.getParty(id);
    }
    
    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @GET 
    @Path("/all")
    @Produces({MediaType.APPLICATION_JSON})	
    public List<Party> getParties() throws Exception {
        return partyService.getParties();
    }
    
    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @GET 
    @Path("/selectablecharacters")
    @Produces({MediaType.APPLICATION_JSON})	
    public SelectableCharacters getSelectableCharacters(@QueryParam("userid") String userid, @QueryParam("class") CharacterClass cClass) throws Exception {
        return partyService.getSelectableCharacters(userid, cClass);
    }
    
    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @POST 
    @Path("/{id}/difficulty")
    @Produces({MediaType.APPLICATION_JSON})	
    public PartyDetails updatePartyDifficulty(@PathParam("id") String id, @QueryParam("difficulty") Difficulty difficulty) throws Exception {
        return partyService.updatePartyDifficulty(id, difficulty);
    }
    
    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @POST 
    @Path("/{id}/addcharacter")
    @Produces({MediaType.APPLICATION_JSON})	
    public PartyDetails addPartyCharacter(@PathParam("id") String id, @QueryParam("characterId") String characterId) throws Exception {
        return partyService.addPartyCharacter(id, characterId);
    }
    
    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @DELETE 
    @Path("/{id}/removecharacter")
    @Produces({MediaType.APPLICATION_JSON})	
    public PartyDetails addPartyCharacter(@PathParam("id") String id, @QueryParam("characterClass") CharacterClass cClass) throws Exception {
        return partyService.removePartyCharacter(id, cClass);
    }
    
    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @DELETE 
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})	
    public List<Party> deleteParty(@PathParam("id") @NotNull @NotEmpty String id) throws Exception {
        return partyService.deleteParty(id);
    }
}

