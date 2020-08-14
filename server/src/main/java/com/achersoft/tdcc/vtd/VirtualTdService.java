package com.achersoft.tdcc.vtd;

import com.achersoft.tdcc.character.dao.CharacterDetails;
import com.achersoft.tdcc.character.dao.CharacterName;
import com.achersoft.tdcc.party.dao.SelectableCharacters;
import com.achersoft.tdcc.vtd.dao.VtdDetails;

import javax.ws.rs.core.StreamingOutput;
import java.util.List;

public interface VirtualTdService {
    VtdDetails getVtdCharacter(String id);
    
}
