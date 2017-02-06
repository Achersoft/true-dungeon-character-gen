package com.achersoft.tdcc.token.persistence;

import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.enums.Slot;
import com.achersoft.tdcc.token.dao.Token;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TokenMapper {
    public List<Token> getNonWeaponSlotItems(@Param("characterId") String characterId, @Param("characterClass") CharacterClass characterClass, @Param("slot") Slot slot);
    public void setTokenSlot(@Param("soltId") String soltId, @Param("tokenId") String tokenId);
}