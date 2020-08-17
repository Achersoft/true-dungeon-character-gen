package com.achersoft.tdcc.vtd.dto;

import com.achersoft.tdcc.enums.Buff;
import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.vtd.dao.VtdDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class BuffDTO {
    public String id;
    public String name;
    public String displayText;
    public boolean bardsong;

    public static BuffDTO fromDAO(Buff dao) {
        return BuffDTO.builder()
                .id(dao.name())
                .name(dao.getName())
                .displayText(dao.getDisplayText())
                .bardsong(dao.isBardsong())
                .build();
    }
}
