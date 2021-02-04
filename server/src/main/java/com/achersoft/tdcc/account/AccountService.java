package com.achersoft.tdcc.account;

import com.achersoft.tdcc.account.dao.AccountSettings;

public interface AccountService {
    public AccountSettings getAccountSettings(String id);
    public AccountSettings updateAccountSettings(AccountSettings settings);
}
