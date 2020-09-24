package com.achersoft.tdcc.vtd.admin.dao;

import com.achersoft.tdcc.enums.CritType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class VtdRoom {
    private @Builder.Default String id = UUID.randomUUID().toString();
    private String vtdId;
    private int room;
    private @Builder.Default CritType critType = CritType.ANY;
    private String name;
    private @Builder.Default int one = 5;
    private @Builder.Default int two = 5;
    private @Builder.Default int three = 5;
    private @Builder.Default int four = 5;
    private @Builder.Default int five = 5;
    private @Builder.Default int six = 5;
    private @Builder.Default int seven = 5;
    private @Builder.Default int eight = 5;
    private @Builder.Default int nine = 5;
    private @Builder.Default int ten = 5;
    private @Builder.Default int eleven = 5;
    private @Builder.Default int twelve = 5;
    private @Builder.Default int thirteen = 5;
    private @Builder.Default int fourteen = 5;
    private @Builder.Default int fifteen = 5;
    private @Builder.Default int sixteen = 5;
    private @Builder.Default int seventeen = 5;
    private @Builder.Default int eighteen = 5;
    private @Builder.Default int nineteen = 5;
    private @Builder.Default int twenty = 5;
    private @Builder.Default int cold = 0;
    private @Builder.Default int fire = 0;
    private @Builder.Default int shock = 0;
    private @Builder.Default int sonic = 0;
    private @Builder.Default int poison = 0;
    private @Builder.Default int sacred = 0;
    private @Builder.Default int darkrift = 0;
    private @Builder.Default int acid = 0;
}
