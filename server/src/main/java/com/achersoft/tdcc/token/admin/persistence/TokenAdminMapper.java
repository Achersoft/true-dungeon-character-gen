package com.achersoft.tdcc.token.admin.persistence;

import com.achersoft.tdcc.token.admin.dao.TokenFullDetails;


public interface TokenAdminMapper {
    public void addToken(TokenFullDetails token);
    public void addTokenDetails(TokenFullDetails token);
}