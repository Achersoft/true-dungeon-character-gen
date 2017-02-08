package com.achersoft.tdcc.token.admin.persistence;

import com.achersoft.tdcc.token.admin.dao.TokenFullDetails;
import java.util.List;


public interface TokenAdminMapper {
    public void addToken(TokenFullDetails token);
    public void addTokenDetails(TokenFullDetails token);
    public void editToken(TokenFullDetails token);
    public void editTokenDetails(TokenFullDetails token);
    public List<TokenFullDetails> search(String name);
    public TokenFullDetails getTokenDetails(String id);
}