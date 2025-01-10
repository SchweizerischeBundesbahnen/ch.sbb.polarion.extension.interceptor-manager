package ch.sbb.polarion.extension.interceptor_manager.rest.controller;

import ch.sbb.polarion.extension.generic.rest.controller.settings.NamedSettingsInternalController;
import ch.sbb.polarion.extension.generic.settings.NamedSettings;
import ch.sbb.polarion.extension.generic.settings.Revision;
import ch.sbb.polarion.extension.generic.settings.SettingsModel;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jetbrains.annotations.NotNull;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static ch.sbb.polarion.extension.generic.settings.GenericNamedSettings.DEFAULT_SCOPE;

@Hidden
@Tag(name = "Hooks settings")
@Path("/internal")
public class HooksSettingsInternalController {

    private final NamedSettingsInternalController settingsController = new NamedSettingsInternalController();

    @GET
    @Path("/hook-settings/{hook}/revisions")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Returns revisions history of specified hook setting",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved revisions",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Revision.class)))
            )
    )
    public @NotNull List<Revision> readRevisionsList(@PathParam("hook") String hook) {
        return settingsController.readRevisionsList(hook, NamedSettings.DEFAULT_NAME, DEFAULT_SCOPE);
    }

    @GET
    @Path("/hook-settings/{hook}/content")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Returns values (content) of specified hook setting",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved hook content",
                    content = @Content(schema = @Schema(implementation = SettingsModel.class))
            )
    )
    public SettingsModel readSetting(@PathParam("hook") String hook, @QueryParam("revision") String revision) {
        return settingsController.readSetting(hook, NamedSettings.DEFAULT_NAME, DEFAULT_SCOPE, revision);
    }

    @PUT
    @Path("/hook-settings/{hook}/content")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Creates or updates hook setting. Creation scenario will use default setting value if no body specified in the request.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Successfully created or updated hook setting"
            )
    )
    public void saveSetting(@PathParam("hook") String hook, final String content) {
        settingsController.saveSetting(hook, NamedSettings.DEFAULT_NAME, DEFAULT_SCOPE, content);
    }

    @GET
    @Path("/hook-settings/{hook}/default-content")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Returns default values of specified hook setting",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved default hook setting values",
                    content = @Content(schema = @Schema(implementation = SettingsModel.class))
            )
    )
    public SettingsModel getDefaultValues(@PathParam("hook") String hook) {
        return settingsController.getDefaultValues(hook);
    }
}
