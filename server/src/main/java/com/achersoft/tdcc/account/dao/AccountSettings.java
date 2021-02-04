package com.achersoft.tdcc.account.dao;

import com.achersoft.security.type.Privilege;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSettings {
    private String id;
    private boolean interactive;
    private String settings;
}
