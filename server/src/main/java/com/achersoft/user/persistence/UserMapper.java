package com.achersoft.user.persistence;

import com.achersoft.security.dto.UserLoginRequest;
import com.achersoft.security.type.Privilege;
import com.achersoft.user.dao.User;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {   
    public void createUser(User user);
    public List<User> getUsers();
    public User getUser(String id);
    public User getUserFromName(String userName);
    public List<Privilege> getUserPrivileges(String id);
    public void addUserPrivilege(@Param("id") String id, @Param("privilege")Privilege privilege);
    public void removeUserPrivileges(@Param("id") String id);
    public void editUser(User user);
    public void resetPassword(@Param("resetId") String resetId, @Param("password") String password);
    public void changePassword(@Param("username") String username, @Param("password") String password);
    public void deleteUser(String id);
    public boolean validateCredentials(UserLoginRequest request);
}

