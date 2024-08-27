package ch.sbb.polarion.extension.interceptor_manager.settings;

import ch.sbb.polarion.extension.generic.settings.GenericNamedSettings;
import ch.sbb.polarion.extension.generic.settings.SettingsService;
import ch.sbb.polarion.extension.interceptor_manager.model.ActionHook;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Optional;

public class HookSettings extends GenericNamedSettings<HookModel> {
    private final ActionHook hook;

    public HookSettings(ActionHook hook) {
        super(hook.getName());
        this.hook = hook;
    }

    @VisibleForTesting
    public HookSettings(ActionHook hook, SettingsService settingsService) {
        super(hook.getName(), settingsService);
        this.hook = hook;
    }

    @Override
    public void beforeSave(@NotNull HookModel what) {
        what.setHookVersion(hook.getVersion());
        Optional.ofNullable(hook.validateSettings(what)).ifPresent(error -> {
            throw new IllegalArgumentException(error);
        });
    }

    @Override
    public void afterSave(@NotNull HookModel what) {
        hook.loadSettings(true);
    }

    @Override
    public @NotNull HookModel defaultValues() {
        return HookModel.builder()
                .enabled(false)
                .hookVersion(hook.getVersion())
                .properties(hook.getDefaultSettings())
                .build();
    }
}
