package com.achersoft.tdcc.character.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class CharacterNote {
    private boolean alwaysInEffect = false;
    private boolean oncePerRound = false;
    private boolean oncePerRoom = false;
    private boolean oncePerGame = false;
    private String note;
}
