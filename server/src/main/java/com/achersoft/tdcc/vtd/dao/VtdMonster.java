package com.achersoft.tdcc.vtd.dao;

import com.achersoft.tdcc.enums.CritType;
import com.achersoft.tdcc.enums.MonsterEffects;
import com.achersoft.tdcc.vtd.admin.dao.VtdRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class VtdMonster {
    private int room;
    private String name;
    private boolean critical;
    private boolean sneak;
    private @Builder.Default List<MonsterEffects> monsterEffects = new ArrayList<>();
    private @Builder.Default List<Integer> roller = new ArrayList<>();
    private @Builder.Default int bonusDmg = 0;
    private @Builder.Default int cold = 0;
    private @Builder.Default int fire = 0;
    private @Builder.Default int shock = 0;
    private @Builder.Default int sonic = 0;
    private @Builder.Default int poison = 0;
    private @Builder.Default int sacred = 0;
    private @Builder.Default int darkrift = 0;
    private @Builder.Default int acid = 0;
    private @Builder.Default int universalDr = 0;
    private @Builder.Default int meleeDr = 0;
    private @Builder.Default int rangeDr = 0;
    private @Builder.Default int spellDr = 0;
    private @Builder.Default int mAC = 0;
    private @Builder.Default int rAC = 0;
    private @Builder.Default int fort = 0;
    private @Builder.Default int reflex = 0;
    private @Builder.Default int will = 0;

    public static List<VtdMonster> fromRoom(List<VtdRoom> vtdRooms, Set<CritType> critTypes, int meleeWeaponHit, int meleeWeaponHitOff, int rangeWeaponHit, int rangeWeaponHitOff, boolean isRanger, boolean isLevel5) {
        if (vtdRooms == null)
            return new ArrayList<>();
        return vtdRooms.stream().map(vtdRoom -> VtdMonster.fromRoom(vtdRoom, critTypes, meleeWeaponHit, meleeWeaponHitOff, rangeWeaponHit, rangeWeaponHitOff, isRanger, isLevel5)).collect(Collectors.toList());
    }

    public static VtdMonster fromRoom(VtdRoom vtdRoom, Set<CritType> critTypes, int meleeWeaponHit,  int meleeWeaponHitOff, int rangeWeaponHit, int rangeWeaponHitOff, boolean isRanger, boolean isLevel5) {
        if (vtdRoom == null)
            return null;

        final List<Integer> rollerValues = new ArrayList<>();
        for (int i=0; i<vtdRoom.getOne(); i++)
            rollerValues.add(1);
        for (int i=0; i<vtdRoom.getTwo(); i++)
            rollerValues.add(2);
        for (int i=0; i<vtdRoom.getThree(); i++)
            rollerValues.add(3);
        for (int i=0; i<vtdRoom.getFour(); i++)
            rollerValues.add(4);
        for (int i=0; i<vtdRoom.getFive(); i++)
            rollerValues.add(5);
        for (int i=0; i<vtdRoom.getSix(); i++)
            rollerValues.add(6);
        for (int i=0; i<vtdRoom.getSeven(); i++)
            rollerValues.add(7);
        for (int i=0; i<vtdRoom.getEight(); i++)
            rollerValues.add(8);
        for (int i=0; i<vtdRoom.getNine(); i++)
            rollerValues.add(9);
        for (int i=0; i<vtdRoom.getTen(); i++)
            rollerValues.add(10);
        for (int i=0; i<vtdRoom.getEleven(); i++)
            rollerValues.add(11);
        for (int i=0; i<vtdRoom.getTwelve(); i++)
            rollerValues.add(12);
        for (int i=0; i<vtdRoom.getThirteen(); i++)
            rollerValues.add(13);
        for (int i=0; i<vtdRoom.getFourteen(); i++)
            rollerValues.add(14);
        for (int i=0; i<vtdRoom.getFifteen(); i++)
            rollerValues.add(15);
        for (int i=0; i<vtdRoom.getSixteen(); i++)
            rollerValues.add(16);
        for (int i=0; i<vtdRoom.getSeventeen(); i++)
            rollerValues.add(17);
        for (int i=0; i<vtdRoom.getEighteen(); i++)
            rollerValues.add(18);
        for (int i=0; i<vtdRoom.getNineteen(); i++)
            rollerValues.add(19);
        for (int i=0; i<vtdRoom.getTwenty(); i++)
            rollerValues.add(20);

        boolean isCritable = false;
        if (vtdRoom.getCritType() == CritType.ANY)
            isCritable = true;
        else if (critTypes != null) {
            if (critTypes.contains(CritType.ANY) || critTypes.contains(vtdRoom.getCritType()))
                isCritable = true;
        }

        int bonusDamage = 0;
        if (vtdRoom.getCritType() == CritType.UNDEAD && isRanger) {
            if (isLevel5)
                bonusDamage = 2;
            else
                bonusDamage = 1;
        }

        List<MonsterEffects> monsterEffects = new ArrayList<>();
        if (vtdRoom.getMonsterEffects() != null && !vtdRoom.getMonsterEffects().isEmpty()) {
            monsterEffects.addAll(Arrays.stream(vtdRoom.getMonsterEffects().split(",")).map(MonsterEffects::valueOf).distinct().collect(Collectors.toList()));

            if (monsterEffects.contains(MonsterEffects.PHASING_HARDCORE)) {
                if (meleeWeaponHit == 0)
                    monsterEffects.add(MonsterEffects.MELEE_MAIN_ON_20);
                if (meleeWeaponHitOff == 0)
                    monsterEffects.add(MonsterEffects.MELEE_OFFHAND_ON_20);
                if (rangeWeaponHit == 0)
                    monsterEffects.add(MonsterEffects.RANGE_MAIN_ON_20);
                if (rangeWeaponHitOff == 0)
                    monsterEffects.add(MonsterEffects.RANGE_OFFHAND_ON_20);
            } else if (monsterEffects.contains(MonsterEffects.PHASING_NIGHTMARE)) {
                if (meleeWeaponHit < 3)
                    monsterEffects.add(MonsterEffects.MELEE_MAIN_ON_20);
                if (meleeWeaponHitOff < 3)
                    monsterEffects.add(MonsterEffects.MELEE_OFFHAND_ON_20);
                if (rangeWeaponHit < 3)
                    monsterEffects.add(MonsterEffects.RANGE_MAIN_ON_20);
                if (rangeWeaponHitOff < 3)
                    monsterEffects.add(MonsterEffects.RANGE_OFFHAND_ON_20);
            } else if (monsterEffects.contains(MonsterEffects.PHASING_EPIC)) {
                if (meleeWeaponHit < 5)
                    monsterEffects.add(MonsterEffects.MELEE_MAIN_ON_20);
                if (meleeWeaponHitOff < 5)
                    monsterEffects.add(MonsterEffects.MELEE_OFFHAND_ON_20);
                if (rangeWeaponHit < 5)
                    monsterEffects.add(MonsterEffects.RANGE_MAIN_ON_20);
                if (rangeWeaponHitOff < 5)
                    monsterEffects.add(MonsterEffects.RANGE_OFFHAND_ON_20);
            }
        }

        Collections.shuffle(rollerValues);

        return VtdMonster.builder()
                .room(vtdRoom.getRoom())
                .critical(isCritable)
                .sneak(isCritable)
                .monsterEffects(monsterEffects)
                .name(vtdRoom.getName())
                .roller(rollerValues)
                .bonusDmg(bonusDamage)
                .cold(vtdRoom.getCold())
                .fire(vtdRoom.getFire())
                .shock(vtdRoom.getShock())
                .sonic(vtdRoom.getSonic())
                .poison(vtdRoom.getPoison())
                .sacred(vtdRoom.getSacred())
                .darkrift(vtdRoom.getDarkrift())
                .acid(vtdRoom.getAcid())
                .universalDr(vtdRoom.getUniversalDr())
                .meleeDr(vtdRoom.getMeleeDr())
                .rangeDr(vtdRoom.getRangeDr())
                .spellDr(vtdRoom.getSpellDr())
                .mAC(vtdRoom.getMAC())
                .rAC(vtdRoom.getRAC())
                .fort(vtdRoom.getFort())
                .reflex(vtdRoom.getReflex())
                .will(vtdRoom.getWill())
                .build();
    }
}
