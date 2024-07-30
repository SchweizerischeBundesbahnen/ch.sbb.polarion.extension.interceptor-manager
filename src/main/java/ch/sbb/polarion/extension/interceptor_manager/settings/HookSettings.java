package ch.sbb.polarion.extension.interceptor_manager.settings;

import ch.sbb.polarion.extension.generic.settings.GenericNamedSettings;
import ch.sbb.polarion.extension.interceptor_manager.model.ActionHook;
import org.jetbrains.annotations.NotNull;

public class HookSettings extends GenericNamedSettings<HookModel> {
    private final ActionHook hook;

    public HookSettings(ActionHook hook) {
        super(hook.getName());
        this.hook = hook;
    }

    @Override
    public void beforeSave(@NotNull HookModel what) {
        what.setHookVersion(hook.getVersion());
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
