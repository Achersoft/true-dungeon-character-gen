package com.achersoft.tdcc.party;

import com.achersoft.security.providers.UserPrincipalProvider;
import com.achersoft.tdcc.character.CharacterService;
import com.achersoft.tdcc.character.dao.CharacterDetails;
import com.achersoft.tdcc.enums.Difficulty;
import com.achersoft.tdcc.party.dao.Party;
import com.achersoft.tdcc.party.dao.PartyCharacter;
import com.achersoft.tdcc.party.dao.PartyDetails;
import com.achersoft.tdcc.party.persistence.PartyMapper;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import org.springframework.transaction.annotation.Transactional;

@Transactional(rollbackFor = Exception.class)
public class PartyServiceImpl implements PartyService {
    
    private @Inject PartyMapper mapper;
    private @Inject CharacterService characterService; 
    private @Inject UserPrincipalProvider userPrincipalProvider;
    
    @Override
    public PartyDetails createParty(Party party) {
        party.setId(UUID.randomUUID().toString());
        party.setUserId(userPrincipalProvider.getUserPrincipal().getSub());
        party.setCreatedOn(new Date());
        party.setSize(0);
        party.setDifficulty(Difficulty.NORMAL);
        mapper.createParty(party);
        return PartyDetails.builder().id(party.getId()).name(party.getName()).difficulty(party.getDifficulty()).initiative(0).build();
    }

    @Override
    public PartyDetails getParty(String id) {
        Party party = mapper.getParty(id);
        Integer charmOfAwareness = 0;
        Integer charmOfSynergy = 0;
        Integer charmOfGoodFortune = 0;
        Integer treasures = 0;
        PartyDetails details = PartyDetails.builder()
                .id(party.getId())
                .initiative(0)
                .difficulty(party.getDifficulty())
                .name(party.getName())
                .build();
        
        if(party.getBarbarian() != null) {
            details.setBarbarian(getCharacterInfo(party.getBarbarian(), charmOfAwareness, charmOfSynergy, charmOfGoodFortune));
        }
        if(party.getBard() != null) {
            details.setBard(getCharacterInfo(party.getBard(), charmOfAwareness, charmOfSynergy, charmOfGoodFortune));
        }
        if(party.getCleric() != null) {
            details.setCleric(getCharacterInfo(party.getCleric(), charmOfAwareness, charmOfSynergy, charmOfGoodFortune));
        }
        if(party.getDruid() != null) {
            details.setDruid(getCharacterInfo(party.getDruid(), charmOfAwareness, charmOfSynergy, charmOfGoodFortune));
        }
        if(party.getDwarfFighter() != null) {
            details.setDwarfFighter(getCharacterInfo(party.getDwarfFighter(), charmOfAwareness, charmOfSynergy, charmOfGoodFortune));
        }
        if(party.getElfWizard() != null) {
            details.setElfWizard(getCharacterInfo(party.getElfWizard(), charmOfAwareness, charmOfSynergy, charmOfGoodFortune));
        }
        if(party.getFighter() != null) {
            details.setFighter(getCharacterInfo(party.getFighter(), charmOfAwareness, charmOfSynergy, charmOfGoodFortune));
        }
        if(party.getMonk() != null) {
            details.setMonk(getCharacterInfo(party.getMonk(), charmOfAwareness, charmOfSynergy, charmOfGoodFortune));
        }
        if(party.getPaladin() != null) {
            details.setPaladin(getCharacterInfo(party.getPaladin(), charmOfAwareness, charmOfSynergy, charmOfGoodFortune));
        }
        if(party.getRanger() != null) {
            details.setRanger(getCharacterInfo(party.getRanger(), charmOfAwareness, charmOfSynergy, charmOfGoodFortune));
        }
        if(party.getRogue() != null) {
            details.setRogue(getCharacterInfo(party.getRogue(), charmOfAwareness, charmOfSynergy, charmOfGoodFortune));
        }
        if(party.getWizard() != null) {
            details.setWizard(getCharacterInfo(party.getWizard(), charmOfAwareness, charmOfSynergy, charmOfGoodFortune));
        }
        
        if(charmOfSynergy > 1) {
            if(party.getBarbarian() != null && details.getBarbarian().isHasCoS()) {
                details.getBarbarian().setHealth(details.getBarbarian().getHealth() + (charmOfSynergy-1));
            }
            if(party.getBard()!= null && details.getBard().isHasCoS()) {
                details.getBard().setHealth(details.getBard().getHealth() + (charmOfSynergy-1));
            }
            if(party.getCleric()!= null && details.getCleric().isHasCoS()) {
                details.getCleric().setHealth(details.getCleric().getHealth() + (charmOfSynergy-1));
            }
            if(party.getDruid()!= null && details.getDruid().isHasCoS()) {
                details.getDruid().setHealth(details.getDruid().getHealth() + (charmOfSynergy-1));
            }
            if(party.getDwarfFighter() != null && details.getDwarfFighter().isHasCoS()) {
                details.getDwarfFighter().setHealth(details.getDwarfFighter().getHealth() + (charmOfSynergy-1));
            }
            if(party.getElfWizard() != null && details.getElfWizard().isHasCoS()) {
                details.getElfWizard().setHealth(details.getElfWizard().getHealth() + (charmOfSynergy-1));
            }
            if(party.getFighter() != null && details.getFighter().isHasCoS()) {
                details.getFighter().setHealth(details.getFighter().getHealth() + (charmOfSynergy-1));
            }
            if(party.getMonk() != null && details.getMonk().isHasCoS()) {
                details.getMonk().setHealth(details.getMonk().getHealth() + (charmOfSynergy-1));
            }
            if(party.getPaladin() != null && details.getPaladin().isHasCoS()) {
                details.getPaladin().setHealth(details.getPaladin().getHealth() + (charmOfSynergy-1));
            }
            if(party.getRanger() != null && details.getRanger().isHasCoS()) {
                details.getRanger().setHealth(details.getRanger().getHealth() + (charmOfSynergy-1));
            }
            if(party.getRogue() != null && details.getRogue().isHasCoS()) {
                details.getRogue().setHealth(details.getRogue().getHealth() + (charmOfSynergy-1));
            }
            if(party.getWizard() != null && details.getWizard().isHasCoS()) {
                details.getWizard().setHealth(details.getWizard().getHealth() + (charmOfSynergy-1));
            }
        }
        
        if(charmOfGoodFortune > 5) {
            treasures++;
        }
        if(charmOfGoodFortune > 9) {
            treasures++;
        }
        
        if(treasures > 0) {
            if(party.getBarbarian() != null && details.getBarbarian().isHasCoGF()) {
                details.getBarbarian().setTreasure(details.getBarbarian().getTreasure() + treasures);
            }
            if(party.getBard()!= null && details.getBard().isHasCoGF()) {
                details.getBard().setTreasure(details.getBard().getTreasure() + treasures);
            }
            if(party.getCleric()!= null && details.getCleric().isHasCoGF()) {
                details.getCleric().setTreasure(details.getCleric().getTreasure() + treasures);
            }
            if(party.getDruid()!= null && details.getDruid().isHasCoGF()) {
                details.getDruid().setTreasure(details.getDruid().getTreasure() + treasures);
            }
            if(party.getDwarfFighter() != null && details.getDwarfFighter().isHasCoGF()) {
                details.getDwarfFighter().setTreasure(details.getDwarfFighter().getTreasure() + treasures);
            }
            if(party.getElfWizard() != null && details.getElfWizard().isHasCoGF()) {
                details.getElfWizard().setTreasure(details.getElfWizard().getTreasure() + treasures);
            }
            if(party.getFighter() != null && details.getFighter().isHasCoGF()) {
                details.getFighter().setTreasure(details.getFighter().getTreasure() + treasures);
            }
            if(party.getMonk() != null && details.getMonk().isHasCoGF()) {
                details.getMonk().setTreasure(details.getMonk().getTreasure() + treasures);
            }
            if(party.getPaladin() != null && details.getPaladin().isHasCoGF()) {
                details.getPaladin().setTreasure(details.getPaladin().getTreasure() + treasures);
            }
            if(party.getRanger() != null && details.getRanger().isHasCoGF()) {
                details.getRanger().setTreasure(details.getRanger().getTreasure() + treasures);
            }
            if(party.getRogue() != null && details.getRogue().isHasCoGF()) {
                details.getRogue().setTreasure(details.getRogue().getTreasure() + treasures);
            }
            if(party.getWizard() != null && details.getWizard().isHasCoGF()) {
                details.getWizard().setTreasure(details.getWizard().getTreasure() + treasures);
            }
        }
        
        return details;
    }

