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
        Integer earcuffAC = 0;
        Integer shieldAC = 0;
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
            try {
                details.setBarbarian(getCharacterInfo(characterService.getCharacter(party.getBarbarian()), enhancements));
            } catch(Exception e) {
                party.setBarbarian(null);
                mapper.removeBarbarian(id);
            }
        }
        if(party.getBard() != null) {
            try {
                details.setBard(getCharacterInfo(characterService.getCharacter(party.getBard()), enhancements));
            } catch(Exception e) {
                party.setBard(null);
                mapper.removeBard(id);
            }
        }
        if(party.getCleric() != null) {
            try {
                details.setCleric(getCharacterInfo(characterService.getCharacter(party.getCleric()), enhancements));
            } catch(Exception e) {
                party.setCleric(null);
                mapper.removeCleric(id);
            }
        }
        if(party.getDruid() != null) {
            try {
                details.setDruid(getCharacterInfo(characterService.getCharacter(party.getDruid()), enhancements));
            } catch(Exception e) {
                party.setDruid(null);
                mapper.removeDruid(id);
            }
        }
        if(party.getDwarfFighter() != null) {
            try {
                details.setDwarfFighter(getCharacterInfo(characterService.getCharacter(party.getDwarfFighter()), enhancements));
            } catch(Exception e) {
                party.setDwarfFighter(null);
                mapper.removeDwarf(id);
            }
        }
        if(party.getElfWizard() != null) {
            try {
                details.setElfWizard(getCharacterInfo(characterService.getCharacter(party.getElfWizard()), enhancements));
            } catch(Exception e) {
                party.setElfWizard(null);
                mapper.removeElf(id);
            }
        }
        if(party.getFighter() != null) {
            try {
                details.setFighter(getCharacterInfo(characterService.getCharacter(party.getFighter()), enhancements)); 
            } catch(Exception e) {
                party.setFighter(null);
                mapper.removeFighter(id);
            }
        }
        if(party.getMonk() != null) {
            try {
                details.setMonk(getCharacterInfo(characterService.getCharacter(party.getMonk()), enhancements)); 
            } catch(Exception e) {
                party.setMonk(null);
                mapper.removeMonk(id);
            }
        }
        if(party.getPaladin() != null) {
            try {
                details.setPaladin(getCharacterInfo(characterService.getCharacter(party.getPaladin()), enhancements));
            } catch(Exception e) {
                party.setPaladin(null);
                mapper.removePaladin(id);
            }
        }
        if(party.getRanger() != null) {
            try {
                details.setRanger(getCharacterInfo(characterService.getCharacter(party.getRanger()), enhancements));
            } catch(Exception e) {
                party.setRanger(null);
                mapper.removeRanger(id);
            }
        }
        if(party.getRogue() != null) {
            try {
                details.setRogue(getCharacterInfo(characterService.getCharacter(party.getRogue()), enhancements)); 
            } catch(Exception e) {
                party.setRogue(null);
                mapper.removeRogue(id);
            }
        }
        if(party.getWizard() != null) {
            try {
                details.setWizard(getCharacterInfo(characterService.getCharacter(party.getWizard()), enhancements));
            } catch(Exception e) {
                party.setWizard(null);
                mapper.removeWizard(id);
            }
        }

        // Group level enhancing items
        if(enhancements.isLevelEnhancer()) {
            enhancements = PartyEnhancements.builder().charmOfAwareness(0).charmOfGoodFortune(0).charmOfSynergy(0).build();
            if(party.getBarbarian() != null) {
                details.setBarbarian(getCharacterInfo(characterService.getCharacterMaxLevel(party.getBarbarian()), enhancements));
            }
            if(party.getBard() != null) {
                details.setBard(getCharacterInfo(characterService.getCharacterMaxLevel(party.getBard()), enhancements));
            }
            if(party.getCleric() != null) {
                details.setCleric(getCharacterInfo(characterService.getCharacterMaxLevel(party.getCleric()), enhancements));
            }
            if(party.getDruid() != null) {
                details.setDruid(getCharacterInfo(characterService.getCharacterMaxLevel(party.getDruid()), enhancements));
            }
            if(party.getDwarfFighter() != null) {
                details.setDwarfFighter(getCharacterInfo(characterService.getCharacterMaxLevel(party.getDwarfFighter()), enhancements));
            }
            if(party.getElfWizard() != null) {
                details.setElfWizard(getCharacterInfo(characterService.getCharacterMaxLevel(party.getElfWizard()), enhancements));
            }
            if(party.getFighter() != null) {
                details.setFighter(getCharacterInfo(characterService.getCharacterMaxLevel(party.getFighter()), enhancements));
            }
            if(party.getMonk() != null) {
                details.setMonk(getCharacterInfo(characterService.getCharacterMaxLevel(party.getMonk()), enhancements));
            }
            if(party.getPaladin() != null) {
                details.setPaladin(getCharacterInfo(characterService.getCharacterMaxLevel(party.getPaladin()), enhancements));
            }
            if(party.getRanger() != null) {
                details.setRanger(getCharacterInfo(characterService.getCharacterMaxLevel(party.getRanger()), enhancements));
            }
            if(party.getRogue() != null) {
                details.setRogue(getCharacterInfo(characterService.getCharacterMaxLevel(party.getRogue()), enhancements));
            }
            if(party.getWizard() != null) {
                details.setWizard(getCharacterInfo(characterService.getCharacterMaxLevel(party.getWizard()), enhancements));
            }
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

        // Treasures
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
        if (enhancements.getCharmOfAwareness() > 10)
            details.setInitiative(10);
        else
            details.setInitiative(enhancements.getCharmOfAwareness());
        
        // Earcuff of the Phalanx 
        if(enhancements.getEarcuffOfThePhalanx() > 5) 
            earcuffAC++;
        if(enhancements.getEarcuffOfThePhalanx() > 9) 
            earcuffAC++;
        if(earcuffAC > 0) {
            if(party.getBarbarian() != null && details.getBarbarian().isHasEotP()) {
                details.getBarbarian().setMeleeAC(details.getBarbarian().getMeleeAC()+earcuffAC);
                details.getBarbarian().setRangeAC(details.getBarbarian().getRangeAC()+earcuffAC);
            }
            if(party.getBard()!= null && details.getBard().isHasEotP()) {
                details.getBard().setMeleeAC(details.getBard().getMeleeAC()+earcuffAC);
                details.getBard().setRangeAC(details.getBard().getRangeAC()+earcuffAC);
            }
            if(party.getCleric()!= null && details.getCleric().isHasEotP()) {
                details.getCleric().setMeleeAC(details.getCleric().getMeleeAC()+earcuffAC);
                details.getCleric().setRangeAC(details.getCleric().getRangeAC()+earcuffAC);
            }
            if(party.getDruid()!= null && details.getDruid().isHasEotP()) {
                details.getDruid().setMeleeAC(details.getDruid().getMeleeAC()+earcuffAC);
                details.getDruid().setRangeAC(details.getDruid().getRangeAC()+earcuffAC);
            }
            if(party.getDwarfFighter() != null && details.getDwarfFighter().isHasEotP()) {
                details.getDwarfFighter().setMeleeAC(details.getDwarfFighter().getMeleeAC()+earcuffAC);
                details.getDwarfFighter().setRangeAC(details.getDwarfFighter().getRangeAC()+earcuffAC);
            }
            if(party.getElfWizard() != null && details.getElfWizard().isHasEotP()) {
                details.getElfWizard().setMeleeAC(details.getElfWizard().getMeleeAC()+earcuffAC);
                details.getElfWizard().setRangeAC(details.getElfWizard().getRangeAC()+earcuffAC);
            }
            if(party.getFighter() != null && details.getFighter().isHasEotP()) {
                details.getFighter().setMeleeAC(details.getFighter().getMeleeAC()+earcuffAC);
                details.getFighter().setRangeAC(details.getFighter().getRangeAC()+earcuffAC);
            }
            if(party.getMonk() != null && details.getMonk().isHasEotP()) {
                details.getMonk().setMeleeAC(details.getMonk().getMeleeAC()+earcuffAC);
                details.getMonk().setRangeAC(details.getMonk().getRangeAC()+earcuffAC);
            }
            if(party.getPaladin() != null && details.getPaladin().isHasEotP()) {
                details.getPaladin().setMeleeAC(details.getPaladin().getMeleeAC()+earcuffAC);
                details.getPaladin().setRangeAC(details.getPaladin().getRangeAC()+earcuffAC);
            }
            if(party.getRanger() != null && details.getRanger().isHasEotP()) {
                details.getRanger().setMeleeAC(details.getRanger().getMeleeAC()+earcuffAC);
                details.getRanger().setRangeAC(details.getRanger().getRangeAC()+earcuffAC);
            }
            if(party.getRogue() != null && details.getRogue().isHasEotP()) {
                details.getRogue().setMeleeAC(details.getRogue().getMeleeAC()+earcuffAC);
                details.getRogue().setRangeAC(details.getRogue().getRangeAC()+earcuffAC);
            }
            if(party.getWizard() != null && details.getWizard().isHasEotP()) {
                details.getWizard().setMeleeAC(details.getWizard().getMeleeAC()+earcuffAC);
                details.getWizard().setRangeAC(details.getWizard().getRangeAC()+earcuffAC);
            }
        }
        
        // Shield of the Phalanx 
        if(enhancements.getSheildOfThePhalanx() > 1) 
            shieldAC++;
        if(enhancements.getSheildOfThePhalanx() > 2) 
            shieldAC++;
        if(enhancements.getSheildOfThePhalanx() > 3) 
            shieldAC++;
        if(enhancements.getSheildOfThePhalanx() > 4) 
            shieldAC++;
        if(shieldAC > 0) {
            if(party.getBarbarian() != null && details.getBarbarian().isHasSotP()) {
                details.getBarbarian().setMeleeAC(details.getBarbarian().getMeleeAC()+shieldAC);
                details.getBarbarian().setRangeAC(details.getBarbarian().getRangeAC()+shieldAC);
            }
            if(party.getCleric() != null && details.getCleric().isHasSotP()) {
                details.getCleric().setMeleeAC(details.getCleric().getMeleeAC()+shieldAC);
                details.getCleric().setRangeAC(details.getCleric().getRangeAC()+shieldAC);
            }
            if(party.getDwarfFighter() != null && details.getDwarfFighter().isHasSotP()) {
                details.getDwarfFighter().setMeleeAC(details.getDwarfFighter().getMeleeAC()+shieldAC);
                details.getDwarfFighter().setRangeAC(details.getDwarfFighter().getRangeAC()+shieldAC);
            }
            if(party.getFighter() != null && details.getFighter().isHasSotP()) {
                details.getFighter().setMeleeAC(details.getFighter().getMeleeAC()+shieldAC);
                details.getFighter().setRangeAC(details.getFighter().getRangeAC()+shieldAC);
            }
            if(party.getPaladin() != null && details.getPaladin().isHasSotP()) {
                details.getPaladin().setMeleeAC(details.getPaladin().getMeleeAC()+shieldAC);
                details.getPaladin().setRangeAC(details.getPaladin().getRangeAC()+shieldAC);
            }
        }
        
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
                details.getCleric().setSpellDmg(details.getCleric().getSpellDmg()+spell);
            }
            if(party.getDruid()!= null && details.getDruid().isHasBoC()) {
                details.getDruid().setSpellHeal(details.getDruid().getSpellHeal()+spell);
                details.getDruid().setSpellDmg(details.getDruid().getSpellDmg()+spell);
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
        details.setSize(size);
        
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
                
                partyCardSetDetails("barb", party.getBarbarian(),fields, stamper);
                partyCardSetDetails("bard", party.getBard(),fields, stamper);
                partyCardSetDetails("cleric", party.getCleric(),fields, stamper);
                partyCardSetDetails("druid", party.getDruid(),fields, stamper);
                partyCardSetDetails("dfighter", party.getDwarfFighter(),fields, stamper);
                partyCardSetDetails("fighter", party.getFighter(),fields, stamper);
                partyCardSetDetails("monk", party.getMonk(),fields, stamper);
                partyCardSetDetails("paladin", party.getPaladin(),fields, stamper);
                partyCardSetDetails("ewizard", party.getElfWizard(),fields, stamper);
                partyCardSetDetails("ranger", party.getRanger(),fields, stamper);
                partyCardSetDetails("rogue", party.getRogue(),fields, stamper);
                partyCardSetDetails("wizard", party.getWizard(),fields, stamper);
                
                fields.setField("initiative", Integer.toString(party.getInitiative()));
                
                Image fillBox = Image.getInstance(new ClassPathResource("fill.png").getURL());
                PdfContentByte over = stamper.getOverContent(1);
                PdfImage stream = new PdfImage(fillBox, "", null);
                stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                PdfIndirectObject ref = stamper.getWriter().addToBody(stream);
                fillBox.setDirectReference(ref.getIndirectReference());
                fillBox.scaleToFit(8,8);
                if(null != party.getDifficulty())
                    switch (party.getDifficulty()) {
                    case NON_LETHAL:
                        fillBox.setAbsolutePosition(127, 486);
                        break;
                    case NORMAL:
                        fillBox.setAbsolutePosition(127, 460);
                        break;
                    case HARDCORE:
                        fillBox.setAbsolutePosition(127, 432);
                        break;
                    case NIGHTMARE:
                        fillBox.setAbsolutePosition(127, 401);
                        break;
                    case EPIC:
                        fillBox.setAbsolutePosition(127, 369);
                        break;
                    default:
                        break;
                }
                over.addImage(fillBox);
 
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

    public PartyCharacter getCharacterInfo(CharacterDetails cd, PartyEnhancements enhancements) {
        PartyCharacter pc = PartyCharacter.fromCharacterDetails(cd);

        // Check GoGF and Averice 
        if(cd.getItems().stream().filter((item) -> item.getItemId()!=null&&(item.getItemId().equals("0448ddb1214a3f5c03af24653383d507fa0ea85c")||item.getItemId().equals("853d3fae881bf8907db600f8c4fc6d09e3e8f34d"))).count() > 0) {
            enhancements.setCharmOfGoodFortune(enhancements.getCharmOfGoodFortune()+1);
            pc.setHasCoGF(true);
        }

        // Check CoS
        if(cd.getItems().stream().filter((item) -> item.getItemId()!=null&&(item.getItemId().equals("333788757f49e48272a25e8b5994ac6503ad2adc")||item.getItemId().equals("63cc231ebcbb18e23c9979ba26b38f3ff9f21d92"))).count() > 0) {
            enhancements.setCharmOfSynergy(enhancements.getCharmOfSynergy()+1);
            pc.setHasCoS(true);
        }

        // Check CoA
        enhancements.setCharmOfAwareness(enhancements.getCharmOfAwareness()+cd.getStats().getInitiative());
        
        // Check Earcuff of the Phalanx
        if(cd.getItems().stream().distinct().filter((item) -> item.getItemId()!=null&&(item.getItemId().equals("b8291a0675a0230bce3102b5d91e06068610b6c6"))).count() > 0){
            enhancements.setEarcuffOfThePhalanx(enhancements.getEarcuffOfThePhalanx()+1);
            pc.setHasEotP(true);
        }
        
        // Check Shield of the Phalanx
        if(cd.getItems().stream().distinct().filter((item) -> item.getItemId()!=null&&(item.getItemId().equals("1bbf38e319f899841c7cbef78516229b0174c644"))).count() > 0){
            enhancements.setSheildOfThePhalanx(enhancements.getSheildOfThePhalanx()+1);
            pc.setHasSotP(true);
        }
        
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

        // Check for level boosting items
        if(cd.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("f44d007c35b18b83e85a1ee183cda08180030012"))).count() > 0) {
            enhancements.setLevelEnhancer(true);
        }
       
        if(userPrincipalProvider.getUserPrincipal().getSub() == null || !userPrincipalProvider.getUserPrincipal().getSub().equals(cd.getUserId()))
            pc.setUserName(cd.getUsername());
                
        return pc;
    }
    
    private void partyCardSetDetails(String prefix,PartyCharacter character, AcroFields fields, PdfStamper stamper) throws IOException, DocumentException {
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
            fields.setField(prefix+"Ret", Integer.toString(character.getRetDmg()));
            fields.setField(prefix+"SpellDamage", Integer.toString(character.getSpellDmg()));
            fields.setField(prefix+"SpellHeal", Integer.toString(character.getSpellHeal()));
            fields.setField(prefix+"SpellResist", Integer.toString(character.getSpellResist()));
            fields.setField(prefix+"TreasureHigh", Integer.toString(character.getTreasure()/10));
            fields.setField(prefix+"TreasureLow", Integer.toString(character.getTreasure()%10));
        }
        
        if(character == null)
            character = PartyCharacter.builder()
                    .meleeCold(false)
                    .meleeShock(false)
                    .meleeSonic(false)
                    .meleeDarkrift(false)
                    .meleeFire(false)
                    .meleeEldritch(false)
                    .meleePoison(false)
                    .meleeSacred(false)
                    .rangeCold(false)
                    .rangeShock(false)
                    .rangeSonic(false)
                    .rangeDarkrift(false)
                    .rangeFire(false)
                    .rangeEldritch(false)
                    .rangePoison(false)
                    .rangeSacred(false)
                    .retCold(false)
                    .retShock(false)
                    .retSonic(false)
                    .retDarkrift(false)
                    .retFire(false)
                    .retEldritch(false)
                    .retPoison(false)
                    .retSacred(false)
                    .build();
        
        Image cold= Image.getInstance(new ClassPathResource("cold.png").getURL());
        Image shock = Image.getInstance(new ClassPathResource("shock.png").getURL());
        Image sonic = Image.getInstance(new ClassPathResource("sonic.png").getURL());
        Image darkrift = Image.getInstance(new ClassPathResource("darkrift.png").getURL());
        Image fire = Image.getInstance(new ClassPathResource("fire.png").getURL());
        Image eldritch = Image.getInstance(new ClassPathResource("eldritch.png").getURL());
        Image poison = Image.getInstance(new ClassPathResource("poison.png").getURL());
        Image sacred = Image.getInstance(new ClassPathResource("sacred.png").getURL());
        Image coldHighlight = Image.getInstance(new ClassPathResource("cold-c.png").getURL());
        Image shockHighlight = Image.getInstance(new ClassPathResource("shock-c.png").getURL());
        Image sonicHighlight = Image.getInstance(new ClassPathResource("sonic-c.png").getURL());
        Image darkriftHighlight = Image.getInstance(new ClassPathResource("darkrift-c.png").getURL());
        Image fireHighlight = Image.getInstance(new ClassPathResource("fire-c.png").getURL());
        Image eldritchHighlight = Image.getInstance(new ClassPathResource("eldritch-c.png").getURL());
        Image poisonHighlight = Image.getInstance(new ClassPathResource("poison-c.png").getURL());
        Image sacredHighlight = Image.getInstance(new ClassPathResource("sacred-c.png").getURL());
        Image check = Image.getInstance(new ClassPathResource("check.png").getURL());
        PdfContentByte over = stamper.getOverContent(1);

        Image meleeCold = character.isMeleeCold()?coldHighlight:cold;
        PdfImage stream = new PdfImage(meleeCold, "", null);
        stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
        PdfIndirectObject ref = stamper.getWriter().addToBody(stream);
        meleeCold.setDirectReference(ref.getIndirectReference());
        meleeCold.scaleToFit(8,8);

        Image meleeShock = character.isMeleeShock()?shockHighlight:shock;
        stream = new PdfImage(meleeShock, "", null);
        stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
        ref = stamper.getWriter().addToBody(stream);
        meleeShock.setDirectReference(ref.getIndirectReference());
        meleeShock.scaleToFit(8,8);

        Image meleeSonic = character.isMeleeSonic()?sonicHighlight:sonic;
        stream = new PdfImage(meleeSonic, "", null);
        stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
        ref = stamper.getWriter().addToBody(stream);
        meleeSonic.setDirectReference(ref.getIndirectReference());
        meleeSonic.scaleToFit(8,8);

        Image meleeDarkrift = character.isMeleeDarkrift()?darkriftHighlight:darkrift;
        stream = new PdfImage(meleeDarkrift, "", null);
        stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
        ref = stamper.getWriter().addToBody(stream);
        meleeDarkrift.setDirectReference(ref.getIndirectReference());
        meleeDarkrift.scaleToFit(8,8);

        Image meleeFire = character.isMeleeFire()?fireHighlight:fire;
        stream = new PdfImage(meleeFire, "", null);
        stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
        ref = stamper.getWriter().addToBody(stream);
        meleeFire.setDirectReference(ref.getIndirectReference());
        meleeFire.scaleToFit(8,8);

        Image meleeEldritch = character.isMeleeEldritch()?eldritchHighlight:eldritch;
        stream = new PdfImage(meleeEldritch, "", null);
        stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
        ref = stamper.getWriter().addToBody(stream);
        meleeEldritch.setDirectReference(ref.getIndirectReference());
        meleeEldritch.scaleToFit(8,8);

        Image meleePoison = character.isMeleePoison()?poisonHighlight:poison;
        stream = new PdfImage(meleePoison, "", null);
        stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
        ref = stamper.getWriter().addToBody(stream);
        meleePoison.setDirectReference(ref.getIndirectReference());
        meleePoison.scaleToFit(8,8);

        Image meleeSacred = character.isMeleeSacred()?sacredHighlight:sacred;
        stream = new PdfImage(meleeSacred, "", null);
        stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
        ref = stamper.getWriter().addToBody(stream);
        meleeSacred.setDirectReference(ref.getIndirectReference());
        meleeSacred.scaleToFit(8,8);

        Image rangeCold = character.isRangeCold()?coldHighlight:cold;
        stream = new PdfImage(rangeCold, "", null);
        stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
        ref = stamper.getWriter().addToBody(stream);
        rangeCold.setDirectReference(ref.getIndirectReference());
        rangeCold.scaleToFit(8,8);

        Image rangeShock = character.isRangeShock()?shockHighlight:shock;
        stream = new PdfImage(rangeShock, "", null);
        stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
        ref = stamper.getWriter().addToBody(stream);
        rangeShock.setDirectReference(ref.getIndirectReference());
        rangeShock.scaleToFit(8,8);

        Image rangeSonic = character.isRangeSonic()?sonicHighlight:sonic;
        stream = new PdfImage(rangeSonic, "", null);
        stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
        ref = stamper.getWriter().addToBody(stream);
        rangeSonic.setDirectReference(ref.getIndirectReference());
        rangeSonic.scaleToFit(8,8);

        Image rangeDarkrift = character.isRangeDarkrift()?darkriftHighlight:darkrift;
        stream = new PdfImage(rangeDarkrift, "", null);
        stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
        ref = stamper.getWriter().addToBody(stream);
        rangeDarkrift.setDirectReference(ref.getIndirectReference());
        rangeDarkrift.scaleToFit(8,8);

        Image rangeFire = character.isRangeFire()?fireHighlight:fire;
        stream = new PdfImage(rangeFire, "", null);
        stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
        ref = stamper.getWriter().addToBody(stream);
        rangeFire.setDirectReference(ref.getIndirectReference());
        rangeFire.scaleToFit(8,8);

        Image rangeEldritch = character.isRangeEldritch()?eldritchHighlight:eldritch;
        stream = new PdfImage(rangeEldritch, "", null);
        stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
        ref = stamper.getWriter().addToBody(stream);
        rangeEldritch.setDirectReference(ref.getIndirectReference());
        rangeEldritch.scaleToFit(8,8);

        Image rangePoison = character.isRangePoison()?poisonHighlight:poison;
        stream = new PdfImage(rangePoison, "", null);
        stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
        ref = stamper.getWriter().addToBody(stream);
        rangePoison.setDirectReference(ref.getIndirectReference());
        rangePoison.scaleToFit(8,8);

        Image rangeSacred = character.isRangeSacred()?sacredHighlight:sacred;
        stream = new PdfImage(rangeSacred, "", null);
        stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
        ref = stamper.getWriter().addToBody(stream);
        rangeSacred.setDirectReference(ref.getIndirectReference());
        rangeSacred.scaleToFit(8,8);

        Image retCold = character.isRetCold()?coldHighlight:cold;
        stream = new PdfImage(retCold, "", null);
        stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
        ref = stamper.getWriter().addToBody(stream);
        retCold.setDirectReference(ref.getIndirectReference());
        retCold.scaleToFit(8,8);

        Image retShock = character.isRetShock()?shockHighlight:shock;
        stream = new PdfImage(retShock, "", null);
        stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
        ref = stamper.getWriter().addToBody(stream);
        retShock.setDirectReference(ref.getIndirectReference());
        retShock.scaleToFit(8,8);

        Image retSonic = character.isRetSonic()?sonicHighlight:sonic;
        stream = new PdfImage(retSonic, "", null);
        stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
        ref = stamper.getWriter().addToBody(stream);
        retSonic.setDirectReference(ref.getIndirectReference());
        retSonic.scaleToFit(8,8);

        Image retDarkrift = character.isRetDarkrift()?darkriftHighlight:darkrift;
        stream = new PdfImage(retDarkrift, "", null);
        stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
        ref = stamper.getWriter().addToBody(stream);
        retDarkrift.setDirectReference(ref.getIndirectReference());
        retDarkrift.scaleToFit(8,8);

        Image retFire = character.isRetFire()?fireHighlight:fire;
        stream = new PdfImage(retFire, "", null);
        stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
        ref = stamper.getWriter().addToBody(stream);
        retFire.setDirectReference(ref.getIndirectReference());
        retFire.scaleToFit(8,8);

        Image retEldritch = character.isRetEldritch()?eldritchHighlight:eldritch;
        stream = new PdfImage(retEldritch, "", null);
        stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
        ref = stamper.getWriter().addToBody(stream);
        retEldritch.setDirectReference(ref.getIndirectReference());
        retEldritch.scaleToFit(8,8);

        Image retPoison = character.isRetPoison()?poisonHighlight:poison;
        stream = new PdfImage(retPoison, "", null);
        stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
        ref = stamper.getWriter().addToBody(stream);
        retPoison.setDirectReference(ref.getIndirectReference());
        retPoison.scaleToFit(8,8);

        Image retSacred = character.isRetSacred()?sacredHighlight:sacred;
        stream = new PdfImage(retSacred, "", null);
        stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
        ref = stamper.getWriter().addToBody(stream);
        retSacred.setDirectReference(ref.getIndirectReference());
        retSacred.scaleToFit(8,8);
        
        switch (prefix) {
            case "barb":
                meleeCold.setAbsolutePosition(241, 534);
                over.addImage(meleeCold);
                meleeShock.setAbsolutePosition(241, 524);
                over.addImage(meleeShock);
                meleeSonic.setAbsolutePosition(241, 514);
                over.addImage(meleeSonic);
                meleeDarkrift.setAbsolutePosition(241, 504);
                over.addImage(meleeDarkrift);
                meleeFire.setAbsolutePosition(285, 534);
                over.addImage(meleeFire);
                meleeEldritch.setAbsolutePosition(285, 524);
                over.addImage(meleeEldritch);
                meleePoison.setAbsolutePosition(285, 514);
                over.addImage(meleePoison);
                meleeSacred.setAbsolutePosition(285, 504);
                over.addImage(meleeSacred);
                rangeCold.setAbsolutePosition(386, 534);
                over.addImage(rangeCold);
                rangeShock.setAbsolutePosition(386, 524);
                over.addImage(rangeShock);
                rangeSonic.setAbsolutePosition(386, 514);
                over.addImage(rangeSonic);
                rangeDarkrift.setAbsolutePosition(386, 504);
                over.addImage(rangeDarkrift);
                rangeFire.setAbsolutePosition(430, 534);
                over.addImage(rangeFire);
                rangeEldritch.setAbsolutePosition(430, 524);
                over.addImage(rangeEldritch);
                rangePoison.setAbsolutePosition(430, 514);
                over.addImage(rangePoison);
                rangeSacred.setAbsolutePosition(430, 504);
                over.addImage(rangeSacred);
                retCold.setAbsolutePosition(646, 534);
                over.addImage(retCold);
                retShock.setAbsolutePosition(646, 524);
                over.addImage(retShock);
                retSonic.setAbsolutePosition(646, 514);
                over.addImage(retSonic);
                retDarkrift.setAbsolutePosition(646, 504);
                over.addImage(retDarkrift);
                retFire.setAbsolutePosition(668, 534);
                over.addImage(retFire);
                retEldritch.setAbsolutePosition(668, 524);
                over.addImage(retEldritch);
                retPoison.setAbsolutePosition(668, 514);
                over.addImage(retPoison);
                retSacred.setAbsolutePosition(668, 504);
                over.addImage(retSacred);
                if(character.isCannotBeSuprised()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(686, 512);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   if(character.isFreeMovement()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(718, 512);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   if(character.isPsychic()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(752, 512);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   break;
            case "bard":
                meleeCold.setAbsolutePosition(241, 490);
                over.addImage(meleeCold);
                meleeShock.setAbsolutePosition(241, 480);
                over.addImage(meleeShock);
                meleeSonic.setAbsolutePosition(241, 470);
                over.addImage(meleeSonic);
                meleeDarkrift.setAbsolutePosition(241, 460);
                over.addImage(meleeDarkrift);
                meleeFire.setAbsolutePosition(285, 490);
                over.addImage(meleeFire);
                meleeEldritch.setAbsolutePosition(285, 480);
                over.addImage(meleeEldritch);
                meleePoison.setAbsolutePosition(285, 470);
                over.addImage(meleePoison);
                meleeSacred.setAbsolutePosition(285, 460);
                over.addImage(meleeSacred);
                rangeCold.setAbsolutePosition(386, 490);
                over.addImage(rangeCold);
                rangeShock.setAbsolutePosition(386, 480);
                over.addImage(rangeShock);
                rangeSonic.setAbsolutePosition(386, 470);
                over.addImage(rangeSonic);
                rangeDarkrift.setAbsolutePosition(386, 460);
                over.addImage(rangeDarkrift);
                rangeFire.setAbsolutePosition(430, 490);
                over.addImage(rangeFire);
                rangeEldritch.setAbsolutePosition(430, 480);
                over.addImage(rangeEldritch);
                rangePoison.setAbsolutePosition(430, 470);
                over.addImage(rangePoison);
                rangeSacred.setAbsolutePosition(430, 460);
                over.addImage(rangeSacred);
                retCold.setAbsolutePosition(646, 490);
                over.addImage(retCold);
                retShock.setAbsolutePosition(646, 480);
                over.addImage(retShock);
                retSonic.setAbsolutePosition(646, 470);
                over.addImage(retSonic);
                retDarkrift.setAbsolutePosition(646, 460);
                over.addImage(retDarkrift);
                retFire.setAbsolutePosition(668, 490);
                over.addImage(retFire);
                retEldritch.setAbsolutePosition(668, 480);
                over.addImage(retEldritch);
                retPoison.setAbsolutePosition(668, 470);
                over.addImage(retPoison);
                retSacred.setAbsolutePosition(668, 460);
                over.addImage(retSacred);
                if(character.isCannotBeSuprised()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(686, 468);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   if(character.isFreeMovement()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(718, 468);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   if(character.isPsychic()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(752, 468);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   break;
            case "cleric":
                meleeCold.setAbsolutePosition(241, 446);
                over.addImage(meleeCold);
                meleeShock.setAbsolutePosition(241, 436);
                over.addImage(meleeShock);
                meleeSonic.setAbsolutePosition(241, 426);
                over.addImage(meleeSonic);
                meleeDarkrift.setAbsolutePosition(241, 416);
                over.addImage(meleeDarkrift);
                meleeFire.setAbsolutePosition(285, 446);
                over.addImage(meleeFire);
                meleeEldritch.setAbsolutePosition(285, 436);
                over.addImage(meleeEldritch);
                meleePoison.setAbsolutePosition(285, 426);
                over.addImage(meleePoison);
                meleeSacred.setAbsolutePosition(285, 416);
                over.addImage(meleeSacred);
                rangeCold.setAbsolutePosition(386, 446);
                over.addImage(rangeCold);
                rangeShock.setAbsolutePosition(386, 436);
                over.addImage(rangeShock);
                rangeSonic.setAbsolutePosition(386, 426);
                over.addImage(rangeSonic);
                rangeDarkrift.setAbsolutePosition(386, 416);
                over.addImage(rangeDarkrift);
                rangeFire.setAbsolutePosition(430, 446);
                over.addImage(rangeFire);
                rangeEldritch.setAbsolutePosition(430, 436);
                over.addImage(rangeEldritch);
                rangePoison.setAbsolutePosition(430, 426);
                over.addImage(rangePoison);
                rangeSacred.setAbsolutePosition(430, 416);
                over.addImage(rangeSacred);
                retCold.setAbsolutePosition(646, 446);
                over.addImage(retCold);
                retShock.setAbsolutePosition(646, 436);
                over.addImage(retShock);
                retSonic.setAbsolutePosition(646, 426);
                over.addImage(retSonic);
                retDarkrift.setAbsolutePosition(646, 416);
                over.addImage(retDarkrift);
                retFire.setAbsolutePosition(668, 446);
                over.addImage(retFire);
                retEldritch.setAbsolutePosition(668, 436);
                over.addImage(retEldritch);
                retPoison.setAbsolutePosition(668, 426);
                over.addImage(retPoison);
                retSacred.setAbsolutePosition(668, 416);
                over.addImage(retSacred);
                if(character.isCannotBeSuprised()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(686, 424);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   if(character.isFreeMovement()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(718, 424);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   if(character.isPsychic()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(752, 424);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   break;
            case "druid":
                meleeCold.setAbsolutePosition(241, 402);
                over.addImage(meleeCold);
                meleeShock.setAbsolutePosition(241, 392);
                over.addImage(meleeShock);
                meleeSonic.setAbsolutePosition(241, 382);
                over.addImage(meleeSonic);
                meleeDarkrift.setAbsolutePosition(241, 372);
                over.addImage(meleeDarkrift);
                meleeFire.setAbsolutePosition(285, 402);
                over.addImage(meleeFire);
                meleeEldritch.setAbsolutePosition(285, 392);
                over.addImage(meleeEldritch);
                meleePoison.setAbsolutePosition(285, 382);
                over.addImage(meleePoison);
                meleeSacred.setAbsolutePosition(285, 372);
                over.addImage(meleeSacred);
                rangeCold.setAbsolutePosition(386, 402);
                over.addImage(rangeCold);
                rangeShock.setAbsolutePosition(386, 392);
                over.addImage(rangeShock);
                rangeSonic.setAbsolutePosition(386, 382);
                over.addImage(rangeSonic);
                rangeDarkrift.setAbsolutePosition(386, 372);
                over.addImage(rangeDarkrift);
                rangeFire.setAbsolutePosition(430, 402);
                over.addImage(rangeFire);
                rangeEldritch.setAbsolutePosition(430, 392);
                over.addImage(rangeEldritch);
                rangePoison.setAbsolutePosition(430, 382);
                over.addImage(rangePoison);
                rangeSacred.setAbsolutePosition(430, 372);
                over.addImage(rangeSacred);
                retCold.setAbsolutePosition(646, 402);
                over.addImage(retCold);
                retShock.setAbsolutePosition(646, 392);
                over.addImage(retShock);
                retSonic.setAbsolutePosition(646, 382);
                over.addImage(retSonic);
                retDarkrift.setAbsolutePosition(646, 372);
                over.addImage(retDarkrift);
                retFire.setAbsolutePosition(668, 402);
                over.addImage(retFire);
                retEldritch.setAbsolutePosition(668, 392);
                over.addImage(retEldritch);
                retPoison.setAbsolutePosition(668, 382);
                over.addImage(retPoison);
                retSacred.setAbsolutePosition(668, 372);
                over.addImage(retSacred);
                if(character.isCannotBeSuprised()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(686, 380);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   if(character.isFreeMovement()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(718, 380);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   if(character.isPsychic()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(752, 380);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   break;
            case "dfighter":
                meleeCold.setAbsolutePosition(241, 358);
                over.addImage(meleeCold);
                meleeShock.setAbsolutePosition(241, 348);
                over.addImage(meleeShock);
                meleeSonic.setAbsolutePosition(241, 338);
                over.addImage(meleeSonic);
                meleeDarkrift.setAbsolutePosition(241, 328);
                over.addImage(meleeDarkrift);
                meleeFire.setAbsolutePosition(285, 358);
                over.addImage(meleeFire);
                meleeEldritch.setAbsolutePosition(285, 348);
                over.addImage(meleeEldritch);
                meleePoison.setAbsolutePosition(285, 338);
                over.addImage(meleePoison);
                meleeSacred.setAbsolutePosition(285, 328);
                over.addImage(meleeSacred);
                rangeCold.setAbsolutePosition(386, 358);
                over.addImage(rangeCold);
                rangeShock.setAbsolutePosition(386, 348);
                over.addImage(rangeShock);
                rangeSonic.setAbsolutePosition(386, 338);
                over.addImage(rangeSonic);
                rangeDarkrift.setAbsolutePosition(386, 328);
                over.addImage(rangeDarkrift);
                rangeFire.setAbsolutePosition(430, 358);
                over.addImage(rangeFire);
                rangeEldritch.setAbsolutePosition(430, 348);
                over.addImage(rangeEldritch);
                rangePoison.setAbsolutePosition(430, 338);
                over.addImage(rangePoison);
                rangeSacred.setAbsolutePosition(430, 328);
                over.addImage(rangeSacred);
                retCold.setAbsolutePosition(646, 358);
                over.addImage(retCold);
                retShock.setAbsolutePosition(646, 348);
                over.addImage(retShock);
                retSonic.setAbsolutePosition(646, 338);
                over.addImage(retSonic);
                retDarkrift.setAbsolutePosition(646, 328);
                over.addImage(retDarkrift);
                retFire.setAbsolutePosition(668, 358);
                over.addImage(retFire);
                retEldritch.setAbsolutePosition(668, 348);
                over.addImage(retEldritch);
                retPoison.setAbsolutePosition(668, 338);
                over.addImage(retPoison);
                retSacred.setAbsolutePosition(668, 328);
                over.addImage(retSacred);
                if(character.isCannotBeSuprised()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(686, 336);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   if(character.isFreeMovement()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(718, 336);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   if(character.isPsychic()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(752, 336);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   break;
            case "ewizard":
                meleeCold.setAbsolutePosition(241, 315);
                over.addImage(meleeCold);
                meleeShock.setAbsolutePosition(241, 305);
                over.addImage(meleeShock);
                meleeSonic.setAbsolutePosition(241, 295);
                over.addImage(meleeSonic);
                meleeDarkrift.setAbsolutePosition(241, 285);
                over.addImage(meleeDarkrift);
                meleeFire.setAbsolutePosition(285, 315);
                over.addImage(meleeFire);
                meleeEldritch.setAbsolutePosition(285, 305);
                over.addImage(meleeEldritch);
                meleePoison.setAbsolutePosition(285, 295);
                over.addImage(meleePoison);
                meleeSacred.setAbsolutePosition(285, 285);
                over.addImage(meleeSacred);
                rangeCold.setAbsolutePosition(386, 315);
                over.addImage(rangeCold);
                rangeShock.setAbsolutePosition(386, 305);
                over.addImage(rangeShock);
                rangeSonic.setAbsolutePosition(386, 295);
                over.addImage(rangeSonic);
                rangeDarkrift.setAbsolutePosition(386, 285);
                over.addImage(rangeDarkrift);
                rangeFire.setAbsolutePosition(430, 315);
                over.addImage(rangeFire);
                rangeEldritch.setAbsolutePosition(430, 305);
                over.addImage(rangeEldritch);
                rangePoison.setAbsolutePosition(430, 295);
                over.addImage(rangePoison);
                rangeSacred.setAbsolutePosition(430, 285);
                over.addImage(rangeSacred);
                retCold.setAbsolutePosition(646, 315);
                over.addImage(retCold);
                retShock.setAbsolutePosition(646, 305);
                over.addImage(retShock);
                retSonic.setAbsolutePosition(646, 295);
                over.addImage(retSonic);
                retDarkrift.setAbsolutePosition(646, 285);
                over.addImage(retDarkrift);
                retFire.setAbsolutePosition(668, 315);
                over.addImage(retFire);
                retEldritch.setAbsolutePosition(668, 305);
                over.addImage(retEldritch);
                retPoison.setAbsolutePosition(668, 295);
                over.addImage(retPoison);
                retSacred.setAbsolutePosition(668, 285);
                over.addImage(retSacred);
                if(character.isCannotBeSuprised()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(686, 293);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   if(character.isFreeMovement()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(718, 293);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   if(character.isPsychic()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(752, 293);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   break;
            case "fighter":
                meleeCold.setAbsolutePosition(241, 270);
                over.addImage(meleeCold);
                meleeShock.setAbsolutePosition(241, 260);
                over.addImage(meleeShock);
                meleeSonic.setAbsolutePosition(241, 250);
                over.addImage(meleeSonic);
                meleeDarkrift.setAbsolutePosition(241, 240);
                over.addImage(meleeDarkrift);
                meleeFire.setAbsolutePosition(285, 270);
                over.addImage(meleeFire);
                meleeEldritch.setAbsolutePosition(285, 260);
                over.addImage(meleeEldritch);
                meleePoison.setAbsolutePosition(285, 250);
                over.addImage(meleePoison);
                meleeSacred.setAbsolutePosition(285, 240);
                over.addImage(meleeSacred);
                rangeCold.setAbsolutePosition(386, 270);
                over.addImage(rangeCold);
                rangeShock.setAbsolutePosition(386, 260);
                over.addImage(rangeShock);
                rangeSonic.setAbsolutePosition(386, 250);
                over.addImage(rangeSonic);
                rangeDarkrift.setAbsolutePosition(386, 240);
                over.addImage(rangeDarkrift);
                rangeFire.setAbsolutePosition(430, 270);
                over.addImage(rangeFire);
                rangeEldritch.setAbsolutePosition(430, 260);
                over.addImage(rangeEldritch);
                rangePoison.setAbsolutePosition(430, 250);
                over.addImage(rangePoison);
                rangeSacred.setAbsolutePosition(430, 240);
                over.addImage(rangeSacred);
                retCold.setAbsolutePosition(646, 270);
                over.addImage(retCold);
                retShock.setAbsolutePosition(646, 260);
                over.addImage(retShock);
                retSonic.setAbsolutePosition(646, 250);
                over.addImage(retSonic);
                retDarkrift.setAbsolutePosition(646, 240);
                over.addImage(retDarkrift);
                retFire.setAbsolutePosition(668, 270);
                over.addImage(retFire);
                retEldritch.setAbsolutePosition(668, 260);
                over.addImage(retEldritch);
                retPoison.setAbsolutePosition(668, 250);
                over.addImage(retPoison);
                retSacred.setAbsolutePosition(668, 240);
                over.addImage(retSacred);
                if(character.isCannotBeSuprised()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(686, 248);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   if(character.isFreeMovement()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(718, 248);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   if(character.isPsychic()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(752, 248);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   break;
            case "monk":
                meleeCold.setAbsolutePosition(241, 226);
                over.addImage(meleeCold);
                meleeShock.setAbsolutePosition(241, 216);
                over.addImage(meleeShock);
                meleeSonic.setAbsolutePosition(241, 206);
                over.addImage(meleeSonic);
                meleeDarkrift.setAbsolutePosition(241, 196);
                over.addImage(meleeDarkrift);
                meleeFire.setAbsolutePosition(285, 226);
                over.addImage(meleeFire);
                meleeEldritch.setAbsolutePosition(285, 216);
                over.addImage(meleeEldritch);
                meleePoison.setAbsolutePosition(285, 206);
                over.addImage(meleePoison);
                meleeSacred.setAbsolutePosition(285, 196);
                over.addImage(meleeSacred);
                rangeCold.setAbsolutePosition(386, 226);
                over.addImage(rangeCold);
                rangeShock.setAbsolutePosition(386, 216);
                over.addImage(rangeShock);
                rangeSonic.setAbsolutePosition(386, 206);
                over.addImage(rangeSonic);
                rangeDarkrift.setAbsolutePosition(386, 196);
                over.addImage(rangeDarkrift);
                rangeFire.setAbsolutePosition(430, 226);
                over.addImage(rangeFire);
                rangeEldritch.setAbsolutePosition(430, 216);
                over.addImage(rangeEldritch);
                rangePoison.setAbsolutePosition(430, 206);
                over.addImage(rangePoison);
                rangeSacred.setAbsolutePosition(430, 196);
                over.addImage(rangeSacred);
                retCold.setAbsolutePosition(646, 226);
                over.addImage(retCold);
                retShock.setAbsolutePosition(646, 216);
                over.addImage(retShock);
                retSonic.setAbsolutePosition(646, 206);
                over.addImage(retSonic);
                retDarkrift.setAbsolutePosition(646, 196);
                over.addImage(retDarkrift);
                retFire.setAbsolutePosition(668, 226);
                over.addImage(retFire);
                retEldritch.setAbsolutePosition(668, 216);
                over.addImage(retEldritch);
                retPoison.setAbsolutePosition(668, 206);
                over.addImage(retPoison);
                retSacred.setAbsolutePosition(668, 196);
                over.addImage(retSacred);
                if(character.isCannotBeSuprised()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(686, 204);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   if(character.isFreeMovement()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(718, 204);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   if(character.isPsychic()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(752, 204);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   break;
            case "paladin":
                meleeCold.setAbsolutePosition(241, 183);
                over.addImage(meleeCold);
                meleeShock.setAbsolutePosition(241, 173);
                over.addImage(meleeShock);
                meleeSonic.setAbsolutePosition(241, 163);
                over.addImage(meleeSonic);
                meleeDarkrift.setAbsolutePosition(241, 153);
                over.addImage(meleeDarkrift);
                meleeFire.setAbsolutePosition(285, 183);
                over.addImage(meleeFire);
                meleeEldritch.setAbsolutePosition(285, 173);
                over.addImage(meleeEldritch);
                meleePoison.setAbsolutePosition(285, 163);
                over.addImage(meleePoison);
                meleeSacred.setAbsolutePosition(285, 153);
                over.addImage(meleeSacred);
                rangeCold.setAbsolutePosition(386, 183);
                over.addImage(rangeCold);
                rangeShock.setAbsolutePosition(386, 173);
                over.addImage(rangeShock);
                rangeSonic.setAbsolutePosition(386, 163);
                over.addImage(rangeSonic);
                rangeDarkrift.setAbsolutePosition(386, 153);
                over.addImage(rangeDarkrift);
                rangeFire.setAbsolutePosition(430, 183);
                over.addImage(rangeFire);
                rangeEldritch.setAbsolutePosition(430, 173);
                over.addImage(rangeEldritch);
                rangePoison.setAbsolutePosition(430, 163);
                over.addImage(rangePoison);
                rangeSacred.setAbsolutePosition(430, 153);
                over.addImage(rangeSacred);
                retCold.setAbsolutePosition(646, 183);
                over.addImage(retCold);
                retShock.setAbsolutePosition(646, 173);
                over.addImage(retShock);
                retSonic.setAbsolutePosition(646, 163);
                over.addImage(retSonic);
                retDarkrift.setAbsolutePosition(646, 153);
                over.addImage(retDarkrift);
                retFire.setAbsolutePosition(668, 183);
                over.addImage(retFire);
                retEldritch.setAbsolutePosition(668, 173);
                over.addImage(retEldritch);
                retPoison.setAbsolutePosition(668, 163);
                over.addImage(retPoison);
                retSacred.setAbsolutePosition(668, 153);
                over.addImage(retSacred);
                if(character.isCannotBeSuprised()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(686, 161);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   if(character.isFreeMovement()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(718, 161);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   if(character.isPsychic()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(752, 161);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   break;
            case "ranger":
                meleeCold.setAbsolutePosition(241, 139);
                over.addImage(meleeCold);
                meleeShock.setAbsolutePosition(241, 129);
                over.addImage(meleeShock);
                meleeSonic.setAbsolutePosition(241, 119);
                over.addImage(meleeSonic);
                meleeDarkrift.setAbsolutePosition(241, 109);
                over.addImage(meleeDarkrift);
                meleeFire.setAbsolutePosition(285, 139);
                over.addImage(meleeFire);
                meleeEldritch.setAbsolutePosition(285, 129);
                over.addImage(meleeEldritch);
                meleePoison.setAbsolutePosition(285, 119);
                over.addImage(meleePoison);
                meleeSacred.setAbsolutePosition(285, 109);
                over.addImage(meleeSacred);
                rangeCold.setAbsolutePosition(386, 139);
                over.addImage(rangeCold);
                rangeShock.setAbsolutePosition(386, 129);
                over.addImage(rangeShock);
                rangeSonic.setAbsolutePosition(386, 119);
                over.addImage(rangeSonic);
                rangeDarkrift.setAbsolutePosition(386, 109);
                over.addImage(rangeDarkrift);
                rangeFire.setAbsolutePosition(430, 139);
                over.addImage(rangeFire);
                rangeEldritch.setAbsolutePosition(430, 129);
                over.addImage(rangeEldritch);
                rangePoison.setAbsolutePosition(430, 119);
                over.addImage(rangePoison);
                rangeSacred.setAbsolutePosition(430, 109);
                over.addImage(rangeSacred);
                retCold.setAbsolutePosition(646, 139);
                over.addImage(retCold);
                retShock.setAbsolutePosition(646, 129);
                over.addImage(retShock);
                retSonic.setAbsolutePosition(646, 119);
                over.addImage(retSonic);
                retDarkrift.setAbsolutePosition(646, 109);
                over.addImage(retDarkrift);
                retFire.setAbsolutePosition(668, 139);
                over.addImage(retFire);
                retEldritch.setAbsolutePosition(668, 129);
                over.addImage(retEldritch);
                retPoison.setAbsolutePosition(668, 119);
                over.addImage(retPoison);
                retSacred.setAbsolutePosition(668, 109);
                over.addImage(retSacred);
                if(character.isCannotBeSuprised()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(686, 117);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   if(character.isFreeMovement()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(718, 117);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   if(character.isPsychic()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(752, 117);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   break;
            case "rogue":
                meleeCold.setAbsolutePosition(241, 95);
                over.addImage(meleeCold);
                meleeShock.setAbsolutePosition(241, 85);
                over.addImage(meleeShock);
                meleeSonic.setAbsolutePosition(241, 75);
                over.addImage(meleeSonic);
                meleeDarkrift.setAbsolutePosition(241, 65);
                over.addImage(meleeDarkrift);
                meleeFire.setAbsolutePosition(285, 95);
                over.addImage(meleeFire);
                meleeEldritch.setAbsolutePosition(285, 85);
                over.addImage(meleeEldritch);
                meleePoison.setAbsolutePosition(285, 75);
                over.addImage(meleePoison);
                meleeSacred.setAbsolutePosition(285, 65);
                over.addImage(meleeSacred);
                rangeCold.setAbsolutePosition(386, 95);
                over.addImage(rangeCold);
                rangeShock.setAbsolutePosition(386, 85);
                over.addImage(rangeShock);
                rangeSonic.setAbsolutePosition(386, 75);
                over.addImage(rangeSonic);
                rangeDarkrift.setAbsolutePosition(386, 65);
                over.addImage(rangeDarkrift);
                rangeFire.setAbsolutePosition(430, 95);
                over.addImage(rangeFire);
                rangeEldritch.setAbsolutePosition(430, 85);
                over.addImage(rangeEldritch);
                rangePoison.setAbsolutePosition(430, 75);
                over.addImage(rangePoison);
                rangeSacred.setAbsolutePosition(430, 65);
                over.addImage(rangeSacred);
                retCold.setAbsolutePosition(646, 95);
                over.addImage(retCold);
                retShock.setAbsolutePosition(646, 85);
                over.addImage(retShock);
                retSonic.setAbsolutePosition(646, 75);
                over.addImage(retSonic);
                retDarkrift.setAbsolutePosition(646, 65);
                over.addImage(retDarkrift);
                retFire.setAbsolutePosition(668, 95);
                over.addImage(retFire);
                retEldritch.setAbsolutePosition(668, 85);
                over.addImage(retEldritch);
                retPoison.setAbsolutePosition(668, 75);
                over.addImage(retPoison);
                retSacred.setAbsolutePosition(668, 65);
                over.addImage(retSacred);
                if(character.isCannotBeSuprised()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(686, 73);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   if(character.isFreeMovement()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(718, 73);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   if(character.isPsychic()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(752, 73);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   break;
            case "wizard":
                meleeCold.setAbsolutePosition(241, 51);
                over.addImage(meleeCold);
                meleeShock.setAbsolutePosition(241, 41);
                over.addImage(meleeShock);
                meleeSonic.setAbsolutePosition(241, 31);
                over.addImage(meleeSonic);
                meleeDarkrift.setAbsolutePosition(241, 21);
                over.addImage(meleeDarkrift);
                meleeFire.setAbsolutePosition(285, 51);
                over.addImage(meleeFire);
                meleeEldritch.setAbsolutePosition(285, 41);
                over.addImage(meleeEldritch);
                meleePoison.setAbsolutePosition(285, 31);
                over.addImage(meleePoison);
                meleeSacred.setAbsolutePosition(285, 21);
                over.addImage(meleeSacred);
                rangeCold.setAbsolutePosition(386, 51);
                over.addImage(rangeCold);
                rangeShock.setAbsolutePosition(386, 41);
                over.addImage(rangeShock);
                rangeSonic.setAbsolutePosition(386, 31);
                over.addImage(rangeSonic);
                rangeDarkrift.setAbsolutePosition(386, 21);
                over.addImage(rangeDarkrift);
                rangeFire.setAbsolutePosition(431, 51);
                over.addImage(rangeFire);
                rangeEldritch.setAbsolutePosition(431, 41);
                over.addImage(rangeEldritch);
                rangePoison.setAbsolutePosition(431, 31);
                over.addImage(rangePoison);
                rangeSacred.setAbsolutePosition(431, 21);
                over.addImage(rangeSacred);
                retCold.setAbsolutePosition(646, 51);
                over.addImage(retCold);
                retShock.setAbsolutePosition(646, 41);
                over.addImage(retShock);
                retSonic.setAbsolutePosition(646, 31);
                over.addImage(retSonic);
                retDarkrift.setAbsolutePosition(646, 21);
                over.addImage(retDarkrift);
                retFire.setAbsolutePosition(668, 51);
                over.addImage(retFire);
                retEldritch.setAbsolutePosition(668, 41);
                over.addImage(retEldritch);
                retPoison.setAbsolutePosition(668, 31);
                over.addImage(retPoison);
                retSacred.setAbsolutePosition(668, 21);
                over.addImage(retSacred);
                if(character.isCannotBeSuprised()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(686, 29);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   if(character.isFreeMovement()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(718, 29);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   if(character.isPsychic()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(752, 29);
                    check.scaleToFit(22,22);
                    over = stamper.getOverContent(1);
                    over.addImage(check);
                }   break;
            default:
                break;
        }
    }
}
