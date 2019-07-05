package com.achersoft.security.context;

import com.achersoft.security.dao.UserPrincipal;
import com.achersoft.security.type.Privilege;
import javax.ws.rs.core.SecurityContext;
import lombok.Getter;
import lombok.Setter;

public abstract class CustomSecurityContext implements SecurityContext {
    private @Getter @Setter String authenticationToken;
    private @Getter @Setter UserPrincipal userPrincipal;
    
    public abstract boolean userHasPrivilege(Privilege privilege);
}
