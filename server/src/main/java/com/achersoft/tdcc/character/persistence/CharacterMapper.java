package com.achersoft.tdcc.character.persistence;

import com.achersoft.tdcc.character.dao.CharacterDetails;
import com.achersoft.tdcc.character.dao.CharacterStats;
import com.achersoft.tdcc.enums.CharacterClass;
import org.apache.ibatis.annotations.Param;

public interface CharacterMapper {   
    public void addCharacter(CharacterDetails characterDetails);
    public void addCharacterStats(CharacterStats characterStats);
    public void addCharacterItems(CharacterStats characterStats);
    
    
    public CharacterStats getStartingStats( @Param("characterClass") CharacterClass characterClass, @Param("level") int level);
}