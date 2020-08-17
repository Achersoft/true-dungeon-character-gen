package com.achersoft.tdcc.vtd.dao;

import com.achersoft.tdcc.enums.Buff;
import com.achersoft.tdcc.enums.Stat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class VtdBuff {
    private String characterId;
    private boolean bardsong;
    private Buff buff;
}
