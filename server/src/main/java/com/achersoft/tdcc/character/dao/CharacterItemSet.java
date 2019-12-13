package com.achersoft.tdcc.character.dao;

import com.achersoft.tdcc.token.admin.dao.TokenFullDetails;
import lombok.*;


@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
@EqualsAndHashCode
public class CharacterItemSet {
    private CharacterItem item;
    private TokenFullDetails tokenFullDetails;
}
