package ch.sbb.polarion.extension.interceptor.rest;

import ch.sbb.polarion.extension.generic.rest.GenericRestApplication;
import ch.sbb.polarion.extension.interceptor.rest.controller.HooksApiController;
import ch.sbb.polarion.extension.interceptor.rest.controller.HooksInternalController;
import ch.sbb.polarion.extension.interceptor.rest.controller.HooksSettingsApiController;
import ch.sbb.polarion.extension.interceptor.rest.controller.HooksSettingsInternalController;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class InterceptorRestApplication extends GenericRestApplication {

    @Override
    protected @NotNull Set<Object> getExtensionControllerSingletons() {
        return Set.of(
                new HooksSettingsInternalController(),
                new HooksSettingsApiController(),
                new HooksInternalController(),
                new HooksApiController()
        );
    }

}
