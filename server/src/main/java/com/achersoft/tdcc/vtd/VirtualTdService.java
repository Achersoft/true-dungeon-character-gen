package com.achersoft.tdcc.vtd;

import com.achersoft.tdcc.character.dao.CharacterName;
import com.achersoft.tdcc.enums.Buff;
import com.achersoft.tdcc.enums.Debuff;
import com.achersoft.tdcc.enums.InGameEffect;
import com.achersoft.tdcc.party.dao.SelectableCharacters;
import com.achersoft.tdcc.vtd.dao.VtdBuff;
import com.achersoft.tdcc.vtd.dao.VtdDetails;

import java.util.List;

public interface VirtualTdService {
    List<CharacterName> getSelectableCharacters();
    List<CharacterName> getPregeneratedCharacters();
    List<CharacterName> importCharacter(String id);
    VtdDetails getVtdCharacter(String id, boolean reset, boolean activatePrestige);
    VtdDetails modifyDifficulty(String id, int difficulty);
    VtdDetails setBonusInit(String id, int init);
    VtdDetails setBonusHealth(String id, int health);
    VtdDetails setBonusBraceletCabal(String id, int bonus);
    VtdDetails setBonusGloveCabal(String id, int bonus);
    VtdDetails setBonusCharmCabal(String id, int bonus);
    VtdDetails modifyHealth(String id, int health);
    VtdDetails useSkill(String id, String skillId, boolean selfTarget, int selfHeal, boolean madEvoker, int lohNumber, InGameEffect inGameEffect, boolean markUse, boolean ignoreUse);
    VtdDetails queueSkill(String id, String skillId, boolean selfTarget, int selfHeal, boolean madEvoker, int lohNumber, InGameEffect inGameEffect, boolean markUse, boolean ignoreUse, int damage);
    VtdDetails execSkillQueue(String id);
    VtdDetails unuseSkill(String id, String skillId);
    VtdDetails addBuff(String id, Buff buff, int level);
    VtdDetails addDebuff(String id, Debuff debuff);
    VtdDetails removeBuff(String id, Buff buff);
    VtdDetails removeDebuff(String id, Debuff debuff);
    VtdDetails removeEffect(String id, InGameEffect inGameEffect);
    VtdDetails previousRoom(String id);
    VtdDetails nextRoom(String id);
    VtdDetails setPoly(String id, String polyId);
    VtdDetails setCompanion(String id, String polyId);
    VtdDetails setAdventure(String id, String passcode);
    VtdDetails setRollerId(String id, String rollerId);
    VtdDetails resetCharacter(String id);
    VtdDetails deleteQueuedSkills(String id);
    List<CharacterName> deleteCharacter(String id);
    VtdDetails activatePrestigeClass(String id);
}
