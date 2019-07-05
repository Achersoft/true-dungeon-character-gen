package com.achersoft.user;

import com.achersoft.user.dao.ChangePassword;
import com.achersoft.user.dao.ResetPassword;
import com.achersoft.user.dao.User;
import java.util.List;

public interface UserService {
    public User createUser(User user);
    public List<User> getUsers(); 
    public User getUser(String id);
    public User editUser(User user);
    public void resetPassword(ResetPassword resetPassword) throws Exception;
    public void resetChangePassword(ChangePassword changePassword) throws Exception;
    public void changePassword(ChangePassword changePassword) throws Exception;
    public void deleteUser(String id);
}
