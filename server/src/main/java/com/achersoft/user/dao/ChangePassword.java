package com.achersoft.user.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePassword {
    private String resetId;
    private String username;
    private String currentPassword;
    private String newPassword;
}
