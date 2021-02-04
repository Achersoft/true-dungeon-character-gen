package com.achersoft.tdcc.account;

import com.achersoft.security.providers.UserPrincipalProvider;
import com.achersoft.tdcc.account.dao.AccountSettings;
import com.achersoft.tdcc.account.persistence.AccountMapper;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

@Transactional(rollbackFor = Exception.class)
public class AccountServiceImpl implements AccountService {

    private @Inject AccountMapper mapper;
    private @Inject UserPrincipalProvider userPrincipalProvider;

    @Override
    public AccountSettings getAccountSettings(String id) {
        final AccountSettings accountSettings = mapper.getAccountSettings(id);

        if (accountSettings != null)
            return accountSettings;

        final AccountSettings settings = AccountSettings.builder().id(id).interactive(true).settings("").build();

        mapper.createAccountSettings(settings);

        return settings;
    }

    @Override
    public AccountSettings updateAccountSettings(AccountSettings settings) {
        return null;
    }
}
