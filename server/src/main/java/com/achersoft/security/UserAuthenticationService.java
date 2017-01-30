package com.achersoft.security;

import com.achersoft.exception.AuthenticationException;
import com.achersoft.security.dao.UserPrincipal;
import com.achersoft.security.dto.UserLoginRequest;

public interface UserAuthenticationService {
    public void login(UserLoginRequest request);
    public void logout();
   // public void changeUserPassword(UserChangePassword userChangePassword);
    public UserPrincipal getUserPrincipal(String token);
    public String authenticate(String token) throws AuthenticationException;
}
