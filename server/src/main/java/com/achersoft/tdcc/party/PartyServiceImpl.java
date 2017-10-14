package com.achersoft.tdcc.party;

import com.achersoft.exception.InvalidDataException;
import com.achersoft.security.providers.UserPrincipalProvider;
import com.achersoft.tdcc.character.CharacterService;
import com.achersoft.tdcc.character.dao.CharacterDetails;
import com.achersoft.tdcc.character.dao.CharacterItem;
import com.achersoft.tdcc.character.persistence.CharacterMapper;
import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.enums.Difficulty;
import com.achersoft.tdcc.enums.Slot;
import com.achersoft.tdcc.enums.SlotStatus;
import com.achersoft.tdcc.party.dao.Party;
import com.achersoft.tdcc.party.dao.PartyCharacter;
import com.achersoft.tdcc.party.dao.PartyDetails;
import com.achersoft.tdcc.party.dao.PartyEnhancements;
import com.achersoft.tdcc.party.dao.SelectableCharacters;
import com.achersoft.tdcc.party.persistence.PartyMapper;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImage;
import com.itextpdf.text.pdf.PdfIndirectObject;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.core.StreamingOutput;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
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
        Integer spell = 0;
        Integer resist = 0;
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
        
        // CoS
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
        
        // Teasures
        if(enhancements.getCharmOfGoodFortune() > 5) 
            treasures++;
        if(enhancements.getCharmOfGoodFortune() > 9) 
            treasures++;
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
        
        // CoA
        details.setInitiative(enhancements.getCharmOfAwareness());
        
        // Bracelets of Cabal
        if(enhancements.getBraceletsOfCabal() > 2) 
            spell++;
        if(enhancements.getBraceletsOfCabal() > 4) 
            spell++;
        if(spell > 0) {
            if(party.getBard()!= null && details.getBard().isHasBoC()) {
                details.getBard().setSpellHeal(details.getBard().getSpellHeal()+spell);
                details.getBard().setSpellDmg(details.getBard().getSpellDmg()+spell);
            }
            if(party.getCleric()!= null && details.getCleric().isHasBoC()) {
                details.getCleric().setSpellHeal(details.getCleric().getSpellHeal()+spell);
                details.getBard().setSpellDmg(details.getBard().getSpellDmg()+spell);
            }
            if(party.getDruid()!= null && details.getDruid().isHasBoC()) {
                details.getDruid().setSpellHeal(details.getDruid().getSpellHeal()+spell);
                details.getBard().setSpellDmg(details.getBard().getSpellDmg()+spell);
            }
            if(party.getElfWizard() != null && details.getElfWizard().isHasBoC()) {
                details.getElfWizard().setSpellDmg(details.getElfWizard().getSpellDmg()+spell);
            }
            if(party.getWizard() != null && details.getWizard().isHasBoC()) {
                details.getWizard().setSpellDmg(details.getWizard().getSpellDmg()+spell);
            }
        }
        
        // Gloves of Cabal
        if(enhancements.getGlovesOfCabal() > 0) {
            if(party.getBard()!= null && details.getBard().isHasGoC()) {
                details.getBard().setCommonScroll(true);
                if(enhancements.getGlovesOfCabal() > 2)
                    details.getBard().setUncommonScroll(true);
                if(enhancements.getGlovesOfCabal() > 4)
                    details.getBard().setRareScroll(true);
            }
            if(party.getCleric()!= null && details.getCleric().isHasGoC()) {
                details.getCleric().setCommonScroll(true);
                if(enhancements.getGlovesOfCabal() > 2)
                    details.getCleric().setUncommonScroll(true);
                if(enhancements.getGlovesOfCabal() > 4)
                    details.getCleric().setRareScroll(true);
            }
            if(party.getDruid()!= null && details.getDruid().isHasGoC()) {
                details.getDruid().setCommonScroll(true);
                if(enhancements.getGlovesOfCabal() > 2)
                    details.getDruid().setUncommonScroll(true);
                if(enhancements.getGlovesOfCabal() > 4)
                    details.getDruid().setRareScroll(true);
            }
            if(party.getElfWizard() != null && details.getElfWizard().isHasGoC()) {
                details.getElfWizard().setCommonScroll(true);
                if(enhancements.getGlovesOfCabal() > 2)
                    details.getElfWizard().setUncommonScroll(true);
                if(enhancements.getGlovesOfCabal() > 4)
                    details.getElfWizard().setRareScroll(true);
            }
            if(party.getWizard() != null && details.getWizard().isHasGoC()) {
                details.getWizard().setCommonScroll(true);
                if(enhancements.getGlovesOfCabal() > 2)
                    details.getWizard().setUncommonScroll(true);
                if(enhancements.getGlovesOfCabal() > 4)
                    details.getWizard().setRareScroll(true);
            }
        }
        
        // Charm of Cabal
        if(enhancements.getCharmOfCabal() > 2) 
            resist+=5;
        if(enhancements.getCharmOfCabal() > 4) 
            resist+=5;
        if(spell > 0) {
            if(party.getBard()!= null && details.getBard().isHasCoC() && details.getBard().getSpellResist() < 100) {
                details.getBard().setSpellResist(details.getBard().getSpellResist()+resist);
            }
            if(party.getCleric()!= null && details.getCleric().isHasCoC() && details.getCleric().getSpellResist() < 100) {
                details.getCleric().setSpellResist(details.getCleric().getSpellResist()+resist);
            }
            if(party.getDruid()!= null && details.getDruid().isHasCoC() && details.getDruid().getSpellResist() < 100) {
                details.getDruid().setSpellResist(details.getDruid().getSpellResist()+resist);
            }
            if(party.getElfWizard() != null && details.getElfWizard().isHasCoC() && details.getElfWizard().getSpellResist() < 100) {
                details.getElfWizard().setSpellResist(details.getElfWizard().getSpellResist()+resist);
            }
            if(party.getWizard() != null && details.getWizard().isHasCoC() && details.getWizard().getSpellResist() < 100) {
                details.getWizard().setSpellResist(details.getWizard().getSpellResist()+resist);
            }
        }
        
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
    public StreamingOutput exportPartyPdf(String id) {
        final PartyDetails party = getParty(id);
        return (OutputStream out) -> {
            try {
                Resource pdfFile = new ClassPathResource("party.pdf");
                PdfReader reader = new PdfReader(pdfFile.getURL());
                PdfStamper stamper = new PdfStamper(reader, out);
                AcroFields fields = stamper.getAcroFields();
                //System.err.println(fields.getFields().keySet());
                
                partyCardSetDetails("barb", party.getBarbarian(),fields);
                partyCardSetDetails("bard", party.getBard(),fields);
                partyCardSetDetails("cleric", party.getCleric(),fields);
                partyCardSetDetails("druid", party.getDruid(),fields);
                partyCardSetDetails("dfighter", party.getDwarfFighter(),fields);
                partyCardSetDetails("fighter", party.getFighter(),fields);
                partyCardSetDetails("monk", party.getMonk(),fields);
                partyCardSetDetails("paladin", party.getPaladin(),fields);
                partyCardSetDetails("ewizard", party.getElfWizard(),fields);
                partyCardSetDetails("ranger", party.getRanger(),fields);
                partyCardSetDetails("rogue", party.getRogue(),fields);
                partyCardSetDetails("wizard", party.getWizard(),fields);
                
                fields.setField("initiative", Integer.toString(party.getInitiative()));

                fields.setGenerateAppearances(true);
                stamper.setFormFlattening(true);
                stamper.close();
                reader.close();
            } catch (DocumentException ex) {
                throw new IOException(ex);
            }
        };
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
        
        // Gloves
        if(cd.getItems().stream().distinct().filter((item) -> item.getItemId()!=null&&(item.getItemId().equals("3dcfd7948a3c9196556ef7e069a36174396297ad"))).count() > 0){
            enhancements.setGlovesOfCabal(enhancements.getGlovesOfCabal()+1);
            pc.setHasGoC(true);
        }
        // Bracelets
        if(cd.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("f225241f60605ef641beeecd5003ba4129dbf46e"))).count() > 0) {
            enhancements.setBraceletsOfCabal(enhancements.getBraceletsOfCabal()+1);
            pc.setHasBoC(true);
        }
        // Charm
        if(cd.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("1c688491fcb8a12199e9eca6d97e3da5ef4f3d65"))).count() > 0) {
            enhancements.setCharmOfCabal(enhancements.getCharmOfCabal()+1);
            pc.setHasCoC(true);
        }
       
        if(userPrincipalProvider.getUserPrincipal().getSub() == null || !userPrincipalProvider.getUserPrincipal().getSub().equals(cd.getUserId()))
            pc.setUserName(cd.getUsername());
                
        return pc;
    }
    
    private void partyCardSetDetails(String prefix,PartyCharacter character, AcroFields fields) throws IOException, DocumentException{
        if(character != null) {
            fields.setField(prefix+"MeleeHit", Integer.toString(character.getMeleeHit()));
            fields.setField(prefix+"MeleeDamage", Integer.toString(character.getMeleeDmg()));
            fields.setField(prefix+"MeleeAC", Integer.toString(character.getMeleeAC()));
            fields.setField(prefix+"RangeHit", Integer.toString(character.getRangeHit()));
            fields.setField(prefix+"RangeDamage", Integer.toString(character.getRangeDmg()));
            fields.setField(prefix+"RangeAC", Integer.toString(character.getRangeAC()));
            fields.setField(prefix+"RangeBonusAC", Integer.toString(character.getRangeMissileAC()));
            fields.setField(prefix+"Fort", Integer.toString(character.getFort()));
            fields.setField(prefix+"Reflex", Integer.toString(character.getReflex()));
            fields.setField(prefix+"Will", Integer.toString(character.getWill()));
            fields.setField(prefix+"SpellDamage", Integer.toString(character.getSpellDmg()));
            fields.setField(prefix+"SpellHeal", Integer.toString(character.getSpellHeal()));
            fields.setField(prefix+"SpellResist", Integer.toString(character.getSpellResist()));
            fields.setField(prefix+"TreasureHigh", Integer.toString(character.getTreasure()/10));
            fields.setField(prefix+"TreasureLow", Integer.toString(character.getTreasure()%10));
        }
    }
 
}
