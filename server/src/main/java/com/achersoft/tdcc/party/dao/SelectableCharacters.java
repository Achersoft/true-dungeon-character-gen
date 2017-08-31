package com.achersoft.tdcc.party.dao;

import com.achersoft.tdcc.character.dao.CharacterName;
import com.achersoft.user.dao.User;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class SelectableCharacters {
    public List<User> userAccounts;
    public List<CharacterName> characters;
}
