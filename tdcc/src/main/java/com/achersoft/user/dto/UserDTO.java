package com.achersoft.user.dto;

import com.achersoft.security.type.Privilege;
import com.achersoft.user.dao.User;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String id;
    private @NotNull @NotEmpty String username;
    private @NotNull @NotEmpty String firstName;
    private @NotNull @NotEmpty String lastName;
    private @NotNull @NotEmpty String email;
    private String password;
    private Boolean locked;
    private Integer loginAttempts;
    private Date lastAccessed;
    private Boolean admin;
    private Boolean systemUser;
    
    public static UserDTO fromDAO(User dao){
        return UserDTO.builder()
                .id(dao.getId())
                .username(dao.getUsername())
                .firstName(dao.getFirstName())
                .lastName(dao.getLastName())  
                .email(dao.getEmail())
                .locked(dao.getLocked())
                .loginAttempts(dao.getLoginAttempts())
                .lastAccessed(dao.getLastAccessed())
                .admin((dao.getPrivileges()!= null)?dao.getPrivileges().contains(Privilege.ADMIN):false)
                .systemUser((dao.getPrivileges()!= null)?dao.getPrivileges().contains(Privilege.SYSTEM_USER):false)
                .build();
    }
    
    public User toDAO(){
        List<Privilege> privileges = new ArrayList();
        if(admin != null && admin)
            privileges.add(Privilege.ADMIN);
        if(systemUser != null && systemUser)
            privileges.add(Privilege.SYSTEM_USER);
        return User.builder()
                .id(id)
                .username(username)
                .firstName(firstName)
                .lastName(lastName) 
                .email(email)
                .password(password)
                .locked(locked)
                .loginAttempts(loginAttempts)
                .lastAccessed(lastAccessed)
                .privileges(privileges)
                .build();
    }
}
