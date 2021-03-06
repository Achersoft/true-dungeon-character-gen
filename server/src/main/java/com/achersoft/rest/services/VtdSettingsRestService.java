package com.achersoft.rest.services;

import com.achersoft.security.UserAuthenticationService;
import com.achersoft.security.annotations.RequiresPrivilege;
import com.achersoft.security.type.Privilege;
import com.achersoft.tdcc.vtd.admin.VirtualTdAdminService;
import com.achersoft.tdcc.vtd.admin.dao.VtdSetting;
import com.achersoft.tdcc.vtd.dao.VtdDetails;
import com.achersoft.user.UserService;
import com.achersoft.user.dao.ChangePassword;
import com.achersoft.user.dao.ResetPassword;
import com.achersoft.user.dao.User;
import com.achersoft.user.dto.UserDTO;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/settings/vtd")
public class VtdSettingsRestService {

    private @Inject VirtualTdAdminService virtualTdAdminService;
    private @Inject UserAuthenticationService userAuthenticationProvider;

    @PUT
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public VtdSetting addAdventure(@Valid @NotNull VtdSetting vtdSetting) throws Exception {
        return virtualTdAdminService.createAdventure(vtdSetting);
    }

    @RequiresPrivilege({Privilege.ADMIN})
    @GET 
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON})	
    public List<VtdSetting> getAdventures() throws Exception {
        return virtualTdAdminService.getAdventures();
    }

    @RequiresPrivilege({Privilege.ADMIN})
    @GET 
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})	
    public VtdSetting getAdventure(@PathParam("id") String id) throws Exception {
        return virtualTdAdminService.getAdventure(id);
    }

    @POST 
    @Path("/{id}/update")
    @Produces({MediaType.APPLICATION_JSON})	
    @Consumes({MediaType.APPLICATION_JSON})
    public VtdSetting updateAdventure(@PathParam("id") String id, @Valid @NotNull VtdSetting vtdSetting) throws Exception {
        return virtualTdAdminService.updateAdventure(id, vtdSetting);
    }

    @RequiresPrivilege({Privilege.ADMIN})
    @DELETE 
    @Path("/{id}")
    public void deleteAdventure(@PathParam("id") String id) throws Exception {
        virtualTdAdminService.deleteAdventure(id);
    }
}