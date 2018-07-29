package com.achersoft.rest.services;

import com.achersoft.security.annotations.RequiresPrivilege;
import com.achersoft.security.type.Privilege;
import com.achersoft.tdcc.character.CharacterService;
import com.achersoft.tdcc.character.create.CharacterCreatorService;
import com.achersoft.tdcc.character.dao.CharacterName;
import com.achersoft.tdcc.character.dto.CharacterDetailsDTO;
import com.achersoft.tdcc.enums.CharacterClass;
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
import javax.ws.rs.core.StreamingOutput;
import org.hibernate.validator.constraints.NotEmpty;

@Path("/character")
public class CharacterRestService {

    private @Inject CharacterCreatorService characterCreatorService; 
    private @Inject CharacterService characterService; 
    
    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @PUT 
    @Path("/create")
    @Produces({MediaType.APPLICATION_JSON})	
    public CharacterDetailsDTO createCharacter(@QueryParam("characterClass") CharacterClass characterClass, @QueryParam("name") String name) throws Exception {
        return CharacterDetailsDTO.fromDAO(characterService.validateCharacterItems(characterCreatorService.createCharacter(characterClass, name).getId()));
    }
    
    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @PUT 
    @Path("/clone/{id}")
    @Produces({MediaType.APPLICATION_JSON})	
    public CharacterDetailsDTO cloneCharacter(@PathParam("id") @NotNull @NotEmpty String id, @QueryParam("characterClass") CharacterClass characterClass, @QueryParam("name") String name) throws Exception {
        return CharacterDetailsDTO.fromDAO(characterService.validateCharacterItems(characterCreatorService.copyCharacter(characterClass, name, id).getId()));
    }
    
    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @PUT 
    @Path("/rename/{id}")
    @Produces({MediaType.APPLICATION_JSON})	
    public CharacterDetailsDTO renameCharacter(@PathParam("id") @NotNull @NotEmpty String id, @QueryParam("name") String name) throws Exception {
        return CharacterDetailsDTO.fromDAO(characterCreatorService.renameCharacter(id, name));
    }

    @GET 
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})	
    public CharacterDetailsDTO getCharacter(@PathParam("id") @NotNull @NotEmpty String id) throws Exception {
        return CharacterDetailsDTO.fromDAO(characterService.getCharacter(id));
    }

    @GET 
    @Path("/pdf/{id}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getPDF(@PathParam("id") @NotNull @NotEmpty String id) throws Exception {
        return characterService.exportCharacterPdf(id);
    }
    
    @GET 
    @Path("/html/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getHTML(@PathParam("id") @NotNull @NotEmpty String id) throws Exception {
        return characterService.exportCharacterHTML(id);
    }
    
    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @GET 
    @Path("/all")
    @Produces({MediaType.APPLICATION_JSON})	
    public List<CharacterName> getCharacters() throws Exception {
        return characterService.getCharacters();
    }
    
    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @GET 
    @Path("/selectablecharacters")
    @Produces({MediaType.APPLICATION_JSON})	
    public SelectableCharacters getSelectableCharacters(@QueryParam("userid") String userid) throws Exception {
        return characterService.getSelectableCharacters(userid);
    }
    
    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @POST 
    @Path("/token")
    @Produces({MediaType.APPLICATION_JSON})	
    public CharacterDetailsDTO setTokenSlot(@QueryParam("id") @NotNull @NotEmpty String id, @QueryParam("soltId") @NotNull @NotEmpty String soltId, @QueryParam("tokenId") @NotNull @NotEmpty String tokenId) throws Exception {
        return CharacterDetailsDTO.fromDAO(characterService.setTokenSlot(id, soltId, tokenId));
    }
    
    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @POST 
    @Path("/token/unequip")
    @Produces({MediaType.APPLICATION_JSON})	
    public CharacterDetailsDTO unequipTokenSlot(@QueryParam("id") @NotNull @NotEmpty String id, @QueryParam("soltId") @NotNull @NotEmpty String soltId) throws Exception {
        return CharacterDetailsDTO.fromDAO(characterService.unequipTokenSlot(id, soltId));
    }
    
    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @DELETE 
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})	
    public List<CharacterName> deleteCharacter(@PathParam("id") @NotNull @NotEmpty String id) throws Exception {
        return characterService.deleteCharacter(id);
    }
}

