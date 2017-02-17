package com.achersoft.user;

import com.achersoft.exception.InvalidDataException;
import com.achersoft.security.helpers.PasswordHelper;
import com.achersoft.security.type.Privilege;
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

    @Override
    public User createUser(User user) {
        if(user.getPassword() == null || user.getPassword().isEmpty())
            throw new InvalidDataException("A user password must be provided.");
        if(user.getEmail() == null || user.getEmail().isEmpty() || !isValidEmailAddress(user.getEmail()))
            throw new InvalidDataException("A valid email must be provided.");
  
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
    
    private boolean isValidEmailAddress(String email) {
           String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
           java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
           java.util.regex.Matcher m = p.matcher(email);
           return m.matches();
    }
}
