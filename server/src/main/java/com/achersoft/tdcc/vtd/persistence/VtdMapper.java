package com.achersoft.tdcc.vtd.persistence;

import com.achersoft.tdcc.character.dao.CharacterStats;
import com.achersoft.tdcc.enums.Buff;
import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.enums.Debuff;
import com.achersoft.tdcc.vtd.dao.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface VtdMapper {
    public void addCharacter(VtdDetails vtdDetails);
    public void addCharacterSkill(CharacterSkill characterSkill);
    public void addCharacterStats(CharacterStats characterStats);
    public void addCharacterBuff(VtdBuff vtdBuff);
    public void addCharacterDebuff(VtdDebuff vtdDebuff);
    public void addPolys(@Param("polyList") List<VtdPoly> polyList);
    public VtdDetails getCharacter(@Param("id") String id);
    public VtdDetails getCharacterByName(@Param("userId") String userId, @Param("name") String name);
    public List<VtdDetails> getCharacters(@Param("userId") String userId);
    public List<CharacterSkill> getSkills(@Param("characterClass") CharacterClass characterClass, @Param("characterLevel") int characterLevel);
    public CharacterSkill getCharacterSkill(@Param("id") String id, @Param("characterId") String characterId);
    public List<CharacterSkill> getCharacterSkills(@Param("characterId") String characterId);
    public List<VtdBuff> getCharacterBuffs(@Param("characterId") String characterId);
    public List<VtdDebuff> getCharacterDebuffs(@Param("characterId") String characterId);
    public List<VtdPoly> getCharacterPolys(@Param("characterId") String characterId);
    public CharacterStats getCharacterStats(@Param("id") String id);
    public boolean buffExists(@Param("characterId") String characterId, @Param("buff") Buff buff);
    public void updateCharacterDr(CharacterStats characterStats);
    public void updateCharacter(VtdDetails vtdDetails);
    public void updateCharacterSkill(CharacterSkill characterSkill);
    public void updateCharacterPoly(VtdPoly poly);
    public void deleteCharacter(@Param("id") String id);
    public void deleteCharacterSkills(@Param("id") String id);
    public void deleteCharacterStats(@Param("id") String id);
    public void deleteCharacterBuffs(@Param("id") String id);
    public void deleteCharacterPolys(@Param("id") String id);
    public void resetCharacterBuffs(@Param("id") String id);
    public void deleteCharacterBuff(@Param("id") String id, @Param("buff") Buff buff);
    public void deleteCharacterDebuffs(@Param("id") String id);
    public void resetCharacterDebuffs(@Param("id") String id);
    public void deleteCharacterDebuff(@Param("id") String id, @Param("debuff") Debuff debuff);
}