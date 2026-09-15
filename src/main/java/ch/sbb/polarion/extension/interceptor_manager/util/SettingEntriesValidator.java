package ch.sbb.polarion.extension.interceptor_manager.util;

import ch.sbb.polarion.extension.interceptor_manager.model.ActionHook;
import ch.sbb.polarion.extension.interceptor_manager.model.IActionHook;
import ch.sbb.polarion.extension.interceptor_manager.model.RequireSettingEntries;
import ch.sbb.polarion.extension.interceptor_manager.settings.HookModel;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Checks the settings of a hook against the entries it declares via {@link RequireSettingEntries}.
 */
@UtilityClass
public class SettingEntriesValidator {

    public static final String MISSING_ENTRY_MESSAGE = "Settings must contain the entry \"%s\"";
    public static final String MISSING_MATCHING_ENTRY_MESSAGE = "Settings must contain an entry matching \"%s\", where \"*\" stands for any selector";

    private static final String SEGMENT_SEPARATOR_REGEX = "\\.";

    /**
     * Reports every entry the hook declares and the given settings miss. The list is empty when nothing is
     * missing, and for a hook which declares nothing.
     * <p>
     * The result is derived, never stored: it is recomputed on every read, so settings written by an older
     * hook version start reporting the entries that version did not know about.
     * <p>
     * Defensive throughout, because this runs on the read path and {@link HookModel} is read while a work
     * item is being saved: a hook jar built separately can return null where {@code @NotNull} says it will not.
     */
    @SuppressWarnings("java:S2583") // the null check below is dead code only if @NotNull holds, and nothing
    // enforces it at runtime: hooks are compiled into their own jars against this interface, so the annotation
    // is a contract for their authors rather than a guarantee for this method
    public @NotNull List<String> validateRequiredEntries(@Nullable IActionHook hook, @Nullable HookModel model) {
        if (model == null || !(hook instanceof RequireSettingEntries requiring)) {
            return List.of();
        }

        List<String> requiredNames = requiring.getRequiredSettingEntryNames();
        if (requiredNames == null) {
            return List.of();
        }

        List<String> errors = new ArrayList<>();
        Set<String> presentKeys = model.getPropertiesMap().keySet();
        for (String requiredName : requiredNames) {
            if (requiredName == null || requiredName.isBlank()) {
                continue; // a hook which declares nothing here checks nothing, rather than reporting an unnamed entry
            }
            if (presentKeys.stream().noneMatch(key -> matches(requiredName, key))) {
                errors.add(formatMissingEntry(requiredName));
            }
        }
        return List.copyOf(errors);
    }

    /**
     * The declared entries check plus whatever {@link IActionHook#validateSettings(HookModel)} reports.
     * <p>
     * Save path only. {@code validateSettings} is arbitrary hook code documented as running when an
     * administrator submits settings, so it must not be dragged onto the read path, which a hook reaches
     * through {@code loadSettings} while intercepting an ordinary save.
     */
    public @NotNull List<String> validateForSave(@Nullable IActionHook hook, @Nullable HookModel model) {
        if (hook == null || model == null) {
            return List.of();
        }

        List<String> errors = new ArrayList<>(validateRequiredEntries(hook, model));
        String validationMessage = hook.validateSettings(model);
        if (validationMessage != null && !validationMessage.isBlank()) {
            errors.add(validationMessage);
        }
        return List.copyOf(errors);
    }

    /**
     * Whether the given settings key satisfies the declared entry name. Both are split into dot-separated
     * segments: the counts must be equal, a declared {@code *} accepts any segment and every other declared
     * segment must be equal to its counterpart.
     */
    public boolean matches(@NotNull String requiredName, @NotNull String key) {
        String[] requiredSegments = requiredName.split(SEGMENT_SEPARATOR_REGEX, -1);
        String[] keySegments = key.split(SEGMENT_SEPARATOR_REGEX, -1);
        if (requiredSegments.length != keySegments.length) {
            return false;
        }
        for (int i = 0; i < requiredSegments.length; i++) {
            if (!ActionHook.ALL_WILDCARD.equals(requiredSegments[i]) && !requiredSegments[i].equals(keySegments[i])) {
                return false;
            }
        }
        return true;
    }

    private String formatMissingEntry(@NotNull String requiredName) {
        boolean wildcarded = List.of(requiredName.split(SEGMENT_SEPARATOR_REGEX, -1)).contains(ActionHook.ALL_WILDCARD);
        return String.format(wildcarded ? MISSING_MATCHING_ENTRY_MESSAGE : MISSING_ENTRY_MESSAGE, requiredName);
    }
}
