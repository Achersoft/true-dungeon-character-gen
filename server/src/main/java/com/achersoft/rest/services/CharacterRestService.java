package com.achersoft.rest.services;

import com.achersoft.security.annotations.RequiresPrivilege;
import com.achersoft.security.type.Privilege;
import com.achersoft.tdcc.character.CharacterService;
import com.achersoft.tdcc.character.create.CharacterCreatorService;
import com.achersoft.tdcc.character.dao.CharacterName;
import com.achersoft.tdcc.character.dto.CharacterDetailsDTO;
import com.achersoft.tdcc.enums.CharacterClass;
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
    public CharacterDetailsDTO createCharacter(@QueryParam("characterClass") @NotNull CharacterClass characterClass, @QueryParam("name") @NotNull @NotEmpty String name) throws Exception {
        return CharacterDetailsDTO.fromDAO(characterCreatorService.createCharacter(characterClass, name));
    }
    
    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @GET 
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})	
    public CharacterDetailsDTO getCharacter(@PathParam("id") @NotNull @NotEmpty String id) throws Exception {
        return CharacterDetailsDTO.fromDAO(characterService.getCharacter(id));
    }
    
   // @RequiresPrivilege({Privilege.ADMIN})
  //  @GET 
 //   @Path("/pdf/{id}")
  //  @Produces("application/pdf")
 /*   public Response getCharacterPDF(@PathParam("id") @NotNull @NotEmpty String id) throws Exception {
       
        PdfReader reader = new PdfReader("C:\\Users\\shaun\\Repositories\\true-dungeon-character-gen\\server\\barb.pdf");
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, ba);
        AcroFields fields = stamper.getAcroFields();
        fields.setField("MeleeToHit", "5");
        stamper.setFormFlattening(true);
        stamper.close();
        reader.close();

        ResponseBuilder response = Response.ok((Object) ba);
        response.header("Content-Disposition",
                "attachment; filename=" + "Test" + ".pdf");
        return response.build();//characterService.getCharacter(id).getName()
    }*/
    
    @GET 
    @Path("/pdf/{id}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    //@Produces({"application/pdf"})
    public StreamingOutput getPDF(@PathParam("id") @NotNull @NotEmpty String id) throws Exception {
        return characterService.exportCharacterPdf(id);
    }
    
    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @GET 
    @Path("/all")
    @Produces({MediaType.APPLICATION_JSON})	
    public List<CharacterName> getCharacters() throws Exception {
        return characterService.getCharacters();
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

