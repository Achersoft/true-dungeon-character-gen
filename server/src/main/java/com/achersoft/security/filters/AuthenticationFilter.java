package com.achersoft.security.filters;

import com.achersoft.exception.AuthenticationException;
import com.achersoft.exception.SystemError;
import com.achersoft.security.UserAuthenticationService;
import com.achersoft.security.context.CustomSecurityContext;
import com.achersoft.security.type.Privilege;
import java.io.IOException;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter, ContainerResponseFilter {
    
    private static final String TOKEN_AUTH = "Token";
    private static final String AUTH_HEADER = "Authorization";
    
    private @Inject UserAuthenticationService userAuthenticationServiceProvider; 

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        requestContext.setSecurityContext(new CustomSecurityContext() {
            {
                setAuthenticationToken(getAuthToken(requestContext));
                setUserPrincipal(userAuthenticationServiceProvider.getUserPrincipal(getAuthenticationToken()));
            }
            
            @Override
            public boolean userHasPrivilege(Privilege privilege) {
                if(getUserPrincipal() != null && getUserPrincipal().getPrivileges() != null) {
                    setAuthenticationToken(userAuthenticationServiceProvider.authenticate(getAuthenticationToken()));
                    return getUserPrincipal().getPrivileges().contains(privilege);
                }
                throw new AuthenticationException("Invalid session.");
            }

            @Override
            public boolean isUserInRole(String role) {
                throw new AuthenticationException("Invalid session.");
            }

            @Override
            public boolean isSecure() {
                return true;
            }

            @Override
            public String getAuthenticationScheme() {
                if(requestContext.getHeaderString(AUTH_HEADER) != null) 
                    return requestContext.getHeaderString(AUTH_HEADER).trim().split(" ")[0];
                return null;
            }
        });
    } 

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        if(requestContext.getSecurityContext() instanceof CustomSecurityContext){
            if(!requestContext.getUriInfo().getPath().equals("logout")) {
                String authenticationToken = ((CustomSecurityContext)requestContext.getSecurityContext()).getAuthenticationToken();
                if(authenticationToken != null)
                    responseContext.getHeaders().putSingle(AUTH_HEADER, TOKEN_AUTH + " " + authenticationToken);
            }
        }
    }
    
    private String getAuthToken(final ContainerRequestContext requestContext) {
        if(requestContext.getHeaderString(AUTH_HEADER) != null) {
            String[] authHeader = requestContext.getHeaderString(AUTH_HEADER).trim().split(" ");
            if(authHeader.length == 2 && authHeader[0].equals(TOKEN_AUTH))        
                return authHeader[1];
        }
        return null;
    }
}
