package com.achersoft.rest.services;

import com.achersoft.security.annotations.RequiresPrivilege;
import com.achersoft.security.type.Privilege;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import com.achersoft.tdcc.token.admin.TokenAdminService;
import com.achersoft.tdcc.token.admin.dto.TokenFullDetailsDTO;
import javax.validation.Valid;

@Path("/token/admin")
public class TokenAdminRestService {

    private @Inject TokenAdminService tokenAdminService; 
    
    @RequiresPrivilege({Privilege.ADMIN})
    @PUT 
    @Consumes({MediaType.APPLICATION_JSON})	
    public void addToken(@Valid TokenFullDetailsDTO token) throws Exception {
        tokenAdminService.addToken(token.toDAO());
    }
}

