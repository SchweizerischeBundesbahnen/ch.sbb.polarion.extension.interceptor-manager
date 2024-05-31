package ch.sbb.polarion.extension.interceptor.rest.controller;

import ch.sbb.polarion.extension.generic.service.PolarionService;
import ch.sbb.polarion.extension.interceptor.model.ActionHook;
import ch.sbb.polarion.extension.interceptor.model.HooksRegistry;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
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
public class InternalController {

    protected final PolarionService polarionService = new PolarionService();

    @Operation(summary = "Returns existing hooks list. By using 'reload' param hooks list will be also refreshed/reloaded from hooks folder.")
    @GET
    @Path("/hooks")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ActionHook> getHooksList(@QueryParam("reload") Boolean reload) {
        if (Boolean.TRUE.equals(reload)) {
            HooksRegistry.HOOKS.refresh();
        }
        return HooksRegistry.HOOKS.list()
                .stream().peek(h -> h.loadSettings(false)) //prevent SVNCancelException inside jackson object serialization
                .toList();
    }
}
