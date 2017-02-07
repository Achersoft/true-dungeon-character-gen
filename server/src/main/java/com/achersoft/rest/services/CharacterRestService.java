package com.achersoft.rest.services;

import com.achersoft.security.annotations.RequiresPrivilege;
import com.achersoft.security.type.Privilege;
import com.achersoft.tdcc.character.CharacterService;
import com.achersoft.tdcc.character.create.CharacterCreatorService;
import com.achersoft.tdcc.character.dto.CharacterDetailsDTO;
import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.token.dao.Token;
import java.util.List;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.hibernate.validator.constraints.NotEmpty;

@Path("/character")
public class CharacterRestService {

    private @Inject CharacterCreatorService characterCreatorService; 
    private @Inject CharacterService characterService; 
    
    @RequiresPrivilege({Privilege.ADMIN})
    @PUT 
    @Path("/create")
    @Consumes({MediaType.APPLICATION_JSON})	
    public CharacterDetailsDTO createCharacter(@QueryParam("characterClass") @NotNull CharacterClass characterClass, @QueryParam("name") @NotNull @NotEmpty String name) throws Exception {
        return CharacterDetailsDTO.fromDAO(characterCreatorService.createCharacter(characterClass, name));
    }
    
    @RequiresPrivilege({Privilege.ADMIN})
    @GET 
    @Consumes({MediaType.APPLICATION_JSON})	
    public CharacterDetailsDTO getCharacter(@QueryParam("id") @NotNull @NotEmpty String id) throws Exception {
        return CharacterDetailsDTO.fromDAO(characterService.getCharacter(id));
    }
    
    @RequiresPrivilege({Privilege.ADMIN})
    @POST 
    @Path("/token")
    @Produces({MediaType.APPLICATION_JSON})	
    public CharacterDetailsDTO setTokenSlot(@QueryParam("id") @NotNull @NotEmpty String id, @QueryParam("soltId") @NotNull @NotEmpty String soltId, @QueryParam("tokenId") @NotNull @NotEmpty String tokenId) throws Exception {
        return CharacterDetailsDTO.fromDAO(characterService.setTokenSlot(id, soltId, tokenId));
    }
    
    @RequiresPrivilege({Privilege.ADMIN})
    @POST 
    @Path("/token/unequip")
    @Produces({MediaType.APPLICATION_JSON})	
    public CharacterDetailsDTO unequipTokenSlot(@QueryParam("id") @NotNull @NotEmpty String id, @QueryParam("soltId") @NotNull @NotEmpty String soltId) throws Exception {
        return CharacterDetailsDTO.fromDAO(characterService.unequipTokenSlot(id, soltId));
    }
}

