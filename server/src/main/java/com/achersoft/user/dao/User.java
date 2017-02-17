package com.achersoft.user.dao;

import com.achersoft.security.type.Privilege;
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
public class User {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Boolean locked;
    private Integer loginAttempts;
    private Date lastAccessed;
    private List<Privilege> privileges;
}
