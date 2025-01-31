package com.achersoft.tdcc.character;

import com.achersoft.exception.InvalidDataException;
import com.achersoft.security.providers.UserPrincipalProvider;
import com.achersoft.tdcc.character.dao.*;
import com.achersoft.tdcc.character.persistence.CharacterMapper;
import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.enums.ConditionalUse;
import com.achersoft.tdcc.enums.Rarity;
import com.achersoft.tdcc.enums.Slot;
import com.achersoft.tdcc.enums.SlotStatus;
import com.achersoft.tdcc.party.dao.SelectableCharacters;
import com.achersoft.tdcc.party.persistence.PartyMapper;
import com.achersoft.tdcc.token.admin.dao.SlotModifier;
import com.achersoft.tdcc.token.admin.dao.TokenFullDetails;
import com.achersoft.tdcc.token.admin.persistence.TokenAdminMapper;
import com.achersoft.tdcc.token.persistence.TokenMapper;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.core.StreamingOutput;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;

public class CharacterServiceImpl implements CharacterService {
    
    private @Inject CharacterMapper mapper;
    private @Inject PartyMapper partyMapper;
    private @Inject TokenMapper tokenMapper;
    private @Inject TokenAdminMapper tokenAdminMapper;
    private @Inject UserPrincipalProvider userPrincipalProvider;

    @Override
    public void recalculateCharacters() {
        final List<String> allCharacters = mapper.getAllCharacters();
        int index = 0;
        System.err.println("Calculating stats for all characters #: " + allCharacters.size());
        for (String id : allCharacters) {
            System.err.println("Remaining ids: " + (allCharacters.size() - index) + " for id: " + id);
            //if (index % 50 == 0)
            //    System.err.println("Remaining ids: " + (allCharacters.size() - index));
            try {
                validateCharacterItems(id);
            } catch (Exception e) {
                System.err.println("Checking id: " + id +"\n" + e.getMessage());
            }
            index++;
        }
        System.err.println("Stat calculation complete");
    }

    @Override
    public CharacterDetails getCharacter(String id) {
        CharacterDetails characterDetails = mapper.getCharacter(id);
        
        if(characterDetails == null)
            throw new InvalidDataException("Requested character could not be found."); 
        
        if(userPrincipalProvider.getUserPrincipal().getSub() != null && userPrincipalProvider.getUserPrincipal().getSub().equalsIgnoreCase(characterDetails.getUserId()))
            characterDetails.setEditable(true);
        else
            characterDetails.setEditable(false);
        
        characterDetails.setStats(mapper.getCharacterStats(id));
        characterDetails.setItems(mapper.getCharacterItems(id));
        characterDetails.setNotes(mapper.getCharacterNotes(id));
        
        return characterDetails;
    }
    
    @Override
    public List<CharacterName> getCharacters() {
        return mapper.getCharacters(userPrincipalProvider.getUserPrincipal().getSub());
    }
    
