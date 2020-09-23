package com.achersoft.tdcc.vtd.admin;

import com.achersoft.security.providers.UserPrincipalProvider;
import com.achersoft.tdcc.vtd.admin.dao.VtdSetting;
import com.achersoft.tdcc.vtd.admin.persistence.VtdAdminMapper;
import com.achersoft.tdcc.vtd.dao.VtdDetails;

import javax.inject.Inject;
import java.util.List;

public class VirtualTdAdminServiceImpl implements VirtualTdAdminService {

    private @Inject VtdAdminMapper vtdAdminMapper;
    private @Inject UserPrincipalProvider userPrincipalProvider;

    @Override
    public List<VtdSetting> getAdventures() {
        return null;
    }

    @Override
    public VtdSetting getAdventure(String id) {
        return null;
    }

    @Override
    public VtdSetting updateAdventure(String id, VtdSetting vtdSetting) {
        return null;
    }

    @Override
    public VtdDetails deleteAdventure(String id) {
        return null;
    }
}
