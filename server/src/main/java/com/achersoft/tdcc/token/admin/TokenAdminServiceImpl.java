package com.achersoft.tdcc.token.admin;

import com.achersoft.tdcc.token.admin.dao.SlotModifier;
import com.achersoft.tdcc.token.admin.dao.TokenFullDetails;
import com.achersoft.tdcc.token.admin.persistence.TokenAdminMapper;
import java.util.List;
import java.util.stream.Collectors;
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
        if (token.getSlotModifiers() != null) {
            for (SlotModifier slotModifier : token.getSlotModifiers())
                mapper.addSlotModifier(slotModifier);
        }
    }
    
    @Override
    public void editToken(TokenFullDetails token) {
        mapper.editToken(token);
        mapper.editTokenDetails(token);
        mapper.deleteSlotModifier(token.getId());
        if (token.getSlotModifiers() != null) {
            for (SlotModifier slotModifier : token.getSlotModifiers())
                mapper.addSlotModifier(slotModifier);
        }
    }
    
    @Override
    public List<TokenFullDetails> search(String name) {
        return mapper.search(name).stream().peek(td -> td.setSlotModifiers(mapper.getSlotModifiers(td.getId()))).collect(Collectors.toList());
    }
}
