package ch.sbb.polarion.extension.interceptor.rest.controller;

import ch.sbb.polarion.extension.interceptor.service.PolarionService;
import ch.sbb.polarion.extension.interceptor.settings.HookSettings;
import ch.sbb.polarion.extension.interceptor.settings.Revision;
import ch.sbb.polarion.extension.interceptor.settings.SettingsModel;
import ch.sbb.polarion.extension.interceptor.settings.SettingsRegistry;
import ch.sbb.polarion.extension.interceptor.util.ScopeUtils;
import com.polarion.subterra.base.location.ILocation;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jetbrains.annotations.NotNull;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static ch.sbb.polarion.extension.interceptor.util.ScopeUtils.SCOPE_DEFAULT;

@Hidden
@Tag(name = "Settings")
@Path("/internal")
public class SettingsInternalController {

    protected final PolarionService polarionService = new PolarionService();

    @GET
    @Path("/settings/{hook}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Read settings for the hook")
    public SettingsModel readSetting(@PathParam("hook") @Parameter(description = "hook name") String hook, @QueryParam("revision") String revision) {
        HookSettings settings = SettingsRegistry.INSTANCE.getByHookName(hook);
        ILocation scopeLocation = ScopeUtils.getContextLocation(SCOPE_DEFAULT);
        SettingsModel model = settings.read(scopeLocation, revision);
        if (model != null) {
            return model;
        } else {
            throw new NotFoundException("Cannot find data using specified parameters");
        }
    }

    @POST
    @Path("/settings/{hook}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Save settings for the hook")
    public Response saveSetting(@PathParam("hook") @Parameter(description = "hook name") String hook, final String content) {
        HookSettings settings = SettingsRegistry.INSTANCE.getByHookName(hook);
        ILocation scopeLocation = ScopeUtils.getContextLocation(SCOPE_DEFAULT);

        settings.save(scopeLocation, settings.fromJson(content));
        return Response.noContent().build();
    }

    @GET
    @Path("/settings/{hook}/default")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Read default settings for the hook")
    public SettingsModel readDefaultSetting(@PathParam("hook") @Parameter(description = "hook name") String hook) {
        HookSettings settings = SettingsRegistry.INSTANCE.getByHookName(hook);
        return settings.defaultValues();
    }

    @GET
    @Path("/settings/{hook}/revisions")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Read settings revisions for the hook")
    public @NotNull List<Revision> readRevisionsList(@PathParam("hook") @Parameter(description = "hook name") String hook) {
        HookSettings settings = SettingsRegistry.INSTANCE.getByHookName(hook);
        ILocation scopeLocation = ScopeUtils.getContextLocation(SCOPE_DEFAULT);
        return settings.listRevisions(scopeLocation);
    }
}
