package com.achersoft.tdcc.character.persistence;

import com.achersoft.tdcc.character.dao.CharacterDetails;
import com.achersoft.tdcc.character.dao.CharacterItem;
import com.achersoft.tdcc.character.dao.CharacterName;
import com.achersoft.tdcc.character.dao.CharacterNote;
import com.achersoft.tdcc.character.dao.CharacterStats;
import com.achersoft.tdcc.enums.CharacterClass;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CharacterMapper {   
    public void addCharacter(CharacterDetails characterDetails);
    public void addCharacterStats(CharacterStats characterStats); 
    public void addCharacterItems(@Param("characterItems") List<CharacterItem> characterItem);
    public void addCharacterNotes(@Param("id") String id, @Param("notes") List<CharacterNote> notes);
    public int getCharacterCount(@Param("userId") String userId);
    public CharacterDetails getCharacter(@Param("id") String id);
    public List<CharacterName> getCharacters(@Param("userId") String userId);
    public List<CharacterName> getCharactersClass(@Param("userId") String userId, @Param("characterClass") CharacterClass characterClass);
    public CharacterStats getCharacterStats(@Param("id") String id);
    public List<CharacterItem> getCharacterItems(@Param("id") String id);
    public List<CharacterNote> getCharacterNotes(@Param("id") String id);
    public CharacterStats getStartingStats(@Param("characterClass") CharacterClass characterClass, @Param("level") int level);
    public void updateCharacterStats(CharacterStats characterStats); 
    public void deleteCharacter(@Param("id") String id, @Param("userId") String userId);
    public void deleteCharacterItems(@Param("id") String id);
    public void deleteCharacterNotes(@Param("id") String id);
}