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
import com.achersoft.tdcc.token.admin.dao.TokenFullDetails;
import com.achersoft.tdcc.token.admin.persistence.TokenAdminMapper;
import com.achersoft.tdcc.token.persistence.TokenMapper;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
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

@Transactional(rollbackFor = Exception.class)
public class CharacterServiceImpl implements CharacterService {
    
    private @Inject CharacterMapper mapper;
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
    public CharacterDetails setTokenSlot(String id, String soltId, String tokenId) {
        tokenMapper.setTokenSlot(soltId, tokenId);
        return validateCharacterItems(id);
    }
    
    @Override
    public CharacterDetails unequipTokenSlot(String id, String soltId) {
        tokenMapper.unequipTokenSlot(soltId);
        return validateCharacterItems(id);
    }
    
    @Override
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
                Resource pdfFile = null;
                if(null != character.getCharacterClass())
                    switch (character.getCharacterClass()) {
                    case BARBARIAN:
                        pdfFile = new ClassPathResource("barbarian.pdf");
                        break;
                    case BARD:
                        pdfFile = new ClassPathResource("bard.pdf");
                        break;
                    case CLERIC:
                        pdfFile = new ClassPathResource("clearic.pdf");
                        break;
                    case DRUID:
                        pdfFile = new ClassPathResource("druid.pdf");
                        break;
                    case DWARF_FIGHTER:
                        pdfFile = new ClassPathResource("dwarfFighter.pdf");
                        break;
                    case ELF_WIZARD:
                        pdfFile = new ClassPathResource("elfWizard.pdf");
                        break;
                    case FIGHTER:
                        pdfFile = new ClassPathResource("fighter.pdf");
                        break;
                    case MONK:
                        pdfFile = new ClassPathResource("monk.pdf");
                        break;
                    case PALADIN:
                        pdfFile = new ClassPathResource("paladin.pdf");
                        break;
                    case RANGER:
                        pdfFile = new ClassPathResource("ranger.pdf");
                        break;
                    case ROGUE:
                        pdfFile = new ClassPathResource("rogue.pdf");
                        break;
                    case WIZARD:
                        pdfFile = new ClassPathResource("wizard.pdf");
                        break;
                    default:
                        break;
                }
                PdfReader reader = new PdfReader(pdfFile.getURL());
                PdfStamper stamper = new PdfStamper(reader, out);
                AcroFields fields = stamper.getAcroFields();
                //System.err.println(fields.getFields().keySet());
                
