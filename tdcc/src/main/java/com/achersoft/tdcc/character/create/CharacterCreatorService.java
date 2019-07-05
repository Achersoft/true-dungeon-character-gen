package com.achersoft.tdcc.character.create;

import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.character.dao.CharacterDetails;

public interface CharacterCreatorService {
    public CharacterDetails createCharacter(CharacterClass characterClass, String name) throws Exception;
    public CharacterDetails renameCharacter(String id, String name) throws Exception;
    public CharacterDetails copyCharacter(CharacterClass characterClass, String name, String cloneId) throws Exception;
}
