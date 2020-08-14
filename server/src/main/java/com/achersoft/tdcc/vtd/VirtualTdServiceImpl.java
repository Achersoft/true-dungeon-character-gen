package com.achersoft.tdcc.vtd;

import com.achersoft.exception.InvalidDataException;
import com.achersoft.security.providers.UserPrincipalProvider;
import com.achersoft.tdcc.character.CharacterService;
import com.achersoft.tdcc.character.dao.*;
import com.achersoft.tdcc.character.dao.CharacterName;
import com.achersoft.tdcc.character.persistence.CharacterMapper;
import com.achersoft.tdcc.enums.*;
import com.achersoft.tdcc.party.dao.SelectableCharacters;
import com.achersoft.tdcc.party.persistence.PartyMapper;
import com.achersoft.tdcc.token.admin.dao.TokenFullDetails;
import com.achersoft.tdcc.token.admin.persistence.TokenAdminMapper;
import com.achersoft.tdcc.token.persistence.TokenMapper;
import com.achersoft.tdcc.vtd.dao.CharacterSkill;
import com.achersoft.tdcc.vtd.dao.VtdDetails;
import com.achersoft.tdcc.vtd.persistence.VtdMapper;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class VirtualTdServiceImpl implements VirtualTdService {
    
    private @Inject CharacterMapper mapper;
    private @Inject VtdMapper vtdMapper;
    private @Inject UserPrincipalProvider userPrincipalProvider;

    @Override
    public VtdDetails getVtdCharacter(String id) {
        CharacterDetails characterDetails = mapper.getCharacter(id);
        
        if(characterDetails == null)
            throw new InvalidDataException("Requested character could not be found.");
        if(!(userPrincipalProvider.getUserPrincipal().getSub() != null && userPrincipalProvider.getUserPrincipal().getSub().equalsIgnoreCase(characterDetails.getUserId())))
            throw new InvalidDataException("Virtual True Dungeon is only for your own characters.");

        final CharacterStats characterStats = mapper.getCharacterStats(id);
        VtdDetails vtdDetails = vtdMapper.getCharacter(id);

        if (vtdDetails == null || vtdDetails.getExpires().before(new Date())) {
            vtdMapper.deleteCharacterSkills(id);
            vtdMapper.deleteCharacter(id);
            List<CharacterSkill> skills = vtdMapper.getSkills(characterDetails.getCharacterClass(), characterStats.getLevel());

            if (skills != null)
                skills.forEach(skill -> {
                    skill.setId(UUID.randomUUID().toString());
                    skill.setCharacterId(id);
                    skill.setUsedNumber(0);
                    vtdMapper.addCharacterSkill(skill);
                });

            vtdDetails = VtdDetails.builder().characterId(id)
                    .expires(new Date(new Date().getTime() + 86400000))
                    .currentHealth(characterStats.getHealth())
                    .build();

            vtdMapper.addCharacter(vtdDetails);
        }

        vtdDetails.setStats(characterStats);
        vtdDetails.setNotes(mapper.getCharacterNotes(id));
        vtdDetails.setCharacterSkills(vtdMapper.getCharacterSkills(id));
        vtdDetails.setName(characterDetails.getName());
        vtdDetails.setCharacterClass(characterDetails.getCharacterClass());
        vtdDetails.setMeleeDmgRange(Arrays.asList(4,5,6));
        vtdDetails.setMeleeOffhandDmgRange(Arrays.asList(4,5,6));
        vtdDetails.setMeleePolyDmgRange(Arrays.asList(4,5,6));
        vtdDetails.setRangeDmgRange(Arrays.asList(4,5,6));
        vtdDetails.setRangeOffhandDmgRange(Arrays.asList(4,5,6));
        vtdDetails.setMeleeCritMin(18);
        vtdDetails.setMeleePolyCritMin(18);
        vtdDetails.setRangeCritMin(18);

        return vtdDetails;
    }
}
