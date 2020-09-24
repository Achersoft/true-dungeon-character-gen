package com.achersoft.tdcc.vtd.admin;

import com.achersoft.security.providers.UserPrincipalProvider;
import com.achersoft.tdcc.vtd.admin.dao.VtdRoom;
import com.achersoft.tdcc.vtd.admin.dao.VtdSetting;
import com.achersoft.tdcc.vtd.admin.persistence.VtdAdminMapper;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class VirtualTdAdminServiceImpl implements VirtualTdAdminService {

    private @Inject VtdAdminMapper vtdAdminMapper;
    private @Inject UserPrincipalProvider userPrincipalProvider;

    @Override
    public VtdSetting createAdventure(VtdSetting vtdSetting) {
        vtdSetting.setId(UUID.randomUUID().toString());
        vtdSetting.setLastModified(new Date());

        vtdAdminMapper.addAdventure(vtdSetting);
        vtdAdminMapper.addRoom(VtdRoom.builder().room(1).vtdId(vtdSetting.getId()).build());
        vtdAdminMapper.addRoom(VtdRoom.builder().room(2).vtdId(vtdSetting.getId()).build());
        vtdAdminMapper.addRoom(VtdRoom.builder().room(3).vtdId(vtdSetting.getId()).build());
        vtdAdminMapper.addRoom(VtdRoom.builder().room(4).vtdId(vtdSetting.getId()).build());
        vtdAdminMapper.addRoom(VtdRoom.builder().room(5).vtdId(vtdSetting.getId()).build());
        vtdAdminMapper.addRoom(VtdRoom.builder().room(6).vtdId(vtdSetting.getId()).build());
        vtdAdminMapper.addRoom(VtdRoom.builder().room(7).vtdId(vtdSetting.getId()).build());

        return getAdventure(vtdSetting.getId());
    }

    @Override
    public List<VtdSetting> getAdventures() {
        final List<VtdSetting> adventures = vtdAdminMapper.getAdventures();

        if (adventures != null)
            adventures.forEach(adventure -> adventure.setRooms(vtdAdminMapper.getRooms(adventure.getId())));

        return adventures;
    }

    @Override
    public VtdSetting getAdventure(String id) {
        final VtdSetting adventure = vtdAdminMapper.getAdventure(id);

        if (adventure != null)
            adventure.setRooms(vtdAdminMapper.getRooms(adventure.getId()));

        return adventure;
    }

    @Override
    public VtdSetting updateAdventure(String id, VtdSetting vtdSetting) {
        vtdSetting.setLastModified(new Date());
        vtdAdminMapper.updateAdventure(vtdSetting);
        vtdSetting.getRooms().forEach(vtdRoom -> vtdAdminMapper.updateRoom(vtdRoom));

        return getAdventure(id);
    }

    @Override
    public void deleteAdventure(String id) {
        vtdAdminMapper.deleteAdventure(id);
    }
}
