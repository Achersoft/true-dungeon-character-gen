package com.achersoft.rest.services;

import com.achersoft.security.UserAuthenticationService;
import com.achersoft.security.annotations.RequiresPrivilege;
import com.achersoft.security.dto.UserLoginRequest;
import com.achersoft.security.type.Privilege;
import com.achersoft.user.UserService;
import com.achersoft.user.dao.ResetPassword;
import com.achersoft.user.dao.User;
import com.achersoft.user.dto.UserDTO;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/users")
public class UserRestService {

    private @Inject UserService userProvider; 
    private @Inject UserAuthenticationService userAuthenticationProvider; 

    @RequiresPrivilege({Privilege.ADMIN})
    @GET 
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON})	
    public List<UserDTO> getUsers() throws Exception {
        return userProvider.getUsers().stream().map((user) -> {
            return UserDTO.fromDAO(user);
        }).collect(Collectors.toList());	
    }
    
    @RequiresPrivilege({Privilege.ADMIN})
    @GET 
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})	
    public UserDTO getUser(@PathParam("id") String id) throws Exception {
        return UserDTO.fromDAO(userProvider.getUser(id));	
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
    
    @RequiresPrivilege({Privilege.ADMIN})
    @PUT 
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})	
    @Consumes({MediaType.APPLICATION_JSON})
    public UserDTO editUser(@PathParam("id")String id, @Valid @NotNull UserDTO user) throws Exception {
        return UserDTO.fromDAO(userProvider.editUser(user.toDAO()));
    }
    
    @POST 
    @Path("/resetpassword")
    public void resetPassword(@Valid @NotNull ResetPassword resetPassword) throws Exception {
        userProvider.resetPassword(resetPassword);	
    }
    
    @RequiresPrivilege({Privilege.ADMIN})
    @DELETE 
    @Path("/{id}")
    public void deleteUser(@PathParam("id") String id) throws Exception {
        userProvider.deleteUser(id);	
    }
}