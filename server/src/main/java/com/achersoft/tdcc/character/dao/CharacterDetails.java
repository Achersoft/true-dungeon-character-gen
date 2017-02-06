package com.achersoft.tdcc.character.dao;

import com.achersoft.tdcc.enums.CharacterClass;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class CharacterDetails {
    private String id;
    private String userId;
    private String name;
    private CharacterClass characterClass;
    private Date createdOn;
    private CharacterStats stats;
    private List<CharacterItem> items;
  /*  private TokenFullDetails head;
    private TokenFullDetails eyes;
    private TokenFullDetails leftEar;
    private TokenFullDetails rightEar;
    private TokenFullDetails neck;
    private TokenFullDetails torso;
    private TokenFullDetails wrists;
    private TokenFullDetails hands;
    private TokenFullDetails meleeMainhand;
    private TokenFullDetails meleeOffhand;
    private List<TokenFullDetails> instruments;
    private TokenFullDetails rangedMainhand;
    private TokenFullDetails rangedOffhand;
    private List<TokenFullDetails> backs;
    private List<TokenFullDetails> rings;
    private TokenFullDetails waist;
    private TokenFullDetails shirt;
    private TokenFullDetails boots;
    private TokenFullDetails legs;
    private List<TokenFullDetails> charms;
    private List<TokenFullDetails> iounStones;
    private List<TokenFullDetails> slotless;
    private List<TokenFullDetails> runestones;*/
}
