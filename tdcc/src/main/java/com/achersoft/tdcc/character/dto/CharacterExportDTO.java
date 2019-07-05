package com.achersoft.tdcc.character.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class CharacterExportDTO {
    public String text;
    
    public static CharacterExportDTO fromDAO(String str) {
        return CharacterExportDTO.builder()
                .text(str)
                .build();
    }
}