    @Override
    public SelectableCharacters getSelectableCharacters(String userid) {
        if (userid == null || userid.isEmpty())
            userid = userPrincipalProvider.getUserPrincipal().getSub();
        
        return SelectableCharacters.builder()
                .characters(mapper.getCharacters(userid))
                .userAccounts(partyMapper.getUsers())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CharacterDetails setTokenSlot(String id, String soltId, String tokenId) {
        CharacterDetails characterDetails = getCharacter(id);
        
        if(!(userPrincipalProvider.getUserPrincipal().getSub() != null && userPrincipalProvider.getUserPrincipal().getSub().equalsIgnoreCase(characterDetails.getUserId())))
            throw new InvalidDataException("Operation is not allowed. Character does not belong to logged in user."); 


        if(characterDetails.getCharacterClass() == CharacterClass.MONK){
            if(characterDetails.getItems().stream().filter((item) -> item.getId().equalsIgnoreCase(soltId)&&item.getSlot()==Slot.RANGE_MAINHAND).count() > 0) {
                characterDetails.getItems().stream().filter((item) -> item.getSlot()==Slot.RANGE_OFFHAND).findAny().ifPresent((item) -> {
                    tokenMapper.unequipTokenSlot(item.getId());
                });
            }
        }
        
        tokenMapper.setTokenSlot(soltId, tokenId);
        return validateCharacterItems(id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CharacterDetails unequipTokenSlot(String id, String soltId) {
        CharacterDetails characterDetails = getCharacter(id);
        
        if(!(userPrincipalProvider.getUserPrincipal().getSub() != null && userPrincipalProvider.getUserPrincipal().getSub().equalsIgnoreCase(characterDetails.getUserId())))
            throw new InvalidDataException("Operation is not allowed. Character does not belong to logged in user."); 
        
        if(characterDetails.getCharacterClass() == CharacterClass.MONK){
            if(characterDetails.getItems().stream().filter((item) -> item.getId().equalsIgnoreCase(soltId)&&item.getSlot()==Slot.RANGE_MAINHAND).count() > 0) {
                characterDetails.getItems().stream().filter((item) -> item.getSlot()==Slot.RANGE_OFFHAND).findAny().ifPresent((item) -> {
                    tokenMapper.unequipTokenSlot(item.getId());
                });
            }
        }
            
        tokenMapper.unequipTokenSlot(soltId);
        return validateCharacterItems(id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CharacterName> deleteCharacter(String id) {
        mapper.deleteCharacter(id, userPrincipalProvider.getUserPrincipal().getSub());
        return mapper.getCharacters(userPrincipalProvider.getUserPrincipal().getSub());
    }
    
    @Override
    public StreamingOutput exportCharacterPdf(String id) {
        final CharacterDetails character = getCharacter(id);
        if(character.getItems().stream().filter((item) -> item.getSlotStatus() == SlotStatus.INVALID).count() > 0)
            throw new InvalidDataException("The requested character is currently in a invalid state and cannot be exported. Please correct any item conflicts and try again."); 
        return (OutputStream out) -> {
            try {
                Resource pdfFile = new ClassPathResource("character.pdf");
                PdfReader reader = new PdfReader(pdfFile.getURL());
                PdfStamper stamper = new PdfStamper(reader, out);
                AcroFields fields = stamper.getAcroFields();
                //System.err.println(fields.getFields().keySet());
                
                fields.setField("characterName", character.getName());
                fields.setField("characterLevel", capitalize(character.getCharacterClass().name()) + " - Level " +Integer.toString(character.getStats().getLevel()));
                
                fields.setField("baseStr", Integer.toString(character.getStats().getBaseStr()));
                fields.setField("baseDex", Integer.toString(character.getStats().getBaseDex()));
                fields.setField("baseCon", Integer.toString(character.getStats().getBaseCon()));
                fields.setField("baseInt", Integer.toString(character.getStats().getBaseIntel()));
                fields.setField("baseWis", Integer.toString(character.getStats().getBaseWis()));
                fields.setField("baseCha", Integer.toString(character.getStats().getBaseCha()));
                fields.setField("totalStr", Integer.toString(character.getStats().getStr()));
                fields.setField("totalDex", Integer.toString(character.getStats().getDex()));
                fields.setField("totalCon", Integer.toString(character.getStats().getCon()));
                fields.setField("totalInt", Integer.toString(character.getStats().getIntel()));
                fields.setField("totalWis", Integer.toString(character.getStats().getWis()));
                fields.setField("totalCha", Integer.toString(character.getStats().getCha()));
                fields.setField("bonusStr", Integer.toString(character.getStats().getStrBonus()));
                fields.setField("bonusDex", Integer.toString(character.getStats().getDexBonus()));
                fields.setField("bonusCon", Integer.toString(character.getStats().getConBonus()));
                fields.setField("bonusInt", Integer.toString(character.getStats().getIntelBonus()));
                fields.setField("bonusWis", Integer.toString(character.getStats().getWisBonus()));
                fields.setField("bonusCha", Integer.toString(character.getStats().getChaBonus()));
                
                fields.setField("meleeHit", Integer.toString(character.getStats().getMeleeHit()));
                fields.setField("meleeDmg", Integer.toString(character.getStats().getMeleeDmg()));
                fields.setField("meleeAC", Integer.toString(character.getStats().getMeleeAC()));
                String meleeDmgTypes = "";
                if(character.getStats().isMeleeFire())
                    meleeDmgTypes += "Fire";
                if(character.getStats().isMeleeCold()) {
                    if(!meleeDmgTypes.isEmpty())
                        meleeDmgTypes += ", ";
                    meleeDmgTypes += "Cold";    
                }if(character.getStats().isMeleeShock()) {
                    if(!meleeDmgTypes.isEmpty())
                        meleeDmgTypes += ", ";
                    meleeDmgTypes += "Shock";    
                }if(character.getStats().isMeleeSonic()) {
                    if(!meleeDmgTypes.isEmpty())
                        meleeDmgTypes += ", ";
                    meleeDmgTypes += "Sonic";    
                }if(character.getStats().isMeleeEldritch()) {
                    if(!meleeDmgTypes.isEmpty())
                        meleeDmgTypes += ", ";
                    meleeDmgTypes += "Eldritch";    
                }if(character.getStats().isMeleePoison()) {
                    if(!meleeDmgTypes.isEmpty())
                        meleeDmgTypes += ", ";
                    meleeDmgTypes += "Poison";    
                }if(character.getStats().isMeleeDarkrift()) {
                    if(!meleeDmgTypes.isEmpty())
                        meleeDmgTypes += ", ";
                    meleeDmgTypes += "Darkrift";    
                }if(character.getStats().isMeleeSacred()) {
                    if(!meleeDmgTypes.isEmpty())
                        meleeDmgTypes += ", ";
                    meleeDmgTypes += "Sacred";    
                }if(meleeDmgTypes.isEmpty())
                    meleeDmgTypes += "None";    
                fields.setField("meleeDmgTypes", meleeDmgTypes);
                
                fields.setField("rangeHit", Integer.toString(character.getStats().getRangeHit()));
                fields.setField("rangeDmg", Integer.toString(character.getStats().getRangeDmg()));
                fields.setField("rangeAC", Integer.toString(character.getStats().getRangeAC()));
                fields.setField("rangeMissileAC", Integer.toString(character.getStats().getRangeMissileAC()));
                String rangeDmgTypes = "";
                if(character.getStats().isRangeFire())
                    rangeDmgTypes += "Fire";
                if(character.getStats().isRangeCold()) {
                    if(!rangeDmgTypes.isEmpty())
                        rangeDmgTypes += ", ";
                    rangeDmgTypes += "Cold";    
                }if(character.getStats().isRangeShock()) {
                    if(!rangeDmgTypes.isEmpty())
                        rangeDmgTypes += ", ";
                    rangeDmgTypes += "Shock";    
                }if(character.getStats().isRangeSonic()) {
                    if(!rangeDmgTypes.isEmpty())
                        rangeDmgTypes += ", ";
                    rangeDmgTypes += "Sonic";    
                }if(character.getStats().isRangeEldritch()) {
                    if(!rangeDmgTypes.isEmpty())
                        rangeDmgTypes += ", ";
                    rangeDmgTypes += "Eldritch";    
                }if(character.getStats().isRangePoison()) {
                    if(!rangeDmgTypes.isEmpty())
                        rangeDmgTypes += ", ";
                    rangeDmgTypes += "Poison";    
                }if(character.getStats().isRangeDarkrift()) {
                    if(!rangeDmgTypes.isEmpty())
                        rangeDmgTypes += ", ";
                    rangeDmgTypes += "Darkrift";    
                }if(character.getStats().isRangeSacred()) {
                    if(!rangeDmgTypes.isEmpty())
                        rangeDmgTypes += ", ";
                    rangeDmgTypes += "Sacred";    
                }if(rangeDmgTypes.isEmpty())
                    rangeDmgTypes += "None";    
                fields.setField("rangeDmgTypes", rangeDmgTypes);
                
                fields.setField("hitPoints", Integer.toString(character.getStats().getHealth()));
                fields.setField("regenPerRoom", Integer.toString(character.getStats().getRegen()));
                
                fields.setField("spellDmg", Integer.toString(character.getStats().getSpellDmg()));
                fields.setField("spellHeal", Integer.toString(character.getStats().getSpellHeal()));
                fields.setField("spellRes", Integer.toString(character.getStats().getSpellResist()));
                
                fields.setField("fortSave", Integer.toString(character.getStats().getFort()));
                fields.setField("reflexSave", Integer.toString(character.getStats().getReflex()));
                fields.setField("willSave", Integer.toString(character.getStats().getWill()));
                
                fields.setField("retDmg", Integer.toString(character.getStats().getRetDmg()));
                String retDmgTypes = "";
                if(character.getStats().isRetFire())
                    retDmgTypes += "Fire";
                if(character.getStats().isRetCold()) {
                    if(!retDmgTypes.isEmpty())
                        retDmgTypes += ", ";
                    retDmgTypes += "Cold";    
                }if(character.getStats().isRetShock()) {
                    if(!retDmgTypes.isEmpty())
                        retDmgTypes += ", ";
                    retDmgTypes += "Shock";    
                }if(character.getStats().isRetSonic()) {
                    if(!retDmgTypes.isEmpty())
                        retDmgTypes += ", ";
                    retDmgTypes += "Sonic";    
                }if(character.getStats().isRetEldritch()) {
                    if(!retDmgTypes.isEmpty())
                        retDmgTypes += ", ";
                    retDmgTypes += "Eldritch";    
                }if(character.getStats().isRetPoison()) {
                    if(!retDmgTypes.isEmpty())
                        retDmgTypes += ", ";
                    retDmgTypes += "Poison";    
                }if(character.getStats().isRetDarkrift()) {
                    if(!retDmgTypes.isEmpty())
                        retDmgTypes += ", ";
                    retDmgTypes += "Darkrift";    
                }if(character.getStats().isRetSacred()) {
                    if(!retDmgTypes.isEmpty())
                        retDmgTypes += ", ";
                    retDmgTypes += "Sacred";    
                }if(retDmgTypes.isEmpty())
                    retDmgTypes += "None";    
                fields.setField("retDmgTypes", retDmgTypes);
                
                fields.setField("drMelee", Integer.toString(character.getStats().getDrMelee()));
                fields.setField("drRange", Integer.toString(character.getStats().getDrRange()));
                fields.setField("drSpell", Integer.toString(character.getStats().getDrSpell()));
                fields.setField("drFire", Integer.toString(character.getStats().getDrFire()));
                fields.setField("drCold", Integer.toString(character.getStats().getDrCold()));
                fields.setField("drShock", Integer.toString(character.getStats().getDrShock()));
                fields.setField("drSonic", Integer.toString(character.getStats().getDrSonic()));
                fields.setField("drEldritch", Integer.toString(character.getStats().getDrEldritch()));
                fields.setField("drPoison", Integer.toString(character.getStats().getDrPoison()));
                fields.setField("drDarkrift", Integer.toString(character.getStats().getDrDarkrift()));
                fields.setField("drSacred", Integer.toString(character.getStats().getDrSacred()));
                fields.setField("drForce", Integer.toString(character.getStats().getDrForce()));
                fields.setField("drAcid", Integer.toString(character.getStats().getDrAcid()));
                
                fields.setField("treasureMin", Integer.toString(character.getStats().getTreasureMin()));
                fields.setField("treasureMax", Integer.toString(character.getStats().getTreasureMax()));
                
                fields.setField("cannotBeSurprised", Boolean.toString(character.getStats().isCannotBeSuprised()));
                fields.setField("freeMovement", Boolean.toString(character.getStats().isFreeMovement()));
                fields.setField("isPsychic", Boolean.toString(character.getStats().isPsychic()));
                fields.setField("psychicLevel", Integer.toString(character.getStats().getPsychicLevel()));
                fields.setField("initiativeBonus", Integer.toString(character.getStats().getInitiative()));
                
                final StringBuilder itemsList = new StringBuilder();
                final StringBuilder itemsListCnt = new StringBuilder();
                AtomicInteger counter = new AtomicInteger(0);
                character.getItems().stream().sorted().forEach((CharacterItem item) -> {
                    if(item.getName() == null)
                        item.setName("Empty");
                    if(counter.incrementAndGet() > 46)
                        itemsListCnt.append(item.getSlot().text()).append(": ").append(item.getName()).append("\n");
                    else
                        itemsList.append(item.getSlot().text()).append(": ").append(item.getName()).append("\n");
                });
                fields.setField("itemList", itemsList.toString());
                fields.setField("itemListCnt", itemsListCnt.toString());
                
                final StringBuilder notesList = new StringBuilder();
                if(character.getNotes().stream().filter((note) -> note.isAlwaysInEffect()).count() > 0) {
                    notesList.append("\n").append("Always In Effect:").append("\n");
                    character.getNotes().stream().filter((note) -> note.isAlwaysInEffect()).forEach((note) -> {
                        notesList.append("    ").append(note.getNote()).append("\n");
                    });
                } if(character.getNotes().stream().filter((note) -> note.isOncePerRound()).count() > 0) {
                    notesList.append("\n").append("Once Per Round:").append("\n");
                    character.getNotes().stream().filter((note) -> note.isOncePerRound()).forEach((note) -> {
                        notesList.append("    ").append(note.getNote()).append("\n");
                    });
                } if(character.getNotes().stream().filter((note) -> note.isOncePerRoom()).count() > 0) {
                    notesList.append("\n").append("Once Per Room:").append("\n");
                    character.getNotes().stream().filter((note) -> note.isOncePerRoom()).forEach((note) -> {
                        notesList.append("    ").append(note.getNote()).append("\n");
                    });
                } if(character.getNotes().stream().filter((note) -> note.isOncePerGame()).count() > 0) {
                    notesList.append("\n").append("Once Per Game:").append("\n");
                    character.getNotes().stream().filter((note) -> note.isOncePerGame()).forEach((note) -> {
                        notesList.append("    ").append(note.getNote()).append("\n");
                    });
                }
                fields.setField("gameNotes", notesList.toString());
                
                fields.setField("pcMeleeHit", Integer.toString(character.getStats().getMeleeHit()));
                fields.setField("pcMeleeDmg", Integer.toString(character.getStats().getMeleeDmg()));
                fields.setField("pcMeleeAC", Integer.toString(character.getStats().getMeleeAC()));
                fields.setField("pcRangeHit", Integer.toString(character.getStats().getRangeHit()));
                fields.setField("pcRangeDmg", Integer.toString(character.getStats().getRangeDmg()));
                fields.setField("pcRangeAC", Integer.toString(character.getStats().getRangeAC()));
                fields.setField("pcRangeMissileAC", Integer.toString(character.getStats().getRangeMissileAC()));
                fields.setField("pcFort", Integer.toString(character.getStats().getFort()));
                fields.setField("pcReflex", Integer.toString(character.getStats().getReflex()));
                fields.setField("pcWill", Integer.toString(character.getStats().getWill()));
                fields.setField("pcRetDmg", Integer.toString(character.getStats().getRetDmg()));
                fields.setField("pcHeal", Integer.toString(character.getStats().getSpellHeal()));
                fields.setField("pcSpellDmg", Integer.toString(character.getStats().getSpellDmg()));
                fields.setField("pcSpellResist", Integer.toString(character.getStats().getSpellResist()));
                fields.setField("pcTreasureMin", Integer.toString(character.getStats().getTreasureMin()));
                fields.setField("pcTreasureMax", Integer.toString(character.getStats().getTreasureMax()));
               
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
        
                Image meleeCold = (character.getStats().isMeleeCold())?coldHighlight:cold;
                PdfImage stream = new PdfImage(meleeCold, "", null);
                stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                PdfIndirectObject ref = stamper.getWriter().addToBody(stream);
                meleeCold.setDirectReference(ref.getIndirectReference());
                meleeCold.setAbsolutePosition(84, 647);
                meleeCold.scaleToFit(7,7);
                PdfContentByte over = stamper.getOverContent(4);
                over.addImage(meleeCold);

                Image meleeShock = (character.getStats().isMeleeShock())?shockHighlight:shock;
                stream = new PdfImage(meleeShock, "", null);
                stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                ref = stamper.getWriter().addToBody(stream);
                meleeShock.setDirectReference(ref.getIndirectReference());
                meleeShock.setAbsolutePosition(84, 640);
                meleeShock.scaleToFit(7,7);
                over = stamper.getOverContent(4);
                over.addImage(meleeShock);

                Image meleeSonic = (character.getStats().isMeleeSonic())?sonicHighlight:sonic;
                stream = new PdfImage(meleeSonic, "", null);
                stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                ref = stamper.getWriter().addToBody(stream);
                meleeSonic.setDirectReference(ref.getIndirectReference());
                meleeSonic.setAbsolutePosition(84, 633);
                meleeSonic.scaleToFit(7,7);
                over = stamper.getOverContent(4);
                over.addImage(meleeSonic);

                Image meleeDarkrift = (character.getStats().isMeleeDarkrift())?darkriftHighlight:darkrift;
                stream = new PdfImage(meleeDarkrift, "", null);
                stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                ref = stamper.getWriter().addToBody(stream);
                meleeDarkrift.setDirectReference(ref.getIndirectReference());
                meleeDarkrift.setAbsolutePosition(84, 626);
                meleeDarkrift.scaleToFit(7,7);
                over = stamper.getOverContent(4);
                over.addImage(meleeDarkrift);

                Image meleeFire = (character.getStats().isMeleeFire())?fireHighlight:fire;
                stream = new PdfImage(meleeFire, "", null);
                stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                ref = stamper.getWriter().addToBody(stream);
                meleeFire.setDirectReference(ref.getIndirectReference());
                meleeFire.setAbsolutePosition(113, 647);
                meleeFire.scaleToFit(7,7);
                over = stamper.getOverContent(4);
                over.addImage(meleeFire);

                Image meleeEldritch = (character.getStats().isMeleeEldritch())?eldritchHighlight:eldritch;
                stream = new PdfImage(meleeEldritch, "", null);
                stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                ref = stamper.getWriter().addToBody(stream);
                meleeEldritch.setDirectReference(ref.getIndirectReference());
                meleeEldritch.setAbsolutePosition(113, 640);
                meleeEldritch.scaleToFit(7,7);
                over = stamper.getOverContent(4);
                over.addImage(meleeEldritch);

                Image meleePoison = (character.getStats().isMeleePoison())?poisonHighlight:poison;
                stream = new PdfImage(meleePoison, "", null);
                stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                ref = stamper.getWriter().addToBody(stream);
                meleePoison.setDirectReference(ref.getIndirectReference());
                meleePoison.setAbsolutePosition(113, 633);
                meleePoison.scaleToFit(7,7);
                over = stamper.getOverContent(4);
                over.addImage(meleePoison);

                Image meleeSacred = (character.getStats().isMeleeSacred())?sacredHighlight:sacred;
                stream = new PdfImage(meleeSacred, "", null);
                stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                ref = stamper.getWriter().addToBody(stream);
                meleeSacred.setDirectReference(ref.getIndirectReference());
                meleeSacred.setAbsolutePosition(113, 626);
                meleeSacred.scaleToFit(7,7);
                over = stamper.getOverContent(4);
                over.addImage(meleeSacred);
        
                Image rangeCold = (character.getStats().isRangeCold())?coldHighlight:cold;
                stream = new PdfImage(rangeCold, "", null);
                stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                ref = stamper.getWriter().addToBody(stream);
                rangeCold.setDirectReference(ref.getIndirectReference());
                rangeCold.setAbsolutePosition(176, 647);
                rangeCold.scaleToFit(7,7);
                over = stamper.getOverContent(4);
                over.addImage(rangeCold);
        
                Image rangeShock = (character.getStats().isRangeShock())?shockHighlight:shock;
                stream = new PdfImage(rangeShock, "", null);
                stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                ref = stamper.getWriter().addToBody(stream);
                rangeShock.setDirectReference(ref.getIndirectReference());
                rangeShock.setAbsolutePosition(176, 640);
                rangeShock.scaleToFit(7,7);
                over = stamper.getOverContent(4);
                over.addImage(rangeShock);
        
                Image rangeSonic = (character.getStats().isRangeSonic())?sonicHighlight:sonic;
                stream = new PdfImage(rangeSonic, "", null);
                stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                ref = stamper.getWriter().addToBody(stream);
                rangeSonic.setDirectReference(ref.getIndirectReference());
                rangeSonic.setAbsolutePosition(176, 633);
                rangeSonic.scaleToFit(7,7);
                over = stamper.getOverContent(4);
                over.addImage(rangeSonic);
        
                Image rangeDarkrift = (character.getStats().isRangeDarkrift())?darkriftHighlight:darkrift;
                stream = new PdfImage(rangeDarkrift, "", null);
                stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                ref = stamper.getWriter().addToBody(stream);
                rangeDarkrift.setDirectReference(ref.getIndirectReference());
                rangeDarkrift.setAbsolutePosition(176, 626);
                rangeDarkrift.scaleToFit(7,7);
                over = stamper.getOverContent(4);
                over.addImage(rangeDarkrift);
        
                Image rangeFire = (character.getStats().isRangeFire())?fireHighlight:fire;
                stream = new PdfImage(rangeFire, "", null);
                stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                ref = stamper.getWriter().addToBody(stream);
                rangeFire.setDirectReference(ref.getIndirectReference());
                rangeFire.setAbsolutePosition(205, 647);
                rangeFire.scaleToFit(7,7);
                over = stamper.getOverContent(4);
                over.addImage(rangeFire);
                
                Image rangeEldritch = (character.getStats().isRangeEldritch())?eldritchHighlight:eldritch;
                stream = new PdfImage(rangeEldritch, "", null);
                stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                ref = stamper.getWriter().addToBody(stream);
                rangeEldritch.setDirectReference(ref.getIndirectReference());
                rangeEldritch.setAbsolutePosition(205, 640);
                rangeEldritch.scaleToFit(7,7);
                over = stamper.getOverContent(4);
                over.addImage(rangeEldritch);
                
                Image rangePoison = (character.getStats().isRangePoison())?poisonHighlight:poison;
                stream = new PdfImage(rangePoison, "", null);
                stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                ref = stamper.getWriter().addToBody(stream);
                rangePoison.setDirectReference(ref.getIndirectReference());
                rangePoison.setAbsolutePosition(205, 633);
                rangePoison.scaleToFit(7,7);
                over = stamper.getOverContent(4);
                over.addImage(rangePoison);
                
                Image rangeSacred = (character.getStats().isRangeSacred())?sacredHighlight:sacred;
                stream = new PdfImage(rangeSacred, "", null);
                stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                ref = stamper.getWriter().addToBody(stream);
                rangeSacred.setDirectReference(ref.getIndirectReference());
                rangeSacred.setAbsolutePosition(205, 626);
                rangeSacred.scaleToFit(7,7);
                over = stamper.getOverContent(4);
                over.addImage(rangeSacred);
                
                Image retCold = (character.getStats().isRetCold())?coldHighlight:cold;
                stream = new PdfImage(retCold, "", null);
                stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                ref = stamper.getWriter().addToBody(stream);
                retCold.setDirectReference(ref.getIndirectReference());
                retCold.setAbsolutePosition(344, 647);
                retCold.scaleToFit(7,7);
                over = stamper.getOverContent(4);
                over.addImage(retCold);
                
                Image retShock = (character.getStats().isRetShock())?shockHighlight:shock;
                stream = new PdfImage(retShock, "", null);
                stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                ref = stamper.getWriter().addToBody(stream);
                retShock.setDirectReference(ref.getIndirectReference());
                retShock.setAbsolutePosition(344, 640);
                retShock.scaleToFit(7,7);
                over = stamper.getOverContent(4);
                over.addImage(retShock);
                
                Image retSonic = (character.getStats().isRetSonic())?sonicHighlight:sonic;
                stream = new PdfImage(retSonic, "", null);
                stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                ref = stamper.getWriter().addToBody(stream);
                retSonic.setDirectReference(ref.getIndirectReference());
                retSonic.setAbsolutePosition(344, 633);
                retSonic.scaleToFit(7,7);
                over = stamper.getOverContent(4);
                over.addImage(retSonic);
                
                Image retDarkrift = (character.getStats().isRetDarkrift())?darkriftHighlight:darkrift;
                stream = new PdfImage(retDarkrift, "", null);
                stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                ref = stamper.getWriter().addToBody(stream);
                retDarkrift.setDirectReference(ref.getIndirectReference());
                retDarkrift.setAbsolutePosition(344, 626);
                retDarkrift.scaleToFit(7,7);
                over = stamper.getOverContent(4);
                over.addImage(retDarkrift);
                
                Image retFire = (character.getStats().isRetFire())?fireHighlight:fire;
                stream = new PdfImage(retFire, "", null);
                stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                ref = stamper.getWriter().addToBody(stream);
                retFire.setDirectReference(ref.getIndirectReference());
                retFire.setAbsolutePosition(373, 647);
                retFire.scaleToFit(7,7);
                over = stamper.getOverContent(4);
                over.addImage(retFire);
                
                Image retEldritch = (character.getStats().isRetEldritch())?eldritchHighlight:eldritch;
                stream = new PdfImage(retEldritch, "", null);
                stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                ref = stamper.getWriter().addToBody(stream);
                retEldritch.setDirectReference(ref.getIndirectReference());
                retEldritch.setAbsolutePosition(373, 640);
                retEldritch.scaleToFit(7,7);
                over = stamper.getOverContent(4);
                over.addImage(retEldritch);
                
                Image retPoison = (character.getStats().isRetPoison())?poisonHighlight:poison;
                stream = new PdfImage(retPoison, "", null);
                stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                ref = stamper.getWriter().addToBody(stream);
                retPoison.setDirectReference(ref.getIndirectReference());
                retPoison.setAbsolutePosition(373, 633);
                retPoison.scaleToFit(7,7);
                over = stamper.getOverContent(4);
                over.addImage(retPoison);
                
                Image retSacred = (character.getStats().isRetSacred())?sacredHighlight:sacred;
                stream = new PdfImage(retSacred, "", null);
                stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                ref = stamper.getWriter().addToBody(stream);
                retSacred.setDirectReference(ref.getIndirectReference());
                retSacred.setAbsolutePosition(373, 626);
                retSacred.scaleToFit(7,7);
                over = stamper.getOverContent(4);
                over.addImage(retSacred);
        
                if(character.getStats().isCannotBeSuprised()) {
                    stream = new PdfImage(check, "", null);
                    stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                    ref = stamper.getWriter().addToBody(stream);
                    check.setDirectReference(ref.getIndirectReference());
                    check.setAbsolutePosition(386, 635);
                    check.scaleToFit(12,12);

                    over = stamper.getOverContent(4);
                    over.addImage(check);
                }
       
                if(character.getStats().isFreeMovement()) {
                     stream = new PdfImage(check, "", null);
                     stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                     ref = stamper.getWriter().addToBody(stream);
                     check.setDirectReference(ref.getIndirectReference());
                     check.setAbsolutePosition(407, 635);
                     check.scaleToFit(12,12);

                     over = stamper.getOverContent(4);
                     over.addImage(check);
                }
       
                if(character.getStats().isPsychic()) {
                     stream = new PdfImage(check, "", null);
                     stream.put(new PdfName("ITXT_SpecialId"), new PdfName("123456789"));
                     ref = stamper.getWriter().addToBody(stream);
                     check.setDirectReference(ref.getIndirectReference());
                     check.setAbsolutePosition(428, 635);
                     check.scaleToFit(12,12);

                     over = stamper.getOverContent(4);
                     over.addImage(check);
                }
                
                final StringBuilder collectedItemsList = new StringBuilder();
                character.getItems().stream().filter((item) -> item.getSlot() == Slot.RUNESTONE && item.getItemId() != null).forEach((item) -> {
                    collectedItemsList.append(item.getName()).append("\n");
                });
                if(character.getItems().stream().filter((item) -> item.getSlot() == Slot.SLOTLESS && item.getName().equals("Special")).count() > 0) 
                    collectedItemsList.append("Special").append("\n");
                if(collectedItemsList.length() == 0) 
                    collectedItemsList.append("None").append("\n");
                fields.setField("pcCollectedItems", collectedItemsList.toString());
                
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
    public String exportCharacterHTML(String id) {
        final CharacterDetails character = getCharacter(id);
        final StringBuilder characterHtml = new StringBuilder();
        
        if(character.getItems().stream().filter((item) -> item.getSlotStatus() == SlotStatus.INVALID).count() > 0)
            throw new InvalidDataException("The requested character is currently in a invalid state and cannot be exported. Please correct any item conflicts and try again."); 
        
        characterHtml.append("[url=http://tdcharactercreator.com/#/character/edit/").append(character.getId()).append("]").append(character.getName()).append("[/url]\n");
        characterHtml.append("[b]Class:[/b] ").append(character.getCharacterClass().getDisplayText()).append("\n\n");
        characterHtml.append("[b]STR:[/b] ").append(character.getStats().getStr()).append("\n");
        characterHtml.append("[b]DEX:[/b] ").append(character.getStats().getDex()).append("\n");
        characterHtml.append("[b]CON:[/b] ").append(character.getStats().getCon()).append("\n");
        characterHtml.append("[b]INT:[/b] ").append(character.getStats().getIntel()).append("\n");
        characterHtml.append("[b]WIS:[/b] ").append(character.getStats().getWis()).append("\n");
        characterHtml.append("[b]CHA:[/b] ").append(character.getStats().getCha()).append("\n\n");
        characterHtml.append("[b]Melee:[/b]").append("\n");
        characterHtml.append("[b]Hit:[/b] ").append(character.getStats().getMeleeHit()).append("  [b]Damage:[/b] ").append(character.getStats().getMeleeDmg()).append("  [b]AC:[/b] ").append(character.getStats().getMeleeAC()).append("\n\n");
        characterHtml.append("[b]Range:[/b]").append("\n");
        characterHtml.append("[b]Hit:[/b] ").append(character.getStats().getRangeHit()).append("  [b]Damage:[/b] ").append(character.getStats().getRangeDmg()).append("  [b]AC:[/b] ").append(character.getStats().getRangeAC()).append("  [b]Missle AC:[/b] ").append(character.getStats().getRangeMissileAC()).append("\n\n");
        characterHtml.append("[b]Spell:[/b]").append("\n");
        characterHtml.append("[b]Damage:[/b] ").append(character.getStats().getSpellDmg()).append("  [b]Heal:[/b] ").append(character.getStats().getSpellHeal()).append("  [b]Resist:[/b] ").append(character.getStats().getSpellResist()).append("\n\n");
        characterHtml.append("[b]Saves:[/b]").append("\n");
        characterHtml.append("[b]Reflex:[/b] ").append(character.getStats().getReflex()).append("  [b]Fort:[/b] ").append(character.getStats().getFort()).append("  [b]Will:[/b] ").append(character.getStats().getWill()).append("\n\n");
        characterHtml.append("[b]Treasure:[/b]").append("\n");
        characterHtml.append("[b]Min:[/b] ").append(character.getStats().getTreasureMin()).append("  [b]Max:[/b] ").append(character.getStats().getTreasureMax()).append("\n\n");
        characterHtml.append("[b]Health:[/b] ").append(character.getStats().getHealth());
        if(character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getItemId().equals("333788757f49e48272a25e8b5994ac6503ad2adc")).count()>0)
            characterHtml.append("(max ").append(character.getStats().getHealth()+9).append(" with Charm of Synergy)");
        characterHtml.append("\n\n");
        characterHtml.append("[b]Damage Reduction:[/b]").append("\n");
        characterHtml.append("[table][tr][td]Melee[/td][td]Range[/td][td]Spell[/td][td]Fire[/td][td]Cold[/td][td]Shock[/td][td]Sonic[/td][td]Eldritch[/td][td]Poison[/td][td]Darkrift[/td][td]Sacred[/td][td]Force[/td][td]Acid[/td][/tr]");
        characterHtml.append("[tr]");
        characterHtml.append("[td]").append(character.getStats().getDrMelee()).append("[/td]");
        characterHtml.append("[td]").append(character.getStats().getDrRange()).append("[/td]");
        characterHtml.append("[td]").append(character.getStats().getDrSpell()).append("[/td]");
        characterHtml.append("[td]").append(character.getStats().getDrFire()).append("[/td]");
        characterHtml.append("[td]").append(character.getStats().getDrCold()).append("[/td]");
        characterHtml.append("[td]").append(character.getStats().getDrShock()).append("[/td]");
        characterHtml.append("[td]").append(character.getStats().getDrSonic()).append("[/td]");
        characterHtml.append("[td]").append(character.getStats().getDrEldritch()).append("[/td]");
        characterHtml.append("[td]").append(character.getStats().getDrPoison()).append("[/td]");
        characterHtml.append("[td]").append(character.getStats().getDrDarkrift()).append("[/td]");
        characterHtml.append("[td]").append(character.getStats().getDrSacred()).append("[/td]");
        characterHtml.append("[td]").append(character.getStats().getDrForce()).append("[/td]");
        characterHtml.append("[td]").append(character.getStats().getDrAcid()).append("[/td]");
        characterHtml.append("[/tr][/table]\n\n");
        
        final StringBuilder meleeMainhand = new StringBuilder();
        characterHtml.append("[b]Melee Mainhand:[/b] ");
        character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.MAINHAND).forEach((item) ->{
            meleeMainhand.append("[color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]");
        });
        if(meleeMainhand.length() == 0)
            meleeMainhand.append("Empty");
        characterHtml.append(meleeMainhand.toString()).append("\n");
        final StringBuilder meleeOffhand = new StringBuilder();
        characterHtml.append("[b]Melee Offhand:[/b] ");
        character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.OFFHAND).forEach((item) ->{
            meleeOffhand.append("[color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]");
        });
        if(meleeOffhand.length() == 0)
            meleeOffhand.append("Empty");
        characterHtml.append(meleeOffhand.toString()).append("\n");
        final StringBuilder rangedMainhand = new StringBuilder();
        characterHtml.append("[b]Ranged Mainhand:[/b] ");
        character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.RANGE_MAINHAND).forEach((item) ->{
            rangedMainhand.append("[color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]");
        });
        if(rangedMainhand.length() == 0)
            rangedMainhand.append("Empty");
        characterHtml.append(rangedMainhand.toString()).append("\n");
        final StringBuilder rangedOffhand = new StringBuilder();
        characterHtml.append("[b]Ranged Offhand:[/b] ");
        character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.RANGE_OFFHAND).forEach((item) ->{
            rangedOffhand.append("[color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]");
        });
        if(rangedOffhand.length() == 0)
            rangedOffhand.append("Empty");
        characterHtml.append(rangedOffhand.toString()).append("\n");
        character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.INSTRUMENT).forEach((item) ->{
            characterHtml.append("[b]Instrument:[/b] ").append("[color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]").append("\n");
        });
        
        if (character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.HEAD).count() > 0) {
            character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.HEAD).forEach((item) ->{
                characterHtml.append("[b]Head:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
            });
        } else
            characterHtml.append("[b]Head:[/b] Empty");

        if (character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.EYES).count() > 0) {
            character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.EYES).forEach((item) ->{
                characterHtml.append("[b]Eyes:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
            });
        } else
            characterHtml.append("[b]Eyes:[/b] Empty");
        
        if (character.getItems().stream().filter((item) ->item.getItemId()!=null&&item.getSlot()==Slot.EAR&&item.getIndex()==0).count() > 0) {
            character.getItems().stream().filter((item) ->item.getItemId()!=null&&item.getSlot()==Slot.EAR&&item.getIndex()==0).forEach((item) ->{
                characterHtml.append("[b]Left Ear:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
            });
        } else
            characterHtml.append("[b]Left Ear:[/b] Empty");

        if (character.getItems().stream().filter((item) ->item.getItemId()!=null&&item.getSlot()==Slot.EAR&&item.getIndex()==1).count() > 0) {
            character.getItems().stream().filter((item) ->item.getItemId()!=null&&item.getSlot()==Slot.EAR&&item.getIndex()==1).forEach((item) ->{
                characterHtml.append("[b]Right Ear:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
            });
        } else
            characterHtml.append("[b]Right Ear:[/b] Empty");
       
        if (character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.NECK).count() > 0) {
            character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.NECK).forEach((item) ->{
                characterHtml.append("[b]Neck:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
            });
        } else
            characterHtml.append("[b]Neck:[/b] Empty");
        
        if (character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.BEAD).count() > 0) {
            character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.BEAD).forEach((item) ->{
                characterHtml.append("[b]Bead:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
            });
        } else
            characterHtml.append("[b]Bead:[/b] Empty");
        
        if (character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.TORSO).count() > 0) {
            character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.TORSO).forEach((item) ->{
                characterHtml.append("[b]Torso:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
            });
        } else
            characterHtml.append("[b]Torso:[/b] Empty");
        
