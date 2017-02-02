package com.achersoft.tdcc.token.admin;

import com.achersoft.tdcc.token.admin.dao.TokenFullDetails;
import java.util.List;

public interface TokenAdminService {
    public void addToken(TokenFullDetails token);
    public void editToken(TokenFullDetails token);
    public List<TokenFullDetails> search(String name);
}
