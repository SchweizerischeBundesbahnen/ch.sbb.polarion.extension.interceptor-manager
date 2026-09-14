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
import java.util.concurrent.atomic.AtomicBoolean;

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

        assertTrue(SettingEntriesValidator.validateRequiredEntries(new TestHook(), model).isEmpty());
    }

    @Test
    void completeSettingsAreAccepted() {
        HookModel model = new HookModel(true, "1.0.0", """
                projects=*
                types.myProject=task,defect
                errorMessage=nope
                """);

        assertTrue(SettingEntriesValidator.validateRequiredEntries(new RequiringTestHook("projects", "types.*", "errorMessage"), model).isEmpty());
    }

    @Test
    void everyMissingEntryIsReportedOnce() {
        HookModel model = new HookModel(true, "1.0.0", "projects=*");

        List<String> errors = SettingEntriesValidator.validateRequiredEntries(new RequiringTestHook("projects", "types.*", "errorMessage"), model);

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
                SettingEntriesValidator.validateRequiredEntries(new RequiringTestHook("projects"), model));
    }

    @Test
    void blankDeclaredNamesAreIgnored() {
        HookModel model = new HookModel(true, "1.0.0", "projects=*");

        assertTrue(SettingEntriesValidator.validateRequiredEntries(new RequiringTestHook("projects", " "), model).isEmpty());
    }

    @Test
    void theHooksOwnValidationMessageIsAppendedOnTheSavePath() {
        HookModel model = new HookModel(true, "1.0.0", "");
        RequiringTestHook hook = new RequiringTestHook("projects") {
            @Override
            public String validateSettings(HookModel ignored) {
                return "Some validation error";
            }
        };

        assertEquals(List.of(SettingEntriesValidator.MISSING_ENTRY_MESSAGE.formatted("projects"), "Some validation error"),
                SettingEntriesValidator.validateForSave(hook, model));
    }

    @Test
    void nothingIsReportedWithoutAHookOrAModel() {
        assertTrue(SettingEntriesValidator.validateRequiredEntries(null, new HookModel(true, "1.0.0", "")).isEmpty());
        assertTrue(SettingEntriesValidator.validateRequiredEntries(mock(IActionHook.class), null).isEmpty());
    }

    @Test
    void nullPropertiesAreTreatedAsNoEntries() {
        assertFalse(SettingEntriesValidator.validateRequiredEntries(new RequiringTestHook("projects"), new HookModel(true, "1.0.0", null)).isEmpty());
    }

    @Test
    void theHooksOwnValidationIsNotRunOnTheReadPath() {
        // It is arbitrary hook code documented as running when an administrator submits settings. The read
        // path is reached through loadSettings() while a work item is being saved, so it must stay out.
        HookModel model = new HookModel(true, "1.0.0", "projects=*");
        AtomicBoolean called = new AtomicBoolean(false);
        RequiringTestHook hook = new RequiringTestHook("projects") {
            @Override
            public String validateSettings(HookModel ignored) {
                called.set(true);
                return "Some validation error";
            }
        };

        assertEquals(List.of(), SettingEntriesValidator.validateRequiredEntries(hook, model));
        assertFalse(called.get());
    }

    @Test
    void aHookWhichDeclaresNullIsTreatedAsDeclaringNothing() {
        // @NotNull on the interface is documentation: the implementations come from separately built jars,
        // and an exception here would break the path which decides whether the hook runs at all.
        HookModel model = new HookModel(true, "1.0.0", "");
        RequiringTestHook hook = new RequiringTestHook() {
            @Override
            public @NotNull List<String> getRequiredSettingEntryNames() {
                return null;
            }
        };

        assertEquals(List.of(), SettingEntriesValidator.validateRequiredEntries(hook, model));
        assertEquals(List.of(), SettingEntriesValidator.validateForSave(hook, model));
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
