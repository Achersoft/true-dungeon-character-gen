package com.achersoft.tdcc.vtd.dao;

import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.enums.DamageModEffect;
import com.achersoft.tdcc.enums.WeaponExplodeCondition;
import com.achersoft.tdcc.token.admin.dao.TokenFullDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class VtdPoly {
    private String id;
    private String characterId;
    private String name;
    private boolean companion;
    private boolean active;
    private String dmgRange;
    private String explodeRange;
    private WeaponExplodeCondition explodeEffect;
    private String explodeText;
    private String dmgEffects;
    private int critMin;

    public static VtdPoly getDefault(String characterId) {
        return VtdPoly.builder()
                .id(UUID.randomUUID().toString())
                .characterId(characterId)
                .name("None")
                .active(true)
                .build();
    }

    public static VtdPoly fromToken(String characterId, CharacterClass characterClass, int critMin, List<DamageModEffect> dmgEffects, TokenFullDetails dao, boolean isCompanion) {
        if (dao.getWeaponExplodeCondition() != null) {
            final DamageModEffect damageModEffect = dao.getWeaponExplodeCondition().getDamageModEffect(characterClass);
            if (damageModEffect != null)
                dmgEffects.add(damageModEffect);
        }
        return VtdPoly.builder()
                .id(UUID.randomUUID().toString())
                .characterId(characterId)
                .name(dao.getName())
                .companion(isCompanion)
                .active(false)
                .dmgRange(dao.getDamageRange())
                .explodeRange(dao.getDamageExplodeRange())
                .explodeEffect(dao.getWeaponExplodeCondition())
                .explodeText(dao.getWeaponExplodeText())
                .dmgEffects(String.join(",", dmgEffects.stream().map(Enum::name).collect(Collectors.toList())))
                .critMin((critMin < dao.getCritMin()) ? critMin : dao.getCritMin())
                .build();
    }
}
