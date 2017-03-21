package com.achersoft.tdcc.token;

import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.enums.Rarity;
import com.achersoft.tdcc.enums.Slot;
import com.achersoft.tdcc.token.dao.Token;
import com.achersoft.tdcc.token.persistence.TokenMapper;
import java.util.List;
import javax.inject.Inject;

public class TokenServiceImpl implements TokenService{

    private @Inject TokenMapper mapper;
    
    @Override
    public List<Token> getSlotItems(String slotId, String characterId, CharacterClass characterClass, Slot slot, Rarity rarity) {
        if(slot == Slot.MAINHAND || slot == Slot.OFFHAND || slot == Slot.RANGE_MAINHAND || slot == Slot.RANGE_OFFHAND)
            return mapper.getWeaponSlotItems(slotId, characterId, characterClass.name(), slot, rarity.name());
        if(slot == Slot.RUNESTONE && mapper.getTokenSlotIndex(slotId) > 0)
            return mapper.getNonWeaponSlotItems(slotId, characterId, characterClass.name(), slot.name(), Rarity.RARE.name());
        return mapper.getNonWeaponSlotItems(slotId, characterId, characterClass.name(), slot.name(), rarity.name());
    }
    
}
 