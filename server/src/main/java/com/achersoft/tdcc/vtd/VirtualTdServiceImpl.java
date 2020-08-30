package com.achersoft.tdcc.vtd;

import com.achersoft.exception.InvalidDataException;
import com.achersoft.security.providers.UserPrincipalProvider;
import com.achersoft.tdcc.character.CharacterService;
import com.achersoft.tdcc.character.dao.*;
import com.achersoft.tdcc.character.persistence.CharacterMapper;
import com.achersoft.tdcc.enums.*;
import com.achersoft.tdcc.token.admin.dao.TokenFullDetails;
import com.achersoft.tdcc.token.admin.persistence.TokenAdminMapper;
import com.achersoft.tdcc.token.dao.Token;
import com.achersoft.tdcc.token.persistence.TokenMapper;
import com.achersoft.tdcc.vtd.dao.CharacterSkill;
import com.achersoft.tdcc.vtd.dao.VtdBuff;
import com.achersoft.tdcc.vtd.dao.VtdDetails;
import com.achersoft.tdcc.vtd.dao.VtdPoly;
import com.achersoft.tdcc.vtd.persistence.VtdMapper;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class VirtualTdServiceImpl implements VirtualTdService {
    
    private @Inject CharacterMapper mapper;
    private @Inject CharacterService characterService;
    private @Inject VtdMapper vtdMapper;
    private @Inject TokenMapper tokenMapper;
    private @Inject TokenAdminMapper tokenAdminMapper;
    private @Inject UserPrincipalProvider userPrincipalProvider;

    @Override
    public VtdDetails getVtdCharacter(String id, boolean reset) {
        VtdDetails vtdDetails = vtdMapper.getCharacter(id);

        if (reset || vtdDetails == null || vtdDetails.getExpires().before(new Date())) {
            final CharacterDetails characterDetails = characterService.getCharacter(id);

            if (!characterDetails.isEditable())
                throw new InvalidDataException("Virtual True Dungeon is only for your own characters.");

            vtdMapper.deleteCharacterPolys(id);
            vtdMapper.deleteCharacterBuffs(id);
            vtdMapper.deleteCharacterSkills(id);
            vtdMapper.deleteCharacterStats(id);
            vtdMapper.deleteCharacter(id);

            final List<CharacterSkill> skills = vtdMapper.getSkills(characterDetails.getCharacterClass(), characterDetails.getStats().getLevel());
            if (skills != null) {
                skills.forEach(skill -> {
                    skill.setId(UUID.randomUUID().toString());
                    skill.setCharacterId(id);
                    skill.setUsedNumber(0);
                    vtdMapper.addCharacterSkill(skill);
                });
            }

            final VtdDetails.VtdDetailsBuilder builder = VtdDetails.builder();
            final List<CharacterSkill> characterSkills = Optional.ofNullable(vtdMapper.getCharacterSkills(id)).orElse(new ArrayList<>());
            final List<DamageModEffect> meleeDmgEffects = new ArrayList<>();
            final List<DamageModEffect> meleeOffhandDmgEffects = new ArrayList<>();
            final List<DamageModEffect> rangeDmgEffects = new ArrayList<>();
            final List<DamageModEffect> rangeOffhandDmgEffects = new ArrayList<>();
            final AtomicReference<TokenFullDetails> mainHand = new AtomicReference<>();
            final AtomicReference<TokenFullDetails> offHand = new AtomicReference<>();
            final AtomicReference<TokenFullDetails> rangeMainHand = new AtomicReference<>();
            final AtomicReference<TokenFullDetails> rangeOffHand = new AtomicReference<>();
            final AtomicBoolean hasBarbRelic = new AtomicBoolean(false);
            final AtomicBoolean hasBarbLegendary = new AtomicBoolean(false);
            final AtomicBoolean hasDruidRelic = new AtomicBoolean(false);
            final AtomicBoolean hasDruidLegendary = new AtomicBoolean(false);
            final AtomicBoolean charmShadowShot = new AtomicBoolean(false);
            final AtomicBoolean madEvoker = new AtomicBoolean(false);
            final AtomicBoolean divineSight = new AtomicBoolean(false);

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
                    else if (characterItem.getItemId().equals("0076ceef0f905dda175de13222ce34029a5873f2"))
                        hasBarbLegendary.set(true);
                    else if (characterItem.getItemId().equals("f2f2a4950f8e1a2415890a370b54efc1605b551a"))
                        charmShadowShot.set(true);
                    else if (characterItem.getItemId().equals("18c97b7dc056aaf9e15a1b14f59c86fc18de0c27"))
                        madEvoker.set(true);
                    else if (characterItem.getItemId().equals("c36cb79000e553702d24d216e5fe1997185e704c"))
                        divineSight.set(true);
                    else if (characterItem.getItemId().equals("6a46f2b9c3070fd6076256d4e1c55a89725bcd36"))
                        hasDruidRelic.set(true);
                    else if (characterItem.getItemId().equals("c9182371165f18e7fdbce5da41a69af3934d6ee7"))
                        hasDruidLegendary.set(true);

                }
            }

            switch (characterDetails.getCharacterClass()) {
                case ALL:
                    break;
                case BARBARIAN:
                    if (hasBarbRelic.get() || hasBarbLegendary.get()) {
                        characterSkills.stream().filter(characterSkill -> characterSkill.getName().contains("Rage")).forEach(skill -> {
                            skill.setUsableNumber(2);
                            vtdMapper.updateCharacterSkill(skill);
                        });

                        final CharacterSkill fury = CharacterSkill.builder()
                                .id(UUID.randomUUID().toString())
                                .characterClass(CharacterClass.BARBARIAN)
                                .characterId(id)
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
                    break;
                case DRUID:
                    if (divineSight.get())
                        builder.splitHeal(true);

                    List<VtdPoly> polyList = new ArrayList<>();
                    polyList.add(VtdPoly.getDefault(characterDetails.getId()));

                    final List<Token> all = tokenMapper.getNonWeaponSlotItems(null, characterDetails.getId(), characterDetails.getCharacterClass().name(), Slot.POLYMORPH.name(), "ALL");
                    if (all != null) {
                        for (Token token : all) {
                            final TokenFullDetails tokenDetails = tokenAdminMapper.getTokenDetails(token.getId());
                            if (tokenDetails != null) {
                                polyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                        (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                        tokenDetails));
                            }
                        }
                    }

                    if (hasDruidRelic.get()) {
                        final TokenFullDetails relic = tokenAdminMapper.getTokenDetails("6a46f2b9c3070fd6076256d4e1c55a89725bcd36");
                        relic.setName("Shaman’s Greater Necklace - Air");
                        polyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                relic));
                        relic.setName("Shaman’s Greater Necklace - Earth");
                        polyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                relic));
                        relic.setName("Shaman’s Greater Necklace - Fire");
                        polyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                relic));
                        relic.setName("Shaman’s Greater Necklace - Ice");
                        polyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                relic));
                    } else if (hasDruidLegendary.get()) {
                        final TokenFullDetails legendary = tokenAdminMapper.getTokenDetails("c9182371165f18e7fdbce5da41a69af3934d6ee7");
                        legendary.setName("Iktomi’s Shaper Necklace - Air");
                        polyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                legendary));
                        legendary.setName("Iktomi’s Shaper Necklace - Earth");
                        polyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                legendary));
                        legendary.setName("Iktomi’s Shaper Necklace - Fire");
                        polyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                legendary));
                        legendary.setName("Iktomi’s Shaper Necklace - Ice");
                        polyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                legendary));
                    }
                    vtdMapper.addPolys(polyList);
                    builder.polys(polyList);
                    break;
                case FIGHTER:
                    break;
                case DWARF_FIGHTER:
                    if (characterDetails.getStats().getLevel() == 5) {
                        meleeDmgEffects.add(DamageModEffect.TRIPPLE_CRIT_ON_20);
                        meleeOffhandDmgEffects.add(DamageModEffect.TRIPPLE_CRIT_ON_20);
                        rangeDmgEffects.add(DamageModEffect.TRIPPLE_CRIT_ON_20);
                        rangeOffhandDmgEffects.add(DamageModEffect.TRIPPLE_CRIT_ON_20);
                    }
                    break;
                case ELF_WIZARD:
                    if (madEvoker.get())
                        builder.madEvoker(true);

                    List<VtdPoly> elfPolyList = new ArrayList<>();
                    elfPolyList.add(VtdPoly.getDefault(characterDetails.getId()));

                    final List<Token> allElf = tokenMapper.getNonWeaponSlotItems(null, characterDetails.getId(), characterDetails.getCharacterClass().name(), Slot.POLYMORPH.name(), "ALL");
                    if (allElf != null) {
                        for (Token token : allElf) {
                            final TokenFullDetails tokenDetails = tokenAdminMapper.getTokenDetails(token.getId());
                            if (tokenDetails != null) {
                                elfPolyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                        (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                        tokenDetails));
                            }
                        }
                    }

                    vtdMapper.addPolys(elfPolyList);
                    builder.polys(elfPolyList);
                    break;
                case WIZARD:
                    if (madEvoker.get())
                        builder.madEvoker(true);

                    List<VtdPoly> wizardPolyList = new ArrayList<>();
                    wizardPolyList.add(VtdPoly.getDefault(characterDetails.getId()));

                    final List<Token> allWizard = tokenMapper.getNonWeaponSlotItems(null, characterDetails.getId(), characterDetails.getCharacterClass().name(), Slot.POLYMORPH.name(), "ALL");
                    if (allWizard != null) {
                        for (Token token : allWizard) {
                            final TokenFullDetails tokenDetails = tokenAdminMapper.getTokenDetails(token.getId());
                            if (tokenDetails != null) {
                                wizardPolyList.add(VtdPoly.fromToken(characterDetails.getId(), characterDetails.getCharacterClass(),
                                        (characterDetails.getStats().getLevel() == 5) ? 19 : 20, new ArrayList<>(),
                                        tokenDetails));
                            }
                        }
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
                    break;
                case PALADIN:
                    break;
                case RANGER:
                    if (charmShadowShot.get()) {
                        rangeOffHand.set(tokenAdminMapper.getTokenDetails("f2f2a4950f8e1a2415890a370b54efc1605b551a"));
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
                                isSneakAtRange = true;
                            } else if (characterItem.getName().equalsIgnoreCase("Lenses of Vital Insight")) {
                                isSneakAtRange = true;
                            } else if (characterItem.getName().equalsIgnoreCase("Necklace of the Sneak")) {
                                meleeSneakCritMin = 19;
                                rangeSneakCritMin = 19;
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
                    break;
            }

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

            vtdDetails = builder
                    .characterId(id)
                    .userId(characterDetails.getUserId())
                    .expires(new Date(new Date().getTime() + 86400000))
                    .name(characterDetails.getName())
                    .characterClass(characterDetails.getCharacterClass())
                    .stats(characterDetails.getStats())
                    .currentHealth(characterDetails.getStats().getHealth())
                    .rollerDifficulty(0)
                    .initBonus(0)
                    .roomNumber(1)
                    .notes(characterDetails.getNotes())
                    .characterSkills(characterSkills)
                    .buffs(new ArrayList<>())
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

            vtdDetails = calculateStats(id);
        }

        return vtdDetails;
    }

    @Override
    public VtdDetails modifyDifficulty(String id, int difficulty) {
        final VtdDetails vtdDetails = vtdMapper.getCharacter(id);

        vtdDetails.setRollerDifficulty(difficulty);
        vtdMapper.updateCharacter(vtdDetails);

        return calculateStats(id);
    }

    @Override
    public VtdDetails setBonusInit(String id, int init) {
        final VtdDetails vtdDetails = vtdMapper.getCharacter(id);

        vtdDetails.setInitBonus(init);
        vtdMapper.updateCharacter(vtdDetails);

        return calculateStats(id);
    }

    @Override
    public VtdDetails setBonusHealth(String id, int health) {
        final VtdDetails vtdDetails = vtdMapper.getCharacter(id);
        final CharacterStats characterStats = vtdMapper.getCharacterStats(vtdDetails.getCharacterId());

        vtdDetails.setHealthBonus(health);

        if (vtdDetails.getCurrentHealth() >= characterStats.getHealth())
            vtdDetails.setCurrentHealth(characterStats.getHealth() + vtdDetails.getHealthBonus());
        if (vtdDetails.getCurrentHealth() > (characterStats.getHealth() + vtdDetails.getHealthBonus()))
            vtdDetails.setCurrentHealth(characterStats.getHealth() + vtdDetails.getHealthBonus());

        vtdMapper.updateCharacter(vtdDetails);

        return calculateStats(id);
    }

    @Override
    public VtdDetails modifyHealth(String id, int health) {
        final VtdDetails vtdDetails = vtdMapper.getCharacter(id);
        final CharacterStats characterStats = vtdMapper.getCharacterStats(vtdDetails.getCharacterId());

        vtdDetails.setCurrentHealth(vtdDetails.getCurrentHealth() + health);

        if (vtdDetails.getCurrentHealth() > (characterStats.getHealth() + vtdDetails.getHealthBonus()))
            vtdDetails.setCurrentHealth(characterStats.getHealth() + vtdDetails.getHealthBonus());
        else if (vtdDetails.getCurrentHealth() < 0)
            vtdDetails.setCurrentHealth(0);

        vtdMapper.updateCharacter(vtdDetails);

        return calculateStats(id);
    }

    @Override
    public VtdDetails useSkill(String id, String skillId, boolean selfTarget, int selfHeal, boolean madEvoker) {
        final CharacterSkill skill = vtdMapper.getCharacterSkill(skillId, id);
        final VtdDetails character = vtdMapper.getCharacter(id);

        if(!(userPrincipalProvider.getUserPrincipal().getSub() != null && userPrincipalProvider.getUserPrincipal().getSub().equalsIgnoreCase(character.getUserId())))
            throw new InvalidDataException("Virtual True Dungeon is only for your own characters.");

        if (skill.getUsedNumber() >= skill.getUsableNumber())
            return calculateStats(id);

        skill.setUsedNumber(skill.getUsedNumber() + 1);
        vtdMapper.updateCharacterSkill(skill);

        if (character.getCharacterClass() == CharacterClass.BARD) {
            for (CharacterSkill characterSkill : vtdMapper.getCharacterSkills(id)) {
                if (!characterSkill.getId().equals(skillId) && characterSkill.getSkillLevel() == skill.getSkillLevel()) {
                    characterSkill.setUsedNumber(skill.getUsedNumber());
                    vtdMapper.updateCharacterSkill(characterSkill);
                }
            }
        }

        if (skill.getSkillType() == SkillType.BUFF && (skill.getSkillTarget() == SkillTarget.SELF || skill.getSkillTarget() == SkillTarget.PARTY || (skill.getSkillTarget() == SkillTarget.ANY && selfTarget))) {
            return addBuff(id, Buff.getBuff(skill.getName()));
        } else if (skill.getSkillType() == SkillType.HEAL && selfTarget && selfHeal > 0) {
            final VtdDetails vtdDetails = calculateStats(id);

            vtdDetails.setCurrentHealth(vtdDetails.getCurrentHealth() + selfHeal);
            if (vtdDetails.getCurrentHealth() > (vtdDetails.getStats().getHealth() + vtdDetails.getHealthBonus()))
                vtdDetails.setCurrentHealth(vtdDetails.getStats().getHealth() + vtdDetails.getHealthBonus());

            vtdMapper.updateCharacter(vtdDetails);

            return calculateStats(id);
        } else if ((skill.getSkillType() == SkillType.DAMAGE || skill.getSkillType() == SkillType.DAMAGE_RANGE_AC_15) && madEvoker) {
            final VtdDetails vtdDetails = calculateStats(id);

            if (vtdDetails.getCharacterClass() == CharacterClass.ELF_WIZARD || vtdDetails.getCharacterClass() == CharacterClass.WIZARD) {
                vtdDetails.setCurrentHealth(vtdDetails.getCurrentHealth() - 10);
                if (vtdDetails.getCurrentHealth() < 0)
                    vtdDetails.setCurrentHealth(0);

                vtdMapper.updateCharacter(vtdDetails);
            }

            return calculateStats(id);
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

            if (character.getCharacterClass() == CharacterClass.BARD) {
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
    public VtdDetails addBuff(String id, Buff buff) {
        if (buff != null && !vtdMapper.buffExists(id, buff)) {
            vtdMapper.addCharacterBuff(VtdBuff.builder().characterId(id).buff(buff).build());
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
    public VtdDetails nextRoom(String id) {
        VtdDetails vtdDetails = vtdMapper.getCharacter(id);

        if (vtdDetails == null)
            vtdDetails = getVtdCharacter(id, false);
        if(!(userPrincipalProvider.getUserPrincipal().getSub() != null && userPrincipalProvider.getUserPrincipal().getSub().equalsIgnoreCase(vtdDetails.getUserId())))
            throw new InvalidDataException("Virtual True Dungeon is only for your own characters.");

        vtdDetails.setStats(vtdMapper.getCharacterStats(vtdDetails.getCharacterId()));

        if (vtdDetails.getRoomNumber() < 7)
            vtdDetails.setRoomNumber(vtdDetails.getRoomNumber() + 1);

        vtdDetails.setCurrentHealth(vtdDetails.getCurrentHealth() + vtdDetails.getStats().getRegen());
        if (vtdDetails.getCurrentHealth() > (vtdDetails.getStats().getHealth() + vtdDetails.getHealthBonus()))
            vtdDetails.setCurrentHealth(vtdDetails.getStats().getHealth() + vtdDetails.getHealthBonus());

        vtdMapper.updateCharacter(vtdDetails);
        vtdMapper.resetCharacterBuffs(id);

        return calculateStats(id);
    }

    @Override
    public VtdDetails setPoly(String id, String polyId) {
        final List<VtdPoly> characterPolys = vtdMapper.getCharacterPolys(id);

        if (characterPolys != null) {
            for (VtdPoly characterPoly : characterPolys) {
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

        return calculateStats(id);
    }

    @Override
    public void resetCharacter(String id) {
        getVtdCharacter(id, true);
    }

    private VtdDetails calculateStats(String id) {
        VtdDetails vtdDetails = vtdMapper.getCharacter(id);

        if (vtdDetails == null)
            vtdDetails = getVtdCharacter(id, false);

        vtdDetails.setBuffs(vtdMapper.getCharacterBuffs(vtdDetails.getCharacterId()));
        vtdDetails.setCharacterSkills(vtdMapper.getCharacterSkills(vtdDetails.getCharacterId()));
        vtdDetails.setStats(vtdMapper.getCharacterStats(vtdDetails.getCharacterId()));
        vtdDetails.setNotes(mapper.getCharacterNotes(vtdDetails.getCharacterId()));
        vtdDetails.setPolys(vtdMapper.getCharacterPolys(vtdDetails.getCharacterId()));

        applyBuffsToStats(vtdDetails.getBuffs(), vtdDetails.getStats(), vtdDetails.isMightyWeapon());

        vtdDetails.getStats().setHealth(vtdDetails.getStats().getHealth() + vtdDetails.getHealthBonus());

        return vtdDetails;
    }

    private void applyBuffsToStats(List<VtdBuff> buffs, CharacterStats stats, boolean isMighty) {
        if (buffs != null) {
            buffs.forEach(buff -> {
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
                            stats.setMeleeHit(stats.getMeleeHit() + vtdBuffEffect.getModifier());
                            break;
                        case MELEE_DMG:
                            stats.setMeleeDmg(stats.getMeleeDmg() + vtdBuffEffect.getModifier());
                            break;
                        case MELEE_POLY_HIT:
                            stats.setMeleePolyHit(stats.getMeleePolyHit() + vtdBuffEffect.getModifier());
                            break;
                        case MELEE_POLY_DMG:
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
                            stats.setRangeHit(stats.getRangeHit() + vtdBuffEffect.getModifier());
                            break;
                        case RANGE_DMG:
                            stats.setRangeDmg(stats.getRangeDmg() + vtdBuffEffect.getModifier());
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
                    }
                });
            });
        }

        int strDiff = ((stats.getStr()-10 > 0)?(stats.getStr()-10)/2:(stats.getStr()-11)/2) - stats.getStrBonus();
        int dexDiff = ((stats.getDex()-10 > 0)?(stats.getDex()-10)/2:(stats.getDex()-11)/2) - stats.getDexBonus();
        int conDiff = ((stats.getCon()-10 > 0)?(stats.getCon()-10)/2:(stats.getCon()-11)/2) - stats.getConBonus();
        int wisDiff = ((stats.getWis()-10 > 0)?(stats.getWis()-10)/2:(stats.getWis()-11)/2) - stats.getWisBonus();

        stats.setMeleeHit(stats.getMeleeHit() + strDiff);
        stats.setMeleeDmg(stats.getMeleeDmg() + strDiff);
        stats.setMeleePolyHit(stats.getMeleePolyHit() + strDiff);
        stats.setMeleePolyDmg(stats.getMeleePolyDmg() + strDiff);
        stats.setRangeHit(stats.getRangeHit() + dexDiff);
        stats.setMeleeAC(stats.getMeleeAC() + dexDiff);
        stats.setRangeAC(stats.getRangeAC() + dexDiff);

        stats.setFort(stats.getFort() + conDiff);
        stats.setReflex(stats.getReflex() + dexDiff);
        stats.setWill(stats.getWill() + wisDiff);

        if(isMighty)
            stats.setRangeDmg(stats.getRangeDmg() + strDiff);

        stats.setStrBonus((stats.getStr()-10 > 0)?(stats.getStr()-10)/2:(stats.getStr()-11)/2);
        stats.setDexBonus((stats.getDex()-10 > 0)?(stats.getDex()-10)/2:(stats.getDex()-11)/2);
        stats.setConBonus((stats.getCon()-10 > 0)?(stats.getCon()-10)/2:(stats.getCon()-11)/2);
        stats.setIntelBonus((stats.getIntel()-10 > 0)?(stats.getIntel()-10)/2:(stats.getIntel()-11)/2);
        stats.setWisBonus((stats.getWis()-10 > 0)?(stats.getWis()-10)/2:(stats.getWis()-11)/2);
        stats.setChaBonus((stats.getCha()-10 > 0)?(stats.getCha()-10)/2:(stats.getCha()-11)/2);
    }
}
