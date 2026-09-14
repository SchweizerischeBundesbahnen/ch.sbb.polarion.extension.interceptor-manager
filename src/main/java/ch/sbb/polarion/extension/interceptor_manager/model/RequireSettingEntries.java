package ch.sbb.polarion.extension.interceptor_manager.model;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Optional contract for a hook whose settings can not work without certain entries.
 * <p>
 * A hook which implements it declares the entry names it reads. The interceptor manager checks them
 * against the stored settings when the administration page is opened and again before every save, and
 * refuses a save whose settings miss one of them. This is how an administrator learns that a new hook
 * version added an entry, instead of silently running with settings which predate it.
 * <p>
 * Names are declared exactly as they appear in {@link IActionHook#getDefaultSettings()}. A {@code *}
 * segment stands for any one selector, so {@code "types.*"} is satisfied by {@code types.*} and by
 * {@code types.myProject} alike, while {@code "projects"} is satisfied by that key only. The number of
 * segments must match, because that is what {@link ActionHook#getSettingsValuesWithSelector} looks up.
 * <p>
 * Implementing this interface is optional. A hook which does not implement it keeps its settings
 * unchecked, so existing hooks stay compatible.
 */
public interface RequireSettingEntries {

    /**
     * The setting entry names which must be present. An empty list checks nothing.
     */
    @NotNull List<String> getRequiredSettingEntryNames();
}
