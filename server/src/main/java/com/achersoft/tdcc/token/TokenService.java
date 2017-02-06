package com.achersoft.tdcc.token;

import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.enums.Rarity;
import com.achersoft.tdcc.enums.Slot;
import com.achersoft.tdcc.token.dao.Token;
import java.util.List;

public interface TokenService {
    public List<Token> getSlotItems(String slotId, String characterId, CharacterClass characterClass, Slot slot, Rarity rarity);
}
