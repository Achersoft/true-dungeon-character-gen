package com.achersoft.tdcc.token.admin.persistence;

import com.achersoft.tdcc.token.admin.dao.SlotModifier;
import com.achersoft.tdcc.token.admin.dao.TokenFullDetails;
import java.util.List;
import java.util.Set;


public interface TokenAdminMapper {
    public void addToken(TokenFullDetails token);
    public void addTokenDetails(TokenFullDetails token);
    public void addSlotModifier(SlotModifier token);
    public void editToken(TokenFullDetails token);
    public void editTokenDetails(TokenFullDetails token);
    public void editSlotModifier(SlotModifier token);
    public List<TokenFullDetails> search(String name);
    public TokenFullDetails getTokenDetails(String id);
    public void deleteSlotModifier(String id);
    public Set<SlotModifier> getSlotModifiers(String id);
}