package ch.sbb.polarion.extension.interceptor_manager.rest;

import ch.sbb.polarion.extension.generic.rest.GenericRestApplication;
import ch.sbb.polarion.extension.generic.rest.controller.settings.NamedSettingsApiController;
import ch.sbb.polarion.extension.interceptor_manager.model.HooksRegistry;
import ch.sbb.polarion.extension.interceptor_manager.rest.controller.HooksApiController;
import ch.sbb.polarion.extension.interceptor_manager.rest.controller.HooksInternalController;
import ch.sbb.polarion.extension.interceptor_manager.rest.controller.HooksSettingsApiController;
import ch.sbb.polarion.extension.interceptor_manager.rest.controller.HooksSettingsInternalController;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

public class InterceptorManagerRestApplication extends GenericRestApplication {

    public InterceptorManagerRestApplication() {
        // Refresh actions hooks registry to process guice hooks after Google Guice platform was initialized
        HooksRegistry.HOOKS.refresh();
    }

    @Override
    protected @NotNull Set<Object> getExtensionControllerSingletons() {
        return Set.of(
                new HooksSettingsInternalController(),
                new HooksSettingsApiController(),
                new HooksInternalController(),
                new HooksApiController()
        );
    }

    @Override
    protected @NotNull Set<Object> getGenericControllerSingletons() {
        // we do not remove NamedSettingsInternalController because it is still needed for fetching revisions list (in common.js from generic extension)
        return super.getGenericControllerSingletons().stream().filter(c -> !(c instanceof NamedSettingsApiController)).collect(Collectors.toSet());
    }
}
