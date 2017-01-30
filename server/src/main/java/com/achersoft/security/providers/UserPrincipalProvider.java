package com.achersoft.security.providers;

import com.achersoft.security.context.CustomSecurityContext;
import com.achersoft.security.dao.UserPrincipal;
import javax.ws.rs.ext.Provider;
import lombok.Data;

@Provider
@Data
public class UserPrincipalProvider {
    
    private CustomSecurityContext context;
    
    public UserPrincipal getUserPrincipal() {
        return context.getUserPrincipal();
    }
    
    public String getAuthenticationToken() {
        return context.getAuthenticationToken();
    }
    
    public void setAuthenticationToken(String token) {
        context.setAuthenticationToken(token);
    }
}
