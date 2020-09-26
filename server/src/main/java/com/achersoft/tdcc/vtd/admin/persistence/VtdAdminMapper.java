package com.achersoft.tdcc.vtd.admin.persistence;

import com.achersoft.tdcc.vtd.admin.dao.VtdRoom;
import com.achersoft.tdcc.vtd.admin.dao.VtdSetting;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface VtdAdminMapper {
    public void addAdventure(VtdSetting vtdSetting);
    public void addRoom(VtdRoom vtdRoom);
    public List<VtdSetting> getAdventures();
    public VtdSetting getAdventure(@Param("id") String id);
    public VtdSetting getAdventureByCode(@Param("passcode") String passcode);
    public List<VtdRoom> getRooms(@Param("vtdId") String vtdId);
    public VtdRoom getRoom(@Param("id") String id);
    public List<VtdRoom> getRoomsByNumber(@Param("vtdId") String vtdId, @Param("room") int room);
    public void updateAdventure(VtdSetting vtdSetting);
    public void updateRoom(VtdRoom vtdRoom);
    public void deleteAdventure(@Param("id") String id);
}