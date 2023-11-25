package com.achersoft.tdcc.token.admin.dao;

import com.achersoft.tdcc.enums.Rarity;
import com.achersoft.tdcc.enums.Slot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class SlotModifier {
    private String id;
    private Slot slot;
    private Rarity rarity;
    private int modifier;
}
