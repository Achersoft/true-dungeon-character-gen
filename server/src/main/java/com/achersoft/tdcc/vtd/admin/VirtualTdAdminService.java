package com.achersoft.tdcc.vtd.admin;

import com.achersoft.tdcc.vtd.admin.dao.VtdSetting;
import com.achersoft.tdcc.vtd.dao.VtdDetails;

import java.util.List;

public interface VirtualTdAdminService {
    VtdSetting createAdventure(VtdSetting vtdSetting);
    List<VtdSetting> getAdventures();
    VtdSetting getAdventure(String id);
    VtdSetting updateAdventure(String id, VtdSetting vtdSetting);
    void deleteAdventure(String id);
}
