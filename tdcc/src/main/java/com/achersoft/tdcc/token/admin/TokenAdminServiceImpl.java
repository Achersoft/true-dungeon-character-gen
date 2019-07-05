package com.achersoft.tdcc.token.admin;

import com.achersoft.tdcc.token.admin.dao.TokenFullDetails;
import com.achersoft.tdcc.token.admin.persistence.TokenAdminMapper;
import java.util.List;
import javax.inject.Inject;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.transaction.annotation.Transactional;

@Transactional(rollbackFor = Exception.class)
public class TokenAdminServiceImpl implements TokenAdminService {

    private @Inject TokenAdminMapper mapper;
    
    @Override
    public void addToken(TokenFullDetails token) {
        token.setId(DigestUtils.sha1Hex(token.getName()));
        mapper.addToken(token);
        mapper.addTokenDetails(token);
    }
    
    @Override
    public void editToken(TokenFullDetails token) {
        mapper.editToken(token);
        mapper.editTokenDetails(token);
    }
    
    @Override
    public List<TokenFullDetails> search(String name) {
        return mapper.search(name);
    }
}
