package com.achersoft.rest.services;

import com.achersoft.security.annotations.RequiresPrivilege;
import com.achersoft.security.type.Privilege;
import com.achersoft.tdcc.enums.Difficulty;
import com.achersoft.tdcc.party.PartyService;
import com.achersoft.tdcc.party.dao.Party;
import com.achersoft.tdcc.party.dto.PartyDTO;
import com.achersoft.tdcc.party.dto.PartyDetailsDTO;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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
    public PartyDetailsDTO createParty(@QueryParam("name") String name, @QueryParam("difficulty") Difficulty difficulty) throws Exception {
        return PartyDetailsDTO.fromDAO(partyService.createParty(Party.builder().name(name).difficulty(difficulty).build()));
    }

    @GET 
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})	
    public PartyDetailsDTO getParty(@PathParam("id") @NotNull @NotEmpty String id) throws Exception {
        return PartyDetailsDTO.fromDAO(partyService.getParty(id));
    }
    
    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @GET 
    @Path("/all")
    @Produces({MediaType.APPLICATION_JSON})	
    public List<PartyDTO> getParties() throws Exception {
        return partyService.getParties().stream().map((dao) -> PartyDTO.fromDAO(dao)).collect(Collectors.toList());
    }
    
    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @DELETE 
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})	
    public List<PartyDTO> deleteParty(@PathParam("id") @NotNull @NotEmpty String id) throws Exception {
        return partyService.deleteParty(id).stream().map((dao) -> PartyDTO.fromDAO(dao)).collect(Collectors.toList());
    }
}

