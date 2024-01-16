package ch.sbb.polarion.extension.interceptor.util;

import ch.sbb.polarion.extension.interceptor.model.ActionHook;
import com.polarion.core.util.logging.Logger;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarInputStream;

@UtilityClass
@SuppressWarnings({"rawtypes", "unchecked"})
public class HookJarUtils {

    private static final Logger logger = Logger.getLogger(HookJarUtils.class);
    private static final String HOOKS_DIR = "hooks";
    private static final String MAIN_CLASS_MANIFEST_ATTRIBUTE = "Main-Class";

    public List<ActionHook> loadHooks() {
        List<ActionHook> hooks = new ArrayList<>();

        for (Class hookClass : loadClasses(new File(getRootDir(), HOOKS_DIR).getPath())) {
            if (!ActionHook.class.isAssignableFrom(hookClass)) {
                logger.info(hookClass.getName() + " isn't a hook");
                continue;
            }

            try {
                ActionHook actionHook = (ActionHook) hookClass.getConstructor().newInstance();
                hooks.add(actionHook);
                logger.info("Loaded hook " + actionHook.getName());
            } catch (Exception e) {
                logger.error("Cannot load hook", e);
            }
        }
        return hooks;
    }

    private List<Class> loadClasses(String jarsDirPath) {
        List<Class> classList = new ArrayList<>();

        File[] jarFiles = new File(jarsDirPath).listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));

        if (jarFiles != null) {
            for (File jarFile : jarFiles) {
                try {
                    classList.add(loadClass(jarFile));
                } catch (Throwable e) {
                    logger.error("Cannot load jar file " + jarFile.getPath(), e);
                }
            }
        }
        return classList;
    }

    private Class loadClass(File jarFile) throws IOException, ClassNotFoundException {
        try (JarInputStream jarInputStream = new JarInputStream(new FileInputStream(jarFile))) {
            String mainClassName = jarInputStream.getManifest().getMainAttributes().getValue(MAIN_CLASS_MANIFEST_ATTRIBUTE);
            if (mainClassName != null) {
                //closing urlClassLoader allows us to remove initial jar file after its load
                try (URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, HookJarUtils.class.getClassLoader())) {
                    return Class.forName(mainClassName, true, urlClassLoader);
                }
            } else {
                throw new ClassNotFoundException("Jar file " + jarFile.getPath() + " does not contain " + MAIN_CLASS_MANIFEST_ATTRIBUTE + " attribute in the MANIFEST");
            }
        }
    }

    private File getRootDir() {
        URL codeLocationURL = HookJarUtils.class.getProtectionDomain().getCodeSource().getLocation();
        return new File(codeLocationURL.getPath()).getParentFile();
    }
}
