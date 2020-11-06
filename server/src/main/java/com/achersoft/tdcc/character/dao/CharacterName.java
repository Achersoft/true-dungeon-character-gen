package com.achersoft.tdcc.character.dao;

import com.achersoft.tdcc.enums.CharacterClass;
import java.util.Date;

import lombok.*;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class CharacterName {
    private String id;
    private String userId;
    private String name;
    private CharacterClass characterClass;
    private Date createdOn;
    private Date lastModified;
    private int level;
}
