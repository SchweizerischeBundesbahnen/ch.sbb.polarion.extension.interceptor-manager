package ch.sbb.polarion.extension.interceptor_manager.util;

import ch.sbb.polarion.extension.interceptor_manager.model.ActionHook;
import ch.sbb.polarion.extension.interceptor_manager.model.HookExecutor;
import ch.sbb.polarion.extension.interceptor_manager.model.IActionHook;
import ch.sbb.polarion.extension.interceptor_manager.model.RequireSettingEntries;
import ch.sbb.polarion.extension.interceptor_manager.settings.HookModel;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class SettingEntriesValidatorTest {

    @ParameterizedTest
    @CsvSource({
            "projects, projects, true",
            "projects, projectsX, false",
            "projects, types.projects, false",
            "types.*, types.*, true",
            "types.*, types.myProject, true",
            "types.*, types, false",
            "types.*, types.myProject.myType, false",
            "types.*, otherTypes.myProject, false",
            "bypassProjectRoles.*.*, bypassProjectRoles.myProject.myRole, true",
            "bypassProjectRoles.*.*, bypassProjectRoles.myProject, false"
    })
    void matchesComparesSegmentBySegment(String requiredName, String key, boolean expected) {
        assertEquals(expected, SettingEntriesValidator.matches(requiredName, key));
    }

    @Test
    void hookWithoutDeclaredEntriesIsNeverReported() {
        HookModel model = new HookModel(true, "1.0.0", "anything=goes");

        assertTrue(SettingEntriesValidator.validate(new TestHook(), model).isEmpty());
    }

    @Test
    void completeSettingsAreAccepted() {
        HookModel model = new HookModel(true, "1.0.0", """
                projects=*
                types.myProject=task,defect
                errorMessage=nope
                """);

        assertTrue(SettingEntriesValidator.validate(new RequiringTestHook("projects", "types.*", "errorMessage"), model).isEmpty());
    }

    @Test
    void everyMissingEntryIsReportedOnce() {
        HookModel model = new HookModel(true, "1.0.0", "projects=*");

        List<String> errors = SettingEntriesValidator.validate(new RequiringTestHook("projects", "types.*", "errorMessage"), model);

        assertEquals(List.of(
                SettingEntriesValidator.MISSING_MATCHING_ENTRY_MESSAGE.formatted("types.*"),
                SettingEntriesValidator.MISSING_ENTRY_MESSAGE.formatted("errorMessage")
        ), errors);
    }

    @Test
    void aCommentedOutEntryDoesNotCount() {
        // The administrator who comments an entry out has not configured it - the hook still misses its value.
        HookModel model = new HookModel(true, "1.0.0", "# projects=*");

        assertEquals(List.of(SettingEntriesValidator.MISSING_ENTRY_MESSAGE.formatted("projects")),
                SettingEntriesValidator.validate(new RequiringTestHook("projects"), model));
    }

    @Test
    void blankDeclaredNamesAreIgnored() {
        HookModel model = new HookModel(true, "1.0.0", "projects=*");

        assertTrue(SettingEntriesValidator.validate(new RequiringTestHook("projects", " "), model).isEmpty());
    }

    @Test
    void theHooksOwnValidationMessageIsAppended() {
        HookModel model = new HookModel(true, "1.0.0", "");
        RequiringTestHook hook = new RequiringTestHook("projects") {
            @Override
            public String validateSettings(HookModel ignored) {
                return "Some validation error";
            }
        };

        assertEquals(List.of(SettingEntriesValidator.MISSING_ENTRY_MESSAGE.formatted("projects"), "Some validation error"),
                SettingEntriesValidator.validate(hook, model));
    }

    @Test
    void nothingIsReportedWithoutAHookOrAModel() {
        assertTrue(SettingEntriesValidator.validate(null, new HookModel(true, "1.0.0", "")).isEmpty());
        assertTrue(SettingEntriesValidator.validate(mock(IActionHook.class), null).isEmpty());
    }

    @Test
    void nullPropertiesAreTreatedAsNoEntries() {
        assertFalse(SettingEntriesValidator.validate(new RequiringTestHook("projects"), new HookModel(true, "1.0.0", null)).isEmpty());
    }

    private static class TestHook extends ActionHook {
        TestHook() {
            super(ItemType.WORKITEM, ActionType.SAVE, "1.0.0", "description");
        }

        @Override
        public @NotNull HookExecutor getExecutor() {
            return new HookExecutor() {
                // just empty executor
            };
        }

        @Override
        public String getDefaultSettings() {
            return "";
        }
    }

    private static class RequiringTestHook extends TestHook implements RequireSettingEntries {
        private final List<String> requiredSettingEntryNames;

        RequiringTestHook(String... requiredSettingEntryNames) {
            this.requiredSettingEntryNames = Arrays.asList(requiredSettingEntryNames);
        }

        @Override
        public @NotNull List<String> getRequiredSettingEntryNames() {
            return requiredSettingEntryNames;
        }
    }
}
