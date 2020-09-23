package com.achersoft.tdcc.vtd.admin.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class VtdSetting {
    private String id;
    private Date lastModified;
    private String name;
    private String passcode;
    private List<VtdRoom> rooms;
}
