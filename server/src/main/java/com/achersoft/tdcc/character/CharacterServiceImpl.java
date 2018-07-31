package com.achersoft.tdcc.character;

import com.achersoft.exception.InvalidDataException;
import com.achersoft.security.providers.UserPrincipalProvider;
import com.achersoft.tdcc.character.dao.CharacterDetails;
import com.achersoft.tdcc.character.dao.CharacterItem;
import com.achersoft.tdcc.character.dao.CharacterName;
import com.achersoft.tdcc.character.dao.CharacterNote;
import com.achersoft.tdcc.character.dao.CharacterStats;
import com.achersoft.tdcc.character.persistence.CharacterMapper;
import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.enums.ConditionalUse;
import com.achersoft.tdcc.enums.Rarity;
import com.achersoft.tdcc.enums.Slot;
import com.achersoft.tdcc.enums.SlotStatus;
import com.achersoft.tdcc.party.dao.SelectableCharacters;
import com.achersoft.tdcc.party.persistence.PartyMapper;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
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
        
        characterHtml.append("[url=http://tdcharactercreator.com/#/character/edit/").append(character.getId()).append("]").append(character.getName()).append("[/url]\n\n");
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
        characterHtml.append("[table][tr][td]Melee[/td][td]Range[/td][td]Spell[/td][td]Fire[/td][td]Cold[/td][td]Shock[/td][td]Sonic[/td][td]Eldritch[/td][td]Poison[/td][td]Darkrift[/td][td]Sacred[/td][/tr]");
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
        final StringBuilder head = new StringBuilder();
        characterHtml.append("[b]Head:[/b] ");
        character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.HEAD).forEach((item) ->{
            head.append("[color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]");
        });
        if(head.length() == 0)
            head.append("Empty");
        characterHtml.append(head.toString()).append("\n");
        final StringBuilder eyes = new StringBuilder();
        characterHtml.append("[b]Eyes:[/b] ");
        character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.EYES).forEach((item) ->{
            eyes.append("[color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]");
        });
        if(eyes.length() == 0)
            eyes.append("Empty");
        characterHtml.append(eyes.toString()).append("\n");
        final StringBuilder leftEar = new StringBuilder();
        characterHtml.append("[b]Left Ear:[/b] ");
        character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.EAR&&item.getIndex()==0).forEach((item) ->{
            leftEar.append("[color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]");
        });
        if(leftEar.length() == 0)
            leftEar.append("Empty");
        characterHtml.append(leftEar.toString()).append("\n");
        final StringBuilder rightEar = new StringBuilder();
        characterHtml.append("[b]Right Ear:[/b] ");
        character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.EAR&&item.getIndex()==1).forEach((item) ->{
            rightEar.append("[color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]");
        });
        if(rightEar.length() == 0)
            rightEar.append("Empty");
        characterHtml.append(rightEar.toString()).append("\n");
        final StringBuilder neck = new StringBuilder();
        characterHtml.append("[b]Neck:[/b] ");
        character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.NECK).forEach((item) ->{
            neck.append("[color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]");
        });
        if(neck.length() == 0)
            neck.append("Empty");
        characterHtml.append(neck.toString()).append("\n");
        character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.BEAD).forEach((item) ->{
            characterHtml.append("[b]Bead:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
        });
        final StringBuilder torso = new StringBuilder();
        characterHtml.append("[b]Torso:[/b] ");
        character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.TORSO).forEach((item) ->{
            torso.append("[color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]");
        });
        if(torso.length() == 0)
            torso.append("Empty");
        characterHtml.append(torso.toString()).append("\n");
        final StringBuilder wrists = new StringBuilder();
        characterHtml.append("[b]Wrist:[/b] ");
        character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.WRIST).forEach((item) ->{
            wrists.append("[color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]");
        });
        if(wrists.length() == 0)
            wrists.append("Empty");
        characterHtml.append(wrists.toString()).append("\n");
        final StringBuilder hands = new StringBuilder();
        characterHtml.append("[b]Hands:[/b] ");
        character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.HANDS).forEach((item) ->{
            hands.append("[color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]");
        });
        if(hands.length() == 0)
            hands.append("Empty");
        characterHtml.append(hands.toString()).append("\n");
        character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.BACK).forEach((item) ->{
            characterHtml.append("[b]Back:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
        });
        character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.FINGER).forEach((item) ->{
            characterHtml.append("[b]Ring:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
        });
        character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.WAIST).forEach((item) ->{
            characterHtml.append("[b]Waist:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
        });
        character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.SHIRT).forEach((item) ->{
            characterHtml.append("[b]Shirt:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
        });
        character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.LEGS).forEach((item) ->{
            characterHtml.append("[b]Legs:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
        });
        character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.SHINS).forEach((item) ->{
            characterHtml.append("[b]Shins:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
        });
        character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.FEET).forEach((item) ->{
            characterHtml.append("[b]Boots:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
        });
        character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.FIGURINE).forEach((item) ->{
            characterHtml.append("[b]Figurine:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
        });
        character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.CHARM).forEach((item) ->{
            characterHtml.append("[b]Charm:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
        });
        character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.IOUNSTONE).forEach((item) ->{
            characterHtml.append("[b]Ioun Stone:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
        });
        character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.SLOTLESS).forEach((item) ->{
            characterHtml.append("[b]Slotless:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
        });
        character.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.RUNESTONE).forEach((item) ->{
            characterHtml.append("[b]Runestone:[/b] [color=").append(item.getRarity().htmlColor).append("]").append(item.getName()).append("[/color]\n");
        });
     
        return characterHtml.toString();
    }
   
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CharacterDetails validateCharacterItems(String id) {
        CharacterDetails characterDetails = getCharacter(id);

        chackWeaponAvailability(characterDetails);
        checkSlotModItems(characterDetails);
        addSlotsForFullItems(characterDetails);
        checkSetItems(characterDetails, false);
        calculateStats(characterDetails);
        
        // Set Items
        mapper.deleteCharacterItems(characterDetails.getId());
        mapper.addCharacterItems(characterDetails.getItems());
        
        // Set Stats
        mapper.updateCharacterStats(characterDetails.getStats());
        
        // Save notes
        mapper.deleteCharacterNotes(characterDetails.getId());
        if(!characterDetails.getNotes().isEmpty())
            mapper.addCharacterNotes(characterDetails.getId(), characterDetails.getNotes());
        
        return characterDetails;
    }

    @Override
    public CharacterDetails getCharacterMaxLevel(String id) {
        CharacterDetails characterDetails = getCharacter(id);

        chackWeaponAvailability(characterDetails);
        checkSlotModItems(characterDetails);
        addSlotsForFullItems(characterDetails);
        checkSetItems(characterDetails, true);
        calculateStats(characterDetails);

        return characterDetails;
    }
    
    private void chackWeaponAvailability(CharacterDetails characterDetails) {
        // Check for two handed melee weapon
        characterDetails.getItems().stream().filter((item) -> item.getSlot()==Slot.MAINHAND).findAny().ifPresent((item) ->{
            if(item.getItemId()!=null && tokenAdminMapper.getTokenDetails(item.getItemId()).isTwoHanded())
                characterDetails.setItems(characterDetails.getItems().stream().filter((i) -> i.getSlot()!=Slot.OFFHAND).collect(Collectors.toList()));
            else if(characterDetails.getItems().stream().filter((i) -> i.getSlot()==Slot.OFFHAND).count() == 0)
                characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.OFFHAND).index(0).slotStatus(SlotStatus.OK).build());
        });
        
        // Check to make sure dual is valid
        if (characterDetails.getCharacterClass() != CharacterClass.RANGER && characterDetails.getCharacterClass() != CharacterClass.MONK) {
            characterDetails.getItems().stream().filter((item) -> item.getSlot()==Slot.OFFHAND).findAny().ifPresent((item) -> {
                TokenFullDetails td = tokenAdminMapper.getTokenDetails(item.getItemId());
                if(item.getItemId()!=null && td.isOneHanded() && !td.isShield()) {
                    characterDetails.setItems(characterDetails.getItems().stream().filter((i) -> i.getSlot()!=Slot.OFFHAND).collect(Collectors.toList()));
                    characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.OFFHAND).index(0).slotStatus(SlotStatus.OK).build());
                }
            });
        }
        
        // Check for two handed range weapon
        if(characterDetails.getCharacterClass() != CharacterClass.WIZARD && characterDetails.getCharacterClass() != CharacterClass.MONK && characterDetails.getCharacterClass() != CharacterClass.ROGUE) {
            characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.RANGE_MAINHAND).findAny().ifPresent((item) ->{
                TokenFullDetails tokenDetails = tokenAdminMapper.getTokenDetails(item.getItemId());
                if(tokenDetails.isTwoHanded() && tokenDetails.isThrown() || (characterDetails.getCharacterClass() == CharacterClass.RANGER && (tokenDetails.isThrown() || tokenDetails.isOneHanded()))) 
                    characterDetails.setItems(characterDetails.getItems().stream().filter((i) -> i.getSlot()!=Slot.RANGE_OFFHAND).collect(Collectors.toList()));
                else {
                    if(characterDetails.getItems().stream().filter((i) -> i.getSlot()==Slot.RANGE_OFFHAND).count() == 0)
                        characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.RANGE_OFFHAND).index(0).slotStatus(SlotStatus.OK).build());
                    else {
                        characterDetails.getItems().stream().filter((i) -> i.getItemId()!=null&&i.getSlot()==Slot.RANGE_OFFHAND).findAny().ifPresent((i) ->{
                            TokenFullDetails td = tokenAdminMapper.getTokenDetails(i.getItemId());
                            if (tokenDetails.isTwoHanded()) {
                                if(!(td.isRangedWeapon()&& td.isShield())) {
                                    i.setItemId(null);
                                    i.setRarity(null);
                                    i.setName(null);
                                    i.setSlotStatus(SlotStatus.OK);
                                    i.setStatusText(null);
                                    i.setText(null);
                                }
                            } else if (tokenDetails.isOneHanded()) {
                                if(td.isRangedWeapon() && td.isShield()) {
                                    i.setItemId(null);
                                    i.setRarity(null);
                                    i.setName(null);
                                    i.setSlotStatus(SlotStatus.OK);
                                    i.setStatusText(null);
                                    i.setText(null);
                                }
                            }
                        }); 
                    }
                }
            });
        }
        
        // Check for bracer weapons
        if(characterDetails.getCharacterClass() == CharacterClass.MONK) {
            if(characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null&&(item.getSlot()==Slot.MAINHAND||item.getSlot()==Slot.OFFHAND)&&tokenAdminMapper.getTokenDetails(item.getItemId()).isBracerWeapon()).findAny().isPresent())
                characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> !(item.getSlot()==Slot.WRIST)).collect(Collectors.toList()));
            else if(characterDetails.getItems().stream().filter((item) -> item.getSlot()==Slot.WRIST).count() == 0)
                characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.WRIST).index(0).slotStatus(SlotStatus.OK).build());
        }
    }
    
    private void checkSlotModItems(CharacterDetails characterDetails) {
        final Map<String, CharacterItem> itemsMap = new HashMap();
        characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null).forEach((item) -> {
            itemsMap.put(item.getItemId(), item);
        });

        // Check for Medalion of Heroism
        long fingerCount = 0;
        if(itemsMap.containsKey("d4674a1b2bea57e8b11676fed2bf81bd4c48ac78"))
            characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> item.getSlot()!=Slot.FINGER).collect(Collectors.toList()));
        else {
            fingerCount = characterDetails.getItems().stream().filter((item) -> item.getSlot()==Slot.FINGER).count();
            if(fingerCount == 0) {
                characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.FINGER).index(0).slotStatus(SlotStatus.OK).build());
                characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.FINGER).index(1).slotStatus(SlotStatus.OK).build());
                fingerCount = 2;
            }
        }
        
        // Check for Hand of Glory & Gloves of Glory
        if(itemsMap.containsKey("83f0fa64bcdfcdd374b83a6189d786471d8e4213") || itemsMap.containsKey("68ca031f95876ef5b3e9099894a7ffeb02a73142")) {
            if(fingerCount == 2)
                characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.FINGER).index(2).slotStatus(SlotStatus.OK).build());
        } else {
            if(fingerCount > 2)
                characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> !(item.getSlot()==Slot.FINGER && item.getIndex() > 1)).collect(Collectors.toList()));
        }
        
        // Check for Charm Braclets & Charm Necklace
        long charmCount = characterDetails.getItems().stream().filter((item) -> item.getSlot()==Slot.CHARM).count();
        if(itemsMap.containsKey("0f19e0a1be0f5494e71765590d86390d40f98177") || itemsMap.containsKey("572fb173c78ab1963747b00f22cc42a89c74b7b4")) {
            if(itemsMap.containsKey("0f19e0a1be0f5494e71765590d86390d40f98177")) {
                if(charmCount == 3) {
                    charmCount = 5;
                    characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.CHARM).index(3).slotStatus(SlotStatus.OK).build());
                    characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.CHARM).index(4).slotStatus(SlotStatus.OK).build());
                } else if(charmCount == 6) {
                    charmCount = 8;
                    characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.CHARM).index(6).slotStatus(SlotStatus.OK).build());
                    characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.CHARM).index(7).slotStatus(SlotStatus.OK).build());
                }
            } else {
                if(charmCount == 8) {
                    charmCount = 6;
                    characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> !(item.getSlot()==Slot.CHARM && item.getIndex() > 5)).collect(Collectors.toList()));
                }
            }
            if(itemsMap.containsKey("572fb173c78ab1963747b00f22cc42a89c74b7b4")) {
                if(charmCount == 3) {
                    characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.CHARM).index(3).slotStatus(SlotStatus.OK).build());
                    characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.CHARM).index(4).slotStatus(SlotStatus.OK).build());
                    characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.CHARM).index(5).slotStatus(SlotStatus.OK).build());
                } else if(charmCount == 5) {
                    characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.CHARM).index(5).slotStatus(SlotStatus.OK).build());
                    characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.CHARM).index(6).slotStatus(SlotStatus.OK).build());
                    characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.CHARM).index(7).slotStatus(SlotStatus.OK).build());
                }
            } else {
                if(charmCount == 8) 
                    characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> !(item.getSlot()==Slot.CHARM && item.getIndex() > 4)).collect(Collectors.toList()));
            }
        } else {
            if(charmCount > 3) 
                characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> !(item.getSlot()==Slot.CHARM && item.getIndex() > 2)).collect(Collectors.toList()));
        }
        
        // Check for Earcuff of Orbits
        long iounCount = characterDetails.getItems().stream().filter((item) -> item.getSlot()==Slot.IOUNSTONE).count();
        if(itemsMap.containsKey("a898af8cb818c2f1e56acd8ddb78f0de9d4901e0")) {
            if(iounCount == 5) {
                characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.IOUNSTONE).index(5).slotStatus(SlotStatus.OK).build());
                characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.IOUNSTONE).index(6).slotStatus(SlotStatus.OK).build());
            }
            if(iounCount == 6) {
                characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.IOUNSTONE).index(6).slotStatus(SlotStatus.OK).build());
                characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.IOUNSTONE).index(7).slotStatus(SlotStatus.OK).build());
            }
        } else {
            if(iounCount == 8)
                characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> !(item.getSlot()==Slot.IOUNSTONE && item.getIndex() > 5)).collect(Collectors.toList()));
            else if(iounCount == 7)
                characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> !(item.getSlot()==Slot.IOUNSTONE && item.getIndex() > 4)).collect(Collectors.toList()));
        }
        
        // Check for Eldritch Runestone
        iounCount = characterDetails.getItems().stream().filter((item) -> item.getSlot()==Slot.IOUNSTONE).count();
        //if(itemsMap.containsKey("db0c53293948567ee17fe0715828ba3e380e3d75")) {}
        if(iounCount == 8)
            characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> !(item.getSlot()==Slot.IOUNSTONE && item.getIndex() > 6)).collect(Collectors.toList()));
        else if(iounCount == 6)
            characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> !(item.getSlot()==Slot.IOUNSTONE && item.getIndex() > 4)).collect(Collectors.toList()));
        
        // Check for AoW
       /* long aowCount = characterDetails.getItems().stream().filter((item) -> item.getSlot()==Slot.AOW).count();
        if(itemsMap.containsKey("69174ff87b87325b034df0adc38d418859a11a09")) {
            if(aowCount == 0) {
                characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.AOW).index(0).slotStatus(SlotStatus.OK).build());
            }
        } else {
            if(aowCount > 0)
                characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> !(item.getSlot()==Slot.AOW)).collect(Collectors.toList()));
        }*/
        
        // Check for Charm of Brooching
        long backCount = characterDetails.getItems().stream().filter((item) -> item.getSlot()==Slot.BACK).count();
        if(characterDetails.getItems().stream().filter((item) -> (item.getItemId()!=null&&item.getItemId().equals("2ea75650daa8b7025cd7887c87ccd16f6a6ca369"))).count() > 0) {
            if(backCount == 1)
                characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.BACK).index(1).slotStatus(SlotStatus.OK).build());
        } else {
            if(backCount > 1)
                characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> !(item.getSlot()==Slot.BACK && item.getIndex() > 0)).collect(Collectors.toList()));
        }
        
        // Check Runestone Fitting Base
        long runeCount = characterDetails.getItems().stream().filter((item) -> item.getSlot()==Slot.RUNESTONE).count();
        int fittingBaseCount = (int)characterDetails.getItems().stream().filter((item) -> (item.getItemId()!=null&&item.getItemId().equals("b3079a85fd23a441af7de7dfd794d6ece2760313"))).count();
        switch (fittingBaseCount) {
            case 2:
                if(runeCount == 2)
                    characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.RUNESTONE).index(2).slotStatus(SlotStatus.OK).build());
                break;
            case 1:
                if(runeCount > 2)
                    characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> !(item.getSlot()==Slot.RUNESTONE && item.getIndex() > 1)).collect(Collectors.toList()));
                else if(runeCount == 1)
                    characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.RUNESTONE).index(1).slotStatus(SlotStatus.OK).build());
                break;
            default:
                if(runeCount > 1)
                    characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> !(item.getSlot()==Slot.RUNESTONE && item.getIndex() > 0)).collect(Collectors.toList()));
                break;
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
    
    private void checkSetItems(CharacterDetails characterDetails, boolean levelBoost) {
        final Map<String, CharacterItem> itemsMap = new HashMap();
        characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null).forEach((item) -> {
            itemsMap.put(item.getItemId(), item);
        });
        
        // Rod of Seven Parts
        long eldrichCount = characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("5b4d906cca80b7f2cd719133d4ff6822c435f5c3") || item.getItemId().equals("11dcc7b0267b2e622d58d5ba7219d8f7248b02b7") || item.getItemId().equals("260584166d4d9736a9de96bfe57e32fbe4a0e656"))).count();
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
        
        // First check if we need to boost character level
        // Charm of Heroism, Medallion of Heroism, Ring of Heroism, Eldrich Set, Kubu’s Coin of Coincidence, Smackdown’s Charm of Comraderie
        if(levelBoost || itemsMap.containsKey("d20aa5f4194d09336b0a5974215247cfaa480c9a") || itemsMap.containsKey("d4674a1b2bea57e8b11676fed2bf81bd4c48ac78") || itemsMap.containsKey("85bbc3d8307b702dde0525136fb82bf1636f55d8") ||
        (eldrichCount >= 3 || (eldrichCount >= 2 && (characterDetails.getCharacterClass() == CharacterClass.RANGER || characterDetails.getCharacterClass() == CharacterClass.DRUID))) ||
        itemsMap.containsKey("2f1cfd3d3dbdd218f5cd5bd3935851b7acba5a9c") || itemsMap.containsKey("f44d007c35b18b83e85a1ee183cda08180030012") ||
        mightCount >= 3 || charmingCount >= 3) {
            characterDetails.setStats(mapper.getStartingStats(characterDetails.getCharacterClass(), 5));
            characterDetails.getStats().setCharacterId(characterDetails.getId());
        } else {
            characterDetails.setStats(mapper.getStartingStats(characterDetails.getCharacterClass(), 4));
            characterDetails.getStats().setCharacterId(characterDetails.getId());
        }
        
        // Set character notes
        setCharacterNotes(characterDetails);
        
        // Cabal set
        // Gloves
        if(characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("3dcfd7948a3c9196556ef7e069a36174396297ad"))).count() > 0)
            characterDetails.getNotes().add(CharacterNote.builder().oncePerRoom(true).note("You may have a second target for and COMMON scroll used.").build());
        // Bracelets
        if(characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("f225241f60605ef641beeecd5003ba4129dbf46e"))).count() > 0) {
            characterDetails.getStats().setSpellDmg(characterDetails.getStats().getSpellDmg()+1);
            characterDetails.getStats().setSpellHeal(characterDetails.getStats().getSpellHeal()+1);
        }
        // Charm
        if(characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("1c688491fcb8a12199e9eca6d97e3da5ef4f3d65"))).count() > 0) {
            characterDetails.getStats().setSpellResist(10);
        }
        // all three two spells in one round once per room
        if(characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("3dcfd7948a3c9196556ef7e069a36174396297ad") || item.getItemId().equals("f225241f60605ef641beeecd5003ba4129dbf46e") || item.getItemId().equals("1c688491fcb8a12199e9eca6d97e3da5ef4f3d65"))).count() == 3)
            characterDetails.getNotes().add(CharacterNote.builder().oncePerRoom(true).note("You may cast two spells in one round.  Spell modifiers apply to both.").build());
        
        // Celestial Set
        // all 3 immune to melee and mental attacks from evil outsiders
        if(characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("09a0ad7cb78d898992c96ab00487bc8ffdc66f5a") || item.getItemId().equals("4b4e1b22e1ce20a529920fd48f8b891ec8e0b74a") || item.getItemId().equals("c34921c9623d7d3c1eee054e646fc3cdc7f08659"))).count() == 3)
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
        if(characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("2439ce9af26dfa74294083c13b73e3cc00405793") || item.getItemId().equals("770a1850602b0854e7be9256f55f1b0ce2ac50ea") || item.getItemId().equals("e6d0a8722540345c6a236a0aeeaf92d12c66e370"))).count() == 3)
            characterDetails.getStats().setRetDmg(characterDetails.getStats().getRetDmg() + 2);
       
        
        // Defender Set
        // all 3 free action movement and +1 AC
        if(characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("89abc3b184b2b30d1a967aee7a32ccbf107532ed") || item.getItemId().equals("b29ffe03ba83c567fb95ddedeb8cef8c515c003f") || item.getItemId().equals("debb8b6d4a22654fadfa4983b84cac3bd69db814"))).count() == 3) {
            characterDetails.getStats().setMeleeAC(characterDetails.getStats().getMeleeAC() + 1);
            characterDetails.getStats().setRangeAC(characterDetails.getStats().getRangeAC() + 1);
            characterDetails.getStats().setFreeMovement(true);
        }
        
        // Dragonhide Set
        // 3 or more auto pass saves vs dragon breath, +3 saves
        if(characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("2a05d3436a948f9bb29e1ebfd9f3ac55115445a2") || item.getItemId().equals("33205cbfbe448541fae56600346027284edc52e1") || item.getItemId().equals("643e19c4273dcb6f2e8b2ce56241beb1efde902e") ||
                                                                                                         item.getItemId().equals("67935e7e953d26c83d095fd188a226d16fc16e1f") || item.getItemId().equals("81fbf796065e19a88ea36b4b9413946839f10eb6") || item.getItemId().equals("90af6fea77a9670b41a34812b45be7ace7e11e37"))).count() == 3) {
            characterDetails.getStats().setFort(characterDetails.getStats().getFort() + 3);
            characterDetails.getStats().setReflex(characterDetails.getStats().getReflex() + 3);
            characterDetails.getStats().setWill(characterDetails.getStats().getWill() + 3);
            characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("You automatically make saving throws vs. dragon breath weapons").build());
        }
        
        // Dragonscale Set
        // all 3 - 7 fire dr
        if(characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("3f9d6414ad5259959d23f8b509b8881d47ca1d11") || item.getItemId().equals("72341d583d26a57a7f3d3f460f2e976d315688b0") || item.getItemId().equals("2effa74fb480d06733e2aeff29394badf15e58c8"))).count() == 3) {
            characterDetails.getStats().setDrFire(characterDetails.getStats().getDrFire() + 7);
        }
        
        // Eldritch Set
        // 2 piece ignore spell resistence, healing spells +10, melee igonore DR
        if(eldrichCount >= 2) {
            characterDetails.getStats().setSpellHeal(characterDetails.getStats().getSpellHeal() + 10);
            characterDetails.getStats().setSpellResist(100);
            characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("Your spells and melee attack ignore spell resistance and damage reduction.").build());
        }
        
        // Footman Set
        // all 3 +2 AC and cold DR 1
        if(characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("a0963d04a21f5987224588d320e66dcb1bec5e07") || item.getItemId().equals("c431e2d1507703f78db6d76a5c65c697e9a3f795") || item.getItemId().equals("dff11b4606afca716d94fafa81382a9078ff43d7"))).count() == 3) {
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
        if(characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("886c908995eae55ebb33ff143b07b969fcab09b3") || item.getItemId().equals("d55ed0f77c4a340d2f7fe558e49700861a23d221") || item.getItemId().equals("f67c01d654eacdd3a021dc0d3fe3c319043d8caf"))).count() == 3) {
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
        if(characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("d9ebb109843e8fa1aa04d90dbf7405e572042fa1") || item.getItemId().equals("e5b7a23fc4208752b6e29c1c0c040279425bc898") || item.getItemId().equals("4fb8d5b22892902f33a73630d5dddb8cff8e244b"))).count() == 3) {
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
        if(characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("0d71d03613ff843d7a933d65f848375000e76641") || item.getItemId().equals("5118b837bd908c60e7dac00767c4836a31a85878") || item.getItemId().equals("db2de322945529f5dbf3c97030ffa6ff52df96c4"))).count() == 3) {
            characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("You have the feather fall effect and immunity to non-magical physical missiles.").build());
        }
    }
    
    private void setCharacterNotes(CharacterDetails characterDetails) {
        // Reset Notes
        characterDetails.setNotes(new ArrayList());
        
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
    
    private void calculateStats(CharacterDetails characterDetails) {
        final CharacterStats stats = characterDetails.getStats();
        final List<CharacterItem> conditionalTokens = new ArrayList();
        final Set<ConditionalUse> metCondition = new HashSet();
        AtomicInteger mainWeaponHit = new AtomicInteger(0);
        AtomicInteger offWeaponHit = new AtomicInteger(0);
        AtomicInteger rangeMainWeaponHit = new AtomicInteger(0);
        AtomicInteger rangeOffWeaponHit = new AtomicInteger(0);
        AtomicInteger sixLevelReward = new AtomicInteger(0);
        AtomicInteger scrollPro = new AtomicInteger(0);
        AtomicInteger mightyRanged = new AtomicInteger(0);
        AtomicInteger additionalTreasureTokens = new AtomicInteger(0);
        StringBuilder sheildId = new StringBuilder();

        characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null).forEach((item) -> {
            TokenFullDetails td = tokenAdminMapper.getTokenDetails(item.getItemId());
            
            if(td.getId().equals("c307398cc4eb769adccb78978693d79fa266b2f5") || td.getId().equals("c3c6de9b8951c4961976f147d64a0411cc6f730b")) {
                if(scrollPro.incrementAndGet() > 1)
                    metCondition.add(ConditionalUse.NOT_WITH_PRO_SCROLL);
            }
            if(td.getId().equals("5b4d906cca80b7f2cd719133d4ff6822c435f5c3"))
                metCondition.add(ConditionalUse.NOT_WITH_ROSP);
            if(td.getId().equals("0448ddb1214a3f5c03af24653383d507fa0ea85c"))
                metCondition.add(ConditionalUse.NOT_WITH_COA);
            if(td.getId().equals("63cc231ebcbb18e23c9979ba26b38f3ff9f21d92"))
                metCondition.add(ConditionalUse.NOT_WITH_COS_COA);
            if(td.getTreasureMin()>0 && td.getRarity() != Rarity.ARTIFACT)
                if(additionalTreasureTokens.addAndGet(1) > 1)
                    metCondition.add(ConditionalUse.NO_OTHER_TREASURE);

            if(td.isOneHanded() && !td.isRangedWeapon())
                metCondition.add(ConditionalUse.WEAPON_1H);
            if(td.isRangedWeapon()) {
                metCondition.add(ConditionalUse.WEAPON_RANGED);
                if(td.getName().toLowerCase().contains("sling"))
                    metCondition.add(ConditionalUse.SLING);
                if(td.isTwoHanded())
                    metCondition.add(ConditionalUse.WEAPON_RANGED_2H);
            }
            if(td.isTwoHanded() && !td.isRangedWeapon())
                metCondition.add(ConditionalUse.WEAPON_2H);

            if(td.getConditionalUse() != ConditionalUse.NONE){
                conditionalTokens.add(item);
            } else if (item.getSlot() == Slot.OFFHAND && td.isShield()) {
                item.setSlotStatus(SlotStatus.OK);
                item.setStatusText(null);
                stats.setMeleeAC(stats.getMeleeAC() + td.getMeleeAC());
                if(!sheildId.toString().equals(td.getId())) {
                    sheildId.append(td.getId()); 
                    updateStats(stats, td, characterDetails.getNotes());
                }
            } else if (item.getSlot() == Slot.RANGE_OFFHAND && td.isShield()) {
                item.setSlotStatus(SlotStatus.OK);
                item.setStatusText(null);
                stats.setRangeAC(stats.getRangeAC() + td.getRangeAC());
                if(!sheildId.toString().equals(td.getId())) {
                    sheildId.append(td.getId());
                    updateStats(stats, td, characterDetails.getNotes());
                }
            } else {
                item.setSlotStatus(SlotStatus.OK);
                item.setStatusText(null);
                stats.setMeleeAC(stats.getMeleeAC() + td.getMeleeAC());
                stats.setRangeAC(stats.getRangeAC() + td.getRangeAC());
                updateStats(stats, td, characterDetails.getNotes());
            }

            if(item.getSlot() == Slot.MAINHAND) {
                mainWeaponHit.set(td.getMeleeHit());
                if(td.getMeleeHit() > offWeaponHit.get())
                    stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit() - offWeaponHit.get());
            } else if(item.getSlot() == Slot.OFFHAND) {
                offWeaponHit.set(td.getMeleeHit());
                if(td.getId().equals("8a8856030c0e1ef2fd16a4b8ab38d312d218df6f")) {
                    offWeaponHit.set(0);
                    stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit());
                } else if(td.getMeleeHit() > mainWeaponHit.get())
                    stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit() - mainWeaponHit.get());
            } else if(item.getSlot() != Slot.RANGE_MAINHAND)
                stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit());
            if(item.getSlot() == Slot.RANGE_MAINHAND) {
                rangeMainWeaponHit.set(td.getRangeHit());
                if(td.getRangeHit() > rangeOffWeaponHit.get())
                    stats.setRangeHit(stats.getRangeHit() + td.getRangeHit() - rangeOffWeaponHit.get());
            }
            if(item.getSlot() == Slot.RANGE_OFFHAND) {
                rangeOffWeaponHit.set(td.getRangeHit());
                if(td.getRangeHit() > rangeMainWeaponHit.get())
                    stats.setRangeHit(stats.getRangeHit() + td.getRangeHit() - rangeMainWeaponHit.get());
            }
            if(item.getSlot() == Slot.RANGE_MAINHAND && (td.isThrown() || td.getName().toLowerCase().contains("mighty")))
                mightyRanged.set(1);
            if(item.getSlot() != Slot.MAINHAND && item.getSlot() != Slot.OFFHAND && item.getSlot() != Slot.RANGE_OFFHAND && item.getSlot() != Slot.RANGE_MAINHAND)    
                stats.setRangeHit(stats.getRangeHit() + td.getRangeHit());
            if(td.getId().equals("c7212941453b5970f3bf40daf50271dc5dcb80b4") || td.getId().equals("8469e44d1b5890ad25506a2abbd2986987efb20a") || td.getId().equals("9f35f1840298549d070a818019bd6b7da131a6ec") || td.getId().equals("ab5d855dbeedfd400cfd417dbc1d25d01272203e")) {
                stats.setPsychicLevel(stats.getPsychicLevel() + 1);
            }
            if((td.getId().equals("028d1ddec034be61aa3b3abaed02d76db2139084") || td.getId().equals("3bed20c850924c4b9009f50ed5b4de2998d311b2")) && sixLevelReward.get() == 0) {
                sixLevelReward.set(1);
                stats.setTreasureMin(stats.getTreasureMin() + 1);
                stats.setTreasureMax(stats.getTreasureMax() + 1);
            }
        });   
        
        // Check Conditionals 
        checkConditionals(conditionalTokens, metCondition, stats, characterDetails);
        
        stats.setStrBonus((stats.getStr()-10 > 0)?(stats.getStr()-10)/2:(stats.getStr()-11)/2);
        stats.setDexBonus((stats.getDex()-10 > 0)?(stats.getDex()-10)/2:(stats.getDex()-11)/2);
        stats.setConBonus((stats.getCon()-10 > 0)?(stats.getCon()-10)/2:(stats.getCon()-11)/2);
        stats.setIntelBonus((stats.getIntel()-10 > 0)?(stats.getIntel()-10)/2:(stats.getIntel()-11)/2);
        stats.setWisBonus((stats.getWis()-10 > 0)?(stats.getWis()-10)/2:(stats.getWis()-11)/2);
        stats.setChaBonus((stats.getCha()-10 > 0)?(stats.getCha()-10)/2:(stats.getCha()-11)/2);

        stats.setHealth(stats.getHealth() + stats.getLevel() * (int)((stats.getConBonus() < 0)?stats.getConBonus()+(stats.getBaseCon()-10)/2:stats.getConBonus()-(stats.getBaseCon()-10)/2));
        stats.setMeleeHit(stats.getMeleeHit() + stats.getStrBonus());
        stats.setMeleeDmg(stats.getMeleeDmg() + stats.getStrBonus());
        stats.setRangeHit(stats.getRangeHit() + stats.getDexBonus());
        stats.setMeleeAC(stats.getMeleeAC() + stats.getDexBonus());
        stats.setRangeAC(stats.getRangeAC() + stats.getDexBonus());
        stats.setFort(stats.getFort() + stats.getConBonus());
        stats.setReflex(stats.getReflex() + stats.getDexBonus());
        stats.setWill(stats.getWill() + stats.getWisBonus());
        
        if(mightyRanged.get() == 1)
            stats.setRangeDmg(stats.getRangeDmg() + stats.getStrBonus());
        
        if(null != characterDetails.getCharacterClass()) switch (characterDetails.getCharacterClass()) {
            case BARBARIAN:
                if(metCondition.contains(ConditionalUse.WEAPON_2H))
                    stats.setMeleeDmg(stats.getMeleeDmg() + 4);
            default:
                break;
        }
        
        long figurineCount = characterDetails.getItems().stream().filter((item) -> item.getSlot()==Slot.FIGURINE).count();
        if(stats.getCha() >= 16) {
            if(figurineCount < 2)
                characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.FIGURINE).index(1).slotStatus(SlotStatus.OK).build());
        } else if(figurineCount > 1)       
            characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> !(item.getSlot()==Slot.FIGURINE && item.getIndex() > 0)).collect(Collectors.toList()));
        
    }
    
    private void checkConditionals(List<CharacterItem> conditionalTokens, Set<ConditionalUse> metCondition, CharacterStats stats, CharacterDetails characterDetails) {
        List<CharacterNote> notes = characterDetails.getNotes();
        conditionalTokens.stream().forEach((token) -> {
            TokenFullDetails td = tokenAdminMapper.getTokenDetails(token.getItemId());
            if(null != td.getConditionalUse()) switch (td.getConditionalUse()) {
                case NOT_WITH_ROSP:
                    if(metCondition.contains(ConditionalUse.NOT_WITH_ROSP)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText(token.getName() + " cannot be used with the Rod of Seven Parts.");
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        stats.setMeleeAC(stats.getMeleeAC() + td.getMeleeAC());
                        stats.setRangeAC(stats.getRangeAC() + td.getRangeAC());
                        updateStats(stats, td, notes);
                    }   break;
                default:
                    break;
            }
        });
        conditionalTokens.stream().forEach((token) -> {
            TokenFullDetails td = tokenAdminMapper.getTokenDetails(token.getItemId());
            if(null != td.getConditionalUse()) switch (td.getConditionalUse()) {
                case WEAPON_2H:
                    if(!metCondition.contains(ConditionalUse.WEAPON_2H)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a two handed weapon to use.");
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        stats.setMeleeAC(stats.getMeleeAC() + td.getMeleeAC());
                        stats.setRangeAC(stats.getRangeAC() + td.getRangeAC());
                        updateStats(stats, td, notes);
                    }   break;
                case WEAPON_1H:
                    if(!metCondition.contains(ConditionalUse.WEAPON_1H)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a one handed weapon to use.");
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        stats.setMeleeAC(stats.getMeleeAC() + td.getMeleeAC());
                        stats.setRangeAC(stats.getRangeAC() + td.getRangeAC());
                        updateStats(stats, td, notes);
                    }   break;
                case WEAPON_RANGED:
                    if(!metCondition.contains(ConditionalUse.WEAPON_RANGED)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a range weapon to use.");
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        stats.setMeleeAC(stats.getMeleeAC() + td.getMeleeAC());
                        stats.setRangeAC(stats.getRangeAC() + td.getRangeAC());
                        updateStats(stats, td, notes);
                    }   break;
                case WEAPON_RANGED_2H:
                    if(!metCondition.contains(ConditionalUse.WEAPON_RANGED_2H)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a two handed range weapon to use.");
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        stats.setMeleeAC(stats.getMeleeAC() + td.getMeleeAC());
                        stats.setRangeAC(stats.getRangeAC() + td.getRangeAC());
                        updateStats(stats, td, notes);
                    }   break;    
                case DEXTERITY_18:
                    if(stats.getDex() < 18) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a dexterity of 18 or higher to use.");
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        stats.setMeleeAC(stats.getMeleeAC() + td.getMeleeAC());
                        stats.setRangeAC(stats.getRangeAC() + td.getRangeAC());
                        updateStats(stats, td, notes);
                    }   break;
                case DEXTERITY_20:
                    if(stats.getDex() < 20) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a dexterity of 20 or higher to use.");
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        stats.setMeleeAC(stats.getMeleeAC() + td.getMeleeAC());
                        stats.setRangeAC(stats.getRangeAC() + td.getRangeAC());
                        updateStats(stats, td, notes);
                    }   break;
                case INTELLECT_20:
                    if(stats.getIntel()< 20) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a intellect of 20 or higher to use.");
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        stats.setMeleeAC(stats.getMeleeAC() + td.getMeleeAC());
                        stats.setRangeAC(stats.getRangeAC() + td.getRangeAC());
                        updateStats(stats, td, notes);
                    }   break;
                case WISDOM_20:
                    if(stats.getWis()< 20) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a wisdom of 20 or higher to use.");
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        stats.setMeleeAC(stats.getMeleeAC() + td.getMeleeAC());
                        stats.setRangeAC(stats.getRangeAC() + td.getRangeAC());
                        updateStats(stats, td, notes);
                    }   break;
                case STRENGTH_24:
                    if(stats.getStr()< 24) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a strength of 24 or higher to use.");
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        stats.setMeleeAC(stats.getMeleeAC() + td.getMeleeAC());
                        stats.setRangeAC(stats.getRangeAC() + td.getRangeAC());
                        updateStats(stats, td, notes);
                    }   break;
                case NOT_WITH_COS_COA:
                    if(metCondition.contains(ConditionalUse.NOT_WITH_COS_COA)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText(token.getName() + " cannot be used with the Charm of Awakened Synergy.");
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(stats, td, notes);
                    }   break;
                case NOT_WITH_COA:
                    if(metCondition.contains(ConditionalUse.NOT_WITH_COA)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText(token.getName() + " cannot be used with the Charm of Avarice.");
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        stats.setMeleeAC(stats.getMeleeAC() + td.getMeleeAC());
                        stats.setRangeAC(stats.getRangeAC() + td.getRangeAC());
                        updateStats(stats, td, notes);
                    }   break;
                case NOT_WITH_PRO_SCROLL:
                    if(metCondition.contains(ConditionalUse.NOT_WITH_PRO_SCROLL)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText(token.getName() + " cannot be used with other scroll protection items.");
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(stats, td, notes);
                    }   break;    
                case NO_OTHER_TREASURE:
                    if(metCondition.contains(ConditionalUse.NO_OTHER_TREASURE)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText(token.getName() + " cannot be used with any other treasure enchancing token.");
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        stats.setMeleeAC(stats.getMeleeAC() + td.getMeleeAC());
                        stats.setRangeAC(stats.getRangeAC() + td.getRangeAC());
                        updateStats(stats, td, notes);
                    }   break;
                case SLING:
                    if(!metCondition.contains(ConditionalUse.SLING)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText(token.getName() + " requires a Sling to be equipped.");
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        stats.setMeleeAC(stats.getMeleeAC() + td.getMeleeAC());
                        stats.setRangeAC(stats.getRangeAC() + td.getRangeAC());
                        updateStats(stats, td, notes);
                    }   break;  
                case CROSSBOW:
                    if(characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.RANGE_MAINHAND&&item.getName().toLowerCase().contains("crossbow")).count()>0) {
                        stats.setRangeHit(stats.getRangeHit() + td.getRangeHit());
                        stats.setRangeDmg(stats.getRangeDmg() + td.getRangeDmg());
                    }   break;
                case THRALL_WEAPON:
                    long thrallMelee = characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null&&(item.getSlot()==Slot.MAINHAND||(item.getSlot()==Slot.OFFHAND&&!item.getName().toLowerCase().contains("shield")))&&item.getName().toLowerCase().contains("thrall")).count();
                    long thrallRanged = characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.RANGE_MAINHAND&&item.getName().toLowerCase().contains("thrall")).count();
                    if(thrallMelee > 0) {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit());
                        stats.setMeleeDmg(stats.getMeleeDmg() + td.getMeleeDmg());
                    }
                    if(thrallRanged > 0) {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        stats.setRangeHit(stats.getRangeHit() + td.getRangeHit());
                        stats.setRangeDmg(stats.getRangeDmg() + td.getRangeDmg());
                    }
                    if(thrallMelee == 0 && thrallRanged == 0) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText(token.getName() + " requires a Thrall weapon to be equipped.");
                    }   break;
                case IRON_WEAPON:
                    long ironMelee = characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null&&(item.getSlot()==Slot.MAINHAND||(item.getSlot()==Slot.OFFHAND&&!item.getName().toLowerCase().contains("shield")))&&item.getName().toLowerCase().contains("iron")).count();
                    long ironRanged = characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.RANGE_MAINHAND&&item.getName().toLowerCase().contains("iron")).count();
                    if(ironMelee > 0) {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit());
                        stats.setMeleeDmg(stats.getMeleeDmg() + td.getMeleeDmg());
                    }
                    if(ironRanged > 0) {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        stats.setRangeHit(stats.getRangeHit() + td.getRangeHit());
                        stats.setRangeDmg(stats.getRangeDmg() + td.getRangeDmg());
                    }
                    if(ironMelee == 0 && ironRanged == 0) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText(token.getName() + " requires a Iron weapon to be equipped.");
                    }   break;    
                default:
                    break;
            }
        });
    }
                
    private void updateStats(CharacterStats stats, TokenFullDetails td, List<CharacterNote> notes) {
        stats.setStr(stats.getStr() + td.getStr());
        stats.setDex(stats.getDex() + td.getDex());
        stats.setCon(stats.getCon() + td.getCon());
        stats.setIntel(stats.getIntel() + td.getIntel());
        stats.setWis(stats.getWis() + td.getWis());
        stats.setCha(stats.getCha() + td.getCha());
        stats.setHealth(stats.getHealth() + td.getHealth());
        stats.setRegen(stats.getRegen() + td.getRegen());
        stats.setMeleeDmg(stats.getMeleeDmg() + td.getMeleeDmg());
        stats.setMeleeFire(stats.isMeleeFire() || td.isMeleeFire());
        stats.setMeleeCold(stats.isMeleeCold() || td.isMeleeCold());
        stats.setMeleeShock(stats.isMeleeShock() || td.isMeleeShock());
        stats.setMeleeSonic(stats.isMeleeSonic() || td.isMeleeSonic());
        stats.setMeleeEldritch(stats.isMeleeEldritch() || td.isMeleeEldritch());
        stats.setMeleePoison(stats.isMeleePoison() || td.isMeleePoison());
        stats.setMeleeDarkrift(stats.isMeleeDarkrift() || td.isMeleeDarkrift());
        stats.setMeleeSacred(stats.isMeleeSacred() || td.isMeleeSacred());
        stats.setRangeMissileAC(stats.getRangeMissileAC() + td.getRangeMissileAC());
        stats.setRangeDmg(stats.getRangeDmg() + td.getRangeDmg());
        stats.setRangeFire(stats.isRangeFire() || td.isRangeFire());
        stats.setRangeCold(stats.isRangeCold() || td.isRangeCold());
        stats.setRangeShock(stats.isRangeShock() || td.isRangeShock());
        stats.setRangeSonic(stats.isRangeSonic() || td.isRangeSonic());
        stats.setRangeEldritch(stats.isRangeEldritch() || td.isRangeEldritch());
        stats.setRangePoison(stats.isRangePoison() || td.isRangePoison());
        stats.setRangeDarkrift(stats.isRangeDarkrift() || td.isRangeDarkrift());
        stats.setRangeSacred(stats.isRangeSacred() || td.isRangeSacred());
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

        if(td.getSpecialText() != null && !td.getSpecialText().isEmpty()) {
             notes.add(CharacterNote.builder().alwaysInEffect(td.isAlwaysInEffect()).oncePerRound(td.isOncePerRound()).oncePerRoom(td.isOncePerRoom()).oncePerGame(td.isOncePerGame()).note(td.getSpecialText()).build());
        }
    }
    
    private String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1).toLowerCase();
    }
}
