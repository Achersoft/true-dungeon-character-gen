package com.achersoft.tdcc.vtd.dao;

import com.achersoft.tdcc.enums.Stat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class VtdBuffEffect {
    private Stat stat;
    private int modifier;
}
