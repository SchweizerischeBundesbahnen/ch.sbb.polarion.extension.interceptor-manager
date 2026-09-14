package ch.sbb.polarion.extension.interceptor_manager.settings;

import ch.sbb.polarion.extension.generic.context.CurrentContextConfig;
import ch.sbb.polarion.extension.generic.context.CurrentContextExtension;
import ch.sbb.polarion.extension.generic.settings.NamedSettings;
import ch.sbb.polarion.extension.generic.settings.SettingId;
import ch.sbb.polarion.extension.generic.settings.SettingsService;
import ch.sbb.polarion.extension.interceptor_manager.model.IActionHook;
import ch.sbb.polarion.extension.interceptor_manager.model.RequireSettingEntries;
import ch.sbb.polarion.extension.interceptor_manager.rest.exception.SettingsValidationException;
import ch.sbb.polarion.extension.interceptor_manager.util.SettingEntriesValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static ch.sbb.polarion.extension.generic.settings.GenericNamedSettings.DEFAULT_SCOPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, CurrentContextExtension.class})
@CurrentContextConfig("interceptor-manager")
class HookSettingsTest {

    @Test
    void savingStampsTheInstalledVersionAndValidates() {
        IActionHook hook = mock(IActionHook.class);
        HookModel hookModel = mock(HookModel.class);
        HookSettings settings = new HookSettings(hook, mock(SettingsService.class));

        settings.beforeSave(hookModel);
        verify(hook, times(1)).validateSettings(any());
    }

    @Test
    void aRejectedSaveCarriesEveryProblem() {
        IActionHook hook = mock(IActionHook.class);
        HookModel hookModel = mock(HookModel.class);
        HookSettings settings = new HookSettings(hook, mock(SettingsService.class));
        when(hook.validateSettings(any())).thenReturn("Some validation error");

        SettingsValidationException exception = assertThrows(SettingsValidationException.class, () -> settings.beforeSave(hookModel));

        assertEquals(List.of("Some validation error"), exception.getValidationErrors());
        assertTrue(exception.getMessage().contains("Some validation error"));
    }

    @Test
    void readingAttachesTheValidationResult() {
        // Nothing is stored, so the read answers the hook's own defaults - which the required entry misses.
        IActionHook hook = mock(IActionHook.class, withSettings().extraInterfaces(RequireSettingEntries.class));
        when(hook.getName()).thenReturn("TestHook");
        when(hook.getDefaultSettings()).thenReturn("somethingElse=1");
        when(((RequireSettingEntries) hook).getRequiredSettingEntryNames()).thenReturn(List.of("projects"));
        HookSettings settings = new HookSettings(hook, mock(SettingsService.class));

        HookModel model = settings.read(DEFAULT_SCOPE, SettingId.fromName(NamedSettings.DEFAULT_NAME), null);

        assertEquals(List.of(SettingEntriesValidator.MISSING_ENTRY_MESSAGE.formatted("projects")), model.getValidationErrors());
    }
}
