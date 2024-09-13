package ch.sbb.polarion.extension.interceptor_manager.util;

import ch.sbb.polarion.extension.interceptor_manager.model.ActionHook;
import ch.sbb.polarion.extension.interceptor_manager.model.IActionHook;
import com.polarion.core.util.logging.Logger;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

@UtilityClass
@SuppressWarnings({"rawtypes", "unchecked"})
public class HookJarUtils {

    private static final Logger logger = Logger.getLogger(HookJarUtils.class);
    private static final String HOOKS_DIR = "hooks";
    private static final String MAIN_CLASS_MANIFEST_ATTRIBUTE = "Main-Class";

    public List<IActionHook> loadHooks() {
        List<IActionHook> hooks = new ArrayList<>();

        for (Class hookClass : loadClasses(new File(getRootDir(), HOOKS_DIR).getPath())) {
            if (!ActionHook.class.isAssignableFrom(hookClass)) {
                logger.info(hookClass.getName() + " isn't a hook");
                continue;
            }

            try {
                IActionHook actionHook = (IActionHook) hookClass.getConstructor().newInstance();
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
                    classList.add(loadJarReturnHookClass(jarFile));
                } catch (Exception e) {
                    logger.error("Cannot load jar file " + jarFile.getPath(), e);
                }
            }
        }
        return classList;
    }

    private Class loadJarReturnHookClass(File jarFile) throws IOException, ClassNotFoundException {
        try (JarInputStream jarInputStream = new JarInputStream(new FileInputStream(jarFile));
             URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{new URL("jar:file:" + jarFile.getAbsolutePath() + "!/")}, HookJarUtils.class.getClassLoader())) {
            String mainClassName = jarInputStream.getManifest().getMainAttributes().getValue(MAIN_CLASS_MANIFEST_ATTRIBUTE);

            //load all classes from jar and return hook class which name must be declared in the manifest
            Class hookClass = null;
            JarEntry jarEntry;
            while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                if (jarEntry.getName().endsWith(".class")) {
                    String className = jarEntry.getName().replace("/", ".").replace(".class", "");
                    Class clazz = urlClassLoader.loadClass(className);
                    if (className.equals(mainClassName)) {
                        hookClass = clazz;
                    }
                }
            }

            if (hookClass == null) {
                throw new ClassNotFoundException("Jar file " + jarFile.getPath() + " does not contain " + MAIN_CLASS_MANIFEST_ATTRIBUTE + " attribute in the MANIFEST");
            } else {
                return hookClass;
            }
        }
    }

    private File getRootDir() {
        URL codeLocationURL = HookJarUtils.class.getProtectionDomain().getCodeSource().getLocation();
        return new File(codeLocationURL.getPath()).getParentFile();
    }
}
