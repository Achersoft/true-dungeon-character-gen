package com.achersoft.tdcc.party;

import com.achersoft.exception.InvalidDataException;
import com.achersoft.security.providers.UserPrincipalProvider;
import com.achersoft.tdcc.character.CharacterService;
import com.achersoft.tdcc.character.dao.CharacterDetails;
import com.achersoft.tdcc.character.persistence.CharacterMapper;
import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.enums.Difficulty;
import com.achersoft.tdcc.party.dao.Party;
import com.achersoft.tdcc.party.dao.PartyCharacter;
import com.achersoft.tdcc.party.dao.PartyDetails;
import com.achersoft.tdcc.party.dao.PartyEnhancements;
import com.achersoft.tdcc.party.dao.SelectableCharacters;
import com.achersoft.tdcc.party.persistence.PartyMapper;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.springframework.transaction.annotation.Transactional;

@Transactional(rollbackFor = Exception.class)
public class PartyServiceImpl implements PartyService {
    
    private @Inject PartyMapper mapper;
    private @Inject CharacterMapper characterMapper;
    private @Inject CharacterService characterService; 
    private @Inject UserPrincipalProvider userPrincipalProvider;
    
    @Override
    public PartyDetails createParty(Party party) {
        party.setId(UUID.randomUUID().toString());
        party.setUserId(userPrincipalProvider.getUserPrincipal().getSub());
        party.setCreatedOn(new Date());
        party.setDifficulty(Difficulty.NORMAL);
        mapper.createParty(party);
        return PartyDetails.builder().id(party.getId()).name(party.getName()).difficulty(party.getDifficulty()).initiative(0).build();
    }

