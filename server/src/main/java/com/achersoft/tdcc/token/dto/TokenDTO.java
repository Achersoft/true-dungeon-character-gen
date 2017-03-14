package com.achersoft.tdcc.token.dto;

import com.achersoft.tdcc.enums.Rarity;
import com.achersoft.tdcc.enums.Slot;
import com.achersoft.tdcc.token.dao.Token;
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
public class TokenDTO {
    public String id;
    public String name;
    public String imgName;
    public String text;
    public Rarity rarity;
    public Slot slot;
    
    public static TokenDTO fromDAO(Token dao) {
        return TokenDTO.builder()
                .id(dao.getId())
                .imgName(dao.getName().replaceAll("’","").replaceAll(",", "").replaceAll(":", "").replaceAll(" ","-"))
                .name(dao.getName())
                .text(dao.getText())
                .slot(dao.getSlot())
                .rarity(dao.getRarity())
                .build();
    }
}
