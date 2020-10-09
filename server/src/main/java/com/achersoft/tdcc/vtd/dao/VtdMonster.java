package com.achersoft.tdcc.vtd.dao;

import com.achersoft.tdcc.enums.CritType;
import com.achersoft.tdcc.vtd.admin.dao.VtdRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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
    private @Builder.Default List<Integer> roller = new ArrayList<>();
    private @Builder.Default int cold = 0;
    private @Builder.Default int fire = 0;
    private @Builder.Default int shock = 0;
    private @Builder.Default int sonic = 0;
    private @Builder.Default int poison = 0;
    private @Builder.Default int sacred = 0;
    private @Builder.Default int darkrift = 0;
    private @Builder.Default int acid = 0;

    public static List<VtdMonster> fromRoom(List<VtdRoom> vtdRooms, Set<CritType> critTypes) {
        if (vtdRooms == null)
            return new ArrayList<>();
        return vtdRooms.stream().map(vtdRoom -> VtdMonster.fromRoom(vtdRoom, critTypes)).collect(Collectors.toList());
    }

    public static VtdMonster fromRoom(VtdRoom vtdRoom, Set<CritType> critTypes) {
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

        Collections.shuffle(rollerValues);

        return VtdMonster.builder()
                .room(vtdRoom.getRoom())
                .critical(isCritable)
                .sneak(isCritable)
                .name(vtdRoom.getName())
                .roller(rollerValues)
                .cold(vtdRoom.getCold())
                .fire(vtdRoom.getFire())
                .shock(vtdRoom.getShock())
                .sonic(vtdRoom.getSonic())
                .poison(vtdRoom.getPoison())
                .sacred(vtdRoom.getSacred())
                .darkrift(vtdRoom.getDarkrift())
                .acid(vtdRoom.getAcid())
                .build();
    }
}
