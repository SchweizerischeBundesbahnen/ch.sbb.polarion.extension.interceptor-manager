package ch.sbb.polarion.extension.interceptor_manager.model;

import ch.sbb.polarion.extension.generic.settings.NamedSettingsRegistry;
import ch.sbb.polarion.extension.interceptor_manager.guice.GuiceActionHooksFactory;
import ch.sbb.polarion.extension.interceptor_manager.osgi.OSGiUtils;
import ch.sbb.polarion.extension.interceptor_manager.settings.HookSettings;
import ch.sbb.polarion.extension.interceptor_manager.util.HookJarUtils;
import com.polarion.core.util.logging.Logger;

import java.util.ArrayList;
import java.util.List;

public enum HooksRegistry {

    HOOKS;

    private static final Logger logger = Logger.getLogger(HooksRegistry.class);

    private final List<IActionHook> hooks = new ArrayList<>();

    public synchronized void refresh() {
        logger.info("Loading hooks list...");
        hooks.clear();
        hooks.addAll(HookJarUtils.loadHooks());
        hooks.addAll(OSGiUtils.lookupOSGiService(IActionHook.class));
        hooks.addAll(GuiceActionHooksFactory.build().getActionHooks());
        logger.info(hooks.size() + " hooks loaded.");
        NamedSettingsRegistry.INSTANCE.getAll().clear();
        hooks.forEach(hook -> NamedSettingsRegistry.INSTANCE.getAll().add(new HookSettings(hook)));
    }

    public synchronized void addHook(IActionHook actionHook) {
        logger.info("Adding hook: " + actionHook.getClass().getSimpleName());
        hooks.add(actionHook);
        logger.info("New number of hooks: " + hooks.size());
        NamedSettingsRegistry.INSTANCE.getAll().add(new HookSettings(actionHook));
    }

    public List<IActionHook> list() {
        return new ArrayList<>(hooks);
    }
}
