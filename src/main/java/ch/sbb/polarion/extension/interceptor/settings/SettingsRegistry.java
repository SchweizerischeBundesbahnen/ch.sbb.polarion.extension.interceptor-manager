package ch.sbb.polarion.extension.interceptor.settings;

import javax.ws.rs.NotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum SettingsRegistry {
    INSTANCE;

    private final Set<HookSettings> settingsSet = new HashSet<>();

    /**
     * Register a new setting. It's a good idea to call it before REST application initialization.
     */
    public synchronized void register(List<HookSettings> settingsList) {
        settingsSet.clear();
        settingsSet.addAll(settingsList);
    }

    public synchronized HookSettings getByHookName(String hookName) {
        return settingsSet.stream()
                .filter(s -> s.getHookName().equals(hookName))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No settings found by hookName: " + hookName));
    }
}
