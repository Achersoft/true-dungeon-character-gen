package com.achersoft.rest.services;

import com.achersoft.exception.InvalidDataException;
import com.achersoft.security.annotations.RequiresPrivilege;
import com.achersoft.security.type.Privilege;
import com.achersoft.tdcc.character.dao.CharacterName;
import com.achersoft.tdcc.enums.Buff;
import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.enums.Difficulty;
import com.achersoft.tdcc.enums.InGameEffect;
import com.achersoft.tdcc.party.PartyService;
import com.achersoft.tdcc.party.dao.Party;
import com.achersoft.tdcc.party.dao.PartyDetails;
import com.achersoft.tdcc.party.dao.SelectableCharacters;
import com.achersoft.tdcc.vtd.VirtualTdService;
import com.achersoft.tdcc.vtd.dao.VtdDetails;
import com.achersoft.tdcc.vtd.dto.BuffDTO;
import com.achersoft.tdcc.vtd.dto.VtdDetailsDTO;
import com.achersoft.user.dao.User;
import com.achersoft.user.dto.UserDTO;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.util.List;
import java.util.stream.Collectors;

@Path("/vtd")
public class VtdRestService {

    private @Inject VirtualTdService virtualTdService;

    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @GET
    @Path("/selectablecharacters")
    @Produces({MediaType.APPLICATION_JSON})
    public List<CharacterName> getSelectableCharacters() throws Exception {
        return virtualTdService.getSelectableCharacters();
    }

    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @GET
    @Path("/pregenerated")
    @Produces({MediaType.APPLICATION_JSON})
    public List<CharacterName> getPregeneratedCharacters() throws Exception {
        return virtualTdService.getPregeneratedCharacters();
    }

    @GET
    @Path("/buffs")
    @Produces({MediaType.APPLICATION_JSON})
    public List<BuffDTO> getBuffs() throws Exception {
        return Buff.getSelectableBuffs().stream().map(BuffDTO::fromDAO).collect(Collectors.toList());
    }

    @POST
    @Path("/import/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<CharacterName> importCharacter(@PathParam("id") String id) throws Exception {
        return virtualTdService.importCharacter(id);
    }

