package ch.sbb.polarion.extension.interceptor.rest;

import ch.sbb.polarion.extension.interceptor.rest.controller.ExtensionInfoApiController;
import ch.sbb.polarion.extension.interceptor.rest.controller.ExtensionInfoInternalController;
import ch.sbb.polarion.extension.interceptor.rest.controller.SettingsApiController;
import ch.sbb.polarion.extension.interceptor.rest.controller.SettingsInternalController;
import ch.sbb.polarion.extension.interceptor.rest.controller.SwaggerController;
import ch.sbb.polarion.extension.interceptor.rest.controller.SwaggerDefinitionController;
import ch.sbb.polarion.extension.interceptor.rest.exception.BadRequestExceptionMapper;
import ch.sbb.polarion.extension.interceptor.rest.exception.ForbiddenExceptionMapper;
import ch.sbb.polarion.extension.interceptor.rest.exception.IllegalArgumentExceptionMapper;
import ch.sbb.polarion.extension.interceptor.rest.exception.InternalServerErrorExceptionMapper;
import ch.sbb.polarion.extension.interceptor.rest.exception.NotFoundExceptionMapper;
import ch.sbb.polarion.extension.interceptor.rest.exception.UncaughtExceptionMapper;
import ch.sbb.polarion.extension.interceptor.rest.filter.AuthenticationFilter;
import ch.sbb.polarion.extension.interceptor.rest.filter.LogoutFilter;
import ch.sbb.polarion.extension.interceptor.rest.controller.ApiController;
import ch.sbb.polarion.extension.interceptor.rest.controller.InternalController;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.jetbrains.annotations.NotNull;

import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class InterceptorRestApplication extends Application {

    @Override
    @NotNull
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.addAll(getExceptionMappers());
        classes.addAll(getFilters());
        classes.addAll(getControllerClasses());
        classes.add(JacksonFeature.class);
        return classes;
    }

    @NotNull
    protected Set<Class<?>> getExceptionMappers() {
        return new HashSet<>(Arrays.asList(
                BadRequestExceptionMapper.class,
                IllegalArgumentExceptionMapper.class,
                InternalServerErrorExceptionMapper.class,
                ForbiddenExceptionMapper.class,
                NotFoundExceptionMapper.class,
                UncaughtExceptionMapper.class
        ));
    }

    @NotNull
    protected Set<Class<?>> getFilters() {
        return new HashSet<>(Arrays.asList(
                AuthenticationFilter.class,
                LogoutFilter.class
        ));
    }

    @NotNull
    protected Set<Class<?>> getControllerClasses() {
        return new HashSet<>(Arrays.asList(
                ExtensionInfoApiController.class,
                ExtensionInfoInternalController.class,
                SwaggerController.class,
                SwaggerDefinitionController.class,
                SettingsApiController.class,
                SettingsInternalController.class,
                ApiController.class,
                InternalController.class
        ));
    }
}
