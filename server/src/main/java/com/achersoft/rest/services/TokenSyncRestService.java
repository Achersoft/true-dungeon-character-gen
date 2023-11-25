package com.achersoft.rest.services;

import com.achersoft.security.annotations.RequiresPrivilege;
import com.achersoft.security.type.Privilege;
import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.enums.Rarity;
import com.achersoft.tdcc.enums.Slot;
import com.achersoft.tdcc.token.TokenService;
import com.achersoft.tdcc.token.admin.TokenAdminService;
import com.achersoft.tdcc.token.admin.dto.TokenFullDetailsDTO;
import com.achersoft.tdcc.token.dto.TokenDTO;
import com.achersoft.tdcc.tokendb.TokenSyncService;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/sync")
public class TokenSyncRestService {

    private @Inject TokenSyncService tokenSyncService;

 //   @RequiresPrivilege({Privilege.ADMIN})
    @GET 
    @Path("")
    @Produces({MediaType.APPLICATION_JSON})	
    public void searchForToken() throws Exception {
        tokenSyncService.syncTokens();
    }

}

