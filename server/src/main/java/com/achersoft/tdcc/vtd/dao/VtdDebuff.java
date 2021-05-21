package com.achersoft.tdcc.vtd.dao;

import com.achersoft.tdcc.enums.Debuff;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class VtdDebuff {
    private String characterId;
    private int level;
    private Debuff debuff;
}
