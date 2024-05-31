package ch.sbb.polarion.extension.interceptor.rest;

import ch.sbb.polarion.extension.generic.rest.GenericRestApplication;
import ch.sbb.polarion.extension.generic.rest.controller.NamedSettingsApiController;
import ch.sbb.polarion.extension.generic.rest.controller.NamedSettingsApiScopeAgnosticController;
import ch.sbb.polarion.extension.generic.rest.controller.NamedSettingsInternalController;
import ch.sbb.polarion.extension.interceptor.rest.controller.ApiController;
import ch.sbb.polarion.extension.interceptor.rest.controller.HooksSettingsApiController;
import ch.sbb.polarion.extension.interceptor.rest.controller.HooksSettingsInternalController;
import ch.sbb.polarion.extension.interceptor.rest.controller.InternalController;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class InterceptorRestApplication extends GenericRestApplication {

    @Override
    protected @NotNull Set<Class<?>> getControllerClasses() {
        Set<Class<?>> classes = super.getControllerClasses();
        classes.add(HooksSettingsInternalController.class);
        classes.add(HooksSettingsApiController.class);
        //standard settings internal controller still needed for fetching revisions list
        classes.add(NamedSettingsInternalController.class);
        //remove other standard settings controllers
        classes.remove(NamedSettingsApiController.class);
        classes.remove(NamedSettingsApiScopeAgnosticController.class);
        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        return Set.of(
                new InternalController(),
                new ApiController()
        );
    }
}
