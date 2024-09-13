package ch.sbb.polarion.extension.interceptor_manager.model;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HooksRegistryTest {

    @Test
    public void shouldThrowExceptionForHookLoadedNotFromJar() {
        assertThatThrownBy(() -> HooksRegistry.HOOKS.addHook(new ActionHook(ActionHook.ItemType.PLAN, ActionHook.ActionType.DELETE, "Test hook") {
            @Override
            public @NotNull HookExecutor getExecutor() {
                return new HookExecutor() {};
            }

            @Override
            public String getDefaultSettings() {
                return "";
            }
        }))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("is not loaded from a JAR file");
    }
}