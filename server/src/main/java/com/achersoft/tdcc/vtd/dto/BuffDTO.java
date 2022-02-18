package com.achersoft.tdcc.vtd.dto;

import com.achersoft.tdcc.enums.Buff;
import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.vtd.dao.VtdBuff;
import com.achersoft.tdcc.vtd.dao.VtdDetails;
import lombok.*;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
@EqualsAndHashCode(exclude = "level")
public class BuffDTO {
    public String id;
    public String name;
    public String displayText;
    public Integer level;
    public boolean bardsong;
    public boolean canBeLevel;

    public static BuffDTO fromDAO(Buff dao) {
        return BuffDTO.builder()
                .id(dao.name())
                .name(dao.getName())
                .displayText(dao.getDisplayText())
                .bardsong(dao.isBardsong())
                .canBeLevel(dao.isCanBeLeveled())
                .build();
    }

    public static BuffDTO fromDAO(VtdBuff dao) {
        if (dao == null || dao.getBuff() == null)
            return null;
        return BuffDTO.builder()
                .id(dao.getBuff().name())
                .name(dao.getBuff().getName())
                .displayText(dao.getBuff().getDisplayText())
                .level(dao.getLevel())
                .bardsong(dao.getBuff().isBardsong())
                .canBeLevel(dao.getBuff().isCanBeLeveled())
                .build();
    }
}
