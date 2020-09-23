package com.achersoft.tdcc.vtd.admin.persistence;

import com.achersoft.tdcc.vtd.admin.dao.VtdRoom;
import com.achersoft.tdcc.vtd.admin.dao.VtdSetting;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface VtdAdminMapper {
    public void addAdventure(VtdSetting vtdSetting);
    public void addRoom(VtdRoom vtdRoom);
    public VtdSetting getAdventure(@Param("id") String id);
    public List<VtdRoom> getRooms(@Param("vtdId") String vtdId);
    public VtdRoom getRoom(@Param("id") String id);
    public void deleteAdventure(@Param("id") String id);
}