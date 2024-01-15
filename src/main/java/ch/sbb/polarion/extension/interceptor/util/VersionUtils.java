package ch.sbb.polarion.extension.interceptor.util;

import ch.sbb.polarion.extension.interceptor.rest.model.Version;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.jar.Attributes;

@UtilityClass
public class VersionUtils {

    public static final String BUNDLE_NAME = "Bundle-Name";
    public static final String BUNDLE_VENDOR = "Bundle-Vendor";
    public static final String AUTOMATIC_MODULE_NAME = "Automatic-Module-Name";
    public static final String BUNDLE_VERSION = "Bundle-Version";
    public static final String BUNDLE_BUILD_TIMESTAMP = "Bundle-Build-Timestamp";

    @NotNull
    public static Version getVersion() {
        final Attributes attributes = ManifestUtils.getManifestAttributes();
        String bundleName = attributes.getValue(BUNDLE_NAME);
        String bundleVendor = attributes.getValue(BUNDLE_VENDOR);
        String automaticModuleName = attributes.getValue(AUTOMATIC_MODULE_NAME);
        String bundleVersion = attributes.getValue(BUNDLE_VERSION);
        String bundleBuildTimestamp = attributes.getValue(BUNDLE_BUILD_TIMESTAMP);
        return new Version(bundleName, bundleVendor, automaticModuleName, bundleVersion, bundleBuildTimestamp);
    }

}
