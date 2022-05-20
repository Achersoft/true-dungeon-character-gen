package com.achersoft.tdcc.vtd;

import com.achersoft.exception.InvalidDataException;
import com.achersoft.security.providers.UserPrincipalProvider;
import com.achersoft.tdcc.character.CharacterService;
import com.achersoft.tdcc.character.dao.*;
import com.achersoft.tdcc.character.persistence.CharacterMapper;
import com.achersoft.tdcc.enums.*;
import com.achersoft.tdcc.party.dao.SelectableCharacters;
import com.achersoft.tdcc.token.admin.dao.TokenFullDetails;
import com.achersoft.tdcc.token.admin.persistence.TokenAdminMapper;
import com.achersoft.tdcc.token.dao.Token;
import com.achersoft.tdcc.token.persistence.TokenMapper;
import com.achersoft.tdcc.vtd.admin.dao.VtdRoom;
import com.achersoft.tdcc.vtd.admin.dao.VtdSetting;
import com.achersoft.tdcc.vtd.admin.persistence.VtdAdminMapper;
import com.achersoft.tdcc.vtd.dao.*;
import com.achersoft.tdcc.vtd.persistence.VtdMapper;
import org.springframework.util.StringUtils;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class VirtualTdServiceImpl implements VirtualTdService {
    
    private @Inject CharacterMapper mapper;
    private @Inject CharacterService characterService;
    private @Inject VtdMapper vtdMapper;
    private @Inject VtdAdminMapper vtdAdminMapper;
    private @Inject TokenMapper tokenMapper;
    private @Inject TokenAdminMapper tokenAdminMapper;
    private @Inject UserPrincipalProvider userPrincipalProvider;

    @Override
    public List<CharacterName> getSelectableCharacters() {
        final String userid = userPrincipalProvider.getUserPrincipal().getSub();
        final Set<CharacterName> characterNameSet = new HashSet<>();
        final List<CharacterName> characters = mapper.getCharacters(userid);
        final List<VtdDetails> vtdDetails = vtdMapper.getCharacters(userid);

        if (characters != null)
            characterNameSet.addAll(characters);

        if (vtdDetails != null)
            characterNameSet.addAll(vtdDetails.stream().filter(vtdDetail -> !vtdDetail.getExpires().before(new Date())).map(vtdDetail -> {
                final CharacterStats characterStats = vtdMapper.getCharacterStats(vtdDetail.getCharacterId());
                if (characterStats != null)
                    return CharacterName.builder()
                            .id(vtdDetail.getCharacterId())
                            .name(vtdDetail.getName())
                            .userId(userid)
                            .characterClass(vtdDetail.getCharacterClass())
                            .level(characterStats.getLevel())
                            .deletable(true)
                            .build();
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toSet()));

        final ArrayList<CharacterName> characterNames = new ArrayList<>(characterNameSet);
        characterNames.sort(Comparator.comparing(characterName -> characterName.getName().toLowerCase()));

        return characterNames;
    }

    @Override
    public List<CharacterName> getPregeneratedCharacters() {
        final List<CharacterName> characterNames = mapper.getCharacters("pregen");

        if (characterNames != null)
            characterNames.sort(Comparator.comparing(CharacterName::getName));

        return characterNames;
    }

    @Override
    public List<CharacterName> importCharacter(String id) {
        getVtdCharacter(id, true,false);
        return getSelectableCharacters();
    }

    @Override
    public VtdDetails getVtdCharacter(String id, boolean reset, boolean activatePrestige) {
        VtdDetails vtdDetails = vtdMapper.getCharacter(id);

        if (activatePrestige && vtdDetails != null) {
            if (!vtdDetails.isPrestigeAvailable())
                throw new InvalidDataException("Cannot activate prestige class, you do not have a Rod of Seven Parts or a Skull of Cavadar equipped.");
            if (vtdDetails.getRoomNumber() > 1)
                throw new InvalidDataException("Your VTD character is mid adventure and must be reset before a prestige class can be activated.");
        }

        if (reset || vtdDetails == null || vtdDetails.getExpires().before(new Date())) {
            String origId = id;
            CharacterDetails characterDetails = null;

            if (vtdDetails != null && vtdDetails.getCharacterOrigId() != null && !vtdDetails.getCharacterOrigId().isEmpty() && !vtdDetails.getCharacterOrigId().equals(id)) {
                characterDetails = characterService.getCharacter(vtdDetails.getCharacterOrigId());
                origId = vtdDetails.getCharacterOrigId();
            } else
                characterDetails = characterService.getCharacter(id);

            if (characterDetails == null) {
                vtdMapper.deleteCharacterPolys(id);
                vtdMapper.deleteCharacterBuffs(id);
                vtdMapper.deleteCharacterSkills(id);
                vtdMapper.deleteQueuedSkills(id);
                vtdMapper.deleteCharacterStats(id);
                vtdMapper.deleteCharacter(id);

                throw new InvalidDataException("Character has expired or no longer exists.");
            } else if (characterDetails.getUserId().equals("pregen")) {
                final VtdDetails characterByName = vtdMapper.getCharacterByName(userPrincipalProvider.getUserPrincipal().getSub(), characterDetails.getName() + "-VTD-Pregen");
                if (characterByName == null) {
                    characterDetails.setId(UUID.randomUUID().toString());
                    characterDetails.setUserId(userPrincipalProvider.getUserPrincipal().getSub());
                    characterDetails.setName(characterDetails.getName() + "-VTD-Pregen");
                    characterDetails.getStats().setCharacterId(characterDetails.getId());
                } else if(reset || characterByName.getExpires().before(new Date())) {
                    vtdMapper.deleteCharacterPolys(characterByName.getCharacterId());
                    vtdMapper.deleteCharacterBuffs(characterByName.getCharacterId());
                    vtdMapper.deleteCharacterSkills(characterByName.getCharacterId());
                    vtdMapper.deleteQueuedSkills(characterByName.getCharacterId());
                    vtdMapper.deleteCharacterStats(characterByName.getCharacterId());
                    vtdMapper.deleteCharacter(characterByName.getCharacterId());

                    characterDetails.setId(UUID.randomUUID().toString());
                    characterDetails.setUserId(userPrincipalProvider.getUserPrincipal().getSub());
                    characterDetails.setName(characterDetails.getName() + "-VTD-Pregen");
                    characterDetails.getStats().setCharacterId(characterDetails.getId());
                } else {
                    characterByName.setExpires(new Date(new Date().getTime() + 86400000));
                    vtdMapper.updateCharacter(characterByName);

                    return calculateStats(characterByName.getCharacterId());
                }
            } else if (!characterDetails.isEditable()) {
                origId = characterDetails.getId();
                final VtdDetails characterByName = vtdMapper.getCharacterByName(userPrincipalProvider.getUserPrincipal().getSub(), characterDetails.getName() + "-" + characterDetails.getUsername());
                if (characterByName == null) {
                    characterDetails.setId(UUID.randomUUID().toString());
                    characterDetails.setUserId(userPrincipalProvider.getUserPrincipal().getSub());
                    characterDetails.setName(characterDetails.getName() + "-" + characterDetails.getUsername());
                    characterDetails.getStats().setCharacterId(characterDetails.getId());
                } else if(reset || characterByName.getExpires().before(new Date())) {
                    vtdMapper.deleteCharacterPolys(characterByName.getCharacterId());
                    vtdMapper.deleteCharacterBuffs(characterByName.getCharacterId());
                    vtdMapper.deleteCharacterSkills(characterByName.getCharacterId());
                    vtdMapper.deleteQueuedSkills(characterByName.getCharacterId());
                    vtdMapper.deleteCharacterStats(characterByName.getCharacterId());
                    vtdMapper.deleteCharacter(characterByName.getCharacterId());

                    characterDetails.setId(UUID.randomUUID().toString());
                    characterDetails.setUserId(userPrincipalProvider.getUserPrincipal().getSub());
                    characterDetails.setName(characterDetails.getName() + "-" + characterDetails.getUsername());
                    characterDetails.getStats().setCharacterId(characterDetails.getId());
                } else {
                    characterByName.setExpires(new Date(new Date().getTime() + 86400000));
                    vtdMapper.updateCharacter(characterByName);

                    return calculateStats(characterByName.getCharacterId());
                }
            }

            vtdMapper.deleteCharacterPolys(id);
            vtdMapper.deleteCharacterBuffs(id);
            vtdMapper.deleteCharacterSkills(id);
            vtdMapper.deleteQueuedSkills(id);
            vtdMapper.deleteCharacterStats(id);
            vtdMapper.deleteCharacter(id);

            final List<CharacterSkill> skills = activatePrestige ? vtdMapper.getSkills(characterDetails.getCharacterClass(), 6) :
                    vtdMapper.getSkills(characterDetails.getCharacterClass(), characterDetails.getStats().getLevel());
            if (skills != null) {
                final String charId = characterDetails.getId();
                skills.forEach(skill -> {
                    skill.setId(UUID.randomUUID().toString());
                    skill.setCharacterId(charId);
                    skill.setUsedNumber(0);
                    vtdMapper.addCharacterSkill(skill);
                });
            }

            final VtdDetails.VtdDetailsBuilder builder = VtdDetails.builder();
            final List<CharacterSkill> characterSkills = Optional.ofNullable(vtdMapper.getCharacterSkills(characterDetails.getId())).orElse(new ArrayList<>());
            final List<DamageModEffect> meleeDmgEffects = new ArrayList<>();
            final List<DamageModEffect> meleeOffhandDmgEffects = new ArrayList<>();
            final List<DamageModEffect> rangeDmgEffects = new ArrayList<>();
            final List<DamageModEffect> rangeOffhandDmgEffects = new ArrayList<>();
            final List<InGameEffect> inGameEffects = new ArrayList<>();
            final AtomicReference<TokenFullDetails> mainHand = new AtomicReference<>();
            final AtomicReference<TokenFullDetails> offHand = new AtomicReference<>();
            final AtomicReference<TokenFullDetails> rangeMainHand = new AtomicReference<>();
            final AtomicReference<TokenFullDetails> rangeOffHand = new AtomicReference<>();
            final AtomicBoolean hasMedallionKeenness = new AtomicBoolean(false);
            final AtomicBoolean hasAmuletOfAiming = new AtomicBoolean(false);
            final AtomicBoolean hasBarbRelic = new AtomicBoolean(false);
            final AtomicBoolean hasBarbLegendary = new AtomicBoolean(false);
            final AtomicBoolean hasDruidRelic = new AtomicBoolean(false);
            final AtomicBoolean hasDruidLegendary = new AtomicBoolean(false);
            final AtomicBoolean charmShadowShot = new AtomicBoolean(false);
            final AtomicBoolean madEvoker = new AtomicBoolean(false);
            final AtomicBoolean divineSight = new AtomicBoolean(false);
            final AtomicBoolean hasPaladinRelic = new AtomicBoolean(false);
            final AtomicBoolean hasPaladinLegendary = new AtomicBoolean(false);
            final AtomicBoolean hasMonkRelic = new AtomicBoolean(false);
            final AtomicBoolean hasMonkLegendary = new AtomicBoolean(false);
            final AtomicBoolean hasShamansBelt = new AtomicBoolean(false);
            final AtomicBoolean hasRangerLegendary = new AtomicBoolean(false);
            final AtomicBoolean hasPrestigeClass = new AtomicBoolean(false);
            final AtomicBoolean hasPelor = new AtomicBoolean(false);
            final AtomicInteger turnUndeadDamage = new AtomicInteger(0);
            final AtomicBoolean hasWizardRelic = new AtomicBoolean(false);
            final AtomicBoolean hasWizardLegendary = new AtomicBoolean(false);
            final AtomicBoolean ignoreIncorporeal = new AtomicBoolean(false);
            final AtomicBoolean hasQuestors = new AtomicBoolean(false);
            final AtomicBoolean crownOfElements = new AtomicBoolean(false);
            final Set<CritType> critTypes = new HashSet<>();

            for (CharacterItem characterItem : characterDetails.getItems()) {
                if (characterItem != null && characterItem.getItemId() != null) {
                    if (characterItem.getSlot() == Slot.MAINHAND)
                        mainHand.set(tokenAdminMapper.getTokenDetails(characterItem.getItemId()));
                    else if (characterItem.getSlot() == Slot.OFFHAND)
                        offHand.set(tokenAdminMapper.getTokenDetails(characterItem.getItemId()));
                    else if (characterItem.getSlot() == Slot.RANGE_MAINHAND)
                        rangeMainHand.set(tokenAdminMapper.getTokenDetails(characterItem.getItemId()));
                    else if (characterItem.getSlot() == Slot.RANGE_OFFHAND)
                        rangeOffHand.set(tokenAdminMapper.getTokenDetails(characterItem.getItemId()));

                    if (characterItem.getItemId().equals("92c1ce81e876bc168949f61cdc1a9a33e7e52409"))
                        hasBarbRelic.set(true);
                    else if (characterItem.getItemId().equals("9b9e86587d8776a706ab8dc01bc060fc2560da50") && characterItem.getSlotStatus() == SlotStatus.OK)
                        hasMedallionKeenness.set(true);
                    else if (characterItem.getName().toLowerCase().equals("amulet of aiming"))
                        hasAmuletOfAiming.set(true);
                    else if (characterItem.getItemId().equals("8f0d04e682e039be8833596cb3661c7f4ce4e55a"))
                        meleeDmgEffects.add(DamageModEffect.SMACK_WEAPON);
                    else if (characterItem.getItemId().equals("0076ceef0f905dda175de13222ce34029a5873f2"))
                        hasBarbLegendary.set(true);
                    else if (characterItem.getItemId().equals("f2f2a4950f8e1a2415890a370b54efc1605b551a"))
                        charmShadowShot.set(true);
                    else if (characterItem.getItemId().equals("9b9e067d380d1da6b5ab050687ed72b18085e341"))
                        hasQuestors.set(true);
                    else if (characterItem.getItemId().equals("406e73fa9919a8557b5ab4ab0d12ad74eacbb9cf"))
                        crownOfElements.set(true);
                    else if (characterItem.getItemId().equals("18c97b7dc056aaf9e15a1b14f59c86fc18de0c27"))
                        madEvoker.set(true);
                    else if (characterItem.getItemId().equals("c36cb79000e553702d24d216e5fe1997185e704c"))
                        divineSight.set(true);
                    else if (characterItem.getItemId().equals("6a46f2b9c3070fd6076256d4e1c55a89725bcd36"))
                        hasDruidRelic.set(true);
                    else if (characterItem.getItemId().equals("c9182371165f18e7fdbce5da41a69af3934d6ee7"))
                        hasDruidLegendary.set(true);
                    else if (characterItem.getItemId().equals("3004090e3dde392b1af2b11930b84ec949612c36"))
                        hasPaladinRelic.set(true);
                    else if (characterItem.getItemId().equals("c399f93f1b0a647f6a15e11d8215553fc7c2043f"))
                        hasPaladinLegendary.set(true);
                    else if (characterItem.getItemId().equals("0cf23986f7f79a143b3161564df80c055c409e90"))
                        hasMonkRelic.set(true);
                    else if (characterItem.getItemId().equals("1ee980321b7b26f523b7b5e10b6a2856400d1a67"))
                        hasMonkLegendary.set(true);
                    else if (characterItem.getItemId().equals("64e85eb5edb7f5274adc05b3851732d04fd67949"))
                        hasWizardRelic.set(true);
                    else if (characterItem.getItemId().equals("df1626b8137744b80e95ec254745122f2c1e291f"))
                        hasWizardLegendary.set(true);
                    else if (characterItem.getItemId().equals("e604ae878baea5348138a4b22180b74a34c6ecce"))
                        hasShamansBelt.set(true);
                    else if (characterItem.getItemId().equals("186c34894ca45e6acdd58a0d6cab9ef1be36e3ad"))
                        hasRangerLegendary.set(true);
                    else if (characterItem.getItemId().equals("1c688491fcb8a12199e9eca6d97e3da5ef4f3d65")) //Charm of the Cabal
                        builder.charmCabalBonus(1);
                    else if (characterItem.getItemId().equals("3dcfd7948a3c9196556ef7e069a36174396297ad")) //Gloves of the Cabal
                        builder.glovesCabalBonus(1);
                    else if (characterItem.getItemId().equals("f225241f60605ef641beeecd5003ba4129dbf46e"))  //Bracelets of the Cabal
                        builder.braceletCabalBonus(1);
                    else if (characterItem.getItemId().equals("5b4d906cca80b7f2cd719133d4ff6822c435f5c3") ||
                             characterItem.getItemId().equals("958f1c96f2e1072f0488513bde34e65553b1ebaa"))
                        hasPrestigeClass.set(true);
                    else if (characterItem.getItemId().equals("e3d537d7b1067df3a7f67d121d1394c26efd7937") ||
                             characterItem.getItemId().equals("e8bd935fafdc20dec319a8e2e72dcfc06de30fda") ||
                             characterItem.getItemId().equals("ba1439a19e276be50cce8d0ca3dbf03a39703b56") ||
                             characterItem.getItemId().equals("1414e6288f8c8eed096ee123e11807e55dd73509") ||
                             characterItem.getItemId().equals("37ee8d237352a5f557ace9c93d2405723bf778a5") ||
                             characterItem.getItemId().equals("b1665d38410fa27957dc1d3b64f9b4e808e071a3"))
                        ignoreIncorporeal.set(true);
                    else if (characterItem.getItemId().equals("1ec328d0a1f1144b23fdd400412553a00df1b7b0"))  //Acolyte Holy Symbol
                        turnUndeadDamage.set(1);
                    else if (characterItem.getItemId().equals("1a1f740907d508cff6ca662ef11a83ec6f184806"))  //Commander’s Holy Symbol
                        turnUndeadDamage.set(2);
                    else if (characterItem.getItemId().equals("786e8cf2deed99f909facb6aaa9d58768a663f66"))  //Greater Holy Symbol
                        turnUndeadDamage.set(2);
                    else if (characterItem.getItemId().equals("972f6a25ff5823aa7b51ef56bf9f2b8d0b8c3da6"))  //Masterwork Holy Symbol
                        turnUndeadDamage.set(1);
                    else if (characterItem.getItemId().equals("29dd33d000f88cbd3a571551382d9f17d2b54fbc"))  //Greater Holy Symbol of Pelor
                        hasPelor.set(true);
                    else if (characterItem.getItemId().equals("afd90da9d4f05dbce780a2befb67cd1d47187782") ||
                             characterItem.getItemId().equals("80fdb7fe44986e27f987260c94d2fedebda46888"))
                        critTypes.add(CritType.CONSTRUCT);
                    else if (characterItem.getItemId().equals("65cf6807e85d0ef42294e339ad83c65c4436a61b"))
                        critTypes.add(CritType.UNDEAD);
                    else if (characterItem.getItemId().equals("eb43d244380e38b71f149e07b3993eb7b382ef1a"))
                        critTypes.add(CritType.ELEMENTAL);
                    else if (characterItem.getItemId().equals("99561249745b26c94a83e3e45be1acb4ef44cad2"))
                        critTypes.add(CritType.PLANT);
                    else if (characterItem.getItemId().equals("2bc4f9b17575f86929e7c3e06656c0c3c79a6812") || characterItem.getName().toLowerCase().equals("bead of guided strike") || userPrincipalProvider.getUserPrincipal().getSub().equalsIgnoreCase("af42ca02-434b-4f9a-aa58-8d2e7000ee68"))
                        critTypes.add(CritType.ANY);
                }
            }

            switch (characterDetails.getCharacterClass()) {
                case ALL:
                    if (ignoreIncorporeal.get()) {
                        meleeDmgEffects.add(DamageModEffect.IGNORE_INCORPOREAL);
                        meleeOffhandDmgEffects.add(DamageModEffect.IGNORE_INCORPOREAL);
                        rangeDmgEffects.add(DamageModEffect.IGNORE_INCORPOREAL);
                        rangeOffhandDmgEffects.add(DamageModEffect.IGNORE_INCORPOREAL);
                    }
                    break;
                case BARBARIAN:
                    if (hasBarbRelic.get() || hasBarbLegendary.get()) {
                        characterSkills.stream().filter(characterSkill -> characterSkill.getName().contains("Rage")).forEach(skill -> {
                            if (hasBarbRelic.get())
                                skill.setUsableNumber(2);
                            else
                                skill.setUsableNumber(3);
                            vtdMapper.updateCharacterSkill(skill);
                        });

                        final CharacterSkill fury = CharacterSkill.builder()
                                .id(UUID.randomUUID().toString())
                                .characterClass(CharacterClass.BARBARIAN)
                                .characterId(characterDetails.getId())
                                .characterLevel(characterDetails.getStats().getLevel())
                                .name("Fury")
                                .details("Whether raging or not, as an Instant Action a barbarian can announce that they are " +
                                        "going to fury before making their attack slide. If they hit, the damage done with that " +
                                        "one slide is treated as a standard 2× critical hit. It does not count as a natural 20, " +
                                        "it’s “just” a crit.")
                                .skillLevel(SkillLevel.NA)
                                .skillTarget(SkillTarget.SELF)
                                .skillType(SkillType.BUFF)
                                .skillStatEffect(SkillStatEffect.NONE)
                                .minEffect(0)
                                .maxEffect(0)
                                .usedNumber(0)
                                .build();

                        if (hasBarbRelic.get()) {
                            fury.setUsableNumber(1);
                            vtdMapper.addCharacterSkill(fury);
                        } else {
                            fury.setUsableNumber(2);
                            vtdMapper.addCharacterSkill(fury);
                        }
                    }
                    break;
                case BARD:
                    break;
                case CLERIC:
                    if (divineSight.get())
                        builder.splitHeal(true);
                    if (activatePrestige) {
                        if (mainHand.get() != null && mainHand.get().getCritMin() > 18)
                            mainHand.get().setCritMin(18);
                        if (rangeMainHand.get() != null && rangeMainHand.get().getCritMin() > 18)
                            rangeMainHand.get().setCritMin(18);
                        critTypes.add(CritType.ANY);
                    }
                    if (hasPelor.get()) {
                        characterSkills.stream().filter(characterSkill -> characterSkill.getName().contains("Turn Undead")).forEach(skill -> {
                            skill.setMinEffect(skill.getMinEffect()*2);
                            skill.setMaxEffect(skill.getMaxEffect()*2);
                            vtdMapper.updateCharacterSkill(skill);
                        });
                    } else if (turnUndeadDamage.get() > 0) {
                        characterSkills.stream().filter(characterSkill -> characterSkill.getName().contains("Turn Undead")).forEach(skill -> {
                            skill.setMinEffect(skill.getMinEffect()+turnUndeadDamage.get());
                            skill.setMaxEffect(skill.getMaxEffect()+turnUndeadDamage.get());
                            vtdMapper.updateCharacterSkill(skill);
                        });
                    }
                    break;
                case DRUID:
                    if (divineSight.get())
                        builder.splitHeal(true);

                    List<VtdPoly> polyList = new ArrayList<>();
                    polyList.add(VtdPoly.getDefault(characterDetails.getId()));

                    if (activatePrestige) {
                        final VtdPoly vtdPoly = VtdPoly.getDefault(characterDetails.getId());
                        vtdPoly.setCompanion(true);
                        polyList.add(vtdPoly);
                    }

                    final List<Token> all = tokenMapper.getNonWeaponSlotItems(null, characterDetails.getId(), characterDetails.getCharacterClass().name(), Slot.POLYMORPH.name(), "ALL");
                    if (all != null) {
                        for (Token token : all) {
                            final TokenFullDetails tokenDetails = tokenAdminMapper.getTokenDetails(token.getId());
                            if (tokenDetails != null) {
                                polyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                        (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                        tokenDetails, false));
                                if (activatePrestige)
                                    polyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                             20, new ArrayList<>(), tokenDetails, true));
                            }
                        }
                    }

                    if (hasShamansBelt.get()) {
                        final TokenFullDetails belt = tokenAdminMapper.getTokenDetails("e604ae878baea5348138a4b22180b74a34c6ecce");
                        belt.setName("Shaman’s Belt - Air");
                        polyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                belt, false));
                        belt.setName("Shaman’s Belt - Earth");
                        polyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                belt, false));
                        belt.setName("Shaman’s Belt - Fire");
                        polyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                belt, false));
                        belt.setName("Shaman’s Belt - Ice");
                        polyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                belt, false));
                    }

                    if (hasDruidRelic.get()) {
                        final TokenFullDetails relic = tokenAdminMapper.getTokenDetails("6a46f2b9c3070fd6076256d4e1c55a89725bcd36");
                        relic.setName("Shaman’s Greater Necklace - Air");
                        polyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                relic, false));
                        relic.setName("Shaman’s Greater Necklace - Earth");
                        polyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                relic, false));
                        relic.setName("Shaman’s Greater Necklace - Fire");
                        polyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                relic, false));
                        relic.setName("Shaman’s Greater Necklace - Ice");
                        polyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                relic, false));
                    } else if (hasDruidLegendary.get()) {
                        final TokenFullDetails legendary = tokenAdminMapper.getTokenDetails("c9182371165f18e7fdbce5da41a69af3934d6ee7");
                        legendary.setName("Iktomi’s Shaper Necklace - Air");
                        polyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                legendary, false));
                        legendary.setName("Iktomi’s Shaper Necklace - Earth");
                        polyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                legendary, false));
                        legendary.setName("Iktomi’s Shaper Necklace - Fire");
                        polyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                legendary, false));
                        legendary.setName("Iktomi’s Shaper Necklace - Ice");
                        polyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                legendary, false));
                    }
                    vtdMapper.addPolys(polyList);
                    builder.polys(polyList);
                    break;
                case FIGHTER:
                    if (characterDetails.getStats().getLevel() == 5) {
                        vtdMapper.addCharacterBuff(VtdBuff.builder().characterId(id).bardsong(false).buff(Buff.FIGHTER_REROLL).build());
                    }
                    break;
                case DWARF_FIGHTER:
                    if (activatePrestige) {
                        if (offHand.get() != null && offHand.get().isShield())
                            characterDetails.getStats().setMeleeAC(characterDetails.getStats().getMeleeAC() + 2);
                        if (rangeOffHand.get() != null && rangeOffHand.get().isShield())
                            characterDetails.getStats().setRangeAC(characterDetails.getStats().getRangeAC() + 2);
                    } else if (characterDetails.getStats().getLevel() == 5) {
                        meleeDmgEffects.add(DamageModEffect.TRIPPLE_CRIT_ON_20);
                        meleeOffhandDmgEffects.add(DamageModEffect.TRIPPLE_CRIT_ON_20);
                        rangeDmgEffects.add(DamageModEffect.TRIPPLE_CRIT_ON_20);
                        rangeOffhandDmgEffects.add(DamageModEffect.TRIPPLE_CRIT_ON_20);
                    }
                    break;
                case ELF_WIZARD:
                    if (madEvoker.get())
                        builder.madEvoker(true);
                    if (hasWizardRelic.get()) {
                        builder.madEvoker(true);
                        builder.swapElements(true);
                        builder.magePower(true);
                    } if (hasWizardLegendary.get()) {
                        builder.madEvoker(true);
                        builder.swapElements(true);
                        builder.magePower(true);
                        builder.archMagePower(true);
                    }

                    List<VtdPoly> elfPolyList = new ArrayList<>();
                    elfPolyList.add(VtdPoly.getDefault(characterDetails.getId()));

                    final List<Token> allElf = tokenMapper.getNonWeaponSlotItems(null, characterDetails.getId(), characterDetails.getCharacterClass().name(), Slot.POLYMORPH.name(), "ALL");
                    if (allElf != null) {
                        for (Token token : allElf) {
                            final TokenFullDetails tokenDetails = tokenAdminMapper.getTokenDetails(token.getId());
                            if (tokenDetails != null) {
                                elfPolyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                        (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                        tokenDetails, false));
                            }
                        }
                    }

                    if (hasShamansBelt.get()) {
                        final TokenFullDetails belt = tokenAdminMapper.getTokenDetails("e604ae878baea5348138a4b22180b74a34c6ecce");
                        belt.setName("Shaman’s Belt - Air");
                        elfPolyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                20, new ArrayList<>(), belt, false));
                        belt.setName("Shaman’s Belt - Earth");
                        elfPolyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                20, new ArrayList<>(), belt, false));
                        belt.setName("Shaman’s Belt - Fire");
                        elfPolyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                20, new ArrayList<>(), belt, false));
                        belt.setName("Shaman’s Belt - Ice");
                        elfPolyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                20, new ArrayList<>(), belt, false));
                    }

                    vtdMapper.addPolys(elfPolyList);
                    builder.polys(elfPolyList);
                    break;
                case WIZARD:
                    if (madEvoker.get())
                        builder.madEvoker(true);
                    if (hasWizardRelic.get()) {
                        builder.madEvoker(true);
                        builder.swapElements(true);
                        builder.magePower(true);
                    } if (hasWizardLegendary.get()) {
                        builder.madEvoker(true);
                        builder.swapElements(true);
                        builder.magePower(true);
                        builder.archMagePower(true);
                    }

                    List<VtdPoly> wizardPolyList = new ArrayList<>();
                    wizardPolyList.add(VtdPoly.getDefault(characterDetails.getId()));

                    final List<Token> allWizard = tokenMapper.getNonWeaponSlotItems(null, characterDetails.getId(), characterDetails.getCharacterClass().name(), Slot.POLYMORPH.name(), "ALL");
                    if (allWizard != null) {
                        for (Token token : allWizard) {
                            final TokenFullDetails tokenDetails = tokenAdminMapper.getTokenDetails(token.getId());
                            if (tokenDetails != null) {
                                wizardPolyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                        (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                        tokenDetails, false));
                            }
                        }
                    }

                    if (hasShamansBelt.get()) {
                        final TokenFullDetails belt = tokenAdminMapper.getTokenDetails("e604ae878baea5348138a4b22180b74a34c6ecce");
                        belt.setName("Shaman’s Belt - Air");
                        wizardPolyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                20, new ArrayList<>(), belt, false));
                        belt.setName("Shaman’s Belt - Earth");
                        wizardPolyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                20, new ArrayList<>(), belt, false));
                        belt.setName("Shaman’s Belt - Fire");
                        wizardPolyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                20, new ArrayList<>(), belt, false));
                        belt.setName("Shaman’s Belt - Ice");
                        wizardPolyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                20, new ArrayList<>(), belt, false));
                    }

                    vtdMapper.addPolys(wizardPolyList);
                    builder.polys(wizardPolyList);
                    break;
                case MONK:
                    if (mainHand.get() == null) {
                        mainHand.set(TokenFullDetails.builder().name("Fist").monkOffhand(true).damageRange("1,2,3,4,5,6").critMin(20).build());
                    }
                    if (offHand.get() == null && mainHand.get().isMonkOffhand()) {
                        offHand.set(TokenFullDetails.builder().name("Fist").monkOffhand(true).damageRange("1,2,3,4,5,6").critMin(20).build());
                    }

                    if (characterDetails.getStats().getLevel() == 5) {
                        if (mainHand.get().getMeleeHit() < 4)
                            mainHand.get().setMeleeHit(4);
                        if (offHand.get() != null && offHand.get().getMeleeHit() < 4)
                            offHand.get().setMeleeHit(4);
                    }

                    if (hasMonkRelic.get()) {
                        if (mainHand.get().getCritMin() > 19)
                            mainHand.get().setCritMin(19);
                        if (offHand.get() != null && offHand.get().getCritMin() > 19)
                            offHand.get().setCritMin(19);

                        if (characterDetails.getStats().getLevel() == 4) {
                            meleeDmgEffects.add(DamageModEffect.DAZE_19);
                            meleeOffhandDmgEffects.add(DamageModEffect.DAZE_19);
                        } else {
                            meleeDmgEffects.add(DamageModEffect.STUN_19);
                            meleeOffhandDmgEffects.add(DamageModEffect.STUN_19);
                        }
                    } else if (hasMonkLegendary.get()) {
                        if (mainHand.get().getCritMin() > 19)
                            mainHand.get().setCritMin(19);
                        if (offHand.get() != null && offHand.get().getCritMin() > 19)
                            offHand.get().setCritMin(19);

                        if (characterDetails.getStats().getLevel() == 4) {
                            meleeDmgEffects.add(DamageModEffect.DAZE_19);
                            meleeOffhandDmgEffects.add(DamageModEffect.DAZE_19);
                        } else {
                            meleeDmgEffects.add(DamageModEffect.STUN_19);
                            meleeOffhandDmgEffects.add(DamageModEffect.STUN_19);
                        }

                        if (mainHand.get().isMonkOffhand() && offHand.get() != null && offHand.get().isMonkOffhand())
                            meleeDmgEffects.add(DamageModEffect.FURRY_THROW);
                    } else {
                        if (characterDetails.getStats().getLevel() == 4) {
                            meleeDmgEffects.add(DamageModEffect.DAZE_20);
                            meleeOffhandDmgEffects.add(DamageModEffect.DAZE_20);
                        } else {
                            meleeDmgEffects.add(DamageModEffect.STUN_20);
                            meleeOffhandDmgEffects.add(DamageModEffect.STUN_20);
                        }
                    }

                    if (activatePrestige) {
                        if (mainHand.get().isMonkOffhand() && offHand.get() != null && offHand.get().isMonkOffhand())
                            critTypes.add(CritType.ANY);
                        characterDetails.getStats().setInitiative(characterDetails.getStats().getInitiative() + 3);
                    }
                    break;
                case PALADIN:
                    if (hasPaladinRelic.get())
                        inGameEffects.add(InGameEffect.PLUS_10_LOH);
                    else if (hasPaladinLegendary.get())
                        inGameEffects.add(InGameEffect.PLUS_15_LOH);
                    break;
                case RANGER:
                    if (charmShadowShot.get()) {
                        rangeOffHand.set(tokenAdminMapper.getTokenDetails("f2f2a4950f8e1a2415890a370b54efc1605b551a"));
                    }
                    if (hasRangerLegendary.get()) {
                        List<VtdPoly> rangerPolyList = new ArrayList<>();
                        final VtdPoly vtdPoly = VtdPoly.getDefault(characterDetails.getId());
                        vtdPoly.setCompanion(true);
                        rangerPolyList.add(vtdPoly);

                        final List<Token> allRanger = tokenMapper.getNonWeaponSlotItems(null, characterDetails.getId(), "DRUID", Slot.POLYMORPH.name(), "ALL");
                        if (allRanger != null) {
                            for (Token token : allRanger) {
                                final TokenFullDetails tokenDetails = tokenAdminMapper.getTokenDetails(token.getId());
                                if (tokenDetails != null) {
                                    rangerPolyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                            (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                            tokenDetails, true));
                                }
                            }
                        }

                        vtdMapper.addPolys(rangerPolyList);
                        builder.polys(rangerPolyList);
                    }
                    break;
                case ROGUE:
                    int meleeSneakHit = 0;
                    int meleeSneakDamage = 0;
                    int meleeSneakCritMin = 20;
                    int rangeSneakHit = 0;
                    int rangeSneakDamage = 0;
                    int rangeSneakCritMin = 20;
                    int unmodifiableSneakDamage = 0;
                    boolean isSneakCanCrit = false;
                    boolean isSneakAtRange = false;

                    long viperCount = characterDetails.getItems().stream().filter((item) -> item.getItemId() != null && (item.getItemId().equals("4b469b628a8c57e294268dfac4b51d302b1e9123") || item.getItemId().equals("9431d39ad2fba9953bf4b526d86f41f37022efeb") || item.getItemId().equals("f2ff2a508dd3075633ca2fd9e58c0e1a76088af8") || item.getItemId().equals("dd565d74807cc9094990b324465612d52b3070bf") || item.getItemId().equals("09ad5527813c4f087f3123cd6a40404b9377a4bc"))).count();
                    viperCount += characterDetails.getItems().stream().filter((item) -> item.getItemId() != null && item.getItemId().equals("bd21afd63114346decea5fc899ff697106e99429")).map(CharacterItem::getName).distinct().count();

                    for (CharacterItem characterItem : characterDetails.getItems()) {
                        if (characterItem != null && characterItem.getName() != null) {
                            if (characterItem.getName().equalsIgnoreCase("+1 Assassin’s Blade")) {
                                meleeSneakDamage += 3;
                            } else if (characterItem.getName().equalsIgnoreCase("Boots of Backstabbing")) {
                                unmodifiableSneakDamage += 6;
                            } else if (characterItem.getName().equalsIgnoreCase("Boots of the Underdark")) {
                                meleeSneakHit += 2;
                                rangeSneakHit += 2;
                            } else if (characterItem.getName().equalsIgnoreCase("Cloak of the Footpad")) {
                                meleeSneakDamage += 2;
                                rangeSneakDamage += 2;
                            } else if (characterItem.getName().equalsIgnoreCase("Drow Death Armor")) {
                                meleeSneakHit += 1;
                                rangeSneakHit += 1;
                            } else if (characterItem.getName().equalsIgnoreCase("Gloves of Stabbing")) {
                                meleeSneakHit += 2;
                                rangeSneakHit += 2;
                            } else if (characterItem.getName().equalsIgnoreCase("Gloves of the Footpad")) {
                                meleeSneakHit += 1;
                                rangeSneakHit += 1;
                            } else if (characterItem.getName().equalsIgnoreCase("Lenses of Agility")) {
                                if (rangeMainHand.get() != null && rangeMainHand.get().getName().toLowerCase().contains("bow"))
                                    isSneakAtRange = true;
                            } else if (characterItem.getName().equalsIgnoreCase("Lenses of Vital Insight")) {
                                isSneakAtRange = true;
                            } else if (characterItem.getName().equalsIgnoreCase("Necklace of the Sneak")) {
                                meleeSneakCritMin = 19;
                                rangeSneakCritMin = 19;
                            } else if (characterItem.getName().equalsIgnoreCase("Nightshade’s +2 Throwing Dagger")) {
                                isSneakCanCrit = true;
                                isSneakAtRange = true;
                            } else if (characterItem.getName().equalsIgnoreCase("Nightshade’s +2 Short Sword")) {
                                isSneakCanCrit = true;
                            } else if (characterItem.getName().equalsIgnoreCase("Shoes of Sneaking")) {
                                meleeSneakHit += 2;
                                rangeSneakHit += 2;
                            } else if (characterItem.getName().equalsIgnoreCase("Raphiel’s Sneaky Necklace")) {
                                meleeSneakCritMin = 17;
                                rangeSneakCritMin = 17;
                            } else if (characterItem.getName().equalsIgnoreCase("+2 Viper Strike Fang") && viperCount >= 3) {
                                isSneakCanCrit = true;
                            } else if (characterItem.getName().equalsIgnoreCase("+3 Viper Strike Fang") && viperCount >= 3) {
                                isSneakCanCrit = true;
                            } else if (characterItem.getName().equalsIgnoreCase("Asher’s +5 Viper Strike Fang") && viperCount >= 3) {
                                isSneakCanCrit = true;
                            }
                        }
                    }

                    builder.meleeSneakHit(meleeSneakHit)
                            .meleeSneakDamage(meleeSneakDamage)
                            .meleeSneakCritMin(meleeSneakCritMin)
                            .rangeSneakHit(rangeSneakHit)
                            .rangeSneakDamage(rangeSneakDamage)
                            .rangeSneakCritMin(rangeSneakCritMin)
                            .unmodifiableSneakDamage(unmodifiableSneakDamage)
                            .isSneakCanCrit(isSneakCanCrit)
                            .isSneakAtRange(isSneakAtRange);

                    if (activatePrestige) {
                        builder.meleeWeaponSecondaryExplodeRange("6")
                                .meleeWeaponSecondaryExplodeEffect(WeaponExplodeCondition.NATURAL_20)
                                .meleeWeaponSecondaryExplodeText("Assassinate - You have instantly slain the monster!!!");
                    }

                    break;
            }

            if (hasMedallionKeenness.get() && mainHand.get() != null && mainHand.get().getCritMin() > 19)
                mainHand.get().setCritMin(19);

            if (hasAmuletOfAiming.get() && mainHand.get().getCritMin() > 19)
                mainHand.get().setCritMin(19);

            if (mainHand.get() != null && mainHand.get().getWeaponExplodeCondition() != null) {
                final DamageModEffect damageModEffect = mainHand.get().getWeaponExplodeCondition().getDamageModEffect(characterDetails.getCharacterClass());
                if (damageModEffect != null)
                    meleeDmgEffects.add(damageModEffect);
            }
            if (offHand.get() != null && offHand.get().getWeaponExplodeCondition() != null) {
                final DamageModEffect damageModEffect = offHand.get().getWeaponExplodeCondition().getDamageModEffect(characterDetails.getCharacterClass());
                if (damageModEffect != null)
                    meleeOffhandDmgEffects.add(damageModEffect);
            }
            if (rangeMainHand.get() != null && rangeMainHand.get().getWeaponExplodeCondition() != null) {
                final DamageModEffect damageModEffect = rangeMainHand.get().getWeaponExplodeCondition().getDamageModEffect(characterDetails.getCharacterClass());
                if (damageModEffect != null)
                    rangeDmgEffects.add(damageModEffect);
            }
            if (rangeOffHand.get() != null && rangeOffHand.get().getWeaponExplodeCondition() != null) {
                final DamageModEffect damageModEffect = rangeOffHand.get().getWeaponExplodeCondition().getDamageModEffect(characterDetails.getCharacterClass());
                if (damageModEffect != null)
                    rangeOffhandDmgEffects.add(damageModEffect);
            }

            final VtdSetting aDefault = vtdAdminMapper.getAdventure("default");
            final List<VtdRoom> roomsByNumber = vtdAdminMapper.getRoomsByNumber(aDefault.getId(), 1);

            int meleeMainHit = mainHand.get() != null ? mainHand.get().getMeleeHit() : 0;
            int meleeOffhandHit = offHand.get() != null ? offHand.get().getMeleeHit() : 0;
            int rangeMainHit = rangeMainHand.get() != null ? rangeMainHand.get().getRangeHit() : 0;
            int rangeOffhandHit = rangeOffHand.get() != null ? rangeOffHand.get().getRangeHit() : 0;

            vtdMapper.deleteCharacterDebuffs(characterDetails.getId());
            Debuff.getAll(characterDetails.getId()).forEach(vtdDebuff -> {
                vtdMapper.addCharacterDebuff(vtdDebuff);
            });

            if (crownOfElements.get())
                builder.swapElements(true);

            vtdDetails = builder
                    .characterId(characterDetails.getId())
                    .characterOrigId(origId)
                    .userId(characterDetails.getUserId())
                    .expires(new Date(new Date().getTime() + 86400000))
                    .name(characterDetails.getName())
                    .adventureId(aDefault.getId())
                    .adventureName(aDefault.getName())
                    .rollerId(null)
                    .characterClass(characterDetails.getCharacterClass())
                    .stats(characterDetails.getStats())
                    .currentHealth(characterDetails.getStats().getHealth())
                    .rollerDifficulty(0)
                    .initBonus(0)
                    .roomNumber(0)
                    .meleeMainHit(meleeMainHit)
                    .meleeOffhandHit(meleeOffhandHit)
                    .rangeMainHit(rangeMainHit)
                    .rangeOffhandHit(rangeOffhandHit)
                    .prestigeAvailable(hasPrestigeClass.get())
                    .prestigeActive(activatePrestige)
                    .questers(hasQuestors.get())
                    .critTypes(String.join(",", critTypes.stream().map(Enum::name).collect(Collectors.toList())))
                    .monsters(VtdMonster.fromRoom(roomsByNumber, critTypes, meleeMainHit, meleeOffhandHit, rangeMainHit, rangeOffhandHit, characterDetails.getCharacterClass() == CharacterClass.RANGER, characterDetails.getStats().getLevel() == 5))
                    .availableEffects(String.join(",", inGameEffects.stream().map(Enum::name).collect(Collectors.toList())))
                    .notes(characterDetails.getNotes())
                    .items(characterDetails.getItems())
                    .characterSkills(characterSkills)
                    .buffs(new ArrayList<>())
                    .debuffs(vtdMapper.getCharacterDebuffs(characterDetails.getId()))
                    .meleeDmgRange((mainHand.get() != null) ? mainHand.get().getDamageRange() : "0")
                    .meleeOffhandDmgRange((offHand.get() != null) ? offHand.get().getDamageRange() : (characterDetails.getCharacterClass() ==  CharacterClass.RANGER) ? "0" : null)
                    .rangeDmgRange((rangeMainHand.get() != null) ? rangeMainHand.get().getDamageRange() : null)
                    .rangeOffhandDmgRange((rangeOffHand.get() != null) ? rangeOffHand.get().getDamageRange() : null)
                    .meleeWeaponExplodeRange((mainHand.get() != null) ? mainHand.get().getDamageExplodeRange() : null)
                    .meleeOffhandWeaponExplodeRange((offHand.get() != null) ? offHand.get().getDamageExplodeRange() : null)
                    .rangeWeaponExplodeRange((rangeMainHand.get() != null) ? rangeMainHand.get().getDamageExplodeRange() : null)
                    .meleeWeaponExplodeEffect((mainHand.get() != null) ? mainHand.get().getWeaponExplodeCondition() : null)
                    .meleeOffhandWeaponExplodeEffect((offHand.get() != null) ? offHand.get().getWeaponExplodeCondition() : null)
                    .rangeWeaponExplodeEffect((rangeMainHand.get() != null) ? rangeMainHand.get().getWeaponExplodeCondition() : null)
                    .meleeWeaponExplodeText((mainHand.get() != null) ? mainHand.get().getWeaponExplodeText() : null)
                    .meleeOffhandWeaponExplodeText((offHand.get() != null) ? offHand.get().getWeaponExplodeText() : null)
                    .rangeWeaponExplodeText((rangeMainHand.get() != null) ? rangeMainHand.get().getWeaponExplodeText() : null)
                    .meleeDmgEffects(String.join(",", meleeDmgEffects.stream().map(Enum::name).collect(Collectors.toList())))
                    .meleeOffhandDmgEffects(String.join(",", meleeOffhandDmgEffects.stream().map(Enum::name).collect(Collectors.toList())))
                    .rangeDmgEffects(String.join(",", rangeDmgEffects.stream().map(Enum::name).collect(Collectors.toList())))
                    .rangeOffhandDmgEffects(String.join(",", rangeOffhandDmgEffects.stream().map(Enum::name).collect(Collectors.toList())))
                    .meleeCritMin((mainHand.get() != null) ? mainHand.get().getCritMin() : 20)
                    .meleeOffhandCritMin((offHand.get() != null) ? offHand.get().getCritMin() : 20)
                    .meleePolyCritMin(20)
                    .rangeCritMin((rangeMainHand.get() != null) ? rangeMainHand.get().getCritMin() : 20)
                    .build();

            vtdMapper.addCharacter(vtdDetails);
            vtdMapper.addCharacterStats(characterDetails.getStats());
        } else {
            if(!(userPrincipalProvider.getUserPrincipal().getSub() != null && userPrincipalProvider.getUserPrincipal().getSub().equalsIgnoreCase(vtdDetails.getUserId())))
                throw new InvalidDataException("Virtual True Dungeon is only for your own characters.");

            vtdDetails.setExpires(new Date(new Date().getTime() + 86400000));
            vtdMapper.updateCharacter(vtdDetails);

            vtdDetails = calculateStats(id);
        }

        return vtdDetails;
    }

    @Override
    public VtdDetails modifyDifficulty(String id, int difficulty) {
        final VtdDetails vtdDetails = vtdMapper.getCharacter(id);

        if(!(userPrincipalProvider.getUserPrincipal().getSub() != null && userPrincipalProvider.getUserPrincipal().getSub().equalsIgnoreCase(vtdDetails.getUserId())))
            throw new InvalidDataException("Virtual True Dungeon is only for your own characters.");

        vtdDetails.setRollerDifficulty(difficulty);
        vtdMapper.updateCharacter(vtdDetails);

        return calculateStats(id);
    }

    @Override
    public VtdDetails setBonusInit(String id, int init) {
        final VtdDetails vtdDetails = vtdMapper.getCharacter(id);

        if(!(userPrincipalProvider.getUserPrincipal().getSub() != null && userPrincipalProvider.getUserPrincipal().getSub().equalsIgnoreCase(vtdDetails.getUserId())))
            throw new InvalidDataException("Virtual True Dungeon is only for your own characters.");

        vtdDetails.setInitBonus(init);
        vtdMapper.updateCharacter(vtdDetails);

        return calculateStats(id);
    }

    @Override
    public VtdDetails setBonusHealth(String id, int health) {
        final VtdDetails vtdDetails = vtdMapper.getCharacter(id);
        final CharacterStats characterStats = vtdMapper.getCharacterStats(vtdDetails.getCharacterId());

        if(!(userPrincipalProvider.getUserPrincipal().getSub() != null && userPrincipalProvider.getUserPrincipal().getSub().equalsIgnoreCase(vtdDetails.getUserId())))
            throw new InvalidDataException("Virtual True Dungeon is only for your own characters.");

        vtdDetails.setHealthBonus(health);

        if (vtdDetails.getCurrentHealth() >= characterStats.getHealth())
            vtdDetails.setCurrentHealth(characterStats.getHealth() + vtdDetails.getHealthBonus());
        if (vtdDetails.getCurrentHealth() > (characterStats.getHealth() + vtdDetails.getHealthBonus()))
            vtdDetails.setCurrentHealth(characterStats.getHealth() + vtdDetails.getHealthBonus());

        vtdMapper.updateCharacter(vtdDetails);

        return calculateStats(id);
    }

    @Override
    public VtdDetails setBonusBraceletCabal(String id, int bonus) {
        final VtdDetails vtdDetails = vtdMapper.getCharacter(id);

        if(!(userPrincipalProvider.getUserPrincipal().getSub() != null && userPrincipalProvider.getUserPrincipal().getSub().equalsIgnoreCase(vtdDetails.getUserId())))
            throw new InvalidDataException("Virtual True Dungeon is only for your own characters.");

        if (bonus < 1)
            bonus = 1;

        if (vtdDetails.getBraceletCabalBonus() > 0) {
            vtdDetails.setBraceletCabalBonus(bonus);
            vtdMapper.updateCharacter(vtdDetails);
        }

        return calculateStats(id);
    }

    @Override
    public VtdDetails setBonusGloveCabal(String id, int bonus) {
        final VtdDetails vtdDetails = vtdMapper.getCharacter(id);

        if(!(userPrincipalProvider.getUserPrincipal().getSub() != null && userPrincipalProvider.getUserPrincipal().getSub().equalsIgnoreCase(vtdDetails.getUserId())))
            throw new InvalidDataException("Virtual True Dungeon is only for your own characters.");

        if (bonus < 1)
            bonus = 1;

        if (vtdDetails.getGlovesCabalBonus() > 0) {
            vtdDetails.setGlovesCabalBonus(bonus);
            vtdMapper.updateCharacter(vtdDetails);
        }

        return calculateStats(id);
    }

    @Override
    public VtdDetails setBonusCharmCabal(String id, int bonus) {
        final VtdDetails vtdDetails = vtdMapper.getCharacter(id);

        if(!(userPrincipalProvider.getUserPrincipal().getSub() != null && userPrincipalProvider.getUserPrincipal().getSub().equalsIgnoreCase(vtdDetails.getUserId())))
            throw new InvalidDataException("Virtual True Dungeon is only for your own characters.");

        if (bonus < 1)
            bonus = 1;

        if (vtdDetails.getCharmCabalBonus() > 0) {
            vtdDetails.setCharmCabalBonus(bonus);
            vtdMapper.updateCharacter(vtdDetails);
        }

        return calculateStats(id);
    }

    @Override
    public VtdDetails modifyHealth(String id, int health) {
        final VtdDetails vtdDetails = vtdMapper.getCharacter(id);
        final CharacterStats characterStats = vtdMapper.getCharacterStats(vtdDetails.getCharacterId());

        if(!(userPrincipalProvider.getUserPrincipal().getSub() != null && userPrincipalProvider.getUserPrincipal().getSub().equalsIgnoreCase(vtdDetails.getUserId())))
            throw new InvalidDataException("Virtual True Dungeon is only for your own characters.");

        vtdDetails.setCurrentHealth(vtdDetails.getCurrentHealth() + health);

        if (vtdDetails.getCurrentHealth() > (characterStats.getHealth() + vtdDetails.getHealthBonus()))
            vtdDetails.setCurrentHealth(characterStats.getHealth() + vtdDetails.getHealthBonus());
        else if (vtdDetails.getCurrentHealth() < 0 && !(vtdDetails.getCharacterClass() == CharacterClass.BARBARIAN && vtdDetails.isPrestigeActive()))
            vtdDetails.setCurrentHealth(0);

        vtdMapper.updateCharacter(vtdDetails);

        return calculateStats(id);
    }

    @Override
    public VtdDetails useSkill(String id, String skillId, boolean selfTarget, int selfHeal, boolean madEvoker, int lohNumber, InGameEffect inGameEffect, boolean markUse, boolean ignoreUse) {
        final CharacterSkill skill = vtdMapper.getCharacterSkill(skillId, id);
        final VtdDetails character = vtdMapper.getCharacter(id);

        if(!(userPrincipalProvider.getUserPrincipal().getSub() != null && userPrincipalProvider.getUserPrincipal().getSub().equalsIgnoreCase(character.getUserId())))
            throw new InvalidDataException("Virtual True Dungeon is only for your own characters.");

        if (skill.getUsedNumber() >= skill.getUsableNumber())
            return calculateStats(id);

        if (skill.getName().equalsIgnoreCase("Lay on Hands")) {
            if (lohNumber < 1)
                lohNumber = 1;
            else if (lohNumber > (skill.getUsableNumber() - skill.getUsedNumber()))
                lohNumber = skill.getUsableNumber() - skill.getUsedNumber();
            skill.setUsedNumber(skill.getUsedNumber() + lohNumber);
        } else if (markUse)
            skill.setUsedNumber(skill.getUsedNumber() + 1);
        vtdMapper.updateCharacterSkill(skill);

        if (character.getCharacterClass() == CharacterClass.BARD || (character.getCharacterClass() == CharacterClass.WIZARD && character.isPrestigeActive())) {
            for (CharacterSkill characterSkill : vtdMapper.getCharacterSkills(id)) {
                if (!characterSkill.getId().equals(skillId) && characterSkill.getSkillLevel() == skill.getSkillLevel()) {
                    characterSkill.setUsedNumber(skill.getUsedNumber());
                    vtdMapper.updateCharacterSkill(characterSkill);
                }
            }
        }

        if (inGameEffect != null && inGameEffect != InGameEffect.NONE && !ignoreUse) {
            VtdDetails vtdDetails = vtdMapper.getCharacter(id);

            if (vtdDetails != null && vtdDetails.getAvailableEffects() != null && !vtdDetails.getAvailableEffects().isEmpty()) {
                final List<InGameEffect> inGameEffects = Arrays.stream(vtdDetails.getAvailableEffects().split(",")).map(InGameEffect::valueOf).collect(Collectors.toList());
                inGameEffects.remove(inGameEffect);
                vtdDetails.setAvailableEffects(String.join(",", inGameEffects.stream().map(Enum::name).collect(Collectors.toList())));

                vtdMapper.updateCharacter(vtdDetails);
            }
        }

        if (!ignoreUse) {
            if (skill.getSkillType() == SkillType.BUFF && (skill.getSkillTarget() == SkillTarget.SELF || skill.getSkillTarget() == SkillTarget.PARTY || (skill.getSkillTarget() == SkillTarget.ANY && selfTarget))) {
                return addBuff(id, Buff.getBuff(skill.getName()), 0);
            } else if (skill.getSkillType() == SkillType.HEAL && selfTarget && selfHeal > 0) {
                final VtdDetails vtdDetails = calculateStats(id);

                vtdDetails.setCurrentHealth(vtdDetails.getCurrentHealth() + selfHeal);
                if (vtdDetails.getCurrentHealth() > (vtdDetails.getStats().getHealth()))
                    vtdDetails.setCurrentHealth(vtdDetails.getStats().getHealth());

                vtdMapper.updateCharacter(vtdDetails);
            } else if ((skill.getSkillType() == SkillType.DAMAGE || skill.getSkillType() == SkillType.DAMAGE_RANGE_AC_15) && madEvoker) {
                final VtdDetails vtdDetails = calculateStats(id);

                if (vtdDetails.getCharacterClass() == CharacterClass.ELF_WIZARD || vtdDetails.getCharacterClass() == CharacterClass.WIZARD) {
                    vtdDetails.setCurrentHealth(vtdDetails.getCurrentHealth() - 25);
                    if (vtdDetails.getCurrentHealth() < 0)
                        vtdDetails.setCurrentHealth(0);

                    vtdMapper.updateCharacter(vtdDetails);
                }
            } else if (character.getCharacterClass() == CharacterClass.PALADIN && skill.getName().equalsIgnoreCase("Sacrifice")) {
                character.setCurrentHealth(5);
                vtdMapper.updateCharacter(character);
            }
        }

        return calculateStats(id);
    }

    @Override
    public VtdDetails queueSkill(String id, String skillId, boolean selfTarget, int selfHeal, boolean madEvoker, int lohNumber, InGameEffect inGameEffect, boolean markUse, boolean ignoreUse, int damage) {
        final VtdDetails vtdDetails = vtdMapper.getCharacter(id);

        if(!(userPrincipalProvider.getUserPrincipal().getSub() != null && userPrincipalProvider.getUserPrincipal().getSub().equalsIgnoreCase(vtdDetails.getUserId())))
            throw new InvalidDataException("Virtual True Dungeon is only for your own characters.");

        final CharacterSkill characterSkill = vtdMapper.getCharacterSkill(skillId, id);

        vtdMapper.queueCharacterSkill(QueuedSkill.builder()
                .id(id)
                .skillId(skillId)
                .skillName(characterSkill.getName())
                .selfTarget(selfTarget)
                .selfHeal(selfHeal)
                .madEvoker(madEvoker)
                .lohNumber(lohNumber)
                .inGameEffect(inGameEffect)
                .markUse(markUse)
                .ignoreUse(ignoreUse)
                .damage(damage)
                .build());

        return calculateStats(id);
    }

    @Override
    public VtdDetails execSkillQueue(String id) {
        final VtdDetails vtdDetails = vtdMapper.getCharacter(id);

        if(!(userPrincipalProvider.getUserPrincipal().getSub() != null && userPrincipalProvider.getUserPrincipal().getSub().equalsIgnoreCase(vtdDetails.getUserId())))
            throw new InvalidDataException("Virtual True Dungeon is only for your own characters.");

        final List<QueuedSkill> queuedSkills = vtdMapper.getQueuedSkills(id);

        if (queuedSkills == null || queuedSkills.isEmpty()) {
            vtdDetails.setTotalDamageLastSpell(0);
            vtdMapper.updateCharacter(vtdDetails);
            return calculateStats(id);
        }

        final Map<String, CharacterSkill> skillMap = new HashMap<>();
        final Map<SkillLevel, Integer> skillUseMap = new HashMap<>();
        int totalDamage = 0;

        for (QueuedSkill queuedSkill : queuedSkills) {
            if (!skillMap.containsKey(queuedSkill.getSkillId()))
                skillMap.put(queuedSkill.getSkillId(),  vtdMapper.getCharacterSkill(queuedSkill.getSkillId(), id));
            if (!skillUseMap.containsKey(skillMap.get(queuedSkill.getSkillId()).getSkillLevel()))
                skillUseMap.put(skillMap.get(queuedSkill.getSkillId()).getSkillLevel(), skillMap.get(queuedSkill.getSkillId()).getUsedNumber());

            if (skillMap.get(queuedSkill.getSkillId()).getUsedNumber() >= skillMap.get(queuedSkill.getSkillId()).getUsableNumber())
                continue;
            if ((vtdDetails.getCharacterClass() == CharacterClass.BARD || (vtdDetails.getCharacterClass() == CharacterClass.WIZARD && vtdDetails.isPrestigeActive())) &&
                    skillUseMap.get(skillMap.get(queuedSkill.getSkillId()).getSkillLevel()) >= skillMap.get(queuedSkill.getSkillId()).getUsableNumber())
                continue;

            if (skillMap.get(queuedSkill.getSkillId()).getName().equalsIgnoreCase("Lay on Hands")) {
                if (queuedSkill.getLohNumber() < 1)
                    queuedSkill.setLohNumber(1);
                else if (queuedSkill.getLohNumber() > (skillMap.get(queuedSkill.getSkillId()).getUsableNumber() - skillMap.get(queuedSkill.getSkillId()).getUsedNumber()))
                    queuedSkill.setLohNumber(skillMap.get(queuedSkill.getSkillId()).getUsableNumber() - skillMap.get(queuedSkill.getSkillId()).getUsedNumber());
                skillMap.get(queuedSkill.getSkillId()).setUsedNumber(skillMap.get(queuedSkill.getSkillId()).getUsedNumber() + queuedSkill.getLohNumber());
            } else if (queuedSkill.isMarkUse()) {
                skillMap.get(queuedSkill.getSkillId()).setUsedNumber(skillMap.get(queuedSkill.getSkillId()).getUsedNumber() + 1);
                skillUseMap.replace(skillMap.get(queuedSkill.getSkillId()).getSkillLevel(), skillUseMap.get(skillMap.get(queuedSkill.getSkillId()).getSkillLevel()) + 1);
            }

            if (queuedSkill.getInGameEffect() != null && queuedSkill.getInGameEffect() != InGameEffect.NONE && !queuedSkill.isIgnoreUse()) {
                if (vtdDetails.getAvailableEffects() != null && !vtdDetails.getAvailableEffects().isEmpty()) {
                    final List<InGameEffect> inGameEffects = Arrays.stream(vtdDetails.getAvailableEffects().split(",")).map(InGameEffect::valueOf).collect(Collectors.toList());
                    inGameEffects.remove(queuedSkill.getInGameEffect());
                    vtdDetails.setAvailableEffects(String.join(",", inGameEffects.stream().map(Enum::name).collect(Collectors.toList())));
                }
            }

            if (!queuedSkill.isIgnoreUse()) {
                if (skillMap.get(queuedSkill.getSkillId()).getSkillType() == SkillType.BUFF && (skillMap.get(queuedSkill.getSkillId()).getSkillTarget() == SkillTarget.SELF || skillMap.get(queuedSkill.getSkillId()).getSkillTarget() == SkillTarget.PARTY || (skillMap.get(queuedSkill.getSkillId()).getSkillTarget() == SkillTarget.ANY && queuedSkill.isSelfTarget()))) {
                    return addBuff(id, Buff.getBuff(skillMap.get(queuedSkill.getSkillId()).getName()), 0);
                } else if (skillMap.get(queuedSkill.getSkillId()).getSkillType() == SkillType.HEAL && queuedSkill.isSelfTarget() && queuedSkill.getSelfHeal() > 0) {
                    vtdDetails.setCurrentHealth(vtdDetails.getCurrentHealth() + queuedSkill.getSelfHeal());
                    if (vtdDetails.getCurrentHealth() > (vtdDetails.getStats().getHealth()))
                        vtdDetails.setCurrentHealth(vtdDetails.getStats().getHealth());
                } else if ((skillMap.get(queuedSkill.getSkillId()).getSkillType() == SkillType.DAMAGE || skillMap.get(queuedSkill.getSkillId()).getSkillType() == SkillType.DAMAGE_RANGE_AC_15) && queuedSkill.isMadEvoker()) {
                    if (vtdDetails.getCharacterClass() == CharacterClass.ELF_WIZARD || vtdDetails.getCharacterClass() == CharacterClass.WIZARD) {
                        vtdDetails.setCurrentHealth(vtdDetails.getCurrentHealth() - 25);
                        if (vtdDetails.getCurrentHealth() < 0)
                            vtdDetails.setCurrentHealth(0);
                    }
                } else if (vtdDetails.getCharacterClass() == CharacterClass.PALADIN && skillMap.get(queuedSkill.getSkillId()).getName().equalsIgnoreCase("Sacrifice")) {
                    vtdDetails.setCurrentHealth(5);
                }

                if (skillMap.get(queuedSkill.getSkillId()).getSkillType() == SkillType.DAMAGE || skillMap.get(queuedSkill.getSkillId()).getSkillType() == SkillType.DAMAGE_RANGE_AC_15)
                    totalDamage += queuedSkill.getDamage();
            }
        }

        vtdDetails.setTotalDamageLastSpell(totalDamage);

        vtdMapper.updateCharacter(vtdDetails);

        if (vtdDetails.getCharacterClass() == CharacterClass.BARD || (vtdDetails.getCharacterClass() == CharacterClass.WIZARD && vtdDetails.isPrestigeActive())) {
            for (CharacterSkill characterSkill : skillMap.values()) {
                characterSkill.setUsedNumber(skillUseMap.get(characterSkill.getSkillLevel()));
            }
        }

        for (CharacterSkill characterSkill : skillMap.values()) {
            vtdMapper.updateCharacterSkill(characterSkill);
        }

        return calculateStats(id);
    }

    @Override
    public VtdDetails unuseSkill(String id, String skillId) {
        final CharacterSkill skill = vtdMapper.getCharacterSkill(skillId, id);
        final VtdDetails character = vtdMapper.getCharacter(id);

        if(!(userPrincipalProvider.getUserPrincipal().getSub() != null && userPrincipalProvider.getUserPrincipal().getSub().equalsIgnoreCase(character.getUserId())))
            throw new InvalidDataException("Virtual True Dungeon is only for your own characters.");

        if (skill.getUsedNumber() > 0) {
            skill.setUsedNumber(skill.getUsedNumber() - 1);
            vtdMapper.updateCharacterSkill(skill);

            if (character.getCharacterClass() == CharacterClass.BARD || (character.getCharacterClass() == CharacterClass.WIZARD && character.isPrestigeActive())) {
                for (CharacterSkill characterSkill : vtdMapper.getCharacterSkills(id)) {
                    if (!characterSkill.getId().equals(skillId) && characterSkill.getSkillLevel() == skill.getSkillLevel()) {
                        characterSkill.setUsedNumber(skill.getUsedNumber());
                        vtdMapper.updateCharacterSkill(characterSkill);
                    }
                }
            }
        }

        if (skill.getSkillType() == SkillType.BUFF && (skill.getSkillTarget() == SkillTarget.SELF || skill.getSkillTarget() == SkillTarget.ANY || skill.getSkillTarget() == SkillTarget.PARTY)) {
            return removeBuff(id, Buff.getBuff(skill.getName()));
        }

        return calculateStats(id);
    }

    @Override
    public VtdDetails addBuff(String id, Buff buff, int level) {
        if (buff != null) {
            final boolean buffExists = vtdMapper.buffExists(id, buff);
            if (!buffExists || (level > 0 && buff.isCanBeLeveled())) {
                if (buffExists)
                    vtdMapper.deleteCharacterBuff(id, buff);
                vtdMapper.addCharacterBuff(VtdBuff.builder().characterId(id).bardsong(buff.isBardsong()).buff(buff).level(level).build());
            }
        }

        return calculateStats(id);
    }

    @Override
    public VtdDetails addDebuff(String id, Debuff debuff) {
        if (debuff != null) {
            final List<VtdDebuff> characterDebuffs = vtdMapper.getCharacterDebuffs(id);

            for (VtdDebuff characterDebuff : characterDebuffs) {
                if (characterDebuff.getDebuff() == debuff) {
                    characterDebuff.setLevel(characterDebuff.getLevel() + 1);
                    vtdMapper.deleteCharacterDebuff(id, debuff);
                    vtdMapper.addCharacterDebuff(characterDebuff);
                }
            }
        }

        return calculateStats(id);
    }

    @Override
    public VtdDetails removeBuff(String id, Buff buff) {
        if (buff != null) {
            vtdMapper.deleteCharacterBuff(id, buff);
        }

        return calculateStats(id);
    }

    @Override
    public VtdDetails removeDebuff(String id, Debuff debuff) {
        if (debuff != null) {
            final List<VtdDebuff> characterDebuffs = vtdMapper.getCharacterDebuffs(id);

            for (VtdDebuff characterDebuff : characterDebuffs) {
                if (characterDebuff.getDebuff() == debuff) {
                    characterDebuff.setLevel(characterDebuff.getLevel() - 1);
                    if (characterDebuff.getLevel() < 0)
                        characterDebuff.setLevel(0);
                    vtdMapper.deleteCharacterDebuff(id, debuff);
                    vtdMapper.addCharacterDebuff(characterDebuff);
                }
            }
        }

        return calculateStats(id);
    }

    @Override
    public VtdDetails removeEffect(String id, InGameEffect inGameEffect) {
        VtdDetails vtdDetails = vtdMapper.getCharacter(id);

        if (vtdDetails != null && vtdDetails.getAvailableEffects() != null && !vtdDetails.getAvailableEffects().isEmpty()) {
            final List<InGameEffect> inGameEffects = Arrays.stream(vtdDetails.getAvailableEffects().split(",")).map(InGameEffect::valueOf).collect(Collectors.toList());
            inGameEffects.remove(inGameEffect);
            vtdDetails.setAvailableEffects(String.join(",", inGameEffects.stream().map(Enum::name).collect(Collectors.toList())));

            vtdMapper.updateCharacter(vtdDetails);
        }

        return calculateStats(id);
    }

    @Override
    public VtdDetails previousRoom(String id) {
        VtdDetails vtdDetails = vtdMapper.getCharacter(id);

        if (vtdDetails == null)
            vtdDetails = getVtdCharacter(id, false, false);
        if(!(userPrincipalProvider.getUserPrincipal().getSub() != null && userPrincipalProvider.getUserPrincipal().getSub().equalsIgnoreCase(vtdDetails.getUserId())))
            throw new InvalidDataException("Virtual True Dungeon is only for your own characters.");

        vtdDetails.setStats(vtdMapper.getCharacterStats(vtdDetails.getCharacterId()));

        if (vtdDetails.getRoomNumber() > 1)
            vtdDetails.setRoomNumber(vtdDetails.getRoomNumber() - 1);

        final List<CharacterSkill> characterSkills = vtdMapper.getCharacterSkills(vtdDetails.getCharacterId());

        if (characterSkills != null) {
            characterSkills.forEach(characterSkill -> {
                if (characterSkill.isOncePerRoom() && characterSkill.getUsedNumber() > 0) {
                    characterSkill.setUsedNumber(0);
                    vtdMapper.updateCharacterSkill(characterSkill);
                }
            });
        }

        vtdMapper.updateCharacter(vtdDetails);
        vtdMapper.resetCharacterBuffs(id);

        return calculateStats(id);
    }

    @Override
    public VtdDetails nextRoom(String id) {
        VtdDetails vtdDetails = vtdMapper.getCharacter(id);

        if (vtdDetails == null)
            vtdDetails = getVtdCharacter(id, false, false);
        if(!(userPrincipalProvider.getUserPrincipal().getSub() != null && userPrincipalProvider.getUserPrincipal().getSub().equalsIgnoreCase(vtdDetails.getUserId())))
            throw new InvalidDataException("Virtual True Dungeon is only for your own characters.");

        vtdDetails.setStats(vtdMapper.getCharacterStats(vtdDetails.getCharacterId()));

        if (vtdDetails.getRoomNumber() < 7)
            vtdDetails.setRoomNumber(vtdDetails.getRoomNumber() + 1);

        if (vtdDetails.getCurrentHealth() > 0) {
            vtdDetails.setCurrentHealth(vtdDetails.getCurrentHealth() + vtdDetails.getStats().getRegen());

            if (vtdDetails.getCurrentHealth() > (vtdDetails.getStats().getHealth() + vtdDetails.getHealthBonus()))
                vtdDetails.setCurrentHealth(vtdDetails.getStats().getHealth() + vtdDetails.getHealthBonus());
        }

        final List<CharacterSkill> characterSkills = vtdMapper.getCharacterSkills(vtdDetails.getCharacterId());

        if (characterSkills != null) {
            if (vtdDetails.getRoomNumber() == 1) {
                characterSkills.forEach(characterSkill -> {
                    if (characterSkill.getUsedNumber() > 0) {
                        characterSkill.setUsedNumber(0);
                        vtdMapper.updateCharacterSkill(characterSkill);
                    }
                });
            } else {
                characterSkills.forEach(characterSkill -> {
                    if (characterSkill.isOncePerRoom() && characterSkill.getUsedNumber() > 0) {
                        characterSkill.setUsedNumber(0);
                        vtdMapper.updateCharacterSkill(characterSkill);
                    }
                });
            }
        }

        if (vtdDetails.getRoomNumber() == 1)
            vtdDetails.setCurrentHealth(vtdDetails.getStats().getHealth() + vtdDetails.getHealthBonus());

        vtdMapper.updateCharacter(vtdDetails);
        vtdMapper.resetCharacterBuffs(id);
        vtdMapper.deleteQueuedSkills(id);

        if (vtdDetails.getCharacterClass() == CharacterClass.FIGHTER && vtdDetails.getStats().getLevel() == 5) {
            addBuff(id, Buff.FIGHTER_REROLL, 0);
        }

        return calculateStats(id);
    }

    @Override
    public VtdDetails setPoly(String id, String polyId) {
        final List<VtdPoly> characterPolys = vtdMapper.getCharacterPolys(id);

        if (characterPolys != null) {
            for (VtdPoly characterPoly : characterPolys) {
                if (!characterPoly.isCompanion()) {
                    if (characterPoly.isActive() && !characterPoly.getId().equals(polyId)) {
                        characterPoly.setActive(false);
                        vtdMapper.updateCharacterPoly(characterPoly);

                        if (characterPoly.getName().equals("Shaman’s Greater Necklace - Air")) {
                            final CharacterStats characterStats = vtdMapper.getCharacterStats(id);
                            if (characterStats.getDrShock() >= 10) {
                                characterStats.setDrShock(characterStats.getDrShock() - 10);
                                vtdMapper.updateCharacterDr(characterStats);
                            }
                        } else if (characterPoly.getName().equals("Shaman’s Greater Necklace - Earth")) {
                            final CharacterStats characterStats = vtdMapper.getCharacterStats(id);
                            if (characterStats.getDrSonic() >= 10) {
                                characterStats.setDrSonic(characterStats.getDrSonic() - 10);
                                vtdMapper.updateCharacterDr(characterStats);
                            }
                        } else if (characterPoly.getName().equals("Shaman’s Greater Necklace - Fire")) {
                            final CharacterStats characterStats = vtdMapper.getCharacterStats(id);
                            if (characterStats.getDrFire() >= 10) {
                                characterStats.setDrFire(characterStats.getDrFire() - 10);
                                vtdMapper.updateCharacterDr(characterStats);
                            }
                        } else if (characterPoly.getName().equals("Shaman’s Greater Necklace - Ice")) {
                            final CharacterStats characterStats = vtdMapper.getCharacterStats(id);
                            if (characterStats.getDrCold() >= 10) {
                                characterStats.setDrCold(characterStats.getDrCold() - 10);
                                vtdMapper.updateCharacterDr(characterStats);
                            }
                        } else if (characterPoly.getName().equals("Iktomi’s Shaper Necklace - Air")) {
                            final CharacterStats characterStats = vtdMapper.getCharacterStats(id);
                            if (characterStats.getDrShock() >= 10) {
                                characterStats.setDrShock(characterStats.getDrShock() - 10);
                                vtdMapper.updateCharacterDr(characterStats);
                            }
                        } else if (characterPoly.getName().equals("Iktomi’s Shaper Necklace - Earth")) {
                            final CharacterStats characterStats = vtdMapper.getCharacterStats(id);
                            if (characterStats.getDrSonic() >= 10) {
                                characterStats.setDrSonic(characterStats.getDrSonic() - 10);
                                vtdMapper.updateCharacterDr(characterStats);
                            }
                        } else if (characterPoly.getName().equals("Iktomi’s Shaper Necklace - Fire")) {
                            final CharacterStats characterStats = vtdMapper.getCharacterStats(id);
                            if (characterStats.getDrFire() >= 10) {
                                characterStats.setDrFire(characterStats.getDrFire() - 10);
                                vtdMapper.updateCharacterDr(characterStats);
                            }
                        } else if (characterPoly.getName().equals("Iktomi’s Shaper Necklace - Ice")) {
                            final CharacterStats characterStats = vtdMapper.getCharacterStats(id);
                            if (characterStats.getDrCold() >= 10) {
                                characterStats.setDrCold(characterStats.getDrCold() - 10);
                                vtdMapper.updateCharacterDr(characterStats);
                            }
                        } else if (characterPoly.getName().equals("Shaman’s Belt - Air")) {
                            final CharacterStats characterStats = vtdMapper.getCharacterStats(id);
                            if (characterStats.getDrShock() >= 5) {
                                characterStats.setDrShock(characterStats.getDrShock() - 5);
                                vtdMapper.updateCharacterDr(characterStats);
                            }
                        } else if (characterPoly.getName().equals("Shaman’s Belt - Earth")) {
                            final CharacterStats characterStats = vtdMapper.getCharacterStats(id);
                            if (characterStats.getDrSonic() >= 5) {
                                characterStats.setDrSonic(characterStats.getDrSonic() - 5);
                                vtdMapper.updateCharacterDr(characterStats);
                            }
                        } else if (characterPoly.getName().equals("Shaman’s Belt - Fire")) {
                            final CharacterStats characterStats = vtdMapper.getCharacterStats(id);
                            if (characterStats.getDrFire() >= 5) {
                                characterStats.setDrFire(characterStats.getDrFire() - 5);
                                vtdMapper.updateCharacterDr(characterStats);
                            }
                        } else if (characterPoly.getName().equals("Shaman’s Belt - Ice")) {
                            final CharacterStats characterStats = vtdMapper.getCharacterStats(id);
                            if (characterStats.getDrCold() >= 5) {
                                characterStats.setDrCold(characterStats.getDrCold() - 5);
                                vtdMapper.updateCharacterDr(characterStats);
                            }
                        }
                    } else if (characterPoly.getId().equals(polyId)) {
                        characterPoly.setActive(true);
                        vtdMapper.updateCharacterPoly(characterPoly);

                        final VtdDetails vtdDetails = vtdMapper.getCharacter(id);
                        final List<DamageModEffect> meleeDmgEffects = new ArrayList<>();

                        if (characterPoly.getName().equals("Shaman’s Greater Necklace - Air")) {
                            final CharacterStats characterStats = vtdMapper.getCharacterStats(id);
                            characterStats.setDrShock(characterStats.getDrShock() + 10);
                            vtdMapper.updateCharacterDr(characterStats);
                        } else if (characterPoly.getName().equals("Shaman’s Greater Necklace - Earth")) {
                            final CharacterStats characterStats = vtdMapper.getCharacterStats(id);
                            characterStats.setDrSonic(characterStats.getDrSonic() + 10);
                            vtdMapper.updateCharacterDr(characterStats);
                        } else if (characterPoly.getName().equals("Shaman’s Greater Necklace - Fire")) {
                            final CharacterStats characterStats = vtdMapper.getCharacterStats(id);
                            characterStats.setDrFire(characterStats.getDrFire() + 10);
                            vtdMapper.updateCharacterDr(characterStats);
                        } else if (characterPoly.getName().equals("Shaman’s Greater Necklace - Ice")) {
                            final CharacterStats characterStats = vtdMapper.getCharacterStats(id);
                            characterStats.setDrCold(characterStats.getDrCold() + 10);
                            vtdMapper.updateCharacterDr(characterStats);
                        } else if (characterPoly.getName().equals("Iktomi’s Shaper Necklace - Air")) {
                            final CharacterStats characterStats = vtdMapper.getCharacterStats(id);
                            characterStats.setDrShock(characterStats.getDrShock() + 10);
                            vtdMapper.updateCharacterDr(characterStats);
                        } else if (characterPoly.getName().equals("Iktomi’s Shaper Necklace - Earth")) {
                            final CharacterStats characterStats = vtdMapper.getCharacterStats(id);
                            characterStats.setDrSonic(characterStats.getDrSonic() + 10);
                            vtdMapper.updateCharacterDr(characterStats);
                        } else if (characterPoly.getName().equals("Iktomi’s Shaper Necklace - Fire")) {
                            final CharacterStats characterStats = vtdMapper.getCharacterStats(id);
                            characterStats.setDrFire(characterStats.getDrFire() + 10);
                            vtdMapper.updateCharacterDr(characterStats);
                        } else if (characterPoly.getName().equals("Iktomi’s Shaper Necklace - Ice")) {
                            final CharacterStats characterStats = vtdMapper.getCharacterStats(id);
                            characterStats.setDrCold(characterStats.getDrCold() + 10);
                            vtdMapper.updateCharacterDr(characterStats);
                        }

                        if (characterPoly.getExplodeEffect() != null) {
                            final DamageModEffect damageModEffect = characterPoly.getExplodeEffect().getDamageModEffect(vtdDetails.getCharacterClass());
                            if (damageModEffect != null)
                                meleeDmgEffects.add(damageModEffect);
                        }

                        vtdDetails.setMeleePolyDmgRange(characterPoly.getDmgRange());
                        vtdDetails.setMeleePolyDmgEffects(String.join(",", meleeDmgEffects.stream().map(Enum::name).collect(Collectors.toList())));
                        vtdDetails.setMeleePolyCritMin(characterPoly.getCritMin());
                        vtdMapper.updateCharacter(vtdDetails);
                    }
                }
            }
        }

        return calculateStats(id);
    }

    @Override
    public VtdDetails setCompanion(String id, String polyId) {
        final List<VtdPoly> characterPolys = vtdMapper.getCharacterPolys(id);

        if (characterPolys != null) {
            for (VtdPoly characterPoly : characterPolys) {
                if (characterPoly.isCompanion()) {
                    if (characterPoly.isActive() && !characterPoly.getId().equals(polyId)) {
                        characterPoly.setActive(false);
                        vtdMapper.updateCharacterPoly(characterPoly);
                    } else if (characterPoly.getId().equals(polyId)) {
                        characterPoly.setActive(true);
                        vtdMapper.updateCharacterPoly(characterPoly);
                    }
                }
            }
        }

        return calculateStats(id);
    }

    @Override
    public VtdDetails setAdventure(String id, String passcode) {
        final VtdDetails vtdDetails = calculateStats(id);

        final VtdSetting adventureByCode = vtdAdminMapper.getAdventureByCode(passcode.toLowerCase());
        if (adventureByCode != null) {
            vtdDetails.setAdventureId(adventureByCode.getId());
            vtdDetails.setAdventureName(adventureByCode.getName());

            vtdMapper.updateCharacter(vtdDetails);
        } else
            throw new InvalidDataException("Passcode does not match any known adventures.");

        return calculateStats(id);
    }

    @Override
    public VtdDetails setRollerId(String id, String rollerId) {
        final VtdDetails vtdDetails = calculateStats(id);

        vtdDetails.setRollerId(rollerId);
        vtdMapper.updateCharacter(vtdDetails);

        return calculateStats(id);
    }

    @Override
    public VtdDetails resetCharacter(String id) {
        return getVtdCharacter(id, true, false);
    }

    @Override
    public VtdDetails deleteQueuedSkills(String id) {
        final VtdDetails vtdDetails = calculateStats(id);

        vtdMapper.deleteQueuedSkills(id);
        vtdDetails.setQueuedSkills(new ArrayList<>());

        return vtdDetails;
    }

    @Override
    public List<CharacterName> deleteCharacter(String id) {
        VtdDetails vtdDetails = vtdMapper.getCharacter(id);

        if (vtdDetails == null)
            return getSelectableCharacters();
        if(!(userPrincipalProvider.getUserPrincipal().getSub() != null && userPrincipalProvider.getUserPrincipal().getSub().equalsIgnoreCase(vtdDetails.getUserId())))
            throw new InvalidDataException("Virtual True Dungeon is only for your own characters.");

        vtdMapper.deleteCharacterPolys(id);
        vtdMapper.deleteCharacterBuffs(id);
        vtdMapper.deleteCharacterSkills(id);
        vtdMapper.deleteQueuedSkills(id);
        vtdMapper.deleteCharacterStats(id);
        vtdMapper.deleteCharacter(id);

        return getSelectableCharacters();
    }

    @Override
    public VtdDetails activatePrestigeClass(String id) {
        return getVtdCharacter(id, true, true);
    }

    private VtdDetails calculateStats(String id) {
        VtdDetails vtdDetails = vtdMapper.getCharacter(id);

        if (vtdDetails == null)
            vtdDetails = getVtdCharacter(id, false, false);

        vtdDetails.setBuffs(vtdMapper.getCharacterBuffs(vtdDetails.getCharacterId()));
        vtdDetails.setDebuffs(vtdMapper.getCharacterDebuffs(vtdDetails.getCharacterId()));
        vtdDetails.setCharacterSkills(vtdMapper.getCharacterSkills(vtdDetails.getCharacterId()));
        vtdDetails.setQueuedSkills(vtdMapper.getQueuedSkills(vtdDetails.getCharacterId()));
        vtdDetails.setStats(vtdMapper.getCharacterStats(vtdDetails.getCharacterId()));
        vtdDetails.setNotes(mapper.getCharacterNotes(vtdDetails.getCharacterOrigId()));
        vtdDetails.setItems(mapper.getCharacterItems(vtdDetails.getCharacterOrigId()));
        vtdDetails.setPolys(vtdMapper.getCharacterPolys(vtdDetails.getCharacterId()));

        final Set<CritType> critTypes = (vtdDetails.getCritTypes() == null || vtdDetails.getCritTypes().isEmpty()) ? new HashSet<>() : Arrays.stream(vtdDetails.getCritTypes().split(",")).map(CritType::valueOf).collect(Collectors.toSet());
        if (vtdDetails.getBuffs() != null && vtdDetails.getBuffs().stream().filter(vtdBuff -> vtdBuff.getBuff() == Buff.OIL_OF_THE_TINKERER).count() > 0)
            critTypes.add(CritType.CONSTRUCT);

        vtdDetails.setMonsters(VtdMonster.fromRoom(vtdAdminMapper.getRoomsByNumber(vtdDetails.getAdventureId(), vtdDetails.getRoomNumber()), critTypes, vtdDetails.getMeleeMainHit(), vtdDetails.getMeleeOffhandHit(), vtdDetails.getRangeMainHit(), vtdDetails.getRangeOffhandHit(), vtdDetails.getCharacterClass() == CharacterClass.RANGER, vtdDetails.getStats().getLevel() == 5));

        applyBuffsToStats(vtdDetails.getBuffs(), vtdDetails.getStats(), vtdDetails);

        vtdDetails.getStats().setHealth(vtdDetails.getStats().getHealth() + vtdDetails.getHealthBonus());

        for (VtdDebuff vtdDebuff : vtdDetails.getDebuffs()) {
            if (vtdDebuff.getLevel() > 0) {
                if (vtdDebuff.getDebuff() == Debuff.DRAIN_LIFE) {
                    vtdDetails.getStats().setHealth(vtdDetails.getStats().getHealth() - (int)(vtdDetails.getStats().getHealth() * vtdDebuff.getLevel() * .10));
                } else if (vtdDebuff.getDebuff() == Debuff.DRAIN_LIFE_EPIC) {
                    vtdDetails.getStats().setHealth(vtdDetails.getStats().getHealth() - (int)(vtdDetails.getStats().getHealth() * vtdDebuff.getLevel() * .20));
                }

                if (vtdDetails.getCurrentHealth() > vtdDetails.getStats().getHealth()) {
                    vtdDetails.setCurrentHealth(vtdDetails.getStats().getHealth());
                    vtdMapper.updateCharacter(vtdDetails);
                }
            }
        }

        return vtdDetails;
    }

    private void applyBuffsToStats(List<VtdBuff> buffs, CharacterStats stats, VtdDetails vtdDetails) {
        boolean mysticStaff = vtdDetails.getItems().stream().filter(item -> item != null && item.getName() != null && item.getName().startsWith("Mystic Staff")).count() > 1;

        if (buffs != null) {
            boolean earcuffOfTheShanty = vtdDetails.getItems().stream().filter(item -> item != null && item.getName() != null && item.getItemId().equals("4925fc0848bdd8ac48f29d69b498da6504dfbb3a")).count() > 1;

            buffs.forEach(buff -> {
                if (earcuffOfTheShanty && buff.isBardsong()) {
                    stats.setMeleeSonic(true);
                    stats.setMSonic(stats.getMSonic() + 2);
                }

                buff.getBuff().getEffects().forEach(vtdBuffEffect -> {
                    switch (vtdBuffEffect.getStat()) {
                        case STR:
                            stats.setStr(stats.getStr() + vtdBuffEffect.getModifier());
                            break;
                        case DEX:
                            stats.setDex(stats.getDex() + vtdBuffEffect.getModifier());
                            break;
                        case CON:
                            stats.setCon(stats.getCon() + vtdBuffEffect.getModifier());
                            break;
                        case INTEL:
                            stats.setIntel(stats.getIntel() + vtdBuffEffect.getModifier());
                            break;
                        case WIS:
                            stats.setWis(stats.getWis() + vtdBuffEffect.getModifier());
                            break;
                        case CHA:
                            stats.setCha(stats.getCha() + vtdBuffEffect.getModifier());
                            break;
                        case HEALTH:
                            stats.setHealth(stats.getHealth() + vtdBuffEffect.getModifier());
                            break;
                        case REGEN:
                            stats.setRegen(stats.getRegen() + vtdBuffEffect.getModifier());
                            break;
                        case MELEE_HIT:
                            if (buff.getBuff().isCanBeLeveled())
                                stats.setMeleeHit(stats.getMeleeHit() + buff.getLevel());
                            else
                                stats.setMeleeHit(stats.getMeleeHit() + vtdBuffEffect.getModifier());
                            break;
                        case MELEE_DMG:
                            if (buff.getBuff().isCanBeLeveled())
                                stats.setMeleeDmg(stats.getMeleeDmg() + buff.getLevel());
                            else
                                stats.setMeleeDmg(stats.getMeleeDmg() + vtdBuffEffect.getModifier());
                            break;
                        case MELEE_POLY_HIT:
                            if (buff.getBuff().isCanBeLeveled())
                                stats.setMeleePolyHit(stats.getMeleePolyHit() + buff.getLevel());
                            else
                                stats.setMeleePolyHit(stats.getMeleePolyHit() + vtdBuffEffect.getModifier());
                            break;
                        case MELEE_POLY_DMG:
                            if (buff.getBuff().isCanBeLeveled())
                                stats.setMeleePolyDmg(stats.getMeleePolyDmg() + buff.getLevel());
                            else
                                stats.setMeleePolyDmg(stats.getMeleePolyDmg() + vtdBuffEffect.getModifier());
                            break;
                        case MELEE_FIRE:
                            stats.setMeleeFire(stats.isMeleeFire() || vtdBuffEffect.getModifier() > 0);
                            break;
                        case MELEE_COLD:
                            stats.setMeleeCold(stats.isMeleeCold() || vtdBuffEffect.getModifier() > 0);
                            break;
                        case MELEE_SHOCK:
                            stats.setMeleeShock(stats.isMeleeShock() || vtdBuffEffect.getModifier() > 0);
                            break;
                        case MELEE_SONIC:
                            stats.setMeleeSonic(stats.isMeleeSonic() || vtdBuffEffect.getModifier() > 0);
                            break;
                        case MELEE_ELDRICH:
                            stats.setMeleeEldritch(stats.isMeleeEldritch() || vtdBuffEffect.getModifier() > 0);
                            break;
                        case MELEE_POISON:
                            stats.setMeleePoison(stats.isMeleePoison() || vtdBuffEffect.getModifier() > 0);
                            break;
                        case MELEE_DARKRIFT:
                            stats.setMeleeDarkrift(stats.isMeleeDarkrift() || vtdBuffEffect.getModifier() > 0);
                            break;
                        case MELEE_SACRED:
                            stats.setMeleeSacred(stats.isMeleeSacred() || vtdBuffEffect.getModifier() > 0);
                            break;
                        case MELEE_AC:
                            stats.setMeleeAC(stats.getMeleeAC() + vtdBuffEffect.getModifier());
                            break;
                        case RANGE_HIT:
                            if (buff.getBuff().isCanBeLeveled())
                                stats.setRangeHit(stats.getRangeHit() + buff.getLevel());
                            else {
                                stats.setRangeHit(stats.getRangeHit() + vtdBuffEffect.getModifier());
                                if (buff.getBuff().getEffects().stream().noneMatch(be -> be.getStat() == Stat.BENROW_HIT))
                                    stats.setRangeHitBenrow(stats.getRangeHitBenrow() + vtdBuffEffect.getModifier());
                            }
                            break;
                        case RANGE_DMG:
                            if (buff.getBuff().isCanBeLeveled())
                                stats.setRangeDmg(stats.getRangeDmg() + buff.getLevel());
                            else {
                                stats.setRangeDmg(stats.getRangeDmg() + vtdBuffEffect.getModifier());
                                if (buff.getBuff().getEffects().stream().noneMatch(be -> be.getStat() == Stat.BENROW_DMG))
                                    stats.setRangeDmgBenrow(stats.getRangeDmgBenrow() + vtdBuffEffect.getModifier());
                            }
                            break;
                        case RANGE_FIRE:
                            stats.setRangeFire(stats.isRangeFire() || vtdBuffEffect.getModifier() > 0);
                            break;
                        case RANGE_COLD:
                            stats.setRangeCold(stats.isRangeCold() || vtdBuffEffect.getModifier() > 0);
                            break;
                        case RANGE_SHOCK:
                            stats.setRangeShock(stats.isRangeShock() || vtdBuffEffect.getModifier() > 0);
                            break;
                        case RANGE_SONIC:
                            stats.setRangeSonic(stats.isRangeSonic() || vtdBuffEffect.getModifier() > 0);
                            break;
                        case RANGE_ELDRICH:
                            stats.setRangeEldritch(stats.isRangeEldritch() || vtdBuffEffect.getModifier() > 0);
                            break;
                        case RANGE_POISON:
                            stats.setRangePoison(stats.isRangePoison() || vtdBuffEffect.getModifier() > 0);
                            break;
                        case RANGE_DARKRIFT:
                            stats.setRangeDarkrift(stats.isRangeDarkrift() || vtdBuffEffect.getModifier() > 0);
                            break;
                        case RANGE_SACRED:
                            stats.setRangeSacred(stats.isRangeSacred() || vtdBuffEffect.getModifier() > 0);
                            break;
                        case RANGE_AC:
                            stats.setRangeAC(stats.getRangeAC() + vtdBuffEffect.getModifier());
                            break;
                        case RANGE_MISSILE_AC:
                            stats.setRangeMissileAC(stats.getRangeMissileAC() + vtdBuffEffect.getModifier());
                            break;
                        case FORT:
                            stats.setFort(stats.getFort() + vtdBuffEffect.getModifier());
                            break;
                        case REFLEX:
                            stats.setReflex(stats.getReflex() + vtdBuffEffect.getModifier());
                            break;
                        case WILL:
                            stats.setWill(stats.getWill() + vtdBuffEffect.getModifier());
                            break;
                        case RET_DMG:
                            stats.setRetDmg(stats.getRetDmg() + vtdBuffEffect.getModifier());
                            break;
                        case RET_FIRE:
                            stats.setRetFire(stats.isRetFire() || vtdBuffEffect.getModifier() > 0);
                            break;
                        case RET_COLD:
                            stats.setRetCold(stats.isRetCold() || vtdBuffEffect.getModifier() > 0);
                            break;
                        case RET_SHOCK:
                            stats.setRetShock(stats.isRetShock() || vtdBuffEffect.getModifier() > 0);
                            break;
                        case RET_SONIC:
                            stats.setRetSonic(stats.isRetSonic() || vtdBuffEffect.getModifier() > 0);
                            break;
                        case RET_ELDRICH:
                            stats.setRetEldritch(stats.isRetEldritch() || vtdBuffEffect.getModifier() > 0);
                            break;
                        case RET_POISON:
                            stats.setRetPoison(stats.isRetPoison() || vtdBuffEffect.getModifier() > 0);
                            break;
                        case RET_DARKRIFT:
                            stats.setRetDarkrift(stats.isRetDarkrift() || vtdBuffEffect.getModifier() > 0);
                            break;
                        case RET_SACRED:
                            stats.setRetSacred(stats.isRetSacred() || vtdBuffEffect.getModifier() > 0);
                            break;
                        case CANNOT_BE_SUPRISED:
                            stats.setCannotBeSuprised(stats.isCannotBeSuprised() || vtdBuffEffect.getModifier() > 0);
                            break;
                        case FREE_MOVEMENT:
                            stats.setFreeMovement(stats.isFreeMovement() || vtdBuffEffect.getModifier() > 0);
                            break;
                        case SPELL_DMG:
                            stats.setSpellDmg(stats.getSpellDmg() + vtdBuffEffect.getModifier());
                            break;
                        case SPELL_HEAL:
                            stats.setSpellHeal(stats.getSpellHeal() + vtdBuffEffect.getModifier());
                            break;
                        case SPELL_RESIST:
                            stats.setSpellResist(stats.getSpellResist() + vtdBuffEffect.getModifier());
                            break;
                        case INITIATIVE:
                            stats.setInitiative(stats.getInitiative() + vtdBuffEffect.getModifier());
                            break;
                        case DR_MELEE:
                            stats.setDrMelee(stats.getDrMelee() + vtdBuffEffect.getModifier());
                            break;
                        case DR_RANGE:
                            stats.setDrRange(stats.getDrRange() + vtdBuffEffect.getModifier());
                            break;
                        case DR_SPELL:
                            stats.setDrSpell(stats.getDrSpell() + vtdBuffEffect.getModifier());
                            break;
                        case DR_FIRE:
                            stats.setDrFire(stats.getDrFire() + vtdBuffEffect.getModifier());
                            break;
                        case DR_COLD:
                            stats.setDrCold(stats.getDrCold() + vtdBuffEffect.getModifier());
                            break;
                        case DR_SHOCK:
                            stats.setDrShock(stats.getDrShock() + vtdBuffEffect.getModifier());
                            break;
                        case DR_SONIC:
                            stats.setDrSonic(stats.getDrSonic() + vtdBuffEffect.getModifier());
                            break;
                        case DR_ELDRICH:
                            stats.setDrEldritch(stats.getDrEldritch() + vtdBuffEffect.getModifier());
                            break;
                        case DR_POISON:
                            stats.setDrPoison(stats.getDrPoison() + vtdBuffEffect.getModifier());
                            break;
                        case DR_DARKRIFT:
                            stats.setDrDarkrift(stats.getDrDarkrift() + vtdBuffEffect.getModifier());
                            break;
                        case DR_SACRED:
                            stats.setDrSacred(stats.getDrSacred() + vtdBuffEffect.getModifier());
                            break;
                        case DR_FORCE:
                            stats.setDrForce(stats.getDrFire() + vtdBuffEffect.getModifier());
                            break;
                        case BENROW_HIT:
                            if (buff.getBuff().isCanBeLeveled())
                                stats.setRangeHitBenrow(stats.getRangeHitBenrow() + buff.getLevel());
                            else
                                stats.setRangeHitBenrow(stats.getRangeHitBenrow() + vtdBuffEffect.getModifier());
                            break;
                        case BENROW_DMG:
                            if (buff.getBuff().isCanBeLeveled())
                                stats.setRangeDmgBenrow(stats.getRangeDmgBenrow() + buff.getLevel());
                            else
                                stats.setRangeDmgBenrow(stats.getRangeDmgBenrow() + vtdBuffEffect.getModifier());
                            break;
                    }
                });
            });
        }

        int strDiff = ((stats.getStr()-10 > 0)?(stats.getStr()-10)/2:(stats.getStr()-11)/2) - stats.getStrBonus();
        int dexDiff = ((stats.getDex()-10 > 0)?(stats.getDex()-10)/2:(stats.getDex()-11)/2) - stats.getDexBonus();
        int conDiff = ((stats.getCon()-10 > 0)?(stats.getCon()-10)/2:(stats.getCon()-11)/2) - stats.getConBonus();
        int wisDiff = ((stats.getWis()-10 > 0)?(stats.getWis()-10)/2:(stats.getWis()-11)/2) - stats.getWisBonus();
        int intelDiff = ((stats.getIntel()-10 > 0)?(stats.getIntel()-10)/2:(stats.getIntel()-11)/2) - stats.getIntelBonus();

        stats.setMeleeHit(stats.getMeleeHit() + strDiff);
        stats.setMeleeDmg(stats.getMeleeDmg() + strDiff);
        stats.setMeleePolyHit(stats.getMeleePolyHit() + strDiff);
        stats.setMeleePolyDmg(stats.getMeleePolyDmg() + strDiff);
        stats.setRangeDmgBenrow(stats.getRangeDmgBenrow() + strDiff);

        if (mysticStaff) {
            if (stats.getIntelBonus() > stats.getWisBonus())
                stats.setRangeHit(stats.getRangeHit() + intelDiff);
            else
                stats.setRangeHit(stats.getRangeHit() + wisDiff);
        } else
            stats.setRangeHit(stats.getRangeHit() + dexDiff);

        stats.setRangeHit(stats.getRangeHit() + dexDiff);
        stats.setRangeHitBenrow(stats.getRangeHitBenrow() + dexDiff);
        stats.setMeleeAC(stats.getMeleeAC() + dexDiff);
        stats.setRangeAC(stats.getRangeAC() + dexDiff);

        stats.setFort(stats.getFort() + conDiff);
        stats.setReflex(stats.getReflex() + dexDiff);
        stats.setWill(stats.getWill() + wisDiff);

        if(vtdDetails.isMightyWeapon())
            stats.setRangeDmg(stats.getRangeDmg() + strDiff);

        stats.setStrBonus((stats.getStr()-10 > 0)?(stats.getStr()-10)/2:(stats.getStr()-11)/2);
        stats.setDexBonus((stats.getDex()-10 > 0)?(stats.getDex()-10)/2:(stats.getDex()-11)/2);
        stats.setConBonus((stats.getCon()-10 > 0)?(stats.getCon()-10)/2:(stats.getCon()-11)/2);
        stats.setIntelBonus((stats.getIntel()-10 > 0)?(stats.getIntel()-10)/2:(stats.getIntel()-11)/2);
        stats.setWisBonus((stats.getWis()-10 > 0)?(stats.getWis()-10)/2:(stats.getWis()-11)/2);
        stats.setChaBonus((stats.getCha()-10 > 0)?(stats.getCha()-10)/2:(stats.getCha()-11)/2);
    }
}
