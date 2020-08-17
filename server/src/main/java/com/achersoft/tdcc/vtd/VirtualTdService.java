package com.achersoft.tdcc.vtd;

import com.achersoft.tdcc.enums.Buff;
import com.achersoft.tdcc.vtd.dao.VtdBuff;
import com.achersoft.tdcc.vtd.dao.VtdDetails;

public interface VirtualTdService {
    VtdDetails getVtdCharacter(String id);
    VtdDetails useSkill(String id, String skillId, boolean selfTarget);
    VtdDetails unuseSkill(String id, String skillId);
    VtdDetails addBuff(String id, Buff buff);
    VtdDetails removeBuff(String id, Buff buff);
    VtdDetails nextRoom(String id);
    void resetCharacter(String id);
}
