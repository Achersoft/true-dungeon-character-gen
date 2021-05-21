package com.achersoft.tdcc.vtd.dto;

import com.achersoft.tdcc.enums.Buff;
import com.achersoft.tdcc.enums.Debuff;
import com.achersoft.tdcc.vtd.dao.VtdDebuff;
import lombok.*;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
@EqualsAndHashCode(exclude = "level")
public class DebuffDTO {
    public String id;
    public String name;
    public String displayText;
    public int level;

    public static DebuffDTO fromDAO(VtdDebuff dao) {
        return DebuffDTO.builder()
                .id(dao.getDebuff().name())
                .name(dao.getDebuff().getName())
                .displayText(dao.getDebuff().getDisplayText())
                .level(dao.getLevel())
                .build();
    }
}
