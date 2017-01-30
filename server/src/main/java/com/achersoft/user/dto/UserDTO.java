package com.achersoft.user.dto;

import com.achersoft.security.type.Privilege;
import com.achersoft.user.dao.User;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private int id;
    private String username;
    private String firstName;
    private String lastName;
    private String password;
    private Boolean locked;
    private Integer loginAttempts;
    private Date lastAccessed;
    private Boolean admin;
    private Boolean customer;
    private Boolean employee;
    
    public static UserDTO fromDAO(User dao){
        return UserDTO.builder()
                .id(dao.getId())
                .username(dao.getUsername())
                .firstName(dao.getFirstName())
                .lastName(dao.getLastName())  
                .locked(dao.getLocked())
                .loginAttempts(dao.getLoginAttempts())
                .lastAccessed(dao.getLastAccessed())
                .admin((dao.getPrivileges()!= null)?dao.getPrivileges().contains(Privilege.ADMIN):false)
                .customer((dao.getPrivileges()!= null)?dao.getPrivileges().contains(Privilege.CUSTOMER):false)
                .employee((dao.getPrivileges()!= null)?dao.getPrivileges().contains(Privilege.EMPLOYEE):false)
                .build();
    }
    
    public User toDAO(){
        List<Privilege> privileges = new ArrayList();
        if(admin != null && admin)
            privileges.add(Privilege.ADMIN);
        if(customer != null && customer)
            privileges.add(Privilege.CUSTOMER);
        if(employee != null && employee)
            privileges.add(Privilege.EMPLOYEE);
        return User.builder()
                .id(id)
                .username(username)
                .firstName(firstName)
                .lastName(lastName)  
                .password(password)
                .locked(locked)
                .loginAttempts(loginAttempts)
                .lastAccessed(lastAccessed)
                .privileges(privileges)
                .build();
    }
}
