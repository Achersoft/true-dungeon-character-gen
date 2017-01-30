package com.achersoft.security.filters;

import com.achersoft.security.context.CustomSecurityContext;
import com.achersoft.security.providers.UserPrincipalProvider;
import java.io.IOException;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.USER)
public class UserContextFilter implements ContainerRequestFilter {
   
    private @Inject UserPrincipalProvider userPrincipalProvider;

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        userPrincipalProvider.setContext((CustomSecurityContext)requestContext.getSecurityContext());
    } 
}
