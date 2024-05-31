package ch.sbb.polarion.extension.interceptor.model;

import ch.sbb.polarion.extension.generic.settings.NamedSettingsRegistry;
import ch.sbb.polarion.extension.interceptor.settings.HookSettings;
import ch.sbb.polarion.extension.interceptor.util.HookJarUtils;
import com.polarion.core.util.logging.Logger;

import java.util.ArrayList;
import java.util.List;

public enum HooksRegistry {

    HOOKS;

    private static final Logger logger = Logger.getLogger(HooksRegistry.class);

    private final List<ActionHook> hooks = new ArrayList<>();

    public synchronized void refresh() {
        logger.info("Loading hooks list...");
        hooks.clear();
        hooks.addAll(HookJarUtils.loadHooks());
        logger.info(hooks.size() + " hooks loaded.");
        NamedSettingsRegistry.INSTANCE.getAll().clear();
        hooks.forEach(hook -> NamedSettingsRegistry.INSTANCE.getAll().add(new HookSettings(hook)));
    }

    public List<ActionHook> list() {
        return new ArrayList<>(hooks);
    }
}
