package ch.sbb.polarion.extension.interceptor.util;

import ch.sbb.polarion.extension.interceptor.rest.model.Context;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.jar.Attributes;

@UtilityClass
public class ContextUtils {

    public static final String EXTENSION_CONTEXT = "Extension-Context";

    @NotNull
    public static Context getContext() {
        final Attributes attributes = ManifestUtils.getManifestAttributes();
        String extensionContext = attributes.getValue(EXTENSION_CONTEXT);
        return new Context(extensionContext);
    }
}