        if (character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.WRIST).count() > 0) {
            character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.WRIST).forEach((item) ->{
                characterHtml.append("[b]Wrist:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
            });
        } else
            characterHtml.append("[b]Wrist:[/b] Empty");
        
        if (character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.HANDS).count() > 0) {
            character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.HANDS).forEach((item) ->{
                characterHtml.append("[b]Hands:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
            });
        } else
            characterHtml.append("[b]Hands:[/b] Empty");
        
        if (character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.BACK).count() > 0) {
            character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.BACK).forEach((item) ->{
                characterHtml.append("[b]Back:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
            });
        } else
            characterHtml.append("[b]Back:[/b] Empty");
        
        if (character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.FINGER).count() > 0) {
            character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.FINGER).forEach((item) ->{
                characterHtml.append("[b]Ring:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
            });
        } else
            characterHtml.append("[b]Ring:[/b] Empty");
        
        if (character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.WAIST).count() > 0) {
            character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.WAIST).forEach((item) ->{
                characterHtml.append("[b]Waist:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
            });
        } else
            characterHtml.append("[b]Waist:[/b] Empty");
        
        if (character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.SHIRT).count() > 0) {
            character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.SHIRT).forEach((item) ->{
                characterHtml.append("[b]Shirt:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
            });
        } else
            characterHtml.append("[b]Shirt:[/b] Empty");
        
        if (character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.LEGS).count() > 0) {
            character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.LEGS).forEach((item) ->{
                characterHtml.append("[b]Legs:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
            });
        } else
            characterHtml.append("[b]Legs:[/b] Empty");
        
        if (character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.SHINS).count() > 0) {
            character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.SHINS).forEach((item) ->{
                characterHtml.append("[b]Shins:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
            });
        } else
            characterHtml.append("[b]Shins:[/b] Empty");
        
        if (character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.FEET).count() > 0) {
            character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.FEET).forEach((item) ->{
                characterHtml.append("[b]Boots:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
            });
        } else
            characterHtml.append("[b]Boots:[/b] Empty");
        
        if (character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.FIGURINE).count() > 0) {
            character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.FIGURINE).forEach((item) ->{
                characterHtml.append("[b]Figurine:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
            });
        } else
            characterHtml.append("[b]Figurine:[/b] Empty");
        
        if (character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.CHARM).count() > 0) {
            character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.CHARM).forEach((item) ->{
                characterHtml.append("[b]Charm:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
            });
        } else
            characterHtml.append("[b]Charm:[/b] Empty");
        
        if (character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.IOUNSTONE).count() > 0) {
            character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.IOUNSTONE).forEach((item) ->{
                characterHtml.append("[b]Ioun Stone:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
            });
        } else
            characterHtml.append("[b]Ioun Stone:[/b] Empty");
        
        if (character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.SLOTLESS).count() > 0) {
            character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.SLOTLESS).forEach((item) ->{
                characterHtml.append("[b]Slotless:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
            });
        } else
            characterHtml.append("[b]Slotless:[/b] Empty");
        
        if (character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.RUNESTONE).count() > 0) {
            character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.RUNESTONE).forEach((item) ->{
                characterHtml.append("[b]Runestone:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
            });
        } else
            characterHtml.append("[b]Runestone:[/b] Empty");
     
        return characterHtml.toString();
    }
   
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CharacterDetails validateCharacterItems(String id) {
        final CharacterDetails characterDetails = getCharacter(id);
        final Map<String, CharacterItemSet> itemDetailsMap = new HashMap<>();

        // Fill the map
        characterDetails.getItems().stream().filter((item) -> item.getItemId() != null).forEach((item) -> {
            if (item.getItemId() != null && !item.getItemId().isEmpty()) {
                final TokenFullDetails tokenDetails = tokenAdminMapper.getTokenDetails(item.getItemId());
                tokenDetails.setSlotModifiers(tokenAdminMapper.getSlotModifiers(tokenDetails.getId()));
                itemDetailsMap.put(item.getId(), CharacterItemSet.builder().item(item).tokenFullDetails(tokenDetails).build());
            } else
                itemDetailsMap.put(item.getId(), CharacterItemSet.builder().item(item).build());
        });

        chackWeaponAvailability(characterDetails, itemDetailsMap);
        checkSlotModItems(characterDetails, itemDetailsMap);
        addSlotsForFullItems(characterDetails);
        checkSetItems(characterDetails, itemDetailsMap,false);
        calculateStats(characterDetails, itemDetailsMap);
        
        // Set Items
        mapper.deleteCharacterItems(characterDetails.getId());
        mapper.addCharacterItems(characterDetails.getItems());
        
        // Set Stats
        mapper.updateCharacterStats(characterDetails.getStats());
        
        // Save notes
        mapper.deleteCharacterNotes(characterDetails.getId());
        if(!characterDetails.getNotes().isEmpty())
            mapper.addCharacterNotes(characterDetails.getId(), characterDetails.getNotes());

        mapper.updateCharacterTime(characterDetails.getId(), new Date());
        
        return characterDetails;
    }

    @Override
    public CharacterDetails getCharacterMaxLevel(String id) {
        CharacterDetails characterDetails = getCharacter(id);
        final Map<String, CharacterItemSet> itemDetailsMap = new HashMap<>();

        // Fill the map
        characterDetails.getItems().stream().filter((item) -> item.getItemId() != null).forEach((item) -> {
            if (item.getItemId() != null && !item.getItemId().isEmpty()) {
                final TokenFullDetails tokenDetails = tokenAdminMapper.getTokenDetails(item.getItemId());
                tokenDetails.setSlotModifiers(tokenAdminMapper.getSlotModifiers(tokenDetails.getId()));
                itemDetailsMap.put(item.getId(), CharacterItemSet.builder().item(item).tokenFullDetails(tokenDetails).build());
            } else
                itemDetailsMap.put(item.getId(), CharacterItemSet.builder().item(item).build());
        });

        chackWeaponAvailability(characterDetails, itemDetailsMap);
        checkSlotModItems(characterDetails, itemDetailsMap);
        addSlotsForFullItems(characterDetails);
        checkSetItems(characterDetails, itemDetailsMap,true);
        calculateStats(characterDetails, itemDetailsMap);

        return characterDetails;
    }
    
    private void chackWeaponAvailability(CharacterDetails characterDetails, Map<String, CharacterItemSet> itemDetailsMap) {
        // Check for two handed melee weapon
        itemDetailsMap.values().stream().filter((item) -> item.getItem().getSlot()==Slot.MAINHAND).findAny().ifPresent((item) ->{
            CharacterItem offhandItem = characterDetails.getItems().stream().filter((i) -> i.getSlot() == Slot.OFFHAND).findFirst().orElse(null);
            if(item.getItem().getItemId()!=null && item.getTokenFullDetails().isTwoHanded()) {
                if (offhandItem != null) {
                    characterDetails.getItems().remove(offhandItem);
                    itemDetailsMap.remove(offhandItem.getId());
                }
            } else if(offhandItem == null) {
                CharacterItem characterItem = CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.OFFHAND).index(0).slotStatus(SlotStatus.OK).build();
                characterDetails.getItems().add(characterItem);
                itemDetailsMap.put(characterItem.getId(), CharacterItemSet.builder().item(characterItem).build());
            }
        });
        
        // Check to make sure dual is valid
        if (characterDetails.getCharacterClass() != CharacterClass.RANGER && characterDetails.getCharacterClass() != CharacterClass.MONK) {
            itemDetailsMap.values().stream().filter((item) -> item.getItem().getSlot()==Slot.OFFHAND).findAny().ifPresent((item) -> {
                if(item.getTokenFullDetails() != null && item.getTokenFullDetails().isOneHanded() && !(item.getTokenFullDetails().isShield() || item.getTokenFullDetails().isMug())) {
                    CharacterItem offhandItem = characterDetails.getItems().stream().filter((i) -> i.getSlot() == Slot.OFFHAND).findFirst().orElse(null);
                    if (offhandItem != null) {
                        offhandItem.setItemId(null);
                        offhandItem.setName(null);
                        offhandItem.setStatusText(null);
                        offhandItem.setText(null);
                        offhandItem.setRarity(null);
                        offhandItem.setIndex(0);
                        offhandItem.setSlotStatus(SlotStatus.OK);
                        itemDetailsMap.get(offhandItem.getId()).setTokenFullDetails(null);
                    }
                }
            });
        }
        
        // Check for two handed range weapon
        itemDetailsMap.values().stream().filter((item) -> item.getItem().getSlot()==Slot.RANGE_MAINHAND).findAny().ifPresent((mainHand) -> {
            CharacterItem rangeOffhand = characterDetails.getItems().stream().filter((i) -> i.getSlot() == Slot.RANGE_OFFHAND).findFirst().orElse(null);

            if(rangeOffhand == null) {
                rangeOffhand = CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.RANGE_OFFHAND).index(0).slotStatus(SlotStatus.OK).build();
                characterDetails.getItems().add(rangeOffhand);
                itemDetailsMap.put(rangeOffhand.getId(), CharacterItemSet.builder().item(rangeOffhand).build());
            }

            if (mainHand.getItem().getItemId() == null) {
                rangeOffhand.setItemId(null);
                rangeOffhand.setName(null);
                rangeOffhand.setStatusText(null);
                rangeOffhand.setText(null);
                rangeOffhand.setRarity(null);
                rangeOffhand.setIndex(0);
                rangeOffhand.setSlotStatus(SlotStatus.OK);
                itemDetailsMap.get(rangeOffhand.getId()).setTokenFullDetails(null);
            }

            itemDetailsMap.values().stream().filter((i) -> i.getItem().getItemId()!=null && i.getItem().getSlot()==Slot.RANGE_OFFHAND).findAny().ifPresent((offHand) -> {
                CharacterItem rangeOffhandFinal = characterDetails.getItems().stream().filter((i) -> i.getSlot() == Slot.RANGE_OFFHAND).findFirst().get();

                if ((mainHand.getTokenFullDetails().isTwoHanded() && (!offHand.getTokenFullDetails().isBuckler() || offHand.getTokenFullDetails().isThrown())) ||
                        (mainHand.getTokenFullDetails().isOneHanded() && offHand.getTokenFullDetails().isBuckler()) ||
                        (characterDetails.getCharacterClass() == CharacterClass.MONK && mainHand.getTokenFullDetails().isShuriken() && !offHand.getTokenFullDetails().isShuriken()) ) {
                    rangeOffhandFinal.setItemId(null);
                    rangeOffhandFinal.setName(null);
                    rangeOffhandFinal.setStatusText(null);
                    rangeOffhandFinal.setText(null);
                    rangeOffhandFinal.setRarity(null);
                    rangeOffhandFinal.setIndex(0);
                    rangeOffhandFinal.setSlotStatus(SlotStatus.OK);
                    itemDetailsMap.get(rangeOffhandFinal.getId()).setTokenFullDetails(null);
               }
            });
        });
        
        // Check for bracer weapons
        if(characterDetails.getCharacterClass() == CharacterClass.MONK) {
            final List<CharacterItem> wrists = characterDetails.getItems().stream().filter((i) -> i.getSlot() == Slot.WRIST).collect(Collectors.toList());

            if (itemDetailsMap.values().stream().anyMatch((item) -> item.getItem().getItemId() != null && (item.getItem().getSlot() == Slot.MAINHAND || item.getItem().getSlot() == Slot.OFFHAND) && item.getTokenFullDetails().isBracerWeapon())) {
                wrists.forEach(wrist -> {
                    characterDetails.getItems().remove(wrist);
                    itemDetailsMap.remove(wrist.getId());
                });
            } else if (wrists.isEmpty()) {
                CharacterItem characterItem = CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.WRIST).index(0).slotStatus(SlotStatus.OK).build();
                characterDetails.getItems().add(characterItem);
                itemDetailsMap.put(characterItem.getId(), CharacterItemSet.builder().item(characterItem).build());
            }
        }
    }
    
    private void checkSlotModItems(CharacterDetails characterDetails, Map<String, CharacterItemSet> itemDetailsMap) {
        final Map<Slot, TreeMap<Rarity, Integer>> slotMap = new HashMap<>();
        final Map<Slot, Integer> slotTotals = new HashMap<>();
        AtomicReference<Boolean> threeRings = new AtomicReference<>(false);
        AtomicReference<Boolean> noRings = new AtomicReference<>(false);

        for (Slot slot : Slot.values()) {
            slotMap.put(slot, new TreeMap<>());
            slotTotals.put(slot, slot.defaultSize);

            for (Rarity rarity : Rarity.values()) {
                if (rarity == Rarity.ALL) {
                    if (slot == Slot.FIGURINE && characterDetails.getStats().getCha() >= 16)
                        slotMap.get(slot).put(Rarity.ALL, 2);
                    else
                        slotMap.get(slot).put(Rarity.ALL, slot.defaultSize);
                } else
                    slotMap.get(slot).put(rarity, 0);
            }
        }


        itemDetailsMap.values().stream().filter(characterItemSet -> characterItemSet.getTokenFullDetails() != null).forEach(characterItemSet -> {
            if (characterItemSet.getTokenFullDetails().getSlotModifiers() != null && !characterItemSet.getTokenFullDetails().getSlotModifiers().isEmpty()) {
                for (SlotModifier slotModifier : characterItemSet.getTokenFullDetails().getSlotModifiers()) {
                    if (slotMap.get(slotModifier.getSlot()).containsKey(slotModifier.getRarity())) {
                        slotMap.get(slotModifier.getSlot()).replace(slotModifier.getRarity(),
                                Math.max(0, slotMap.get(slotModifier.getSlot()).get(slotModifier.getRarity()) + slotModifier.getModifier()));
                    } else {
                        slotMap.get(slotModifier.getSlot()).put(slotModifier.getRarity(), slotModifier.getModifier());
                    }
                }
            }
            threeRings.set(threeRings.get() || characterItemSet.getTokenFullDetails().isSetRingsThree());
            noRings.set(noRings.get() || characterItemSet.getTokenFullDetails().isNoRings());
        });

        for (Slot slot : Slot.values()) {
            int total = slotMap.get(slot).keySet().stream().map(key ->
                    slotMap.get(slot).get(key)).reduce(0, Integer::sum);
            slotTotals.replace(slot, Math.min(total, 10));
            if (total > 10 && slot != Slot.SLOTLESS) {
                total -= slotMap.get(slot).get(Rarity.ALL);
                if (total < 0) {
                    slotMap.get(slot).clear();
                    slotMap.get(slot).put(Rarity.ALL, 10);
                } else {
                    for (Rarity rarity : slotMap.get(slot).keySet()) {
                        if (rarity != Rarity.ALL) {
                            if (slotMap.get(slot).get(rarity) > total) {
                                slotMap.get(slot).replace(rarity, total);
                                total = 0;
                            } else
                                total -= slotMap.get(slot).get(rarity);
                        }
                    }
                }
            }
        }

        if (threeRings.get()) {
            slotTotals.replace(Slot.FINGER, 3);
            slotMap.get(Slot.FINGER).clear();
            slotMap.get(Slot.FINGER).put(Rarity.ALL, 3);
        }if (noRings.get()) {
            slotTotals.replace(Slot.FINGER, 0);
            slotMap.get(Slot.FINGER).clear();
            slotMap.get(Slot.FINGER).put(Rarity.ALL, 0);
        }

        for (Slot slot : slotMap.keySet()) {
            if (slot != Slot.SLOTLESS && slot != Slot.INSTRUMENT && slot != Slot.FIGURINE) {
                final TreeSet<Integer> available = new TreeSet<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
                List<CharacterItem> items = characterDetails.getItems().stream().filter(i -> i.getSlot() == slot)
                        .peek(i -> available.remove(i.getIndex()))
                        .sorted(Comparator.comparing(CharacterItem::getIndex)).collect(Collectors.toList());

                if (slotTotals.get(slot) != items.size()) {
                    for (Rarity rarity : slotMap.get(slot).keySet()) {
                        final List<CharacterItem> itemList = items.stream().filter(i -> i.getMaxRarity() == rarity).collect(Collectors.toList());

                        if (slotMap.get(slot).get(rarity) > itemList.size()) {
                            for (int x = 0; x < slotMap.get(slot).get(rarity) - itemList.size(); x++) {
                                CharacterItem characterItem = CharacterItem.builder()
                                        .id(UUID.randomUUID().toString())
                                        .characterId(characterDetails.getId())
                                        .slot(slot)
                                        .index(available.pollFirst())
                                        .maxRarity(rarity)
                                        .slotStatus(SlotStatus.OK)
                                        .build();
                                characterDetails.getItems().add(characterItem);
                                itemDetailsMap.put(characterItem.getId(), CharacterItemSet.builder().item(characterItem).build());
                            }
                        } else if (slotMap.get(slot).get(rarity) < itemList.size()) {
                            int rarityCount = 0;
                            for (CharacterItem characterItem : itemList) {
                                if (++rarityCount > slotMap.get(slot).get(rarity)) {
                                    available.add(characterItem.getIndex());
                                    characterDetails.getItems().remove(characterItem);
                                    itemDetailsMap.remove(characterItem.getId());
                                }
                            }
                        }
                    }
                }
            }
        }
    } 
    
    private void addSlotsForFullItems(CharacterDetails characterDetails) {
        // Check slotless
        List<CharacterItem> orderedSlotless = new ArrayList();
        characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.SLOTLESS).forEach((item) -> {
            item.setIndex(orderedSlotless.size());
            orderedSlotless.add(item);
        });
        orderedSlotless.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.SLOTLESS).index(orderedSlotless.size()).slotStatus(SlotStatus.OK).build());
        characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> item.getSlot()!=Slot.SLOTLESS).collect(Collectors.toList()));
        characterDetails.getItems().addAll(orderedSlotless);

        // Check Bard Instruments
        if(characterDetails.getCharacterClass() == CharacterClass.BARD) {
            List<CharacterItem> orderedInstruments = new ArrayList();
            characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.INSTRUMENT).forEach((item) -> {
                item.setIndex(orderedInstruments.size());
                orderedInstruments.add(item);
            });
            orderedInstruments.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.INSTRUMENT).index(orderedInstruments.size()).slotStatus(SlotStatus.OK).build());
            characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> item.getSlot()!=Slot.INSTRUMENT).collect(Collectors.toList()));
            characterDetails.getItems().addAll(orderedInstruments);
        }
    }
    
    private void checkSetItems(CharacterDetails characterDetails, Map<String, CharacterItemSet> itemDetailsMap, boolean levelBoost) {
        final Map<String, CharacterItem> itemsMap = new HashMap<>();
        characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null).forEach((item) -> {
            itemsMap.put(item.getItemId(), item);
        });

        // Check level items
        boolean levelItem = itemDetailsMap.values().stream().anyMatch(characterItemSet -> characterItemSet.getTokenFullDetails() != null && characterItemSet.getTokenFullDetails().isAddLevel());
        // Rod of Seven Parts
        long eldrichCount = characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && item.getRarity() == Rarity.ELDRITCH).count();
        // Might Set
        long mightCount = characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("ab48a5c6a8f12cfff15d100f8bc217e28c3a0d5f") || item.getItemId().equals("facad47c2da2c265b50ad33dc573bb4988ee2155") || item.getItemId().equals("526f1405624d9704ee44b0f901a8191e95db1f91") || item.getItemId().equals("f94bb88bddf8a88480f7b30d8c81eb65d9703c45") || item.getItemId().equals("5bf7eed1b3dfd81eb772e5ced6075871d9cd1b3e"))).count();
        // Charming Set
        long charmingCount = characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("e0c359ef619d5aa2fae5c93c9e372514e43b4543") || item.getItemId().equals("f9f68f1e599025ab379192633697fca78ab2f7d0") || item.getItemId().equals("c2dddfc5254138f8441486900ec2f2db1714fcc2") || item.getItemId().equals("27b93edab1b6ef69929adfee3f4a9f9da465b1b6"))).count();
        // Mithral Set
        long mithralCount = characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("e3d537d7b1067df3a7f67d121d1394c26efd7937") || item.getItemId().equals("02ffaca2c458066d4b35f1ba20839eef907e5fcb") || item.getItemId().equals("73710528811a2b6167a21b5bc3b8cab3fc071c84") || item.getItemId().equals("b9b4a18df47664a937b54d583f8b4966f928beae") || item.getItemId().equals("52b576b2fbe4b187d6fae824ab645398820f3b12") || item.getItemId().equals("2cc50950b2cfdc3c2abb8100354d2fcf82a6ba51") || item.getItemId().equals("f5649eb2e2450f67aa05af1d7c7d3076e44bf6bd") || item.getItemId().equals("d268fcfd2466c0f21d791c9a18c3f42bc3c61be9"))).count();
        // Redoubt Set
        long redoubtCount = characterDetails.getItems().stream().filter((item) -> item.getItemId() != null).filter((item) -> item.getName().contains("Redoubt")).map(CharacterItem::getName).distinct().count();
        // Viper Strike Set  
        long viperCount = characterDetails.getItems().stream().filter((item) -> item.getItemId() != null && (item.getItemId().equals("4b469b628a8c57e294268dfac4b51d302b1e9123") || item.getItemId().equals("9431d39ad2fba9953bf4b526d86f41f37022efeb") || item.getItemId().equals("f2ff2a508dd3075633ca2fd9e58c0e1a76088af8") || item.getItemId().equals("dd565d74807cc9094990b324465612d52b3070bf") || item.getItemId().equals("09ad5527813c4f087f3123cd6a40404b9377a4bc"))).count();
        viperCount += characterDetails.getItems().stream().filter((item) -> item.getItemId() != null && item.getItemId().equals("bd21afd63114346decea5fc899ff697106e99429")).map(CharacterItem::getName).distinct().count();
        // Lucky Set
        long luckyCount = itemDetailsMap.values().stream().filter(a -> a.getTokenFullDetails() != null && a.getTokenFullDetails().getName().toLowerCase().startsWith("lucky")).map(a -> a.getTokenFullDetails().getName()).distinct().count();
        // Silver elf Set
        long silverElfCount = itemDetailsMap.values().stream().filter(a -> a.getTokenFullDetails() != null && a.getTokenFullDetails().getName().toLowerCase().startsWith("silver elf")).map(a -> a.getTokenFullDetails().getName()).distinct().count();
        // Deadshot set
        long deadshotCount = itemDetailsMap.values().stream().filter(a -> a.getTokenFullDetails() != null && a.getTokenFullDetails().getName().toLowerCase().endsWith("of the deadshot")).map(a -> a.getTokenFullDetails().getName()).distinct().count();
        // Death Knight set
        long deathKnightCount = itemDetailsMap.values().stream().filter(a -> a.getTokenFullDetails() != null && a.getTokenFullDetails().getName().toLowerCase().startsWith("death knight")).map(a -> a.getTokenFullDetails().getName()).distinct().count();
        // Ancients set
        long ancientsCount = itemDetailsMap.values().stream().filter(a -> a.getTokenFullDetails() != null && a.getTokenFullDetails().getName().toLowerCase().endsWith("of the ancients")).map(a -> a.getTokenFullDetails().getName()).distinct().count();
        // Snake Priest set
        long snakePriestCount = itemDetailsMap.values().stream().filter(a -> a.getTokenFullDetails() != null && a.getTokenFullDetails().getName().toLowerCase().startsWith("snake priest")).map(a -> a.getTokenFullDetails().getName()).distinct().count();
        // Arcane Set
        long arcaneCount = itemDetailsMap.values().stream().filter(a -> a.getTokenFullDetails() != null && (a.getTokenFullDetails().getName().toLowerCase().trim().equals("arcane belt") ||
                a.getTokenFullDetails().getName().toLowerCase().trim().equals("arcane bracelets") || a.getTokenFullDetails().getName().toLowerCase().trim().equals("arcane charm") ||
                a.getTokenFullDetails().getName().toLowerCase().trim().equals("arcane earcuff"))).count();
        
        // First check if we need to boost character level
        // Charm of Heroism, Medallion of Heroism, Ring of Heroism, Eldrich Set, Kubu’s Coin of Coincidence, Smackdown’s Charm of Comraderie
        if(levelBoost || levelItem || eldrichCount >= 2 || mightCount >= 3 || charmingCount >= 3 || ancientsCount >= 3 || deadshotCount >= 3 || itemsMap.containsKey("d20aa5f4194d09336b0a5974215247cfaa480c9a") || itemsMap.containsKey("d4674a1b2bea57e8b11676fed2bf81bd4c48ac78") || itemsMap.containsKey("85bbc3d8307b702dde0525136fb82bf1636f55d8") ||
                itemsMap.containsKey("2f1cfd3d3dbdd218f5cd5bd3935851b7acba5a9c") || itemsMap.containsKey("f44d007c35b18b83e85a1ee183cda08180030012") || itemsMap.containsKey("c289cd1accbbcc7af656de459c157bdc40dbaf45")) {
            characterDetails.setStats(mapper.getStartingStats(characterDetails.getCharacterClass(), 5));
            characterDetails.getStats().setCharacterId(characterDetails.getId());
        } else {
            characterDetails.setStats(mapper.getStartingStats(characterDetails.getCharacterClass(), 4));
            characterDetails.getStats().setCharacterId(characterDetails.getId());
        }
        
        // Set character notes
        setCharacterNotes(characterDetails);
        
        // Lucky Set
        if (luckyCount >= 2)
            characterDetails.getStats().setHealth(characterDetails.getStats().getHealth() + 5);

        // Death Knight Set
        if (deathKnightCount >= 3) {
            characterDetails.getStats().setRetDmg(characterDetails.getStats().getRetDmg() + 4);
            characterDetails.getStats().setRetDarkrift(true);
            if (deathKnightCount >= 4) {
                characterDetails.getStats().setRetDmg(characterDetails.getStats().getRetDmg() + 2);
            }
        }

        // Snake Priest Set
        if (snakePriestCount >= 3) {
            characterDetails.getStats().setRangeDmg(characterDetails.getStats().getRangeDmg() + 1);
            characterDetails.getStats().setRangePoison(true);
            characterDetails.getStats().setMeleeDmg(characterDetails.getStats().getMeleeDmg() + 1);
            characterDetails.getStats().setMeleePoison(true);
        }

        // Silver Elf Set
        if (silverElfCount >= 3)
            characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("You are immune to underwater hindrances (Silver Elf Set).").build());

        // Arcane set
        if (arcaneCount == 3)
            characterDetails.getNotes().add(CharacterNote.builder().oncePerGame(true).note("The first 0th, 1st or 2nd level spell you casts is not marked off your character card.").build());
       
        // Cabal set
        // Gloves
        if(itemsMap.keySet().stream().anyMatch((id) -> (id.equals("3dcfd7948a3c9196556ef7e069a36174396297ad"))))
            characterDetails.getNotes().add(CharacterNote.builder().oncePerRoom(true).note("You may have a second target for and COMMON scroll used.").build());
        // Bracelets
        if(itemsMap.keySet().stream().anyMatch((id) -> id.equals("f225241f60605ef641beeecd5003ba4129dbf46e"))) {
            characterDetails.getStats().setSpellDmg(characterDetails.getStats().getSpellDmg()+1);
            characterDetails.getStats().setSpellHeal(characterDetails.getStats().getSpellHeal()+1);
        }
        // Charm
        if(itemsMap.keySet().stream().anyMatch((id) -> (id.equals("1c688491fcb8a12199e9eca6d97e3da5ef4f3d65")))) {
            characterDetails.getStats().setSpellResist(10);
        }
        // all three two spells in one round once per room
        if(itemsMap.keySet().stream().filter((id) -> (id.equals("3dcfd7948a3c9196556ef7e069a36174396297ad") || id.equals("f225241f60605ef641beeecd5003ba4129dbf46e") || id.equals("1c688491fcb8a12199e9eca6d97e3da5ef4f3d65"))).count() == 3)
            characterDetails.getNotes().add(CharacterNote.builder().oncePerRoom(true).note("You may cast two spells in one round.  Spell modifiers apply to both.").build());
        
        // Celestial Set
        // all 3 immune to melee and mental attacks from evil outsiders
        if(itemsMap.keySet().stream().filter((id) -> id.equals("09a0ad7cb78d898992c96ab00487bc8ffdc66f5a") || id.equals("4b4e1b22e1ce20a529920fd48f8b891ec8e0b74a") || id.equals("c34921c9623d7d3c1eee054e646fc3cdc7f08659")).count() == 3)
            characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("You are immune to melee and mental attacks from evil outsiders.").build());
       
        // Charming items
        if(charmingCount > 0) {
            int charmCount = (int) characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && item.getSlot() == Slot.CHARM).count();
            if(charmCount > 6)
                charmCount = 6;
            // Ear
            if(itemsMap.containsKey("f9f68f1e599025ab379192633697fca78ab2f7d0")) {
                characterDetails.getStats().setReflex(characterDetails.getStats().getReflex() + charmCount);
            }
            // Ring
            if(itemsMap.containsKey("c2dddfc5254138f8441486900ec2f2db1714fcc2")) {
                characterDetails.getStats().setFort(characterDetails.getStats().getFort() + charmCount);
            }
            // Crown
            if(itemsMap.containsKey("e0c359ef619d5aa2fae5c93c9e372514e43b4543")) {
                characterDetails.getStats().setWill(characterDetails.getStats().getWill()+ charmCount);
            }
            // Ioun
            if(itemsMap.containsKey("27b93edab1b6ef69929adfee3f4a9f9da465b1b6")) {
                characterDetails.getStats().setHealth(characterDetails.getStats().getHealth()+ charmCount);
            }
        }
        
        // Darkthorn Set
        // all 3 +2 ret dmg
        if(itemsMap.keySet().stream().filter((id) -> id.equals("2439ce9af26dfa74294083c13b73e3cc00405793") || id.equals("770a1850602b0854e7be9256f55f1b0ce2ac50ea") || id.equals("e6d0a8722540345c6a236a0aeeaf92d12c66e370")).count() == 3)
            characterDetails.getStats().setRetDmg(characterDetails.getStats().getRetDmg() + 2);
        
        // Defender Set
        // all 3 free action movement and +1 AC
        if(itemsMap.keySet().stream().filter((item) -> item.equals("89abc3b184b2b30d1a967aee7a32ccbf107532ed") || item.equals("b29ffe03ba83c567fb95ddedeb8cef8c515c003f") || item.equals("debb8b6d4a22654fadfa4983b84cac3bd69db814")).count() >= 3) {
            characterDetails.getStats().setMeleeAC(characterDetails.getStats().getMeleeAC() + 1);
            characterDetails.getStats().setRangeAC(characterDetails.getStats().getRangeAC() + 1);
            characterDetails.getStats().setFreeMovement(true);
        }
        
        // Dragonhide Set
        // 3 or more auto pass saves vs dragon breath, +3 saves
        if(itemsMap.keySet().stream().distinct().filter((id) -> id.equals("2a05d3436a948f9bb29e1ebfd9f3ac55115445a2") || id.equals("33205cbfbe448541fae56600346027284edc52e1") || id.equals("643e19c4273dcb6f2e8b2ce56241beb1efde902e") ||
                 id.equals("67935e7e953d26c83d095fd188a226d16fc16e1f") || id.equals("81fbf796065e19a88ea36b4b9413946839f10eb6") || id.equals("90af6fea77a9670b41a34812b45be7ace7e11e37")).count() >= 3) {
            characterDetails.getStats().setFort(characterDetails.getStats().getFort() + 3);
            characterDetails.getStats().setReflex(characterDetails.getStats().getReflex() + 3);
            characterDetails.getStats().setWill(characterDetails.getStats().getWill() + 3);
            characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("You automatically make saving throws vs. dragon breath weapons").build());
        }
        
        // Dragonscale Set
        // all 3 - 7 fire dr
        if(itemsMap.keySet().stream().filter((item) -> item.equals("3f9d6414ad5259959d23f8b509b8881d47ca1d11") || item.equals("72341d583d26a57a7f3d3f460f2e976d315688b0") || item.equals("2effa74fb480d06733e2aeff29394badf15e58c8")).count() >= 3) {
            characterDetails.getStats().setDrFire(characterDetails.getStats().getDrFire() + 7);
        }

        // Eldritch Set
        // 2 piece ignore spell resistence, healing spells +10, melee igonore DR
        if (eldrichCount >= 5) {
            characterDetails.getStats().setSpellHeal(characterDetails.getStats().getSpellHeal() + 10);
            characterDetails.getStats().setSpellDmg(characterDetails.getStats().getSpellDmg() + 4);
            characterDetails.getStats().setMeleeDmg(characterDetails.getStats().getMeleeDmg() + 4);
            characterDetails.getStats().setRangeDmg(characterDetails.getStats().getRangeDmg() + 4);
        } else if (eldrichCount == 4) {
            characterDetails.getStats().setSpellHeal(characterDetails.getStats().getSpellHeal() + 8);
            characterDetails.getStats().setSpellDmg(characterDetails.getStats().getSpellDmg() + 3);
            characterDetails.getStats().setMeleeDmg(characterDetails.getStats().getMeleeDmg() + 3);
            characterDetails.getStats().setRangeDmg(characterDetails.getStats().getRangeDmg() + 3);
        } else if (eldrichCount == 3) {
            characterDetails.getStats().setSpellHeal(characterDetails.getStats().getSpellHeal() + 6);
            characterDetails.getStats().setSpellDmg(characterDetails.getStats().getSpellDmg() + 2);
            characterDetails.getStats().setMeleeDmg(characterDetails.getStats().getMeleeDmg() + 2);
            characterDetails.getStats().setRangeDmg(characterDetails.getStats().getRangeDmg() + 2);
        } else if (eldrichCount == 2) {
            characterDetails.getStats().setSpellHeal(characterDetails.getStats().getSpellHeal() + 4);
            characterDetails.getStats().setSpellDmg(characterDetails.getStats().getSpellDmg() + 1);
            characterDetails.getStats().setMeleeDmg(characterDetails.getStats().getMeleeDmg() + 1);
            characterDetails.getStats().setRangeDmg(characterDetails.getStats().getRangeDmg() + 1);
        }
        
        // Footman Set
        // all 3 +2 AC and cold DR 1
        if(itemsMap.keySet().stream().filter((item) -> item.equals("a0963d04a21f5987224588d320e66dcb1bec5e07") || item.equals("c431e2d1507703f78db6d76a5c65c697e9a3f795") || item.equals("dff11b4606afca716d94fafa81382a9078ff43d7")).count() >= 3) {
            characterDetails.getStats().setMeleeAC(characterDetails.getStats().getMeleeAC() + 2);
            characterDetails.getStats().setRangeAC(characterDetails.getStats().getRangeAC() + 2);
            characterDetails.getStats().setDrCold(characterDetails.getStats().getDrCold() + 1);
        }
        
        // Might Set
        // 3 or more +1 level
        // 4 or more +2 melee dmg
        // 5 +4 melee dmg
        if(mightCount == 5)
            characterDetails.getStats().setMeleeDmg(characterDetails.getStats().getMeleeDmg() + 4);
        else if(mightCount == 4)
            characterDetails.getStats().setMeleeDmg(characterDetails.getStats().getMeleeDmg() + 2);
        
        // Mithral Set
        // 3 half dmg breath weapons
        // 5 +10 ret dmg vs undead
        // 6 instant kill dragon on nat 20 if dmg is 8 or 9
        if(mithralCount >= 3)
            characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("Your take half damage from breath weapons.").build());
        if(mithralCount >= 5)
            characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("You have Deadbane: any successful melee attack from an undead monster to the wearer does 10 points of damage to that undead monster").build());
        if(mithralCount >= 6)
            characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("Your +1 MITHRAL LONG SWORD will instantly kill a dragon on a “natural 20” if the 8 or 9 on its damage wheel is closest to the damage dot on the combat board.").build());
        
        // Mountain Dwarf Set
        //all 3 1 cold DR
        if(itemsMap.keySet().stream().filter((item) -> item.equals("886c908995eae55ebb33ff143b07b969fcab09b3") || item.equals("d55ed0f77c4a340d2f7fe558e49700861a23d221") || item.equals("f67c01d654eacdd3a021dc0d3fe3c319043d8caf")).count() >= 3) {
            characterDetails.getStats().setDrCold(characterDetails.getStats().getDrCold() + 1);
        }
        
        // Redoubt Set
        // 3 or more +2 str and +5 HP
        if(redoubtCount >= 3){
            characterDetails.getStats().setStr(characterDetails.getStats().getStr() + 2);
            characterDetails.getStats().setHealth(characterDetails.getStats().getHealth() + 5);
        }
        
        // Templar Set
        // All 3 regen 3
        if(itemsMap.keySet().stream().filter((item) -> item.equals("d9ebb109843e8fa1aa04d90dbf7405e572042fa1") || item.equals("e5b7a23fc4208752b6e29c1c0c040279425bc898") || item.equals("4fb8d5b22892902f33a73630d5dddb8cff8e244b")).count() >= 3) {
            characterDetails.getStats().setRegen(characterDetails.getStats().getRegen() + 3);
        }
        
        // Viper Strike Set
        // 3 or more +2 hit range and melee
        /* Monks: If their Viper Strike weapon critically hits, it
            deals +5 Poison damage—which will get doubled to
            10 because it’s a critical hit. (If you prefer to think of
            this as +10 Poison damage which is not doubled, that’s
            fine.)*/
        /*Rogue: When making a sneak attack with a Viper
            Strike weapon, the bonus damage from the sneak
            attack (+15 if the rogue is 4th level or +20 if the rogue
            is 5th level) is doubled if a critical hit is scored. Under
            normal circumstances, only the non-bonus damage
            from a sneak attack is doubled on a crit.*/
        if(viperCount >= 3) {
            characterDetails.getStats().setMeleeHit(characterDetails.getStats().getMeleeHit() + 2);
            characterDetails.getStats().setRangeHit(characterDetails.getStats().getRangeHit() + 2);
            if(characterDetails.getCharacterClass() == CharacterClass.MONK)
                characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("When your Viper Strike weapon critically hits, it deals +5 Poison damage—which will get doubled to 10 because it’s a critical hit.").build());
            if(characterDetails.getCharacterClass() == CharacterClass.ROGUE) {
                if(characterDetails.getStats().getLevel() > 4)
                    characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("When making a sneak attack with a Viper Strike weapon, the bonus damage from the sneak attack +20 damage is doubled if a critical hit is scored.").build());
                else
                    characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("When making a sneak attack with a Viper Strike weapon, the bonus damage from the sneak attack +15 damage is doubled if a critical hit is scored.").build());
            }     
        }
        
        // Wind Set
        // all 3 character gains the feather fall effect and immunity to non-magical physical missiles.
        if(itemsMap.keySet().stream().filter((item) -> item.equals("0d71d03613ff843d7a933d65f848375000e76641") || item.equals("5118b837bd908c60e7dac00767c4836a31a85878") || item.equals("db2de322945529f5dbf3c97030ffa6ff52df96c4")).count() >= 3) {
            characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("You have the feather fall effect and immunity to non-magical physical missiles.").build());
        }
    }
    
    private void setCharacterNotes(CharacterDetails characterDetails) {
        // Reset Notes
        characterDetails.setNotes(new ArrayList<>());
        
        if(null != characterDetails.getCharacterClass()) switch (characterDetails.getCharacterClass()) {
            case BARBARIAN:
                if(characterDetails.getStats().getLevel() == 5) {
                    characterDetails.getNotes().add(CharacterNote.builder().oncePerGame(true).note("Greater Rage - Until end of combat you have +6 damage with all melee attacks.").build());
                } else {
                    characterDetails.getNotes().add(CharacterNote.builder().oncePerGame(true).note("Rage - Until end of combat you have +4 damage with all melee attacks.").build());  
                }   break;
            case BARD:
                characterDetails.getNotes().add(CharacterNote.builder().oncePerRound(true).note("Monster Lore Check.").build());
                break;
            case CLERIC:
                if(characterDetails.getStats().getLevel() == 5) {
                    characterDetails.getNotes().add(CharacterNote.builder().oncePerRoom(true).note("Improved Turn Undead - Deals 9 or 12 damage to all undead.").build());
                } else {
                    characterDetails.getNotes().add(CharacterNote.builder().oncePerRoom(true).note("Turn Undead - Deals 5 or 8 damage to all undead.").build());  
                }   break;
            case DRUID:
                characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("Communicate with Animals.").build());
                if(characterDetails.getStats().getLevel() == 5) 
                    characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("Keen Polymorph - When polymorphed your melee attacks deal +5 damage and can crit on 19-20.").build());
                break;
            case DWARF_FIGHTER:
                characterDetails.getNotes().add(CharacterNote.builder().oncePerRoom(true).note("Taunt - Target's next attack must include you.").build());
                if(characterDetails.getStats().getLevel() == 5) 
                    characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("Enhanced Critical - Anytime you slide a natural 20 the result is triple damage.").build());
                break;
            case ELF_WIZARD:
                if(characterDetails.getStats().getLevel() == 5) 
                    characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("Focused Polymorph - When you are polymorphed your melee attacks have +3 to hit.").build());
                break;
            case FIGHTER:
                if(characterDetails.getStats().getLevel() == 5) 
                    characterDetails.getNotes().add(CharacterNote.builder().oncePerRoom(true).note("Weapon Specialization - You may immediately re-slide a melee attack but must take the second result.").build());
                break;
            case MONK:
                characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("Deflect Missles - You are immune to non-magical missile attacks").build());
                characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("Feather Fall - You take no fall damage from falling 60 feet or less.").build());
                if(characterDetails.getStats().getLevel() == 5) {
                    characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("Diamond Body - You are immune to posions.").build());
                    characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("Improved Evasion - You take no damage if you succeed half if you fail against required Reflex saving throw against damage from spells, traps, or breath.").build());
                    characterDetails.getNotes().add(CharacterNote.builder().oncePerRoom(true).note("Stunning Fist - Your first natural 20 with Flurry of Blows stuns the target for 1 round.").build());
                } else {
                    characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("Evasion - You take no damage if you succeed on a required Reflex saving throw against damage from spells, traps, or breath.").build());
                    characterDetails.getNotes().add(CharacterNote.builder().oncePerRoom(true).note("Dazing Fist - Your first natural 20 with Flurry of Blows dazes the target for 1 round.").build());  
                }   break;
            case PALADIN:
                characterDetails.getNotes().add(CharacterNote.builder().oncePerRoom(true).note("Guard - Guard a target party member.  If that party member were to be attacked with a melee attack, that attack is resolved against you instead.").build());
                characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("Immunity to Disease - You are immune to all disease.").build());
                break;
            case RANGER:
                if(characterDetails.getStats().getLevel() == 5) {
                    characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("Favored Enemy - Your attack have +2 damage against undead.").build());
                } else {
                    characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("Favored Enemy - Your attack have +1 damage against undead.").build());  
                }   break;
            case ROGUE:
                if(characterDetails.getStats().getLevel() == 5) {
                    characterDetails.getNotes().add(CharacterNote.builder().oncePerRoom(true).note("Flank Attack - Instead of attacking, you may put a upside down token in slider and places it on the board to act as a bumper.").build());
                    characterDetails.getNotes().add(CharacterNote.builder().oncePerRoom(true).note("Improved Sneak Attack - You do +20 to Damage on a successful melee hit.").build());
                } else {
                    characterDetails.getNotes().add(CharacterNote.builder().oncePerRoom(true).note("Sneak Attack - You do +15 to Damage on a successful melee hit.").build());  
                }   break; 
            case WIZARD:
                if(characterDetails.getStats().getLevel() == 5) 
                    characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("Wand Mastery - Damage-dealing wands deal +2 Damage.").build());
                break;
            default:
                break;
        }
    }
    
    private void calculateStats(CharacterDetails characterDetails, Map<String, CharacterItemSet> itemDetailsMap) {
        final CharacterStats stats = characterDetails.getStats();
        final List<CharacterItem> conditionalTokens = new ArrayList<>();
        final List<CharacterItemSet> postConditionalTokens = new ArrayList<>();
        final Set<ConditionalUse> metCondition = new HashSet<>();
        final List<Integer> meleeWeaponHit = new ArrayList<>();
        final List<Integer> rangeWeaponHit = new ArrayList<>();
        AtomicBoolean mysticStaff = new AtomicBoolean(false);
        AtomicBoolean hasSkullOfCavadar = new AtomicBoolean(false);
        AtomicBoolean hasMysticOrb = new AtomicBoolean(false);
        AtomicBoolean hasFighterRelic = new AtomicBoolean(false);
        AtomicBoolean hasFighterLegendary = new AtomicBoolean(false);
        AtomicBoolean hasWonderEffect = new AtomicBoolean(false);
        AtomicBoolean hasBenrows = new AtomicBoolean(false);
        AtomicBoolean canSemiLichCharm = new AtomicBoolean(true);
        AtomicBoolean hasSemiLichCharm = new AtomicBoolean(false);
        AtomicBoolean canHaveRareMelee = new AtomicBoolean(true);
        AtomicBoolean canHaveCommonMelee = new AtomicBoolean(true);
        AtomicBoolean canHaveUncommonMelee = new AtomicBoolean(true);
        AtomicInteger sixLevelReward = new AtomicInteger(0);
        AtomicInteger mightyRanged = new AtomicInteger(0);
        AtomicInteger additionalTreasureTokens = new AtomicInteger(0);
        AtomicInteger additionalHavocRings = new AtomicInteger(0);
        AtomicInteger iounStoneCount = new AtomicInteger(0);
        AtomicInteger meleeShieldAc = new AtomicInteger(0);
        AtomicInteger meleeShieldStr = new AtomicInteger(0);
        AtomicInteger rangeShieldStr = new AtomicInteger(0);
        AtomicInteger figurineSlots = new AtomicInteger(0);
        AtomicReference<String> sheildId = new AtomicReference<>("");

        itemDetailsMap.values().stream().filter((item) -> item.getItem().getItemId()!=null).forEach((item) -> {
            if (item.getTokenFullDetails().isWonderEffect()) {
                if (hasWonderEffect.get()) {
                    metCondition.add(ConditionalUse.NOT_WITH_OTHER_WONDER);
                } else {
                    hasWonderEffect.set(true);
                }
            }

            if (item.getTokenFullDetails().getId().equals("5b4d906cca80b7f2cd719133d4ff6822c435f5c3"))
                metCondition.add(ConditionalUse.NOT_WITH_ROSP);
            else if (item.getTokenFullDetails().getId().equals("5e9d4e09f933a6e2a09ecd875f762683baaa09c8") && canSemiLichCharm.get())
                hasSemiLichCharm.set(true);
            else if (item.getTokenFullDetails().getId().equals("1ee980321b7b26f523b7b5e10b6a2856400d1a67"))
                hasBenrows.set(true);
            else if (item.getTokenFullDetails().getName().equals("Amulet of Noble Might"))
                hasFighterRelic.set(true);
            else if (item.getTokenFullDetails().getName().equals("Viv’s Amulet of Noble Might")) {
                hasFighterRelic.set(true);
                hasFighterLegendary.set(true);
            } else if (item.getTokenFullDetails().getName().toLowerCase().contains("mystic staff"))
                mysticStaff.set(true);
            else if (item.getTokenFullDetails().getId().equals("958f1c96f2e1072f0488513bde34e65553b1ebaa")) {
                metCondition.add(ConditionalUse.NOT_WITH_SOC);
                hasSemiLichCharm.set(false);
                canSemiLichCharm.set(false);
            } else if (item.getTokenFullDetails().getId().equals("5953c6d97cf694aa577f954898d267b8a13e1d88")) {
                metCondition.add(ConditionalUse.NOT_WITH_ROSS);
            } else if (item.getTokenFullDetails().getId().equals("0448ddb1214a3f5c03af24653383d507fa0ea85c"))
                metCondition.add(ConditionalUse.NOT_WITH_COA);
            else if (item.getTokenFullDetails().getId().equals("63cc231ebcbb18e23c9979ba26b38f3ff9f21d92"))
                metCondition.add(ConditionalUse.NOT_WITH_COS_COA);
            else if (item.getTokenFullDetails().getTreasureMin() > 0 && item.getTokenFullDetails().getRarity() != Rarity.ARTIFACT && !item.getTokenFullDetails().getName().equals("Charm of Treasure Boosting")) {
                if (additionalTreasureTokens.addAndGet(1) > 1)
                    metCondition.add(ConditionalUse.NO_OTHER_TREASURE);
                if (item.getTokenFullDetails().getRarity().isHigherThanExalted() || additionalTreasureTokens.get() > 1)
                    metCondition.add(ConditionalUse.ONE_OTHER_UR_TREASURE);
            } else if (item.getTokenFullDetails().getName().toLowerCase().contains("ring of havoc")) {
                if (additionalHavocRings.addAndGet(1) > 1)
                    metCondition.add(ConditionalUse.NO_OTHER_HAVOC);
            }

            if (item.getItem().getSlot() == Slot.MAINHAND || item.getItem().getSlot() == Slot.OFFHAND) {
                if (item.getTokenFullDetails().isOneHanded())
                    metCondition.add(ConditionalUse.WEAPON_1H);
                if (item.getTokenFullDetails().isTwoHanded())
                    metCondition.add(ConditionalUse.WEAPON_2H);
                if (isMeleeWeapon(item.getItem(), item.getTokenFullDetails(), characterDetails.getCharacterClass())) {
                    if (item.getTokenFullDetails().getRarity() == Rarity.COMMON) {
                        if (canHaveCommonMelee.get()) {
                            metCondition.add(ConditionalUse.COMMON_WEAPON_MELEE);
                            metCondition.add(ConditionalUse.UNCOMMON_OR_BELOW_WEAPON_MELEE);
                        } else {
                            canHaveRareMelee.set(false);
                            canHaveUncommonMelee.set(false);
                            metCondition.remove(ConditionalUse.RARE_WEAPON_MELEE);
                            metCondition.remove(ConditionalUse.UNCOMMON_WEAPON_MELEE);
                        }
                    } else if (item.getTokenFullDetails().getRarity() == Rarity.UNCOMMON) {
                        if (canHaveUncommonMelee.get()) {
                            metCondition.add(ConditionalUse.UNCOMMON_WEAPON_MELEE);
                            metCondition.add(ConditionalUse.UNCOMMON_OR_BELOW_WEAPON_MELEE);
                        } else {
                            canHaveRareMelee.set(false);
                            canHaveCommonMelee.set(false);
                            metCondition.remove(ConditionalUse.RARE_WEAPON_MELEE);
                            metCondition.remove(ConditionalUse.COMMON_WEAPON_MELEE);
                        }
                    } else if (item.getTokenFullDetails().getRarity() == Rarity.RARE) {
                        if (canHaveRareMelee.get()) {
                            metCondition.add(ConditionalUse.RARE_WEAPON_MELEE);
                            metCondition.remove(ConditionalUse.UNCOMMON_OR_BELOW_WEAPON_MELEE);
                        } else {
                            canHaveUncommonMelee.set(false);
                            canHaveCommonMelee.set(false);
                            metCondition.remove(ConditionalUse.COMMON_WEAPON_MELEE);
                            metCondition.remove(ConditionalUse.UNCOMMON_WEAPON_MELEE);
                        }
                    }
                }

                if(item.getTokenFullDetails().getConditionalUse() == ConditionalUse.NONE && !item.getTokenFullDetails().isMug() && isMeleeWeapon(item.getItem(), item.getTokenFullDetails(), characterDetails.getCharacterClass()))
                    meleeWeaponHit.add(item.getTokenFullDetails().getMeleeHit());
            }
            else if (item.getItem().getSlot() == Slot.RANGE_MAINHAND || item.getItem().getSlot() == Slot.RANGE_OFFHAND) {
                metCondition.add(ConditionalUse.WEAPON_RANGED);
                if (item.getItem().getSlot() == Slot.RANGE_MAINHAND)
                    metCondition.add(ConditionalUse.MISSILE_ATTACK);
                if (item.getTokenFullDetails().getName().toLowerCase().contains("sling"))
                    metCondition.add(ConditionalUse.SLING);
                if (item.getTokenFullDetails().getName().toLowerCase().contains("crossbow"))
                    metCondition.add(ConditionalUse.CROSSBOW);
                else if (item.getTokenFullDetails().getName().toLowerCase().contains("bow"))
                    metCondition.add(ConditionalUse.BOW);
                if (item.getTokenFullDetails().isTwoHanded())
                    metCondition.add(ConditionalUse.WEAPON_RANGED_2H);
                if (item.getItem().getSlot() == Slot.RANGE_MAINHAND) {
                    if (item.getTokenFullDetails().getRarity() == Rarity.RARE)
                        metCondition.add(ConditionalUse.RARE_WEAPON_RANGE);
                    else if (item.getTokenFullDetails().getRarity() == Rarity.COMMON) {
                        metCondition.add(ConditionalUse.COMMON_WEAPON_RANGE);
                        metCondition.add(ConditionalUse.UNCOMMON_OR_BELOW_WEAPON_RANGE);
                    } else if (item.getTokenFullDetails().getRarity() == Rarity.UNCOMMON) {
                        metCondition.add(ConditionalUse.UNCOMMON_WEAPON_RANGE);
                        metCondition.add(ConditionalUse.UNCOMMON_OR_BELOW_WEAPON_RANGE);
                    }
                }
                if(item.getTokenFullDetails().isThrown() || item.getTokenFullDetails().getName().toLowerCase().contains("mighty"))
                    mightyRanged.set(1);
                if (item.getTokenFullDetails().getConditionalUse() == ConditionalUse.NONE && isRangeWeapon(item.getItem(), item.getTokenFullDetails()))
                    rangeWeaponHit.add(item.getTokenFullDetails().getRangeHit());
            }
            else if(item.getItem().getSlot() == Slot.TORSO && item.getItem().getRarity().ordinal() > Rarity.UNCOMMON.ordinal())
                metCondition.add(ConditionalUse.NOT_RARE_PLUS_TORSO);
            else if(item.getItem().getSlot() == Slot.FINGER && item.getItem().getRarity().ordinal() > Rarity.RARE.ordinal())
                metCondition.add(ConditionalUse.NOT_UR_PLUS_RING);
            else if (item.getTokenFullDetails().isShield())
                metCondition.add(ConditionalUse.MAY_NOT_USE_SHIELDS);
            else if (item.getTokenFullDetails().getSlot() == Slot.IOUNSTONE) {
                if(iounStoneCount.addAndGet(1) > 1)
                    metCondition.add(ConditionalUse.NO_OTHER_IOUN_STONE);
            }

            if(item.getTokenFullDetails().getName().contains("Tooth of Cavadar")) {
                stats.setPsychicLevel(stats.getPsychicLevel() + 1);
                if (stats.getPsychicLevel() > 7)
                    stats.setPsychicLevel(7);
            } else if(item.getTokenFullDetails().getName().contains("Skull of Cavadar")) {
                stats.setPsychicLevel(7);
                hasSkullOfCavadar.set(true);
            } else if(item.getTokenFullDetails().getName().contains("Ioun Stone Mystic Orb")) {
                hasMysticOrb.set(true);
            }
            if((item.getTokenFullDetails().getId().equals("028d1ddec034be61aa3b3abaed02d76db2139084") ||
                    item.getTokenFullDetails().getId().equals("3bed20c850924c4b9009f50ed5b4de2998d311b2") ||
                    item.getTokenFullDetails().getId().equals("a7105f233e1b317558d2a94fbf90ca00048aaaa9")) &&
                    sixLevelReward.get() == 0) {
                sixLevelReward.set(1);
                stats.setTreasureMin(stats.getTreasureMin() + 1);
                stats.setTreasureMax(stats.getTreasureMax() + 1);
            }

            if(item.getTokenFullDetails().getConditionalUse() != ConditionalUse.NONE) {
                if (item.getTokenFullDetails().getConditionalUse().isPost())
                    postConditionalTokens.add(item);
                else
                    conditionalTokens.add(item.getItem());
            } else {
                // check for same shield
                if (item.getTokenFullDetails().isShield()) {
                    if (item.getItem().getSlot() == Slot.OFFHAND) {
                        item.getItem().setSlotStatus(SlotStatus.OK);
                        item.getItem().setStatusText(null);
                        stats.setMeleeAC(stats.getMeleeAC()+ item.getTokenFullDetails().getMeleeAC());
                        meleeShieldAc.set(item.getTokenFullDetails().getMeleeAC());
                        meleeShieldStr.set(item.getTokenFullDetails().getStr());
                        final int str = item.getTokenFullDetails().getStr();
                        item.getTokenFullDetails().setStr(0);

                        if (sheildId.get().isEmpty() || !sheildId.get().equals(item.getTokenFullDetails().getId()))
                            updateStats(item.getItem().getSlot(), stats, item.getTokenFullDetails(), characterDetails.getNotes(), false, true, false, false);

                        if (sheildId.get().isEmpty())
                            sheildId.set(item.getTokenFullDetails().getId());

                        item.getTokenFullDetails().setStr(str);
                    } else if (item.getItem().getSlot() == Slot.RANGE_OFFHAND) {
                        item.getItem().setSlotStatus(SlotStatus.OK);
                        item.getItem().setStatusText(null);
                        stats.setRangeAC(stats.getRangeAC() + item.getTokenFullDetails().getRangeAC());
                        stats.setRangeMissileAC(stats.getRangeMissileAC() + item.getTokenFullDetails().getRangeMissileAC());
                        rangeShieldStr.set(item.getTokenFullDetails().getStr());
                        final int str = item.getTokenFullDetails().getStr();
                        item.getTokenFullDetails().setStr(0);

                        if (sheildId.get().isEmpty() || !sheildId.get().equals(item.getTokenFullDetails().getId()))
                            updateStats(item.getItem().getSlot(), stats, item.getTokenFullDetails(), characterDetails.getNotes(), false, true, false, false);

                        if (sheildId.get().isEmpty())
                            sheildId.set(item.getTokenFullDetails().getId());

                        item.getTokenFullDetails().setStr(str);
                    }
                } else {
                    item.getItem().setSlotStatus(SlotStatus.OK);
                    item.getItem().setStatusText(null);
                    updateStats(item.getItem().getSlot(), stats, item.getTokenFullDetails(), characterDetails.getNotes(), false, false, false, false);
                }
            }

            if (item.getTokenFullDetails().getSlotModifiers() != null && !item.getTokenFullDetails().getSlotModifiers().isEmpty()) {
                item.getTokenFullDetails().getSlotModifiers().forEach(slotModifier -> {
                    if (slotModifier.getSlot() == Slot.FIGURINE) {
                        figurineSlots.getAndAdd(slotModifier.getModifier());
                    }
                });
            }
        });

        // Check Conditionals 
        checkConditionals(conditionalTokens, metCondition, stats, characterDetails, meleeWeaponHit, rangeWeaponHit);

        stats.setMeleeStr(stats.getStr()+meleeShieldStr.get());
        stats.setRangeStr(stats.getStr()+rangeShieldStr.get());
        stats.setStr(stats.getMeleeStr());
        stats.setStrBonus((stats.getStr()-10 > 0)?(stats.getStr()-10)/2:(stats.getStr()-11)/2);
        stats.setMeleeStrBonus(stats.getStrBonus());
        stats.setRangeStrBonus((stats.getRangeStr()-10 > 0)?(stats.getRangeStr()-10)/2:(stats.getRangeStr()-11)/2);
        stats.setStrBonus((stats.getStr()-10 > 0)?(stats.getStr()-10)/2:(stats.getStr()-11)/2);
        stats.setDexBonus((stats.getDex()-10 > 0)?(stats.getDex()-10)/2:(stats.getDex()-11)/2);
        stats.setConBonus((stats.getCon()-10 > 0)?(stats.getCon()-10)/2:(stats.getCon()-11)/2);
        stats.setIntelBonus((stats.getIntel()-10 > 0)?(stats.getIntel()-10)/2:(stats.getIntel()-11)/2);
        stats.setWisBonus((stats.getWis()-10 > 0)?(stats.getWis()-10)/2:(stats.getWis()-11)/2);
        stats.setChaBonus((stats.getCha()-10 > 0)?(stats.getCha()-10)/2:(stats.getCha()-11)/2);

        stats.setHealth(stats.getHealth() + stats.getLevel() * (int)((stats.getConBonus() < 0)?stats.getConBonus()+(stats.getBaseCon()-10)/2:stats.getConBonus()-(stats.getBaseCon()-10)/2));
        stats.setMeleeHit(stats.getMeleeHit() + stats.getStrBonus());
        stats.setMeleeDmg(stats.getMeleeDmg() + stats.getStrBonus());
        stats.setMeleePolyHit(stats.getMeleePolyHit() + stats.getMeleeHit());
        stats.setMeleePolyDmg(stats.getMeleePolyDmg() + stats.getMeleeDmg());
        if (mysticStaff.get())
            stats.setRangeHit(stats.getRangeHit() + Math.max(stats.getIntelBonus(), stats.getWisBonus()));
        else
            stats.setRangeHit(stats.getRangeHit() + stats.getDexBonus());
        stats.setMeleeAC(stats.getMeleeAC() + stats.getDexBonus());
        stats.setRangeAC(stats.getRangeAC() + stats.getDexBonus());
        stats.setFort(stats.getFort() + stats.getConBonus());
        stats.setReflex(stats.getReflex() + stats.getDexBonus());
        stats.setWill(stats.getWill() + stats.getWisBonus());

        // Check Post Conditionals
        checkPostConditionals(postConditionalTokens, characterDetails.getNotes(), metCondition, stats, characterDetails, meleeWeaponHit, rangeWeaponHit);

        int maxHitMelee = 0;
        if (!meleeWeaponHit.isEmpty()) {
            maxHitMelee = Collections.max(meleeWeaponHit);
            meleeWeaponHit.remove(Collections.max(meleeWeaponHit));
            meleeWeaponHit.forEach(hit -> {
                stats.setMeleeHit(stats.getMeleeHit() - hit);
            });
        }
        int maxHitRange = 0;
        if (!rangeWeaponHit.isEmpty()) {
            maxHitRange = Collections.max(rangeWeaponHit);
            rangeWeaponHit.remove(Collections.max(rangeWeaponHit));
            rangeWeaponHit.forEach(hit -> {
                stats.setRangeHit(stats.getRangeHit() - hit);
            });
        }

        if(null != characterDetails.getCharacterClass()) switch (characterDetails.getCharacterClass()) {
            case BARBARIAN:
                if(metCondition.contains(ConditionalUse.WEAPON_2H))
                    stats.setMeleeDmg(stats.getMeleeDmg() + 4);
                break;
            case FIGHTER:
            case DWARF_FIGHTER:
                if (hasFighterRelic.get()) {
                    stats.setReflex(stats.getReflex() + meleeShieldAc.get());
                }
            case MONK:
                if (hasBenrows.get()) {
                    stats.setRangeHitBenrow(stats.getRangeHit() - maxHitRange + maxHitMelee);
                    stats.setRangeDmgBenrow(stats.getRangeDmg() + stats.getStrBonus() + 7);
                    stats.setBSacred(stats.getBSacred() + 7);
                    stats.setRangeSacredBenrow(true);
                }
                break;
            default:
                break;
        }
        
        if (hasSemiLichCharm.get()) 
            stats.setHealth(stats.getHealth() + stats.getPsychicLevel());
        if(mightyRanged.get() == 1) {
            stats.setRangeDmg(stats.getRangeDmg() + stats.getRangeStrBonus());
        }
        if (hasMysticOrb.get() && hasSkullOfCavadar.get()) {
            stats.setReflex(stats.getReflex() + 1);
            stats.setWill(stats.getWill() + 1);
            stats.setFort(stats.getFort() + 1);
        }
            
        int figurineCount = (int)characterDetails.getItems().stream().filter((item) -> item.getSlot()==Slot.FIGURINE).count();
        int figurineNeeded = figurineSlots.get() + 1 + ((stats.getCha() >= 16) ? 1 : 0);
        if (figurineCount < figurineNeeded) {
            for (int i=figurineCount; i<figurineNeeded; i++) {
                characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.FIGURINE).index(i).slotStatus(SlotStatus.OK).build());
            }
        } else if (figurineCount > figurineNeeded)
            characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> !(item.getSlot()==Slot.FIGURINE && item.getIndex() > figurineNeeded-1)).collect(Collectors.toList()));
    }
    
    private void checkConditionals(List<CharacterItem> conditionalTokens, Set<ConditionalUse> metCondition, CharacterStats stats, CharacterDetails characterDetails, List<Integer> meleeWeaponHit, List<Integer> rangeWeaponHit) {
        final List<CharacterNote> notes = characterDetails.getNotes();
        List<CharacterItem> failedConditions = checkConditionals(conditionalTokens, null, notes, metCondition, stats, characterDetails, meleeWeaponHit, rangeWeaponHit);

        if (!failedConditions.isEmpty()) {
            int errorCount = failedConditions.size();
            int newErrorCount = 0;
            int retry = 0;

            while (errorCount != newErrorCount && retry < 5) {
                failedConditions = checkConditionals(conditionalTokens, failedConditions, notes, metCondition, stats, characterDetails, meleeWeaponHit, rangeWeaponHit);
                newErrorCount = failedConditions.size();
                retry++;
            }
        }
    }

    private void checkPostConditionals(List<CharacterItemSet> conditionalTokens, List<CharacterNote> notes, Set<ConditionalUse> metCondition, CharacterStats stats, CharacterDetails characterDetails, List<Integer> meleeWeaponHit, List<Integer> rangeWeaponHit) {
        conditionalTokens.forEach((token) -> {
            TokenFullDetails td = token.getTokenFullDetails();
            if (null != td.getConditionalUse()) switch (td.getConditionalUse()) {
                case LESS_THAN_10_HIT_MELEE:
                    if (stats.getMeleeHit() > 6) {
                        token.getItem().setSlotStatus(SlotStatus.INVALID);
                        token.getItem().setStatusText("Your plus to hit cannot exceed 10 to use.");
                    } else {
                        token.getItem().setSlotStatus(SlotStatus.OK);
                        token.getItem().setStatusText(null);

                        if (isMeleeWeapon(token.getItem(), td, characterDetails.getCharacterClass())) {
                            meleeWeaponHit.add(td.getMeleeHit());
                            stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit());
                            stats.setMeleeDmg(stats.getMeleeDmg() + td.getMeleeDmg());
                        } else if (isRangeWeapon(token.getItem(), td)) {
                            rangeWeaponHit.add(td.getRangeHit());
                            stats.setRangeHit(stats.getRangeHit() + td.getRangeHit());
                            stats.setRangeDmg(stats.getRangeDmg() + td.getRangeDmg());
                        }

                        updateStats(token.getItem().getSlot(), stats, td, notes, true, false, false, false);
                    } break;
                case DEX_TO_DMG_10:
                    if (stats.getDexBonus() > 10) {
                        stats.setRangeDmg(stats.getRangeDmg() + 10);
                    } else if (stats.getDexBonus() > 0) {
                        stats.setRangeDmg(stats.getRangeDmg() + stats.getDexBonus());
                    } break;
                default:
                    break;
            }
        });
    }

    private List<CharacterItem> checkConditionals(List<CharacterItem> conditionalTokens, List<CharacterItem> retryTokens, List<CharacterNote> notes, Set<ConditionalUse> metCondition, CharacterStats stats, CharacterDetails characterDetails, List<Integer> meleeWeaponHit, List<Integer> rangeWeaponHit) {
        final List<CharacterItem> failedConditions = new ArrayList<>();
        conditionalTokens.stream().filter(token -> retryTokens == null || retryTokens.contains(token)).forEach((token) -> {
            TokenFullDetails td = tokenAdminMapper.getTokenDetails(token.getItemId());
            td.setSlotModifiers(tokenAdminMapper.getSlotModifiers(td.getId()));
            if(null != td.getConditionalUse()) switch (td.getConditionalUse()) {
                case NOT_WITH_ROSP:
                    if(metCondition.contains(ConditionalUse.NOT_WITH_ROSP)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText(token.getName() + " cannot be used with the Rod of Seven Parts.");
                        failedConditions.add(token);
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(token.getSlot(), stats, td, notes, false, false, false, false);
                    }   break;
                case NOT_WITH_SOC:
                    if(metCondition.contains(ConditionalUse.NOT_WITH_SOC)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText(token.getName() + " cannot be used with the Skull of Cavadar.");
                        failedConditions.add(token);
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(token.getSlot(), stats, td, notes, false, false, false, false);
                    }   break;
                case NOT_WITH_ROSS:
                    if(metCondition.contains(ConditionalUse.NOT_WITH_ROSS)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText(token.getName() + " cannot be used with the Ring od Spell Storing.");
                        failedConditions.add(token);
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(token.getSlot(), stats, td, notes, false, false, false, false);
                    }   break;
                case WEAPON_2H_ANY:
                    boolean somethingUsed = false;
                    if(metCondition.contains(ConditionalUse.WEAPON_2H)) {
                        somethingUsed = true;
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(token.getSlot(), stats, td, notes, false, false, true, false);
                    }
                    if(metCondition.contains(ConditionalUse.WEAPON_RANGED_2H)) {
                        somethingUsed = true;
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(token.getSlot(), stats, td, notes, false, false, false, true);
                    }
                    if (!somethingUsed) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a two handed weapon to use.");
                        failedConditions.add(token);
                    } break;
                case WEAPON_2H:
                    if(!metCondition.contains(ConditionalUse.WEAPON_2H)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a two handed weapon to use.");
                        failedConditions.add(token);
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(token.getSlot(), stats, td, notes, false, false, false, false);
                    }   break;
                case WEAPON_1H:
                    if(!metCondition.contains(ConditionalUse.WEAPON_1H)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a one handed weapon to use.");
                        failedConditions.add(token);
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(token.getSlot(), stats, td, notes, false, false, false, false);
                    }   break;
                case WEAPON_RANGED:
                    if(!metCondition.contains(ConditionalUse.WEAPON_RANGED)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a range weapon to use.");
                        failedConditions.add(token);
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(token.getSlot(), stats, td, notes, false, false, false, false);
                    }   break;
                case WEAPON_RANGED_2H:
                    if(!metCondition.contains(ConditionalUse.WEAPON_RANGED_2H)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a two handed range weapon to use.");
                        failedConditions.add(token);
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(token.getSlot(), stats, td, notes, false, false, false, false);
                    }   break;    
                case DEXTERITY_18:
                    if(stats.getDex() < 18) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a dexterity of 18 or higher to use.");
                        failedConditions.add(token);
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        
                        if (isMeleeWeapon(token, td, characterDetails.getCharacterClass())) {
                            meleeWeaponHit.add(td.getMeleeHit());
                            stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit());
                            stats.setMeleeDmg(stats.getMeleeDmg() + td.getMeleeDmg());
                        } else if (isRangeWeapon(token, td)) {
                            rangeWeaponHit.add(td.getRangeHit());
                            stats.setRangeHit(stats.getRangeHit() + td.getRangeHit());
                            stats.setRangeDmg(stats.getRangeDmg() + td.getRangeDmg());
                        }
                        
                        updateStats(token.getSlot(), stats, td, notes, true, false, false, false);
                    }   break;
                case DEXTERITY_20:
                    if(stats.getDex() < 20) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a dexterity of 20 or higher to use.");
                        failedConditions.add(token);
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        
                        if (isMeleeWeapon(token, td, characterDetails.getCharacterClass())) {
                            meleeWeaponHit.add(td.getMeleeHit());
                            stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit());
                            stats.setMeleeDmg(stats.getMeleeDmg() + td.getMeleeDmg());
                        } else if (isRangeWeapon(token, td)) {
                            rangeWeaponHit.add(td.getRangeHit());
                            stats.setRangeHit(stats.getRangeHit() + td.getRangeHit());
                            stats.setRangeDmg(stats.getRangeDmg() + td.getRangeDmg());
                        }
                        
                        updateStats(token.getSlot(), stats, td, notes, true, false, false, false);
                    }   break;
                case INTELLECT_20:
                    if(stats.getIntel()< 20) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a intellect of 20 or higher to use.");
                        failedConditions.add(token);
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        
                        if (isMeleeWeapon(token, td, characterDetails.getCharacterClass())) {
                            meleeWeaponHit.add(td.getMeleeHit());
                            stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit());
                            stats.setMeleeDmg(stats.getMeleeDmg() + td.getMeleeDmg());
                        } else if (isRangeWeapon(token, td)) {
                            rangeWeaponHit.add(td.getRangeHit());
                            stats.setRangeHit(stats.getRangeHit() + td.getRangeHit());
                            stats.setRangeDmg(stats.getRangeDmg() + td.getRangeDmg());
                        }
                        
                        updateStats(token.getSlot(), stats, td, notes, true, false, false, false);
                    }   break;
                case WISDOM_20:
                    if(stats.getWis()< 20) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a wisdom of 20 or higher to use.");
                        failedConditions.add(token);
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        
                        if (isMeleeWeapon(token, td, characterDetails.getCharacterClass())) {
                            meleeWeaponHit.add(td.getMeleeHit());
                            stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit());
                            stats.setMeleeDmg(stats.getMeleeDmg() + td.getMeleeDmg());
                        } else if (isRangeWeapon(token, td)) {
                            rangeWeaponHit.add(td.getRangeHit());
                            stats.setRangeHit(stats.getRangeHit() + td.getRangeHit());
                            stats.setRangeDmg(stats.getRangeDmg() + td.getRangeDmg());
                        }
                        
                        updateStats(token.getSlot(), stats, td, notes, true, false, false, false);
                    }   break;
                case STRENGTH_24:
                    if(stats.getStr()< 24) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a strength of 24 or higher to use.");
                        failedConditions.add(token);
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        
                        if (isMeleeWeapon(token, td, characterDetails.getCharacterClass())) {
                            meleeWeaponHit.add(td.getMeleeHit());
                            stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit());
                            stats.setMeleeDmg(stats.getMeleeDmg() + td.getMeleeDmg());
                        } else if (isRangeWeapon(token, td)) {
                            rangeWeaponHit.add(td.getRangeHit());
                            stats.setRangeHit(stats.getRangeHit() + td.getRangeHit());
                            stats.setRangeDmg(stats.getRangeDmg() + td.getRangeDmg());
                        }
                        
                        updateStats(token.getSlot(), stats, td, notes, true, false, false, false);
                    }   break;
                case NOT_WITH_COS_COA:
                    if(metCondition.contains(ConditionalUse.NOT_WITH_COS_COA)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText(token.getName() + " cannot be used with the Charm of Awakened Synergy.");
                        failedConditions.add(token);
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(token.getSlot(), stats, td, notes, false, false, false, false);
                    }   break;
                case NOT_WITH_COA:
                    if(metCondition.contains(ConditionalUse.NOT_WITH_COA)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText(token.getName() + " cannot be used with the Charm of Avarice.");
                        failedConditions.add(token);
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(token.getSlot(), stats, td, notes, false, false, false, false);
                    }   break;
                case NOT_WITH_PRO_SCROLL:
                    if(metCondition.contains(ConditionalUse.NOT_WITH_PRO_SCROLL)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText(token.getName() + " cannot be used with other scroll protection items.");
                        failedConditions.add(token);
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(token.getSlot(), stats, td, notes, false, false, false, false);
                    }   break;    
                case NO_OTHER_TREASURE:
                    if(metCondition.contains(ConditionalUse.NO_OTHER_TREASURE)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText(token.getName() + " cannot be used with any other treasure enhancing token.");
                        failedConditions.add(token);
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(token.getSlot(), stats, td, notes, false, false, false, false);
                    }   break;
                case NO_OTHER_HAVOC:
                    if(metCondition.contains(ConditionalUse.NO_OTHER_HAVOC)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText(token.getName() + " cannot be used with any other Rings of Havoc.");
                        failedConditions.add(token);
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(token.getSlot(), stats, td, notes, false, false, false, false);
                    }   break;
                case SLING:
                    if(metCondition.contains(ConditionalUse.SLING)) {
                        stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit());
                        stats.setMeleeDmg(stats.getMeleeDmg() + td.getMeleeDmg());
                        stats.setRangeHit(stats.getRangeHit() + td.getRangeHit());
                        stats.setRangeDmg(stats.getRangeDmg() + td.getRangeDmg());
                    }
                    token.setSlotStatus(SlotStatus.OK);
                    token.setStatusText(null);
                    updateStats(token.getSlot(), stats, td, notes, true, false, false, false);
                    break;
                case CROSSBOW:
                    if(metCondition.contains(ConditionalUse.CROSSBOW)) {
                        stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit());
                        stats.setMeleeDmg(stats.getMeleeDmg() + td.getMeleeDmg());
                        stats.setRangeHit(stats.getRangeHit() + td.getRangeHit());
                        stats.setRangeDmg(stats.getRangeDmg() + td.getRangeDmg());
                    } 
                    token.setSlotStatus(SlotStatus.OK);
                    token.setStatusText(null);
                    updateStats(token.getSlot(), stats, td, notes, true, false, false, false);
                    break;
                case BOW:
                    if(metCondition.contains(ConditionalUse.BOW)) {
                        stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit());
                        stats.setMeleeDmg(stats.getMeleeDmg() + td.getMeleeDmg());
                        stats.setRangeHit(stats.getRangeHit() + td.getRangeHit());
                        stats.setRangeDmg(stats.getRangeDmg() + td.getRangeDmg());
                    } 
                    token.setSlotStatus(SlotStatus.OK);
                    token.setStatusText(null);
                    updateStats(token.getSlot(), stats, td, notes, true, false, false, false);
                    break;
                case THRALL_WEAPON:
                    long thrallMelee = characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null&&(item.getSlot()==Slot.MAINHAND||(item.getSlot()==Slot.OFFHAND&&!item.getName().toLowerCase().contains("shield")))&&item.getName().toLowerCase().contains("thrall")).count();
                    long thrallRanged = characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.RANGE_MAINHAND&&item.getName().toLowerCase().contains("thrall")).count();
                   
                    if(thrallMelee > 0) {
                        stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit());
                        stats.setMeleeDmg(stats.getMeleeDmg() + td.getMeleeDmg());
                    }
                    if(thrallRanged > 0) {
                        stats.setRangeHit(stats.getRangeHit() + td.getRangeHit());
                        stats.setRangeDmg(stats.getRangeDmg() + td.getRangeDmg());
                    }
                    
                    token.setSlotStatus(SlotStatus.OK);
                    token.setStatusText(null);
                    updateStats(token.getSlot(), stats, td, notes, true, false, false, false);
                    break;
                case IRON_WEAPON:
                    long ironMelee = characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null&&(item.getSlot()==Slot.MAINHAND||(item.getSlot()==Slot.OFFHAND&&!item.getName().toLowerCase().contains("shield")))&&item.getName().toLowerCase().contains("iron")).count();
                    long ironRanged = characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.RANGE_MAINHAND&&item.getName().toLowerCase().contains("iron")).count();
                    
                    if(ironMelee > 0) {
                        stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit());
                        stats.setMeleeDmg(stats.getMeleeDmg() + td.getMeleeDmg());
                    }
                    if(ironRanged > 0) {
                        stats.setRangeHit(stats.getRangeHit() + td.getRangeHit());
                        stats.setRangeDmg(stats.getRangeDmg() + td.getRangeDmg());
                    }
                    
                    token.setSlotStatus(SlotStatus.OK);
                    token.setStatusText(null);
                    updateStats(token.getSlot(), stats, td, notes, true, false, false, false);
                    break;
                case GOBLIN_WEAPON:
                    long goblinMelee = characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null&&(item.getSlot()==Slot.MAINHAND||(item.getSlot()==Slot.OFFHAND&&!item.getName().toLowerCase().contains("shield")))&&item.getName().toLowerCase().contains("goblin")).count();
                    long goblinRanged = characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.RANGE_MAINHAND&&item.getName().toLowerCase().contains("goblin")).count();

                    if(goblinMelee > 0) {
                        stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit());
                        stats.setMeleeDmg(stats.getMeleeDmg() + td.getMeleeDmg());
                    }
                    if(goblinRanged > 0) {
                        stats.setRangeHit(stats.getRangeHit() + td.getRangeHit());
                        stats.setRangeDmg(stats.getRangeDmg() + td.getRangeDmg());
                    }

                    token.setSlotStatus(SlotStatus.OK);
                    token.setStatusText(null);
                    updateStats(token.getSlot(), stats, td, notes, true, false, false, false);
                    break;
                case DWARF_WEAPON:
                    long dwarvenMelee = characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null&&(item.getSlot()==Slot.MAINHAND||(item.getSlot()==Slot.OFFHAND&&!item.getName().toLowerCase().contains("shield")))&&item.getName().toLowerCase().contains("dwarven")).count();
                    long dwarvenRanged = characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.RANGE_MAINHAND&&item.getName().toLowerCase().contains("dwarven")).count();

                    if(dwarvenMelee > 0) {
                        stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit());
                        stats.setMeleeDmg(stats.getMeleeDmg() + td.getMeleeDmg());
                    }
                    if(dwarvenRanged > 0) {
                        stats.setRangeHit(stats.getRangeHit() + td.getRangeHit());
                        stats.setRangeDmg(stats.getRangeDmg() + td.getRangeDmg());
                    }

                    token.setSlotStatus(SlotStatus.OK);
                    token.setStatusText(null);
                    updateStats(token.getSlot(), stats, td, notes, true, false, false, false);
                    break;
                case ONE_OTHER_UR_TREASURE:
                    if(metCondition.contains(ConditionalUse.ONE_OTHER_UR_TREASURE)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText(token.getName() + " cannot be used with more than one other treasure enhancing token that is Exalted or lower.");
                        failedConditions.add(token);
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(token.getSlot(), stats, td, notes, false, false, false, false);
                    }   break;
                case NOT_WITH_OTHER_WONDER:
                    if(metCondition.contains(ConditionalUse.NOT_WITH_OTHER_WONDER)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText(token.getName() + " cannot be used with other wonder effects.");
                        failedConditions.add(token);
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(token.getSlot(), stats, td, notes, false, false, false, false);
                    }   break;
                case DIRK_WEAPON:
                    long dirkMelee = characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null&&(item.getSlot()==Slot.MAINHAND||(item.getSlot()==Slot.OFFHAND&&!item.getName().toLowerCase().contains("shield")))&&item.getName().toLowerCase().contains("dirk")).count();
                    long dirkRanged = characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.RANGE_MAINHAND&&item.getName().toLowerCase().contains("dirk")).count();
                    
                    if(dirkMelee > 0) {
                        stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit());
                        stats.setMeleeDmg(stats.getMeleeDmg() + td.getMeleeDmg());
                    }
                    if(dirkRanged > 0) {
                        stats.setRangeHit(stats.getRangeHit() + td.getRangeHit());
                        stats.setRangeDmg(stats.getRangeDmg() + td.getRangeDmg());
                    }
                    
                    token.setSlotStatus(SlotStatus.OK);
                    token.setStatusText(null);
                    updateStats(token.getSlot(), stats, td, notes, true, false, false, false);
                    break;
                case RARE_WEAPON:
                    if(metCondition.contains(ConditionalUse.RARE_WEAPON_MELEE)) {
                        stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit());
                        stats.setMeleeDmg(stats.getMeleeDmg() + td.getMeleeDmg());
                    }
                    if(metCondition.contains(ConditionalUse.RARE_WEAPON_RANGE)) {
                        stats.setRangeHit(stats.getRangeHit() + td.getRangeHit());
                        stats.setRangeDmg(stats.getRangeDmg() + td.getRangeDmg());
                    }
                    
                    token.setSlotStatus(SlotStatus.OK);
                    token.setStatusText(null);
                    updateStats(token.getSlot(), stats, td, notes, true, false, false, false);
                    break;
                case COMMON_WEAPON:
                    if(metCondition.contains(ConditionalUse.COMMON_WEAPON_MELEE)) {
                        stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit());
                        stats.setMeleeDmg(stats.getMeleeDmg() + td.getMeleeDmg());
                    }
                    if(metCondition.contains(ConditionalUse.COMMON_WEAPON_RANGE)) {
                        stats.setRangeHit(stats.getRangeHit() + td.getRangeHit());
                        stats.setRangeDmg(stats.getRangeDmg() + td.getRangeDmg());
                    }

                    token.setSlotStatus(SlotStatus.OK);
                    token.setStatusText(null);
                    updateStats(token.getSlot(), stats, td, notes, true, false, false, false);
                    break;
                case UNCOMMON_OR_BELOW_WEAPON:
                    if(metCondition.contains(ConditionalUse.UNCOMMON_OR_BELOW_WEAPON_MELEE)) {
                        stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit());
                        stats.setMeleeDmg(stats.getMeleeDmg() + td.getMeleeDmg());
                    }
                    if(metCondition.contains(ConditionalUse.UNCOMMON_OR_BELOW_WEAPON_RANGE)) {
                        stats.setRangeHit(stats.getRangeHit() + td.getRangeHit());
                        stats.setRangeDmg(stats.getRangeDmg() + td.getRangeDmg());
                    }

                    token.setSlotStatus(SlotStatus.OK);
                    token.setStatusText(null);
                    updateStats(token.getSlot(), stats, td, notes, true, false, false, false);
                    break;
                case NO_OTHER_IOUN_STONE:
                    if(characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.IOUNSTONE).count() > 1) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText(token.getName() + " cannot be equipped with any other ioun stone.");
                        failedConditions.add(token);
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(token.getSlot(), stats, td, notes, false, false, false, false);
                    }  break;
                case MISSILE_ATTACK:
                    if(!metCondition.contains(ConditionalUse.MISSILE_ATTACK)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a missile projectile range weapon to use.");
                        failedConditions.add(token);
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(token.getSlot(), stats, td, notes, false, false, false, false);
                    }   break; 
                case NOT_RARE_PLUS_TORSO:
                    if(metCondition.contains(ConditionalUse.NOT_RARE_PLUS_TORSO)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText(token.getName() + " cannot be used with a rare or higher torso armor.");
                        failedConditions.add(token);
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(token.getSlot(), stats, td, notes, false, false, false, false);
                    }   break;
                case NOT_UR_PLUS_RING:
                    if(metCondition.contains(ConditionalUse.NOT_UR_PLUS_RING)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText(token.getName() + " cannot be used with a ultra rare or higher ring.");
                        failedConditions.add(token);
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(token.getSlot(), stats, td, notes, false, false, false, false);
                    }   break;
                case NO_OTHER_SIXTH_LEVEL_REWARD:
                    if(metCondition.contains(ConditionalUse.NO_OTHER_SIXTH_LEVEL_REWARD)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("Only one player level reward item can equip at a time.");
                        failedConditions.add(token);
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(token.getSlot(), stats, td, notes, false, false, false, false);
                    } break;
                case PLUS_1_AC_GOBLIN_WEAPON:
                    if(metCondition.contains(ConditionalUse.GOBLIN_WEAPON)) {
                        stats.setMeleeAC(stats.getMeleeAC() + 1);
                    } break;
                case MAY_NOT_USE_SHIELDS:
                    if(metCondition.contains(ConditionalUse.MAY_NOT_USE_SHIELDS)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText(token.getName() + " cannot be used with a shield equipped.");
                        failedConditions.add(token);
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(token.getSlot(), stats, td, notes, false, false, false, false);
                    }   break;
                default:
                    break;
            }
        });

        return failedConditions;
    }
                
    private void updateStats(Slot slot, CharacterStats stats, TokenFullDetails td, List<CharacterNote> notes, boolean ignoreHitAndDamage, boolean ignoreAC, boolean meleeOnly, boolean rangeOnly) {
        if(!ignoreHitAndDamage) {
            if (!rangeOnly) {
                stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit());
                stats.setMeleeDmg(stats.getMeleeDmg() + td.getMeleeDmg());
                stats.setMeleePolyHit(stats.getMeleePolyHit() + td.getMeleePolyHit());
                stats.setMeleePolyDmg(stats.getMeleePolyDmg() + td.getMeleePolyDmg());
            } if (!meleeOnly) {
                stats.setRangeHit(stats.getRangeHit() + td.getRangeHit());
                stats.setRangeDmg(stats.getRangeDmg() + td.getRangeDmg());
            }
        }
        if(!ignoreAC) {
            stats.setMeleeAC(stats.getMeleeAC()+ td.getMeleeAC());
            stats.setRangeAC(stats.getRangeAC() + td.getRangeAC());
            stats.setRangeMissileAC(stats.getRangeMissileAC() + td.getRangeMissileAC());
        }
        if (slot == Slot.MAINHAND || slot == Slot.OFFHAND) {
            if (!rangeOnly) {
                stats.setRangeFireBenrow(stats.isRangeFireBenrow() || td.isMeleeFire());
                stats.setRangeColdBenrow(stats.isRangeColdBenrow() || td.isMeleeCold());
                stats.setRangeShockBenrow(stats.isRangeShockBenrow() || td.isMeleeShock());
                stats.setRangeSonicBenrow(stats.isRangeSonicBenrow() || td.isMeleeSonic());
                stats.setRangeEldritchBenrow(stats.isRangeEldritchBenrow() || td.isMeleeEldritch());
                stats.setRangePoisonBenrow(stats.isRangePoisonBenrow() || td.isMeleePoison());
                stats.setRangeDarkriftBenrow(stats.isRangeDarkriftBenrow() || td.isMeleeDarkrift());
                stats.setRangeSacredBenrow(stats.isRangeSacredBenrow() || td.isMeleeSacred());
                stats.setBFire(stats.getBFire() + td.getMFire());
                stats.setBCold(stats.getBCold() + td.getMCold());
                stats.setBShock(stats.getBShock() + td.getMShock());
                stats.setBSonic(stats.getBSonic() + td.getMSonic());
                stats.setBEldritch(stats.getBEldritch() + td.getMEldritch());
                stats.setBPoison(stats.getBPoison() + td.getMPoison());
                stats.setBDarkrift(stats.getBDarkrift() + td.getMDarkrift());
                stats.setBSacred(stats.getBSacred() + td.getMSacred());
                stats.setBForce(stats.getBForce() + td.getMForce());
                stats.setBAcid(stats.getBAcid() + td.getMAcid());
            }
        } else if (!(slot == Slot.RANGE_MAINHAND || slot == Slot.RANGE_OFFHAND)) {
            if (!meleeOnly) {
                stats.setRangeFireBenrow(stats.isRangeFireBenrow() || td.isRangeFire());
                stats.setRangeColdBenrow(stats.isRangeColdBenrow() || td.isRangeCold());
                stats.setRangeShockBenrow(stats.isRangeShockBenrow() || td.isRangeShock());
                stats.setRangeSonicBenrow(stats.isRangeSonicBenrow() || td.isRangeSonic());
                stats.setRangeEldritchBenrow(stats.isRangeEldritchBenrow() || td.isRangeEldritch());
                stats.setRangePoisonBenrow(stats.isRangePoisonBenrow() || td.isRangePoison());
                stats.setRangeDarkriftBenrow(stats.isRangeDarkriftBenrow() || td.isRangeDarkrift());
                stats.setRangeSacredBenrow(stats.isRangeSacredBenrow() || td.isRangeSacred());
                stats.setBFire(stats.getBFire() + td.getRFire());
                stats.setBCold(stats.getBCold() + td.getRCold());
                stats.setBShock(stats.getBShock() + td.getRShock());
                stats.setBSonic(stats.getBSonic() + td.getRSonic());
                stats.setBEldritch(stats.getBEldritch() + td.getREldritch());
                stats.setBPoison(stats.getBPoison() + td.getRPoison());
                stats.setBDarkrift(stats.getBDarkrift() + td.getRDarkrift());
                stats.setBSacred(stats.getBSacred() + td.getRSacred());
                stats.setBForce(stats.getBForce() + td.getRForce());
                stats.setBAcid(stats.getBAcid() + td.getRAcid());
            }
        }
        stats.setStr(stats.getStr() + td.getStr());
        stats.setDex(stats.getDex() + td.getDex());
        stats.setCon(stats.getCon() + td.getCon());
        stats.setIntel(stats.getIntel() + td.getIntel());
        stats.setWis(stats.getWis() + td.getWis());
        stats.setCha(stats.getCha() + td.getCha());
        stats.setHealth(stats.getHealth() + td.getHealth());
        stats.setRegen(stats.getRegen() + td.getRegen());
        if (!rangeOnly) {
            stats.setMeleeFire(stats.isMeleeFire() || td.isMeleeFire());
            stats.setMeleeCold(stats.isMeleeCold() || td.isMeleeCold());
            stats.setMeleeShock(stats.isMeleeShock() || td.isMeleeShock());
            stats.setMeleeSonic(stats.isMeleeSonic() || td.isMeleeSonic());
            stats.setMeleeEldritch(stats.isMeleeEldritch() || td.isMeleeEldritch());
            stats.setMeleePoison(stats.isMeleePoison() || td.isMeleePoison());
            stats.setMeleeDarkrift(stats.isMeleeDarkrift() || td.isMeleeDarkrift());
            stats.setMeleeSacred(stats.isMeleeSacred() || td.isMeleeSacred());
            stats.setMFire(stats.getMFire() + td.getMFire());
            stats.setMCold(stats.getMCold() + td.getMCold());
            stats.setMShock(stats.getMShock() + td.getMShock());
            stats.setMSonic(stats.getMSonic() + td.getMSonic());
            stats.setMEldritch(stats.getMEldritch() + td.getMEldritch());
            stats.setMPoison(stats.getMPoison() + td.getMPoison());
            stats.setMDarkrift(stats.getMDarkrift() + td.getMDarkrift());
            stats.setMSacred(stats.getMSacred() + td.getMSacred());
            stats.setMForce(stats.getMForce() + td.getMForce());
            stats.setMAcid(stats.getMAcid() + td.getMAcid());
        }
        if (!meleeOnly) {
            stats.setRangeFire(stats.isRangeFire() || td.isRangeFire());
            stats.setRangeCold(stats.isRangeCold() || td.isRangeCold());
            stats.setRangeShock(stats.isRangeShock() || td.isRangeShock());
            stats.setRangeSonic(stats.isRangeSonic() || td.isRangeSonic());
            stats.setRangeEldritch(stats.isRangeEldritch() || td.isRangeEldritch());
            stats.setRangePoison(stats.isRangePoison() || td.isRangePoison());
            stats.setRangeDarkrift(stats.isRangeDarkrift() || td.isRangeDarkrift());
            stats.setRangeSacred(stats.isRangeSacred() || td.isRangeSacred());
            stats.setRFire(stats.getRFire() + td.getRFire());
            stats.setRCold(stats.getRCold() + td.getRCold());
            stats.setRShock(stats.getRShock() + td.getRShock());
            stats.setRSonic(stats.getRSonic() + td.getRSonic());
            stats.setREldritch(stats.getREldritch() + td.getREldritch());
            stats.setRPoison(stats.getRPoison() + td.getRPoison());
            stats.setRDarkrift(stats.getRDarkrift() + td.getRDarkrift());
            stats.setRSacred(stats.getRSacred() + td.getRSacred());
            stats.setRForce(stats.getRForce() + td.getRForce());
            stats.setRAcid(stats.getRAcid() + td.getRAcid());
        }
        stats.setFort(stats.getFort() + td.getFort());
        stats.setReflex(stats.getReflex() + td.getReflex());
        stats.setWill(stats.getWill() + td.getWill());
        stats.setRetDmg(stats.getRetDmg() + td.getRetDmg());
        stats.setRetFire(stats.isRetFire() || td.isRetFire());
        stats.setRetCold(stats.isRetCold() || td.isRetCold());
        stats.setRetShock(stats.isRetShock() || td.isRetShock());
        stats.setRetSonic(stats.isRetSonic() || td.isRetSonic());
        stats.setRetEldritch(stats.isRetEldritch() || td.isRetEldritch());
        stats.setRetPoison(stats.isRetPoison() || td.isRetPoison());
        stats.setRetDarkrift(stats.isRetDarkrift() || td.isRetDarkrift());
        stats.setRetSacred(stats.isRetSacred() || td.isRetSacred());
        stats.setCannotBeSuprised(stats.isCannotBeSuprised() || td.isCannotBeSuprised());
        stats.setFreeMovement(stats.isFreeMovement() || td.isFreeMovement());
        stats.setPsychic(stats.isPsychic() || td.isPsychic());
        stats.setSpellDmg(stats.getSpellDmg() + td.getSpellDmg());
        stats.setSpellHeal(stats.getSpellHeal() + td.getSpellHeal());
        stats.setInitiative(stats.getInitiative()+ td.getInitiative());
        stats.setTreasureMin(stats.getTreasureMin() + td.getTreasureMin());
        stats.setTreasureMax(stats.getTreasureMax() + td.getTreasureMax());
        stats.setDrMelee(stats.getDrMelee() + td.getDrMelee());
        stats.setDrRange(stats.getDrRange() + td.getDrRange());
        stats.setDrSpell(stats.getDrSpell() + td.getDrSpell());
        stats.setDrFire(stats.getDrFire() + td.getDrFire());
        stats.setDrCold(stats.getDrCold() + td.getDrCold());
        stats.setDrShock(stats.getDrShock() + td.getDrShock());
        stats.setDrSonic(stats.getDrSonic() + td.getDrSonic());
        stats.setDrEldritch(stats.getDrEldritch() + td.getDrEldritch());
        stats.setDrPoison(stats.getDrPoison() + td.getDrPoison());
        stats.setDrDarkrift(stats.getDrDarkrift() + td.getDrDarkrift());
        stats.setDrSacred(stats.getDrSacred() + td.getDrSacred());
        stats.setDrForce(stats.getDrForce() + td.getDrForce());
        stats.setDrAcid(stats.getDrAcid() + td.getDrAcid());

        if(td.getSpecialText() != null && !td.getSpecialText().isEmpty()) {
             notes.add(CharacterNote.builder().alwaysInEffect(td.isAlwaysInEffect()).oncePerRound(td.isOncePerRound()).oncePerRoom(td.isOncePerRoom()).oncePerGame(td.isOncePerGame()).note(td.getSpecialText()).build());
        }
    }
    
    private String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1).toLowerCase();
    }
    
    private boolean isMeleeWeapon(CharacterItem token, TokenFullDetails td, CharacterClass characterClass) {
        if (characterClass == CharacterClass.MONK || characterClass == CharacterClass.RANGER)
            return (token.getSlot() == Slot.MAINHAND || token.getSlot() == Slot.OFFHAND) && (td.isOneHanded() || td.isTwoHanded()) && !td.isShield() && !(td.isMug() && !td.isOneHanded());
        return (token.getSlot() == Slot.MAINHAND || token.getSlot() == Slot.OFFHAND) && (td.isOneHanded() || td.isTwoHanded()) && !td.isShield() && !td.isMug();
    }
    
    private boolean isRangeWeapon(CharacterItem token, TokenFullDetails td) {
        return (token.getSlot() == Slot.RANGE_MAINHAND || token.getSlot() == Slot.RANGE_OFFHAND) && (td.isRangedWeapon() || td.isOneHanded() || td.isTwoHanded()) && !td.isShield() && !td.isMug();
    }
}
