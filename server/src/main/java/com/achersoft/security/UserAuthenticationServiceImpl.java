package com.achersoft.security;

import com.adobe.xmp.impl.Base64;
import com.achersoft.exception.AuthenticationException;
import com.achersoft.security.authenticator.Authenticator;
import com.achersoft.security.dao.UserPrincipal;
import com.achersoft.security.providers.SignatureServiceProvider;
import com.achersoft.security.providers.UserPrincipalProvider;
import com.achersoft.user.dao.User;
import com.achersoft.security.dto.UserLoginRequest;
import com.achersoft.user.persistence.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import javax.annotation.Resource;
import javax.inject.Inject;

public class UserAuthenticationServiceImpl implements UserAuthenticationService {
    
    private @Inject Authenticator authenticator;
    private @Inject UserPrincipalProvider userPrincipalProvider;
    private @Inject UserMapper userMapper;
    private @Resource(name = "userMap") Cache<String, String> userMap;
    private @Inject SignatureServiceProvider signatureServiceProvider;
    
    @Override
    public void login(UserLoginRequest request) {
        request.setUserName(request.getUserName().toLowerCase());
        User user = userMapper.getUserFromName(request.getUserName());
        if(user == null)
            userMapper.getUserFromEmail(request.getUserName());
        if(user == null)
            throw new AuthenticationException("Invalid credentials.");
        if(user.getLocked()) {
            if(user.getLastAccessed() != null && user.getLastAccessed().before(new Date(new Date().getTime() - 1800000))) {
                user.setLoginAttempts(0);
                user.setLocked(Boolean.FALSE);
            } else {
                throw new AuthenticationException("User account has been locked.");
            }
        }

        user.setLastAccessed(new Date());
        userMapper.editUser(user);
        authenticator.validateUser(request);
        
        userPrincipalProvider.getUserPrincipal().setSessionId(UUID.randomUUID().toString());
        userPrincipalProvider.getUserPrincipal().setIat(new Date());
        userPrincipalProvider.getUserPrincipal().setExp(new Date());
        userPrincipalProvider.getUserPrincipal().setSub(user.getId());
        userPrincipalProvider.getUserPrincipal().setUserName(user.getUsername());
        userPrincipalProvider.getUserPrincipal().setName(user.getFirstName() + " " + user.getLastName());
        userPrincipalProvider.getUserPrincipal().setPrivileges(userMapper.getUserPrivileges(user.getId()));
        userPrincipalProvider.setAuthenticationToken(generateToken(userPrincipalProvider.getUserPrincipal()));
        
        userMap.put(request.getUserName(), userPrincipalProvider.getAuthenticationToken());
    }
    
    @Override
    public void login(User user) {
        userPrincipalProvider.getUserPrincipal().setSessionId(UUID.randomUUID().toString());
        userPrincipalProvider.getUserPrincipal().setIat(new Date());
        userPrincipalProvider.getUserPrincipal().setExp(new Date());
        userPrincipalProvider.getUserPrincipal().setSub(user.getId());
        userPrincipalProvider.getUserPrincipal().setUserName(user.getUsername());
        userPrincipalProvider.getUserPrincipal().setName(user.getFirstName() + " " + user.getLastName());
        userPrincipalProvider.getUserPrincipal().setPrivileges(userMapper.getUserPrivileges(user.getId()));
        userPrincipalProvider.setAuthenticationToken(generateToken(userPrincipalProvider.getUserPrincipal()));
        
        userMap.put(user.getUsername(), userPrincipalProvider.getAuthenticationToken());
    }
    
    @Override
    public void logout() {
        if(userPrincipalProvider.getUserPrincipal() != null && userPrincipalProvider.getUserPrincipal().getUserName() != null) {
            userMap.invalidate(userPrincipalProvider.getUserPrincipal().getUserName()); 
        }
    }

    @Override
    public UserPrincipal getUserPrincipal(String token) { 
        if(token != null && !token.isEmpty()) {
            String[] authToken = token.split("\\.");
            if(authToken.length == 3) {
                try {
                    return new ObjectMapper().readValue(Base64.decode(authToken[1]), UserPrincipal.class);
                } catch (IOException ex) {}
            }
        }
        return new UserPrincipal();
    }

    @Override
    public String authenticate(String token) throws AuthenticationException{
        UserPrincipal userPrincipal = getUserPrincipal(token);
        
        if(userMap.getIfPresent(userPrincipal.getUserName()) == null) 
            throw new AuthenticationException("Invalid session.");
     
        if(!token.equals(generateToken(userPrincipal))) 
            throw new AuthenticationException("Invalid session.");
        
        /*if(new Date().getTime() > userPrincipal.getExp().getTime()) 
            throw new AuthenticationException(SystemError.SESSION_TIMEOUT, "User session has expired.");*/
        
        userPrincipal.setExp(new Date(new Date().getTime() + 43200000));

        return generateToken(userPrincipal);
    }

    private String generateToken(UserPrincipal userPrincipal) {
        try {
            String token = Base64.encode("{\"typ\":\"JWT\",\"alg\":\"HS256\"}");
            token += "." +  Base64.encode(new ObjectMapper().writeValueAsString(userPrincipal));
            return token + "." + new String(Base64.encode(signatureServiceProvider.sign(token.getBytes("UTF-8"))));
        } catch (Exception e) {
            throw new AuthenticationException("Failed to generate authentication token");
        }
    }
}