                fields.setField("characterName", character.getName());
                fields.setField("characterLevel", Integer.toString(character.getStats().getLevel()));
                
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
                    if(counter.incrementAndGet() > 46)
                        itemsListCnt.append(item.getSlot().text()).append(": ").append(item.getName()).append("\n");
                    else
                        itemsList.append(item.getSlot().text()).append(": ").append(item.getName()).append("\n");
                });
                fields.setField("itemList", itemsList.toString());
                fields.setField("itemListCnt", itemsListCnt.toString());
                
                fields.setField("pcMeleeHit", Integer.toString(character.getStats().getMeleeHit()));
                fields.setField("pcMeleeDmg", Integer.toString(character.getStats().getMeleeDmg()));
                fields.setField("pcMeleeAC", Integer.toString(character.getStats().getMeleeAC()));
                fields.setField("pcMeleeFire", (character.getStats().isMeleeFire())?"Yes":"No");
                fields.setField("pcMeleeCold", (character.getStats().isMeleeCold())?"Yes":"No");
                fields.setField("pcMeleeShock", (character.getStats().isMeleeShock())?"Yes":"No");
                fields.setField("pcMeleeSonic", (character.getStats().isMeleeSonic())?"Yes":"No");
                fields.setField("pcMeleeEldritch", (character.getStats().isMeleeEldritch())?"Yes":"No");
                fields.setField("pcMeleePoison", (character.getStats().isMeleePoison())?"Yes":"No");
                fields.setField("pcMeleeDarkrift", (character.getStats().isMeleeDarkrift())?"Yes":"No");
                fields.setField("pcMeleeSacred", (character.getStats().isMeleeSacred())?"Yes":"No");
                fields.setField("pcRangeHit", Integer.toString(character.getStats().getRangeHit()));
                fields.setField("pcRangeDmg", Integer.toString(character.getStats().getRangeDmg()));
                fields.setField("pcRangeAC", Integer.toString(character.getStats().getRangeAC()));
                fields.setField("pcRangeMissileAC", Integer.toString(character.getStats().getRangeMissileAC()));
                fields.setField("pcRangeFire", (character.getStats().isRangeFire())?"Yes":"No");
                fields.setField("pcRangeCold", (character.getStats().isRangeCold())?"Yes":"No");
                fields.setField("pcRangeShock", (character.getStats().isRangeShock())?"Yes":"No");
                fields.setField("pcRangeSonic", (character.getStats().isRangeSonic())?"Yes":"No");
                fields.setField("pcRangeEldritch", (character.getStats().isRangeEldritch())?"Yes":"No");
                fields.setField("pcRangePoison", (character.getStats().isRangePoison())?"Yes":"No");
                fields.setField("pcRangeDarkrift", (character.getStats().isRangeDarkrift())?"Yes":"No");
                fields.setField("pcRangeSacred", (character.getStats().isRangeSacred())?"Yes":"No");
                fields.setField("pcFort", Integer.toString(character.getStats().getFort()));
                fields.setField("pcReflex", Integer.toString(character.getStats().getReflex()));
                fields.setField("pcWill", Integer.toString(character.getStats().getWill()));
                fields.setField("pcRetDmg", Integer.toString(character.getStats().getRetDmg()));
                fields.setField("pcRetFire", (character.getStats().isRetFire())?"Yes":"No");
                fields.setField("pcRetCold", (character.getStats().isRetCold())?"Yes":"No");
                fields.setField("pcRetShock", (character.getStats().isRetShock())?"Yes":"No");
                fields.setField("pcRetSonic", (character.getStats().isRetSonic())?"Yes":"No");
                fields.setField("pcRetEldritch", (character.getStats().isRetEldritch())?"Yes":"No");
                fields.setField("pcRetPoison", (character.getStats().isRetPoison())?"Yes":"No");
                fields.setField("pcRetDarkrift", (character.getStats().isRetDarkrift())?"Yes":"No");
                fields.setField("pcRetSacred", (character.getStats().isRetSacred())?"Yes":"No");
                fields.setField("pcCBS", (character.getStats().isCannotBeSuprised())?"Yes":"No");
                fields.setField("pcFM", (character.getStats().isFreeMovement())?"Yes":"No");
                fields.setField("pcP", (character.getStats().isPsychic())?"Yes":"No");
                fields.setField("pcTreasureMin", Integer.toString(character.getStats().getTreasureMin()));
                fields.setField("pcTreasureMax", Integer.toString(character.getStats().getTreasureMax()));
                
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
    public CharacterDetails validateCharacterItems(String id) {
        CharacterDetails characterDetails = getCharacter(id);

        chackWeaponAvailability(characterDetails);
        checkSlotModItems(characterDetails);
        addSlotsForFullItems(characterDetails);
        checkSetItems(characterDetails);
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
    
    private void chackWeaponAvailability(CharacterDetails characterDetails) {
        // Check for two handed melee weapon
        characterDetails.getItems().stream().filter((item) -> item.getSlot()==Slot.MAINHAND).findAny().ifPresent((item) ->{
            if(item.getItemId()!=null && tokenAdminMapper.getTokenDetails(item.getItemId()).isTwoHanded())
                characterDetails.setItems(characterDetails.getItems().stream().filter((i) -> i.getSlot()!=Slot.OFFHAND).collect(Collectors.toList()));
            else if(characterDetails.getItems().stream().filter((i) -> i.getSlot()==Slot.OFFHAND).count() == 0)
                characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.OFFHAND).index(0).slotStatus(SlotStatus.OK).build());
        });
        
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
                                    i.setName(null);
                                    i.setSlotStatus(SlotStatus.OK);
                                    i.setStatusText(null);
                                }
                            } else if (tokenDetails.isOneHanded()) {
                                if(td.isRangedWeapon() && td.isShield()) {
                                    i.setItemId(null);
                                    i.setName(null);
                                    i.setSlotStatus(SlotStatus.OK);
                                    i.setStatusText(null);
                                }
                            }
                        }); 
                    }
                }
            });
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
        } else {
            if(iounCount > 5)
                characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> !(item.getSlot()==Slot.IOUNSTONE && item.getIndex() > 4)).collect(Collectors.toList()));
        }
        
        // Check for Charm of Brooching
        long backCount = characterDetails.getItems().stream().filter((item) -> item.getSlot()==Slot.BACK).count();
        if(characterDetails.getItems().stream().filter((item) -> (item.getItemId()!=null&&item.getItemId().equals("2ea75650daa8b7025cd7887c87ccd16f6a6ca369"))).count() > 0) {
            if(backCount == 1)
                characterDetails.getItems().add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.BACK).index(1).slotStatus(SlotStatus.OK).build());
        } else {
            if(backCount > 1)
                characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> !(item.getSlot()==Slot.BACK && item.getIndex() > 0)).collect(Collectors.toList()));
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

        // Check Runestone
        List<CharacterItem> orderedRunestones = new ArrayList();
        characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null&&item.getSlot()==Slot.RUNESTONE).forEach((item) -> {
            item.setIndex(orderedRunestones.size());
            orderedRunestones.add(item);
        });
        orderedRunestones.add(CharacterItem.builder().id(UUID.randomUUID().toString()).characterId(characterDetails.getId()).slot(Slot.RUNESTONE).index(orderedRunestones.size()).slotStatus(SlotStatus.OK).build());
        characterDetails.setItems(characterDetails.getItems().stream().filter((item) -> item.getSlot()!=Slot.RUNESTONE).collect(Collectors.toList()));
        characterDetails.getItems().addAll(orderedRunestones);
        
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
    
    private void checkSetItems(CharacterDetails characterDetails) {
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
        long redoubtCount = characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("f2b637457bc71b5fefd203388e84ba0e362c805a") || item.getItemId().equals("b07a7dad6b7094f91d5a080d4d16a2f505cfb343") || item.getItemId().equals("acaf3474f747654a2e0a3131f578762525173031") || item.getItemId().equals("94fa53c957b26cc84e0934b8425eeb82e77e3fa2") || item.getItemId().equals("b090451773359bd10b8e2d48cb2057c0a5ab9197") || item.getItemId().equals("d7a7ec69cff4f728559b57f4a4abb5beb092079b") || item.getItemId().equals("7784bba18a9c0f2021af27c6d1b07e20d0e774f2") || item.getItemId().equals("807e09264b752e61a405333ee1f5f165a2187848") || item.getItemId().equals("5a9dfb7089e81ad8995db870c890e5f93e4ed764") || item.getItemId().equals("30d3dc0bf82c9524c8f7874eb86571936cab8dd8"))).count();
        // Viper Strike Set
        long viperCount = characterDetails.getItems().stream().filter((item) -> item.getItemId() != null && (item.getItemId().equals("9431d39ad2fba9953bf4b526d86f41f37022efeb") || item.getItemId().equals("f2ff2a508dd3075633ca2fd9e58c0e1a76088af8") || item.getItemId().equals("dd565d74807cc9094990b324465612d52b3070bf") || item.getItemId().equals("09ad5527813c4f087f3123cd6a40404b9377a4bc"))).count();
        viperCount += characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && item.getItemId().equals("bd21afd63114346decea5fc899ff697106e99429")).count();
        
        // First check if we need to boost character level
        // Charm of Heroism, Medallion of Heroism, Ring of Heroism, Eldrich Set, Kubu’s Coin of Coincidence, Smackdown’s Charm of Comraderie
        if(itemsMap.containsKey("d20aa5f4194d09336b0a5974215247cfaa480c9a") || itemsMap.containsKey("d4674a1b2bea57e8b11676fed2bf81bd4c48ac78") || itemsMap.containsKey("85bbc3d8307b702dde0525136fb82bf1636f55d8") || 
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
        // all three two spells in one round once per room
        if(characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("3dcfd7948a3c9196556ef7e069a36174396297ad") || item.getItemId().equals("f225241f60605ef641beeecd5003ba4129dbf46e") || item.getItemId().equals("1c688491fcb8a12199e9eca6d97e3da5ef4f3d65"))).count() == 3)
            characterDetails.getNotes().add(CharacterNote.builder().oncePerRoom(true).note("You may cast two spells in one round.  Spell modifiers apply to both.").build());
        
        // Celestial Set
        // all 3 immune to melee and mental attacks from evil outsiders
        if(characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("09a0ad7cb78d898992c96ab00487bc8ffdc66f5a") || item.getItemId().equals("4b4e1b22e1ce20a529920fd48f8b891ec8e0b74a") || item.getItemId().equals("c34921c9623d7d3c1eee054e646fc3cdc7f08659"))).count() == 3)
            characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("You are immune to melee and mental attacks from evil outsiders.").build());
       
        // Charming items
        if(charmingCount > 0) {
            int charmCount = (int) characterDetails.getItems().stream().distinct().filter((item) -> item.getSlot() == Slot.CHARM).count();
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
      //  if(characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("09a0ad7cb78d898992c96ab00487bc8ffdc66f5a") || item.getItemId().equals("4b4e1b22e1ce20a529920fd48f8b891ec8e0b74a") || item.getItemId().equals("c34921c9623d7d3c1eee054e646fc3cdc7f08659"))).count() == 3)
            //characterDetails.getStats().setRetDmg(characterDetails.getStats().getRetDmg() + 2);
       
        
        // Defender Set
        // all 3 free action movement and +1 AC
        /*if(characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("09a0ad7cb78d898992c96ab00487bc8ffdc66f5a") || item.getItemId().equals("4b4e1b22e1ce20a529920fd48f8b891ec8e0b74a") || item.getItemId().equals("c34921c9623d7d3c1eee054e646fc3cdc7f08659"))).count() == 3) {
            characterDetails.getStats().setMeleeAC(characterDetails.getStats().getMeleeAC() + 1);
            characterDetails.getStats().setRangeAC(characterDetails.getStats().getRangeAC() + 1);
            characterDetails.getStats().setFreeMovement(true);
        }*/
        
        // Dragonhide Set
        // 3 or more auto pass saves vs dragon breath, +3 saves
        /*if(characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("67935e7e953d26c83d095fd188a226d16fc16e1f") || item.getItemId().equals("4b4e1b22e1ce20a529920fd48f8b891ec8e0b74a") || item.getItemId().equals("c34921c9623d7d3c1eee054e646fc3cdc7f08659"))).count() == 3) {
            characterDetails.getStats().setFort(characterDetails.getStats().getFort() + 3);
            characterDetails.getStats().setReflex(characterDetails.getStats().getReflex() + 3);
            characterDetails.getStats().setWill(characterDetails.getStats().getWill() + 3);
            characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("You automatically make saving throws vs. dragon breath weapons").build());
        }*/
        
        // Dragonscale Set
        // all 3 - 7 fire dr
        if(characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("3f9d6414ad5259959d23f8b509b8881d47ca1d11") || item.getItemId().equals("72341d583d26a57a7f3d3f460f2e976d315688b0") || item.getItemId().equals("2effa74fb480d06733e2aeff29394badf15e58c8"))).count() == 3) {
            characterDetails.getStats().setDrFire(characterDetails.getStats().getDrFire() + 7);
        }
        
        // Eldritch Set
        // 2 piece ignore spell resistence, healing spells +10, melee igonore DR
        if(eldrichCount >= 2) {
            characterDetails.getStats().setSpellHeal(characterDetails.getStats().getSpellHeal() + 10);
            characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("Your spells and melee attack ignore spell resistance and damage reduction.").build());
        }
        
        // Footman Set
        // all 3 +2 AC and cold DR 1
        /*if(characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("09a0ad7cb78d898992c96ab00487bc8ffdc66f5a") || item.getItemId().equals("4b4e1b22e1ce20a529920fd48f8b891ec8e0b74a") || item.getItemId().equals("c34921c9623d7d3c1eee054e646fc3cdc7f08659"))).count() == 3) {
            characterDetails.getStats().setMeleeAC(characterDetails.getStats().getMeleeAC() + 2);
            characterDetails.getStats().setRangeAC(characterDetails.getStats().getRangeAC() + 2);
            characterDetails.getStats().setDrCold(characterDetails.getStats().getDrCold() + 1);
        }*/
        
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
        /*if(characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("09a0ad7cb78d898992c96ab00487bc8ffdc66f5a") || item.getItemId().equals("4b4e1b22e1ce20a529920fd48f8b891ec8e0b74a") || item.getItemId().equals("c34921c9623d7d3c1eee054e646fc3cdc7f08659")).count() == 3) {
            characterDetails.getStats().setDrCold(characterDetails.getStats().getDrCold() + 1);
        }*/
        
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
        /*if(characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("d9ebb109843e8fa1aa04d90dbf7405e572042fa1") || item.getItemId().equals("e5b7a23fc4208752b6e29c1c0c040279425bc898") || item.getItemId().equals("4fb8d5b22892902f33a73630d5dddb8cff8e244b")).count() == 3) {
            characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("You have the feather fall effect and immunity to non-magical physical missiles.").build());
        }*/
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
        AtomicInteger mightyRanged = new AtomicInteger(0);
        AtomicInteger additionalTreasureTokens = new AtomicInteger(0);

        characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null).forEach((item) -> {
            TokenFullDetails td = tokenAdminMapper.getTokenDetails(item.getItemId());
            
            if(td.getId().equals("0448ddb1214a3f5c03af24653383d507fa0ea85c"))
                metCondition.add(ConditionalUse.NOT_WITH_COA);
            if(td.getTreasureMin()>0 && td.getRarity() != Rarity.PLAYER_REWARD)
                if(additionalTreasureTokens.addAndGet(1) > 1)
                    metCondition.add(ConditionalUse.NO_OTHER_TREASURE);
            
            if(td.isOneHanded() && !td.isRangedWeapon())
                metCondition.add(ConditionalUse.WEAPON_1H);
            if(td.isRangedWeapon())
                metCondition.add(ConditionalUse.WEAPON_RANGED);
            if(td.isTwoHanded() && !td.isRangedWeapon())
                metCondition.add(ConditionalUse.WEAPON_2H);
            
            if(td.getConditionalUse() != ConditionalUse.NONE){
                conditionalTokens.add(item);
            } else {
                item.setSlotStatus(SlotStatus.OK);
                item.setStatusText(null);
                updateStats(stats, td, characterDetails.getNotes());
            }
            
            if(item.getSlot() == Slot.MAINHAND) {
                mainWeaponHit.set(td.getMeleeHit());
                if(td.getMeleeHit() > offWeaponHit.get())
                    stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit() - offWeaponHit.get());
            } else if(item.getSlot() == Slot.OFFHAND) {
                offWeaponHit.set(td.getMeleeHit());
                if(td.getMeleeHit() > mainWeaponHit.get())
                    stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit() - mainWeaponHit.get());
            } else if(item.getSlot() != Slot.RANGE_MAINHAND)
                stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit());
            if(item.getSlot() != Slot.RANGE_OFFHAND)
                stats.setMeleeAC(stats.getMeleeAC() + td.getMeleeAC());
            if(item.getSlot() == Slot.RANGE_MAINHAND && (td.isThrown() || td.getName().toLowerCase().contains("mighty")))
                mightyRanged.set(1);
            if(item.getSlot() != Slot.MAINHAND && item.getSlot() != Slot.OFFHAND)    
                stats.setRangeHit(stats.getRangeHit() + td.getRangeHit());
            if(item.getSlot() != Slot.OFFHAND) {
                stats.setRangeAC(stats.getRangeAC() + td.getRangeAC());
                stats.setRangeMissileAC(stats.getRangeMissileAC() + td.getRangeMissileAC());
            }
        });   
        
        // Check Conditionals 
        checkConditionals(conditionalTokens, metCondition, stats, characterDetails.getNotes());
        
        stats.setStrBonus((stats.getStr()-10)/2);
        stats.setDexBonus((stats.getDex()-10)/2);
        stats.setConBonus((stats.getCon()-10)/2);
        stats.setIntelBonus((stats.getIntel()-10)/2);
        stats.setWisBonus((stats.getWis()-10)/2);
        stats.setChaBonus((stats.getCha()-10)/2);

        stats.setHealth(stats.getHealth() + stats.getLevel() * ((int)((stats.getCon()+1-stats.getBaseCon())/2)));
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
    }
    
    private void checkConditionals(List<CharacterItem> conditionalTokens, Set<ConditionalUse> metCondition, CharacterStats stats, List<CharacterNote> notes) {
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
                        updateStats(stats, td, notes);
                    }   break;
                case WEAPON_1H:
                    if(!metCondition.contains(ConditionalUse.WEAPON_1H)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a one handed weapon to use.");
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(stats, td, notes);
                    }   break;
                case WEAPON_RANGED:
                    if(!metCondition.contains(ConditionalUse.WEAPON_RANGED)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a range weapon to use.");
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(stats, td, notes);
                    }   break;
                case DEXTERITY_18:
                    if(stats.getDex() < 18) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a dexterity of 18 or higher to use.");
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(stats, td, notes);
                    }   break;
                case DEXTERITY_20:
                    if(stats.getDex() < 20) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a dexterity of 20 or higher to use.");
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(stats, td, notes);
                    }   break;
                case INTELLECT_20:
                    if(stats.getIntel()< 20) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a intellect of 20 or higher to use.");
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(stats, td, notes);
                    }   break;
                case WISDOM_20:
                    if(stats.getWis()< 20) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a wisdom of 20 or higher to use.");
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(stats, td, notes);
                    }   break;
                case STRENGTH_24:
                    if(stats.getStr()< 24) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText("This token requires a strength of 24 or higher to use.");
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
                        updateStats(stats, td, notes);
                    }   break;
                case NO_OTHER_TREASURE:
                    if(metCondition.contains(ConditionalUse.NO_OTHER_TREASURE)) {
                        token.setSlotStatus(SlotStatus.INVALID);
                        token.setStatusText(token.getName() + " cannot be used with any other treasure enchancing token.");
                    } else {
                        token.setSlotStatus(SlotStatus.OK);
                        token.setStatusText(null);
                        updateStats(stats, td, notes);
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
}
