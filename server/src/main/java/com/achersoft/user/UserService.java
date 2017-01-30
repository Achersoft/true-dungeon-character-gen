package com.achersoft.user;

import com.achersoft.user.dao.User;
import java.util.List;

public interface UserService {
    public User createUser(User user);
    public List<User> getUsers(); 
    public User getUser(int id);
    public User editUser(User user);
    public void deleteUser(int id);
}
