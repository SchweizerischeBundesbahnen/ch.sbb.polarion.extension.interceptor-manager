package ch.sbb.polarion.extension.interceptor.util;

import ch.sbb.polarion.extension.interceptor.rest.model.Context;
import ch.sbb.polarion.extension.interceptor.rest.model.Version;
import lombok.Getter;

@Getter
public final class ExtensionInfo {

    private final Version version;
    private final Context context;

    private ExtensionInfo() {
        version = VersionUtils.getVersion();
        context = ContextUtils.getContext();
    }

    public static ExtensionInfo getInstance() {
        return ExtensionInfoHolder.INSTANCE;
    }

    private static class ExtensionInfoHolder {
        private static final ExtensionInfo INSTANCE = new ExtensionInfo();
    }
}
