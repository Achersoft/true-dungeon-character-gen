package com.achersoft.tdcc.vtd.dao;

import com.achersoft.tdcc.enums.CritType;
import com.achersoft.tdcc.vtd.admin.dao.VtdRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class VtdMonster {
    private int room;
    private @Builder.Default CritType critType = CritType.ANY;
    private String name;
    private @Builder.Default List<Integer> roller = new ArrayList<>();

    public static List<VtdMonster> fromRoom(List<VtdRoom> vtdRooms) {
        if (vtdRooms == null)
            return new ArrayList<>();
        return vtdRooms.stream().map(VtdMonster::fromRoom).collect(Collectors.toList());
    }

    public static VtdMonster fromRoom(VtdRoom vtdRoom) {
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

        return VtdMonster.builder()
                .room(vtdRoom.getRoom())
                .critType(vtdRoom.getCritType())
                .name(vtdRoom.getName())
                .roller(rollerValues)
                .build();
    }
}
