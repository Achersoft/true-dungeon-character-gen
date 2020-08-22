package com.achersoft.tdcc.vtd;

import com.achersoft.tdcc.enums.Buff;
import com.achersoft.tdcc.vtd.dao.VtdBuff;
import com.achersoft.tdcc.vtd.dao.VtdDetails;

public interface VirtualTdService {
    VtdDetails getVtdCharacter(String id, boolean reset);
    VtdDetails modifyDifficulty(String id, int difficulty);
    VtdDetails setBonusInit(String id, int init);
    VtdDetails setBonusHealth(String id, int health);
    VtdDetails modifyHealth(String id, int health);
    VtdDetails useSkill(String id, String skillId, boolean selfTarget, int selfHeal, boolean madEvoker);
    VtdDetails unuseSkill(String id, String skillId);
    VtdDetails addBuff(String id, Buff buff);
    VtdDetails removeBuff(String id, Buff buff);
    VtdDetails nextRoom(String id);
    void resetCharacter(String id);
}
