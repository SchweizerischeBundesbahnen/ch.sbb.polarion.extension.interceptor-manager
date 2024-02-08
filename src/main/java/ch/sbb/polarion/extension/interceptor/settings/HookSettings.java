package ch.sbb.polarion.extension.interceptor.settings;

import ch.sbb.polarion.extension.interceptor.model.ActionHook;
import ch.sbb.polarion.extension.interceptor.util.ScopeUtils;
import com.polarion.core.util.StringUtils;
import com.polarion.subterra.base.location.ILocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class HookSettings implements Settings<HookModel> {

    public static final String LOCATION = ".polarion/extensions/interceptor/%s.settings";
    private final SettingsService settingsService;
    private final ActionHook hook;

    public HookSettings(ActionHook hook) {
        this.hook = hook;
        this.settingsService = new SettingsService();
    }

    public @NotNull String getHookName() {
        return hook.getName();
    }

    @Override
    public String relativeLocation() {
        return String.format(LOCATION, hook.getName());
    }

    @Override
    public HookModel read(@NotNull String scope, String revisionName) {
        return read(ScopeUtils.getContextLocation(scope), revisionName);
    }

    @Override
    public HookModel read(@NotNull ILocation contextLocation, String revisionName) {
        final ILocation location = contextLocation.append(relativeLocation());
        String value = settingsService.read(location, revisionName);
        if (value == null) {
            if (!StringUtils.isEmpty(revisionName)) {
                return null;
            }
            ILocation defaultLocation = ScopeUtils.getDefaultLocation().append(relativeLocation());
            if (!settingsService.exists(defaultLocation)) {
                try {
                    return save(ScopeUtils.SCOPE_DEFAULT, defaultValues());
                } catch (Exception e) {
                    return defaultValues();
                }
            }
            value = settingsService.read(defaultLocation, null);
        }
        return fromString(value);
    }

    @Override
    public HookModel save(@NotNull String scope, @NotNull HookModel what) {
        final ILocation contextLocation = ScopeUtils.getContextLocation(scope);
        return save(contextLocation, what);
    }

    @Override
    public HookModel save(@NotNull ILocation contextLocation, @NotNull HookModel what) {
        final ILocation location = contextLocation.append(relativeLocation());
        what.setBundleTimestamp(currentBundleTimestamp());
        what.setHookVersion(hook.getVersion());
        String content = toString(what);
        settingsService.save(location, content);
        hook.loadSettings(true);
        return what;
    }

    public @NotNull List<Revision> listRevisions(ILocation location) {
        final ILocation contextLocation = location.append(relativeLocation());
        return settingsService.listRevisions(contextLocation);
    }

    @Override
    public @NotNull HookModel defaultValues() {
        return HookModel.builder().enabled(true).hookVersion(hook.getVersion()).properties(hook.getDefaultSettings()).build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(getHookName(), ((HookSettings) o).getHookName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHookName());
    }
}
