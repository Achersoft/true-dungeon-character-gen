package com.achersoft.tdcc.vtd.persistence;

import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.vtd.dao.CharacterSkill;
import com.achersoft.tdcc.vtd.dao.VtdDetails;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface VtdMapper {
    public void addCharacter(VtdDetails vtdDetails);
    public void addCharacterSkill(CharacterSkill characterSkill);
    public VtdDetails getCharacter(@Param("id") String id);
    public List<CharacterSkill> getSkills(@Param("characterClass") CharacterClass characterClass, @Param("characterLevel") int characterLevel);
    public List<CharacterSkill> getCharacterSkills(@Param("characterId") String characterId);
    public void useCharacterSkill(@Param("id") String id);
    public void deleteCharacter(@Param("id") String id);
    public void deleteCharacterSkills(@Param("id") String id);
}