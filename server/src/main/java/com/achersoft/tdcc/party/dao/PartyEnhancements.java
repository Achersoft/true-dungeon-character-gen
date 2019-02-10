package com.achersoft.tdcc.party.dao;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class PartyEnhancements {
    int charmOfAwareness = 0;
    int charmOfSynergy = 0;
    int charmOfGoodFortune = 0;
    int glovesOfCabal = 0;
    int braceletsOfCabal = 0;
    int charmOfCabal = 0;
    int earcuffOfThePhalanx = 0;
    int sheildOfThePhalanx = 0;
    boolean levelEnhancer = false;
}
