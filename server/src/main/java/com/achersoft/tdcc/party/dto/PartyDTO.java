package com.achersoft.tdcc.party.dto;

import com.achersoft.tdcc.enums.Difficulty;
import com.achersoft.tdcc.party.dao.*;
import java.net.URI;
import lombok.Builder;

@Builder
public class PartyDTO {
    public String id;
    public String name;
    public Difficulty difficulty;
    public int size;
    public URI link;
    
    public static PartyDTO fromDAO(Party dao) {
        return PartyDTO.builder()
                .id(dao.getId())
                .name(dao.getName())
                .difficulty(dao.getDifficulty())
                .size(dao.getSize())
                .build();
    }
}
