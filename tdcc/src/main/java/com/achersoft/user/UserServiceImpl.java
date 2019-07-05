package com.achersoft.user;

import com.achersoft.email.EmailClient;
import com.achersoft.exception.AuthenticationException;
import com.achersoft.exception.InvalidDataException;
import com.achersoft.exception.SystemError;
import com.achersoft.security.dto.UserLoginRequest;
import com.achersoft.security.helpers.PasswordHelper;
import com.achersoft.security.type.Privilege;
import com.achersoft.user.dao.ChangePassword;
import com.achersoft.user.dao.ResetPassword;
import com.achersoft.user.dao.User;
import com.achersoft.user.persistence.UserMapper;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import org.springframework.transaction.annotation.Transactional;

@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl implements UserService {

    private @Inject UserMapper userMapper;
    private @Inject EmailClient emailClient;

    @Override
    public User createUser(User user) {
        user.setUsername(user.getUsername().toLowerCase());
        if(user.getPassword() == null || user.getPassword().isEmpty())
            throw new InvalidDataException("A user password must be provided.");
        if(user.getEmail() == null || user.getEmail().isEmpty() || !isValidEmailAddress(user.getEmail()))
            throw new InvalidDataException("A valid email must be provided.");
        if(userMapper.emailExists(user.getEmail()))
            throw new InvalidDataException("Email address has already been associated to an account.");
        if(userMapper.userExists(user.getUsername()))
            throw new InvalidDataException("Username is not available.");
        
        // Salt the user password before storage
        user.setPassword(PasswordHelper.generatePasswordHash(user.getPassword()));
        
        user.setId(UUID.randomUUID().toString());
        user.setLocked(Boolean.FALSE);
        user.setLoginAttempts(0);
        user.setLastAccessed(new Date());
        user.setPrivileges(Arrays.asList(Privilege.SYSTEM_USER));
        
        userMapper.createUser(user);
        user.getPrivileges().stream().forEach((priv) -> {
            userMapper.addUserPrivilege(user.getId(), priv);
        });

        return user;
    }
    
    @Override
    public List<User> getUsers() {
        return userMapper.getUsers();
    }

    @Override
    public User getUser(String id) {
        User user = userMapper.getUser(id);
        user.setPrivileges(userMapper.getUserPrivileges(id));
        return user;
    }

    @Override
    public User editUser(User user) {
        userMapper.editUser(user);
        userMapper.removeUserPrivileges(user.getId());
        user.getPrivileges().stream().forEach((priv) -> {
            userMapper.addUserPrivilege(user.getId(), priv);
        });
        return getUser(user.getId());
    }

    @Override
    public void deleteUser(String id) {
        userMapper.deleteUser(id);
    }
    
    @Override
    public void resetPassword(ResetPassword resetPassword) throws Exception {
        User user = userMapper.getUserFromName(resetPassword.getUsername());
        if(user == null || !user.getEmail().equalsIgnoreCase(user.getEmail()))
            throw new InvalidDataException("No accounts found matching username and email.");
        user.setPasswordResetId(UUID.randomUUID().toString());
        userMapper.editUser(user);
        emailClient.sendEmail(user.getEmail(), 
                              "TD Character Creator - Reset Password Request", 
                              "Please use the following link to <a href=\"http://tdcharactercreator.com/#/password/reset/" + user.getPasswordResetId() + "\">reset your password</a>" + 
                              "<br><br>If you did not request this password change feel free to ignore it. <br><br> Regards, <br>TD Character Creator Admin");
    }
    
    @Override
    public void resetChangePassword(ChangePassword changePassword) throws Exception {
        changePassword.setNewPassword(PasswordHelper.generatePasswordHash(changePassword.getNewPassword()));
        userMapper.resetPassword(changePassword.getResetId(), changePassword.getNewPassword());
    }
    
    @Override
    public void changePassword(ChangePassword changePassword) throws Exception {
        changePassword.setCurrentPassword(PasswordHelper.generatePasswordHash(changePassword.getCurrentPassword()));
        changePassword.setNewPassword(PasswordHelper.generatePasswordHash(changePassword.getNewPassword()));
        if(userMapper.validateCredentials(UserLoginRequest.builder().userName(changePassword.getUsername()).password(changePassword.getCurrentPassword()).build()))
            userMapper.changePassword(changePassword.getUsername(), changePassword.getNewPassword());
        else
            throw new AuthenticationException("Invalid credentials.");
    }
    
    private boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }
}