    @DELETE
    @Path("/import/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<CharacterName> deleteImportedCharacter(@PathParam("id") String id) throws Exception {
        return virtualTdService.deleteCharacter(id);
    }

    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @GET 
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})	
    public VtdDetailsDTO getVtdCharacter(@PathParam("id") String id) throws Exception {
        return VtdDetailsDTO.fromDAO(virtualTdService.getVtdCharacter(id, false, false));
    }

    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @POST
    @Path("/{id}/prestige")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public VtdDetailsDTO activatePrestige(@PathParam("id") String id) throws Exception {
        return VtdDetailsDTO.fromDAO(virtualTdService.activatePrestigeClass(id));
    }

    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @POST
    @Path("/{id}/difficulty")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public VtdDetailsDTO modifyDifficulty(@PathParam("id") String id,
                                          @QueryParam("difficulty") int difficulty) throws Exception {
        return VtdDetailsDTO.fromDAO(virtualTdService.modifyDifficulty(id, difficulty));
    }

    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @POST
    @Path("/{id}/bonus/init")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public VtdDetailsDTO setBonusInit(@PathParam("id") String id,
                                      @QueryParam("init") int init) throws Exception {
        return VtdDetailsDTO.fromDAO(virtualTdService.setBonusInit(id, init));
    }

    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @POST
    @Path("/{id}/bonus/health")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public VtdDetailsDTO setBonusHealth(@PathParam("id") String id,
                                        @QueryParam("health") int health) throws Exception {
        return VtdDetailsDTO.fromDAO(virtualTdService.setBonusHealth(id, health));
    }

    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @POST
    @Path("/{id}/bonus/bcabal")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public VtdDetailsDTO setBonusBraceletsOfCabal(@PathParam("id") String id,
                                                  @QueryParam("bonus") int bonus) throws Exception {
        return VtdDetailsDTO.fromDAO(virtualTdService.setBonusBraceletCabal(id, bonus));
    }

    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @POST
    @Path("/{id}/health")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public VtdDetailsDTO modifyHealth(@PathParam("id") String id,
                                      @QueryParam("health") int health) throws Exception {
        return VtdDetailsDTO.fromDAO(virtualTdService.modifyHealth(id, health));
    }

    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @POST
    @Path("/{id}/poly/{polyId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public VtdDetailsDTO setPoly(@PathParam("id") String id,
                                 @PathParam("polyId") String polyId) throws Exception {
        return VtdDetailsDTO.fromDAO(virtualTdService.setPoly(id, polyId));
    }

    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @POST
    @Path("/{id}/companion/{polyId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public VtdDetailsDTO setCompanion(@PathParam("id") String id,
                                      @PathParam("polyId") String polyId) throws Exception {
        return VtdDetailsDTO.fromDAO(virtualTdService.setCompanion(id, polyId));
    }

    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @POST
    @Path("/{id}/adventure")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public VtdDetailsDTO setAdventure(@PathParam("id") String id,
                                      @QueryParam("passcode") String passcode) throws Exception {
        if (passcode == null || passcode.isEmpty())
            throw new InvalidDataException("Passcode was empty");
        return VtdDetailsDTO.fromDAO(virtualTdService.setAdventure(id, passcode));
    }

    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @POST
    @Path("/{id}/{skillId}/use")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public VtdDetailsDTO useSkill(@PathParam("id") String id,
                                  @PathParam("skillId") String skillId,
                                  @QueryParam("selfTarget") boolean selfTarget,
                                  @QueryParam("selfHeal") int selfHeal,
                                  @QueryParam("madEvoker") boolean madEvoker,
                                  @QueryParam("lohNumber") int lohNumber,
                                  @QueryParam("inGameEffect") InGameEffect inGameEffect,
                                  @QueryParam("markUse") boolean markUse,
                                  @QueryParam("ignoreUse") boolean ignoreUse) throws Exception {
        return VtdDetailsDTO.fromDAO(virtualTdService.useSkill(id, skillId, selfTarget, selfHeal, madEvoker, lohNumber, inGameEffect, markUse, ignoreUse));
    }

    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @POST
    @Path("/{id}/{skillId}/unuse")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public VtdDetailsDTO unuseSkill(@PathParam("id") String id,
                                    @PathParam("skillId") String skillId) throws Exception {
        return VtdDetailsDTO.fromDAO(virtualTdService.unuseSkill(id, skillId));
    }

    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @POST
    @Path("/{id}/buff")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public VtdDetailsDTO addBuff(@PathParam("id") String id,
                                 @QueryParam("buff") Buff buff) throws Exception {
        if (buff == null)
            throw new InvalidDataException("Buff was null");
        return VtdDetailsDTO.fromDAO(virtualTdService.addBuff(id, buff));
    }

    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @DELETE
    @Path("/{id}/buff")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public VtdDetailsDTO removeBuff(@PathParam("id") String id,
                                    @QueryParam("buff") Buff buff) throws Exception {
        return VtdDetailsDTO.fromDAO(virtualTdService.removeBuff(id, buff));
    }

    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @DELETE
    @Path("/{id}/effect")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public VtdDetailsDTO removeInGameEffect(@PathParam("id") String id,
                                    @QueryParam("inGameEffect") InGameEffect inGameEffect) throws Exception {
        return VtdDetailsDTO.fromDAO(virtualTdService.removeEffect(id, inGameEffect));
    }

    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @POST
    @Path("/{id}/previous")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public VtdDetailsDTO previousRoom(@PathParam("id") String id) throws Exception {
        return VtdDetailsDTO.fromDAO(virtualTdService.previousRoom(id));
    }

    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @POST
    @Path("/{id}/next")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public VtdDetailsDTO nextRoom(@PathParam("id") String id) throws Exception {
        return VtdDetailsDTO.fromDAO(virtualTdService.nextRoom(id));
    }

    @RequiresPrivilege({Privilege.ADMIN, Privilege.SYSTEM_USER})
    @DELETE
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public VtdDetailsDTO resetCharacter(@PathParam("id") @NotNull @NotEmpty String id) throws Exception {
        return VtdDetailsDTO.fromDAO(virtualTdService.resetCharacter(id));
    }
}