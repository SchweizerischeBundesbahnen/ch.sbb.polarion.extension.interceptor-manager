package ch.sbb.polarion.extension.interceptor_manager.rest.controller;

import ch.sbb.polarion.extension.generic.service.PolarionService;
import ch.sbb.polarion.extension.interceptor_manager.model.HooksRegistry;
import ch.sbb.polarion.extension.interceptor_manager.model.IActionHook;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Tag(name = "Hooks")
@Hidden
@Path("/internal")
public class HooksInternalController {

    protected final PolarionService polarionService = new PolarionService();

    @GET
    @Path("/hooks")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Returns existing hooks list. By using 'reload' param hooks list will be also refreshed/reloaded from hooks folder.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved hooks list",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = IActionHook.class)))
            )
    )
    public List<IActionHook> getHooksList(@QueryParam("reload") Boolean reload) {
        if (Boolean.TRUE.equals(reload)) {
            HooksRegistry.HOOKS.refresh();
        }
        return HooksRegistry.HOOKS.list()
                .stream().peek(h -> h.loadSettings(false)) //prevent SVNCancelException inside jackson object serialization
                .toList();
    }
}
