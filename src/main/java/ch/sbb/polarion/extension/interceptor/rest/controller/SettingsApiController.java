package ch.sbb.polarion.extension.interceptor.rest.controller;

import ch.sbb.polarion.extension.interceptor.rest.filter.Secured;
import ch.sbb.polarion.extension.interceptor.settings.Revision;
import ch.sbb.polarion.extension.interceptor.settings.SettingsModel;
import org.jetbrains.annotations.NotNull;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.List;

@Secured
@Path("/api")
public class SettingsApiController extends SettingsInternalController {

    @Override
    public SettingsModel readSetting(String hook, String revision) {
        return polarionService.callPrivileged(() -> super.readSetting(hook, revision));
    }

    @Override
    public Response saveSetting(String hook, String content) {
        return polarionService.callPrivileged(() -> super.saveSetting(hook, content));
    }

    @Override
    public SettingsModel readDefaultSetting(String hook) {
        return polarionService.callPrivileged(() -> super.readDefaultSetting(hook));
    }

    @Override
    public @NotNull List<Revision> readRevisionsList(String hook) {
        return polarionService.callPrivileged(() -> super.readRevisionsList(hook));
    }
}
