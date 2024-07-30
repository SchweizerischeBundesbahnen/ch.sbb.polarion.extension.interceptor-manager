package ch.sbb.polarion.extension.interceptor_manager.rest;

import ch.sbb.polarion.extension.generic.rest.GenericRestApplication;
import ch.sbb.polarion.extension.generic.rest.controller.NamedSettingsInternalController;
import ch.sbb.polarion.extension.interceptor_manager.rest.controller.HooksApiController;
import ch.sbb.polarion.extension.interceptor_manager.rest.controller.HooksInternalController;
import ch.sbb.polarion.extension.interceptor_manager.rest.controller.HooksSettingsApiController;
import ch.sbb.polarion.extension.interceptor_manager.rest.controller.HooksSettingsInternalController;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class InterceptorManagerRestApplication extends GenericRestApplication {

    @Override
    protected @NotNull Set<Object> getExtensionControllerSingletons() {
        return Set.of(
                new HooksSettingsInternalController(),
                new HooksSettingsApiController(),
                new NamedSettingsInternalController(), // standard settings internal controller still needed for fetching revisions list (in common.js from generic extension)
                new HooksInternalController(),
                new HooksApiController()
        );
    }

}
