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
     * Reports every declared entry which the given settings miss, plus whatever
     * {@link IActionHook#validateSettings(HookModel)} reports. The list is empty when the settings are usable.
     * <p>
     * The result is derived, never stored: it is recomputed on every read and before every save, so settings
     * written by an older hook version start reporting the entries that version did not know about.
     */
    public @NotNull List<String> validate(@Nullable IActionHook hook, @Nullable HookModel model) {
        if (hook == null || model == null) {
            return List.of();
        }

        List<String> errors = new ArrayList<>();
        if (hook instanceof RequireSettingEntries requiring) {
            Set<String> presentKeys = model.getPropertiesMap().keySet();
            for (String requiredName : requiring.getRequiredSettingEntryNames()) {
                if (requiredName == null || requiredName.isBlank()) {
                    continue; // a hook which declares nothing here checks nothing, rather than reporting an unnamed entry
                }
                if (presentKeys.stream().noneMatch(key -> matches(requiredName, key))) {
                    errors.add(formatMissingEntry(requiredName));
                }
            }
        }

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
