package com.achersoft.tdcc.character.persistence;

import com.achersoft.tdcc.character.dao.CharacterDetails;
import com.achersoft.tdcc.character.dao.CharacterItem;
import com.achersoft.tdcc.character.dao.CharacterStats;
import com.achersoft.tdcc.enums.CharacterClass;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CharacterMapper {   
    public void addCharacter(CharacterDetails characterDetails);
    public void addCharacterStats(CharacterStats characterStats); 
    public void addCharacterItems(@Param("characterItems") List<CharacterItem> characterItem);
    public CharacterDetails getCharacter(@Param("id") String id, @Param("userId") String userId);
    public CharacterStats getCharacterStats(@Param("id") String id);
    public List<CharacterItem> getCharacterItems(@Param("id") String id);
    public CharacterStats getStartingStats(@Param("characterClass") CharacterClass characterClass, @Param("level") int level);
}