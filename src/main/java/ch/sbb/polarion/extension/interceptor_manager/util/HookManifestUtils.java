package ch.sbb.polarion.extension.interceptor_manager.util;

import ch.sbb.polarion.extension.interceptor_manager.model.ActionHook;
import com.polarion.core.util.logging.Logger;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

@UtilityClass
@SuppressWarnings("unused")
public class HookManifestUtils {
    private static final Logger logger = Logger.getLogger(HookManifestUtils.class);
    public static final String HOOK_VERSION = "Hook-Version";

    public static @Nullable String getHookVersion(@NotNull Class<? extends ActionHook> hookClass) {
        try {
            Manifest manifest = loadManifestByClass(hookClass);
            if (manifest != null) {
                Attributes attributes = manifest.getMainAttributes();
                return attributes.getValue(HOOK_VERSION);
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("Cannot get hook version from Manifest: " + e.getMessage(), e);
            return null;
        }
    }

    public static @Nullable Manifest loadManifestByClass(@NotNull Class<? extends ActionHook> hookClass) throws IOException {
        CodeSource codeSource = hookClass.getProtectionDomain().getCodeSource();
        if (codeSource != null) {
            URL location = codeSource.getLocation();
            Path jarPath = Paths.get(location.getPath());

            if (Files.isRegularFile(jarPath)) {
                try (JarFile jarFile = new JarFile(jarPath.toFile())) {
                    return jarFile.getManifest();
                }
            } else {
                throw new IllegalArgumentException("The class %s is not loaded from a JAR file.".formatted(hookClass.getName()));
            }
        } else {
            throw new IllegalArgumentException("Could not determine the code source location.");
        }
    }
}
