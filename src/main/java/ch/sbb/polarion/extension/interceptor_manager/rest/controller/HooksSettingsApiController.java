package ch.sbb.polarion.extension.interceptor_manager.rest.controller;

import ch.sbb.polarion.extension.generic.rest.filter.Secured;
import ch.sbb.polarion.extension.generic.service.PolarionService;
import ch.sbb.polarion.extension.generic.settings.Revision;
import ch.sbb.polarion.extension.generic.settings.SettingsModel;
import org.jetbrains.annotations.NotNull;

import javax.ws.rs.Path;
import java.util.List;

@Secured
@Path("/api")
public class HooksSettingsApiController extends HooksSettingsInternalController {

    private final PolarionService polarionService = new PolarionService();

    @Override
    public @NotNull List<Revision> readRevisionsList(String hook) {
        return polarionService.callPrivileged(() -> super.readRevisionsList(hook));
    }

    @Override
    public SettingsModel readSetting(String hook, String revision) {
        return polarionService.callPrivileged(() -> super.readSetting(hook, revision));
    }

    @Override
    public void saveSetting(String hook, final String content) {
        polarionService.callPrivileged(() -> super.saveSetting(hook, content));
    }

    @Override
    public SettingsModel getDefaultValues(String hook) {
        return polarionService.callPrivileged(() -> super.getDefaultValues(hook));
    }
}
