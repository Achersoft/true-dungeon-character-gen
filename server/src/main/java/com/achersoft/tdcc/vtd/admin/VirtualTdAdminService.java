package com.achersoft.tdcc.vtd.admin;

import com.achersoft.tdcc.vtd.admin.dao.VtdSetting;
import com.achersoft.tdcc.vtd.dao.VtdDetails;

import java.util.List;

public interface VirtualTdAdminService {
    List<VtdSetting> getAdventures();
    VtdSetting getAdventure(String id);
    VtdSetting updateAdventure(String id, VtdSetting vtdSetting);
    VtdDetails deleteAdventure(String id);
}