    @Override
    public List<Party> getParties() {
        return mapper.getParties(userPrincipalProvider.getUserPrincipal().getSub());
    }

    @Override
    public List<Party> deleteParty(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public PartyCharacter getCharacterInfo(String id, Integer CoA, Integer CoS, Integer CoGF) {
        CharacterDetails cd = characterService.getCharacter(id);
        PartyCharacter pc = PartyCharacter.fromCharacterDetails(cd);

        // Check GoGF and Averice 
        if(cd.getItems().stream().filter((item) -> item.getItemId()!=null&&(item.getItemId().equals("0448ddb1214a3f5c03af24653383d507fa0ea85c")||item.getItemId().equals("853d3fae881bf8907db600f8c4fc6d09e3e8f34d"))).count() > 0) {
            CoGF++;
            pc.setHasCoGF(true);
        }

        // Check CoS
        if(cd.getItems().stream().filter((item) -> item.getItemId()!=null&&(item.getItemId().equals("333788757f49e48272a25e8b5994ac6503ad2adc"))).count() > 0) {
            CoS++;
            pc.setHasCoS(true);
        }

        // Check CoA
        if(cd.getItems().stream().filter((item) -> item.getItemId()!=null&&(item.getItemId().equals("853b569fa247d18d6f4d542d38c79df8fce50fe3"))).count() > 0)
            CoA++;

        return PartyCharacter.fromCharacterDetails(cd);
    }
 
}
