package ch.sbb.polarion.extension.interceptor_manager.model;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HooksRegistryTest {

    @Test
    public void shouldThrowExceptionForHookLoadedNotFromJarWithoutVersion() {
        assertThatThrownBy(() -> HooksRegistry.HOOKS.addHook(new MyActionHook()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldThrowExceptionForHookWithVersion() {
        assertThatThrownBy(() -> HooksRegistry.HOOKS.addHook(new MyActionHook("test version")))
                .isInstanceOf(IllegalStateException.class);
    }

    private static class MyActionHook extends ActionHook {
        public MyActionHook() {
            this(null);
        }

        public MyActionHook(String version) {
            super(ItemType.PLAN, ActionType.DELETE, version, "Test hook");
        }

        @Override
        public @NotNull HookExecutor getExecutor() {
            return new HookExecutor() {
            };
        }

        @Override
        public String getDefaultSettings() {
            return "";
        }
    }
}