package com.achersoft.rest.services;

import com.achersoft.security.annotations.RequiresPrivilege;
import com.achersoft.security.type.Privilege;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import com.achersoft.tdcc.token.admin.TokenAdminService;
import com.achersoft.tdcc.token.admin.dao.TokenFullDetails;

@Path("/token/admin")
public class TokenAdminRestService {

    private @Inject TokenAdminService tokenAdminService; 
    
    @RequiresPrivilege({Privilege.ADMIN})
    @PUT 
    @Consumes({MediaType.APPLICATION_JSON})	
    public void addToken(TokenFullDetails token) throws Exception {
        tokenAdminService.addToken(token);
    }
}

