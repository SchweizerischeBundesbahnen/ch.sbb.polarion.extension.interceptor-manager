package ch.sbb.polarion.extension.interceptor_manager.settings;

import ch.sbb.polarion.extension.generic.settings.GenericNamedSettings;
import ch.sbb.polarion.extension.generic.settings.SettingId;
import ch.sbb.polarion.extension.generic.settings.SettingsService;
import ch.sbb.polarion.extension.interceptor_manager.model.IActionHook;
import ch.sbb.polarion.extension.interceptor_manager.rest.exception.SettingsValidationException;
import ch.sbb.polarion.extension.interceptor_manager.util.SettingEntriesValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;

public class HookSettings extends GenericNamedSettings<HookModel> {
    private final IActionHook hook;

    public HookSettings(IActionHook hook) {
        super(hook.getName());
        this.hook = hook;
    }

    @VisibleForTesting
    public HookSettings(IActionHook hook, SettingsService settingsService) {
        super(hook.getName(), settingsService);
        this.hook = hook;
    }

    /**
     * Attaches the validation result to what was read, so that the administration page reports settings which
     * miss an entry the installed hook version needs as soon as the hook is opened. Reading never fails on it:
     * settings written by an older version stay visible and editable, they are only marked unusable.
     */
    @Override
    public @NotNull HookModel read(@NotNull String scope, @NotNull SettingId id, @Nullable String revisionName) {
        HookModel model = super.read(scope, id, revisionName);
        model.setValidationErrors(SettingEntriesValidator.validateRequiredEntries(hook, model));
        return model;
    }

    @Override
    public void beforeSave(@NotNull HookModel what) {
        what.setHookVersion(hook.getVersion());
        List<String> validationErrors = SettingEntriesValidator.validateForSave(hook, what);
        if (!validationErrors.isEmpty()) {
            throw new SettingsValidationException(validationErrors);
        }
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
