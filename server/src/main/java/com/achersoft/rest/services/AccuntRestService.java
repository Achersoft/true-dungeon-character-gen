package com.achersoft.rest.services;

import com.achersoft.security.UserAuthenticationService;
import com.achersoft.security.annotations.RequiresPrivilege;
import com.achersoft.security.type.Privilege;
import com.achersoft.user.UserService;
import com.achersoft.user.dao.ChangePassword;
import com.achersoft.user.dao.ResetPassword;
import com.achersoft.user.dao.User;
import com.achersoft.user.dto.UserDTO;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/account")
public class AccuntRestService {

    private @Inject UserService userProvider; 
    private @Inject UserAuthenticationService userAuthenticationProvider; 

    @RequiresPrivilege({Privilege.ADMIN})
    @GET 
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON})	
    public List<UserDTO> getAccountSettings() throws Exception {
        return userProvider.getUsers().stream().map((user) -> {
            return UserDTO.fromDAO(user);
        }).collect(Collectors.toList());	
    }

    @POST 
    @Path("/create")
    @Produces({MediaType.APPLICATION_JSON})	
    @Consumes({MediaType.APPLICATION_JSON})
    public Response createUser(@Valid @NotNull UserDTO user) throws Exception {
        User createUser = userProvider.createUser(user.toDAO());
        userAuthenticationProvider.login(createUser);	
        return Response.status(Response.Status.OK.getStatusCode()).build();
    }
}