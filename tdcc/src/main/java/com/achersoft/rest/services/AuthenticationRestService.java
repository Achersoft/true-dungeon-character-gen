package com.achersoft.rest.services;

import com.achersoft.security.UserAuthenticationService;
import com.achersoft.security.dto.UserLoginRequest;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("/")
public class AuthenticationRestService {

    private @Inject UserAuthenticationService userAuthenticationProvider; 

    @POST 
    @Path("/login")
    @Consumes({MediaType.APPLICATION_JSON})
    public void login(UserLoginRequest request) {
        userAuthenticationProvider.login(request);	
    }
    
    @POST 
    @Path("/logout")
    public void logout() {
        userAuthenticationProvider.logout();
    }
    
 /*   @POST 
    @Path("/changepassword")
    @Consumes({MediaType.APPLICATION_JSON})
    public void changePassword(UserChangePassword userChangePassword) {
        userAuthenticationProvider.changeUserPassword(userChangePassword);
    }*/
}