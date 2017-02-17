package com.achersoft.rest.services;

import com.achersoft.security.annotations.RequiresPrivilege;
import com.achersoft.security.type.Privilege;
import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.enums.Rarity;
import com.achersoft.tdcc.enums.Slot;
import com.achersoft.tdcc.token.TokenService;
import com.achersoft.tdcc.token.admin.TokenAdminService;
import com.achersoft.tdcc.token.admin.dto.TokenFullDetailsDTO;
import com.achersoft.tdcc.token.dao.Token;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.validation.Valid;
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

@Path("/token")
public class TokenRestService {

    private @Inject TokenAdminService tokenAdminService; 
    private @Inject TokenService tokenService; 
    
    @RequiresPrivilege({Privilege.ADMIN})
    @PUT 
    @Path("/admin")
    @Consumes({MediaType.APPLICATION_JSON})	
    public void addToken(@Valid TokenFullDetailsDTO token) throws Exception {
        tokenAdminService.addToken(token.toDAO());
    }
    
    @RequiresPrivilege({Privilege.ADMIN})
    @POST 
    @Path("/admin")
    @Consumes({MediaType.APPLICATION_JSON})	
    public void editToken(@Valid TokenFullDetailsDTO token) throws Exception {
        tokenAdminService.editToken(token.toDAO());
    }
    
    @RequiresPrivilege({Privilege.ADMIN})
    @GET 
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})	
    public List<TokenFullDetailsDTO> searchForToken(@QueryParam("name") @NotNull @NotEmpty String name) throws Exception {
        return tokenAdminService.search("%" + name + "%").stream().map((dao) -> { return TokenFullDetailsDTO.fromDAO(dao); }).collect(Collectors.toList());
    }
    
    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @GET 
    @Path("/character")
    @Produces({MediaType.APPLICATION_JSON})	
    public List<Token> getSlotTokens(@QueryParam("slotId") @NotNull @NotEmpty String slotId, @QueryParam("characterId") @NotNull @NotEmpty String characterId, @QueryParam("characterClass") @NotNull CharacterClass characterClass, @QueryParam("slot") @NotNull Slot slot, @QueryParam("rarity") @NotNull Rarity rarity) throws Exception {
        return tokenService.getSlotItems(slotId, characterId, characterClass, slot, rarity);
    }
}

