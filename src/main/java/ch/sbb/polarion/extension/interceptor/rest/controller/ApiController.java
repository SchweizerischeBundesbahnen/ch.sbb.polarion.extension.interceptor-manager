package ch.sbb.polarion.extension.interceptor.rest.controller;

import ch.sbb.polarion.extension.generic.rest.filter.Secured;
import ch.sbb.polarion.extension.interceptor.model.ActionHook;

import javax.ws.rs.Path;
import java.util.List;

@Secured
@Path("/api")
public class ApiController extends InternalController {

    @Override
    public List<ActionHook> getHooksList(Boolean reload) {
        return polarionService.callPrivileged(() -> super.getHooksList(reload));
    }
}
