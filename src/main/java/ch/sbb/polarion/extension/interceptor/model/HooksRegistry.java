package ch.sbb.polarion.extension.interceptor.model;

import ch.sbb.polarion.extension.interceptor.settings.HookSettings;
import ch.sbb.polarion.extension.interceptor.settings.SettingsRegistry;
import ch.sbb.polarion.extension.interceptor.util.HookJarUtils;
import com.polarion.core.util.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public enum HooksRegistry {

    HOOKS;

    private static final Logger logger = Logger.getLogger(HooksRegistry.class);

    private final List<ActionHook> hooks = new ArrayList<>();

    public synchronized void refresh() {
        logger.info("Loading hooks list...");
        hooks.clear();
        hooks.addAll(HookJarUtils.loadHooks());
        logger.info(hooks.size() + " hooks loaded.");
        SettingsRegistry.INSTANCE
                .register(HooksRegistry.HOOKS.list().stream().map(HookSettings::new).collect(Collectors.toList()));
    }

    public List<ActionHook> list() {
        return new ArrayList<>(hooks);
    }
}
