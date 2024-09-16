package ch.sbb.polarion.extension.interceptor_manager.rest.controller;

import ch.sbb.polarion.extension.generic.rest.filter.Secured;
import ch.sbb.polarion.extension.interceptor_manager.model.IActionHook;

import javax.ws.rs.Path;
import java.util.List;

@Secured
@Path("/api")
public class HooksApiController extends HooksInternalController {

    @Override
    public List<IActionHook> getHooksList(Boolean reload) {
        return polarionService.callPrivileged(() -> super.getHooksList(reload));
    }
}
