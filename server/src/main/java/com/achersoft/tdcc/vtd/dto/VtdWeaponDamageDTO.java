package com.achersoft.tdcc.vtd.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class VtdWeaponDamageDTO {

    private int total = 0;
    private int fire = 0;
    private int cold = 0;
    private int shock = 0;
    private int sonic = 0;
    private int eldritch = 0;
    private int poison = 0;
    private int darkrift = 0;
    private int sacred = 0;
    private int acid = 0;

}