    @Override
    public PartyDetails getParty(String id) {
        Party party = mapper.getParty(id);
        PartyEnhancements enhancements = PartyEnhancements.builder().charmOfAwareness(0).charmOfGoodFortune(0).charmOfSynergy(0).build();
        Integer treasures = 0;
        PartyDetails details = PartyDetails.builder()
                .id(party.getId())
                .initiative(0)
                .editable(false)
                .difficulty(party.getDifficulty())
                .name(party.getName())
                .build();
        
        if(userPrincipalProvider.getUserPrincipal().getSub() != null && userPrincipalProvider.getUserPrincipal().getSub().equalsIgnoreCase(party.getUserId()))
            details.setEditable(true);
        
        if(party.getBarbarian() != null) {
            details.setBarbarian(getCharacterInfo(party.getBarbarian(), enhancements));
        }
        if(party.getBard() != null) {
            details.setBard(getCharacterInfo(party.getBard(), enhancements));
        }
        if(party.getCleric() != null) {
            details.setCleric(getCharacterInfo(party.getCleric(), enhancements));
        }
        if(party.getDruid() != null) {
            details.setDruid(getCharacterInfo(party.getDruid(), enhancements));
        }
        if(party.getDwarfFighter() != null) {
            details.setDwarfFighter(getCharacterInfo(party.getDwarfFighter(), enhancements));
        }
        if(party.getElfWizard() != null) {
            details.setElfWizard(getCharacterInfo(party.getElfWizard(), enhancements));
        }
        if(party.getFighter() != null) {
            details.setFighter(getCharacterInfo(party.getFighter(), enhancements));
        }
        if(party.getMonk() != null) {
            details.setMonk(getCharacterInfo(party.getMonk(), enhancements));
        }
        if(party.getPaladin() != null) {
            details.setPaladin(getCharacterInfo(party.getPaladin(), enhancements));
        }
        if(party.getRanger() != null) {
            details.setRanger(getCharacterInfo(party.getRanger(), enhancements));
        }
        if(party.getRogue() != null) {
            details.setRogue(getCharacterInfo(party.getRogue(), enhancements));
        }
        if(party.getWizard() != null) {
            details.setWizard(getCharacterInfo(party.getWizard(), enhancements));
        }
        
        if(enhancements.getCharmOfSynergy() > 1) {
            if(party.getBarbarian() != null && details.getBarbarian().isHasCoS()) {
                details.getBarbarian().setHealth(details.getBarbarian().getHealth() + (enhancements.getCharmOfSynergy()-1));
            }
            if(party.getBard()!= null && details.getBard().isHasCoS()) {
                details.getBard().setHealth(details.getBard().getHealth() + (enhancements.getCharmOfSynergy()-1));
            }
            if(party.getCleric()!= null && details.getCleric().isHasCoS()) {
                details.getCleric().setHealth(details.getCleric().getHealth() + (enhancements.getCharmOfSynergy()-1));
            }
            if(party.getDruid()!= null && details.getDruid().isHasCoS()) {
                details.getDruid().setHealth(details.getDruid().getHealth() + (enhancements.getCharmOfSynergy()-1));
            }
            if(party.getDwarfFighter() != null && details.getDwarfFighter().isHasCoS()) {
                details.getDwarfFighter().setHealth(details.getDwarfFighter().getHealth() + (enhancements.getCharmOfSynergy()-1));
            }
            if(party.getElfWizard() != null && details.getElfWizard().isHasCoS()) {
                details.getElfWizard().setHealth(details.getElfWizard().getHealth() + (enhancements.getCharmOfSynergy()-1));
            }
            if(party.getFighter() != null && details.getFighter().isHasCoS()) {
                details.getFighter().setHealth(details.getFighter().getHealth() + (enhancements.getCharmOfSynergy()-1));
            }
            if(party.getMonk() != null && details.getMonk().isHasCoS()) {
                details.getMonk().setHealth(details.getMonk().getHealth() + (enhancements.getCharmOfSynergy()-1));
            }
            if(party.getPaladin() != null && details.getPaladin().isHasCoS()) {
                details.getPaladin().setHealth(details.getPaladin().getHealth() + (enhancements.getCharmOfSynergy()-1));
            }
            if(party.getRanger() != null && details.getRanger().isHasCoS()) {
                details.getRanger().setHealth(details.getRanger().getHealth() + (enhancements.getCharmOfSynergy()-1));
            }
            if(party.getRogue() != null && details.getRogue().isHasCoS()) {
                details.getRogue().setHealth(details.getRogue().getHealth() + (enhancements.getCharmOfSynergy()-1));
            }
            if(party.getWizard() != null && details.getWizard().isHasCoS()) {
                details.getWizard().setHealth(details.getWizard().getHealth() + (enhancements.getCharmOfSynergy()-1));
            }
        }
        
        if(enhancements.getCharmOfGoodFortune() > 5) {
            treasures++;
        }
        if(enhancements.getCharmOfGoodFortune() > 9) {
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
        
        details.setInitiative(enhancements.getCharmOfAwareness());
        
        return details;
    }

    @Override
    public List<Party> getParties() {
        return mapper.getParties(userPrincipalProvider.getUserPrincipal().getSub()).stream().map((party) -> {
            int size = 0;
            if(party.getBarbarian()!= null)
                size++;
            if(party.getBard()!= null)
                size++;
            if(party.getCleric()!= null)
                size++;
            if(party.getDruid()!= null)
                size++;
            if(party.getDwarfFighter()!= null)
                size++;
            if(party.getElfWizard()!= null)
                size++;
            if(party.getFighter()!= null)
                size++;
            if(party.getMonk()!= null)
                size++;
            if(party.getPaladin()!= null)
                size++;
            if(party.getRanger()!= null)
                size++;
            if(party.getRogue()!= null)
                size++;
            if(party.getWizard() != null)
                size++;
            party.setSize(size);
            return party;
        }).collect(Collectors.toList());
    }
    
    @Override
    public SelectableCharacters getSelectableCharacters(String userid, CharacterClass cClass) {
        return SelectableCharacters.builder()
                .characters(characterMapper.getCharactersClass(userid, cClass))
                .userAccounts(mapper.getUsers())
                .build();
    }
    
    @Override
    public PartyDetails updatePartyDifficulty(String id, Difficulty difficulty) {
        mapper.updatePartyDifficulty(id, difficulty);
        return getParty(id);
    }
            
    @Override
    public PartyDetails addPartyCharacter(String id, String characterId) {
        if(!(userPrincipalProvider.getUserPrincipal().getSub() != null && userPrincipalProvider.getUserPrincipal().getSub().equalsIgnoreCase(mapper.getParty(id).getUserId())))
            throw new InvalidDataException("Operation is not allowed. Party does not belong to logged in user."); 
        
        switch(characterMapper.getCharacter(characterId).getCharacterClass()) {
            case BARBARIAN:
                mapper.addBarbarian(id, characterId);
                break;
            case BARD:
                mapper.addBard(id, characterId);
                break;
            case CLERIC:
                mapper.addCleric(id, characterId);
                break;
            case DRUID:
                mapper.addDruid(id, characterId);
                break;
            case DWARF_FIGHTER:
                mapper.addDwarf(id, characterId);
                break;
            case ELF_WIZARD:
                mapper.addElf(id, characterId);
                break;
            case FIGHTER:
                mapper.addFighter(id, characterId);
                break;
            case MONK:
                mapper.addMonk(id, characterId);
                break;
            case PALADIN:
                mapper.addPaladin(id, characterId);
                break;
            case RANGER:
                mapper.addRanger(id, characterId);
                break;
            case ROGUE:
                mapper.addRogue(id, characterId);
                break;
            case WIZARD:
                mapper.addWizard(id, characterId);
                break;
            default:
                break;
        }
        return getParty(id);
    }

    @Override
    public PartyDetails removePartyCharacter(String id, CharacterClass cClass) {
        if(!(userPrincipalProvider.getUserPrincipal().getSub() != null && userPrincipalProvider.getUserPrincipal().getSub().equalsIgnoreCase(mapper.getParty(id).getUserId())))
            throw new InvalidDataException("Operation is not allowed. Party does not belong to logged in user."); 
        
        switch(cClass) {
            case BARBARIAN:
                mapper.removeBarbarian(id);
                break;
            case BARD:
                mapper.removeBard(id);
                break;
            case CLERIC:
                mapper.removeCleric(id);
                break;
            case DRUID:
                mapper.removeDruid(id);
                break;
            case DWARF_FIGHTER:
                mapper.removeDwarf(id);
                break;
            case ELF_WIZARD:
                mapper.removeElf(id);
                break;
            case FIGHTER:
                mapper.removeFighter(id);
                break;
            case MONK:
                mapper.removeMonk(id);
                break;
            case PALADIN:
                mapper.removePaladin(id);
                break;
            case RANGER:
                mapper.removeRanger(id);
                break;
            case ROGUE:
                mapper.removeRogue(id);
                break;
            case WIZARD:
                mapper.removeWizard(id);
                break;
            default:
                break;
        }
        return getParty(id);
    }
            
    @Override
    public List<Party> deleteParty(String id) {
        mapper.deleteParty(id, userPrincipalProvider.getUserPrincipal().getSub());
        return getParties();
    }

    public PartyCharacter getCharacterInfo(String id, PartyEnhancements enhancements) {
        CharacterDetails cd = characterService.getCharacter(id);
        PartyCharacter pc = PartyCharacter.fromCharacterDetails(cd);

        // Check GoGF and Averice 
        if(cd.getItems().stream().filter((item) -> item.getItemId()!=null&&(item.getItemId().equals("0448ddb1214a3f5c03af24653383d507fa0ea85c")||item.getItemId().equals("853d3fae881bf8907db600f8c4fc6d09e3e8f34d"))).count() > 0) {
            enhancements.setCharmOfGoodFortune(enhancements.getCharmOfGoodFortune()+1);
            pc.setHasCoGF(true);
        }

        // Check CoS
        if(cd.getItems().stream().filter((item) -> item.getItemId()!=null&&(item.getItemId().equals("333788757f49e48272a25e8b5994ac6503ad2adc"))).count() > 0) {
            enhancements.setCharmOfSynergy(enhancements.getCharmOfSynergy()+1);
            pc.setHasCoS(true);
        }

        // Check CoA
        enhancements.setCharmOfAwareness(enhancements.getCharmOfAwareness()+cd.getStats().getInitiative());
       
        if(userPrincipalProvider.getUserPrincipal().getSub() != null && !userPrincipalProvider.getUserPrincipal().getSub().equals(cd.getUserId()))
            pc.setUserName(cd.getUsername());
                
        return pc;
    }
 
}
