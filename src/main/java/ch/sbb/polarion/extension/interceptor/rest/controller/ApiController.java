package ch.sbb.polarion.extension.interceptor.rest.controller;

import ch.sbb.polarion.extension.interceptor.model.ActionHook;
import ch.sbb.polarion.extension.interceptor.rest.filter.Secured;

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
