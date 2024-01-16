package ch.sbb.polarion.extension.interceptor.rest.controller;

import ch.sbb.polarion.extension.interceptor.rest.filter.Secured;
import ch.sbb.polarion.extension.interceptor.rest.model.Context;
import ch.sbb.polarion.extension.interceptor.rest.model.Version;

import javax.ws.rs.Path;

@Secured
@Path("/api")
public class ExtensionInfoApiController extends ExtensionInfoInternalController {

    public Context getContext() {
        return super.getContext();
    }

    public Version getVersion() {
        return super.getVersion();
    }
}
