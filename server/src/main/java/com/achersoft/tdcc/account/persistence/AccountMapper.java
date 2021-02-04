package com.achersoft.tdcc.account.persistence;

import com.achersoft.security.dto.UserLoginRequest;
import com.achersoft.security.type.Privilege;
import com.achersoft.tdcc.account.dao.AccountSettings;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AccountMapper {
    public void createAccountSettings(AccountSettings user);
    public AccountSettings getAccountSettings(String id);
    public void editAccountSettings(AccountSettings user);
}

