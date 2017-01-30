package com.achersoft.tdcc.token.admin;

import com.achersoft.tdcc.token.admin.dao.TokenFullDetails;
import com.achersoft.tdcc.token.admin.persistence.TokenAdminMapper;
import javax.inject.Inject;
import org.apache.commons.codec.digest.DigestUtils;

public class TokenAdminServiceImpl implements TokenAdminService {

    private @Inject TokenAdminMapper mapper;
    
    @Override
    public void addToken(TokenFullDetails token) {
        token.setId(DigestUtils.sha1Hex(token.getName()));
        mapper.addToken(token);
        mapper.addTokenDetails(token);
    }
    
}
