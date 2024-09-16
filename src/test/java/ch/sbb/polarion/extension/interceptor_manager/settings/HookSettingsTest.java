package ch.sbb.polarion.extension.interceptor_manager.settings;

import ch.sbb.polarion.extension.generic.settings.SettingsService;
import ch.sbb.polarion.extension.interceptor_manager.model.IActionHook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HookSettingsTest {

    @Test
    public void testValidate() {
        IActionHook hook = mock(IActionHook.class);

        HookModel hookModel = mock(HookModel.class);
        HookSettings settings = new HookSettings(hook, mock(SettingsService.class));

        settings.beforeSave(hookModel);
        verify(hook, times(1)).validateSettings(any());

        when(hook.validateSettings(any())).thenReturn("Some validation error");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> settings.beforeSave(hookModel));
        assertEquals("Some validation error", exception.getMessage());
        verify(hook, times(2)).validateSettings(any());
    }
}