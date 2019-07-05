package com.achersoft.security.authenticator;

import com.achersoft.configuration.PropertiesManager;
import com.achersoft.exception.AuthenticationException;
import com.achersoft.exception.SystemError;
import com.achersoft.security.helpers.PasswordHelper;
import com.achersoft.user.dao.User;
import com.achersoft.security.dto.UserLoginRequest;
import com.achersoft.user.persistence.UserMapper;
import javax.inject.Inject;

public class Authenticator {
    
    private @Inject UserMapper userMapper;
    private @Inject PropertiesManager properties;

    public void validateUser(UserLoginRequest request) throws AuthenticationException {
        User user = userMapper.getUserFromName(request.getUserName());
        request.setPassword(PasswordHelper.generatePasswordHash(request.getPassword()));
        if(!userMapper.validateCredentials(request)) {
            if(user.getLoginAttempts() >= properties.getMaxLoginAttempts())
                user.setLocked(Boolean.TRUE);
            user.setLoginAttempts(user.getLoginAttempts()+1);
            userMapper.editUser(user);
            throw new AuthenticationException("Invalid credentials.");
        }
        user.setLoginAttempts(0);
        user.setPasswordResetId(null);
        userMapper.editUser(user);
    }

   /* public void changeUserPassword(UserChangePassword userChangePassword) throws AuthenticationException {
        Boolean validateCredentials = userAuthenticationMapper.validateCredentials(UserLoginRequest.builder()
                .userName(userChangePassword.getUserName())
                .password(PasswordHelper.generatePasswordHash(userChangePassword.getOldPassword()))
                .build());
        
        if(!validateCredentials)
            throw new AuthenticationException(EstaffError.USER_BAD_CREDENTIALS, "Invalid credentials.");
        
        User user = userMapper.getUserFromName(userChangePassword.getUserName());
        user.setPassword(PasswordHelper.generatePasswordHash(userChangePassword.getNewPassword()));
        user.setExpires(new Date(new Date().getTime() + properties.getProperties().getPasswordExpireTime()));
        userMapper.editUser(user);
    }*/
}
