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
import com.achersoft.tdcc.enums.Slot;
import com.achersoft.tdcc.enums.SlotStatus;
import com.achersoft.tdcc.token.admin.dao.TokenFullDetails;
import com.achersoft.tdcc.token.admin.persistence.TokenAdminMapper;
import com.achersoft.tdcc.token.persistence.TokenMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.springframework.transaction.annotation.Transactional;

@Transactional(rollbackFor = Exception.class)
public class CharacterServiceImpl implements CharacterService {
    
    private @Inject CharacterMapper mapper;
    private @Inject TokenMapper tokenMapper;
    private @Inject TokenAdminMapper tokenAdminMapper;
    private @Inject UserPrincipalProvider userPrincipalProvider;

    @Override
    public CharacterDetails getCharacter(String id) {
        CharacterDetails characterDetails = mapper.getCharacter(id, userPrincipalProvider.getUserPrincipal().getSub());
        
        if(characterDetails == null)
            throw new InvalidDataException("Requested character could not be found."); 
        
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
    
    private CharacterDetails validateCharacterItems(String id) {
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
        long viperCount = characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("bd21afd63114346decea5fc899ff697106e99429") || item.getItemId().equals("9431d39ad2fba9953bf4b526d86f41f37022efeb") || item.getItemId().equals("f2ff2a508dd3075633ca2fd9e58c0e1a76088af8") || item.getItemId().equals("dd565d74807cc9094990b324465612d52b3070bf") || item.getItemId().equals("09ad5527813c4f087f3123cd6a40404b9377a4bc"))).count();
        
        // Reset Notes
        characterDetails.setNotes(new ArrayList());
        
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
        
        // Cabal set
        // all three two spells in one round once per room
        if(characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("3dcfd7948a3c9196556ef7e069a36174396297ad") || item.getItemId().equals("f225241f60605ef641beeecd5003ba4129dbf46e") || item.getItemId().equals("1c688491fcb8a12199e9eca6d97e3da5ef4f3d65"))).count() == 3)
            characterDetails.getNotes().add(CharacterNote.builder().oncePerRoom(true).note("You may cast two spells in one round.  Spell modifiers apply to both.").build());
        
        // Celestial Set
        // all 3 immune to melee and mental attacks from evil outsiders
        if(characterDetails.getItems().stream().distinct().filter((item) -> item.getItemId() != null && (item.getItemId().equals("09a0ad7cb78d898992c96ab00487bc8ffdc66f5a") || item.getItemId().equals("4b4e1b22e1ce20a529920fd48f8b891ec8e0b74a") || item.getItemId().equals("c34921c9623d7d3c1eee054e646fc3cdc7f08659"))).count() == 3)
            characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(true).note("You are immune to melee and mental attacks from evil outsiders.").build());
       
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
    
    private void calculateStats(CharacterDetails characterDetails) {
        final CharacterStats stats = characterDetails.getStats();

        characterDetails.getItems().stream().filter((item) -> item.getItemId()!=null).forEach((item) -> {
            TokenFullDetails td = tokenAdminMapper.getTokenDetails(item.getItemId());
            stats.setStr(stats.getStr() + td.getStr());
            stats.setDex(stats.getDex() + td.getDex());
            stats.setCon(stats.getCon() + td.getCon());
            stats.setIntel(stats.getIntel() + td.getIntel());
            stats.setWis(stats.getWis() + td.getWis());
            stats.setCha(stats.getCha() + td.getCha());
            stats.setHealth(stats.getHealth() + td.getHealth());
            stats.setRegen(stats.getRegen() + td.getRegen());
            stats.setMeleeHit(stats.getMeleeHit() + td.getMeleeHit());
            stats.setMeleeDmg(stats.getMeleeDmg() + td.getMeleeDmg());
            stats.setMeleeFire(stats.isMeleeFire() || td.isMeleeFire());
            stats.setMeleeCold(stats.isMeleeCold() || td.isMeleeCold());
            stats.setMeleeShock(stats.isMeleeShock() || td.isMeleeShock());
            stats.setMeleeSonic(stats.isMeleeSonic() || td.isMeleeSonic());
            stats.setMeleeEldritch(stats.isMeleeEldritch() || td.isMeleeEldritch());
            stats.setMeleePoison(stats.isMeleePoison() || td.isMeleePoison());
            stats.setMeleeDarkrift(stats.isMeleeDarkrift() || td.isMeleeDarkrift());
            stats.setMeleeSacred(stats.isMeleeSacred() || td.isMeleeSacred());
            if(item.getSlot() != Slot.RANGE_OFFHAND)
                stats.setMeleeAC(stats.getMeleeAC() + td.getMeleeAC());
            stats.setRangeHit(stats.getRangeHit() + td.getRangeHit());
            stats.setRangeDmg(stats.getRangeDmg() + td.getRangeDmg());
            stats.setRangeFire(stats.isRangeFire() || td.isRangeFire());
            stats.setRangeCold(stats.isRangeCold() || td.isRangeCold());
            stats.setRangeShock(stats.isRangeShock() || td.isRangeShock());
            stats.setRangeSonic(stats.isRangeSonic() || td.isRangeSonic());
            stats.setRangeEldritch(stats.isRangeEldritch() || td.isRangeEldritch());
            stats.setRangePoison(stats.isRangePoison() || td.isRangePoison());
            stats.setRangeDarkrift(stats.isRangeDarkrift() || td.isRangeDarkrift());
            stats.setRangeSacred(stats.isRangeSacred() || td.isRangeSacred());
            if(item.getSlot() != Slot.OFFHAND) {
                stats.setRangeAC(stats.getRangeAC() + td.getRangeAC());
                stats.setRangeMissileAC(stats.getRangeMissileAC() + td.getRangeMissileAC());
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
                 characterDetails.getNotes().add(CharacterNote.builder().alwaysInEffect(td.isAlwaysInEffect()).oncePerRound(td.isOncePerRound()).oncePerRoom(td.isOncePerRoom()).oncePerGame(td.isOncePerGame()).note(td.getSpecialText()).build());
            }
        });   
        
        stats.setStrBonus((stats.getStr()-10)/2);
        stats.setDexBonus((stats.getDex()-10)/2);
        stats.setConBonus((stats.getCon()-10)/2);
        stats.setIntelBonus((stats.getIntel()-10)/2);
        stats.setWisBonus((stats.getWis()-10)/2);
        stats.setChaBonus((stats.getCha()-10)/2);

        stats.setHealth(stats.getHealth() + stats.getLevel() * ((int)((stats.getCon()-stats.getBaseCon())/2)));
        stats.setMeleeHit(stats.getMeleeHit() + stats.getStrBonus());
        stats.setMeleeDmg(stats.getMeleeDmg() + stats.getStrBonus());
        stats.setRangeHit(stats.getRangeHit() + stats.getDexBonus());
        stats.setMeleeAC(stats.getMeleeAC() + stats.getDexBonus());
        stats.setRangeAC(stats.getRangeAC() + stats.getDexBonus());
        stats.setFort(stats.getFort() + stats.getConBonus());
        stats.setReflex(stats.getReflex() + stats.getDexBonus());
        stats.setWill(stats.getWill() + stats.getWisBonus());
    }
}
