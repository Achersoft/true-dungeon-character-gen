package com.achersoft.tdcc.token.dao;

import com.achersoft.tdcc.enums.Rarity;
import com.achersoft.tdcc.enums.Slot;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Token {
    private String id;
    private String name;
    private String text;
    private Rarity rarity;
    private Slot slot;
}
