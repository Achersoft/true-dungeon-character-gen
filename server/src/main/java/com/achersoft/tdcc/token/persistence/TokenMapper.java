package com.achersoft.tdcc.token.persistence;

import com.achersoft.tdcc.enums.Slot;
import com.achersoft.tdcc.token.dao.Token;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TokenMapper {
    public List<Token> getWeaponSlotItems(@Param("slotId") String slotId, @Param("characterId") String characterId, @Param("characterClass") String characterClass, @Param("slot") Slot slot, @Param("rarity") String rarity);
    public List<Token> getNonWeaponSlotItems(@Param("slotId") String slotId, @Param("characterId") String characterId, @Param("characterClass") String characterClass, @Param("slot") Slot slot, @Param("rarity") String rarity);
    public void setTokenSlot(@Param("soltId") String soltId, @Param("tokenId") String tokenId);
    public void unequipTokenSlot(@Param("soltId") String soltId);
}