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
    protected @NotNull Set<Class<?>> getExtensionControllerClasses() {
        return Set.of(
                HooksSettingsInternalController.class,
                HooksSettingsApiController.class,
                HooksInternalController.class,
                HooksApiController.class
        );
    }

    @Override
    protected @NotNull Set<Class<?>> getGenericControllerClasses() {
        // only the public NamedSettingsApiController is dropped - hook settings are exposed through this
        // extension's own /internal endpoints instead. NamedSettingsInternalController stays: HooksSettingsInternalController
        // delegates every settings call to it (see its `settingsController` field).
        return super.getGenericControllerClasses().stream().filter(c -> !NamedSettingsApiController.class.equals(c)).collect(Collectors.toSet());
    }
}
