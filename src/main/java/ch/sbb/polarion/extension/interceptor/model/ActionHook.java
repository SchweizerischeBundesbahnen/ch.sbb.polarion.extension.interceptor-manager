package ch.sbb.polarion.extension.interceptor.model;

import ch.sbb.polarion.extension.generic.settings.NamedSettings;
import ch.sbb.polarion.extension.generic.settings.NamedSettingsRegistry;
import ch.sbb.polarion.extension.generic.settings.SettingId;
import ch.sbb.polarion.extension.interceptor.settings.HookModel;
import ch.sbb.polarion.extension.interceptor.util.HookManifestUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.polarion.alm.projects.model.IUniqueObject;
import com.polarion.alm.tracker.model.IModule;
import com.polarion.alm.tracker.model.IPlan;
import com.polarion.alm.tracker.model.ITestRun;
import com.polarion.alm.tracker.model.IWorkItem;
import com.polarion.core.util.StringUtils;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Base class for a hook.
 */
@Data
@SuppressWarnings("java:S5993") //leave public constructors
public abstract class ActionHook {

    public static final String ALL_WILDCARD = "*";
    public static final String DOT = ".";

    @JsonIgnore
    protected HookModel settings;
    private List<ItemType> itemTypes;
    private ActionType actionType;
    private String version;
    private String description;

    public ActionHook(@NotNull ItemType itemType, @NotNull ActionType actionType, @Nullable String description) {
        this(Collections.singletonList(itemType), actionType, description);
    }

    public ActionHook(@NotNull ItemType itemType, @NotNull ActionType actionType, @Nullable String version, @Nullable String description) {
        this(Collections.singletonList(itemType), actionType, version, description);
    }

    public ActionHook(@NotNull List<ItemType> itemTypes, @NotNull ActionType actionType, @Nullable String description) {
        this(itemTypes, actionType, null, description);
    }

    public ActionHook(@NotNull List<ItemType> itemTypes, @NotNull ActionType actionType, @Nullable String version, @Nullable String description) {
        this.itemTypes = itemTypes;
        this.actionType = actionType;
        this.version = version == null ? HookManifestUtils.getHookVersion(getClass()) : version;
        this.description = description;
    }

    public HookModel loadSettings(boolean forceUpdate) {
        if (forceUpdate || settings == null) {
            settings = (HookModel) NamedSettingsRegistry.INSTANCE.getByFeatureName(getName()).read("", SettingId.fromName(NamedSettings.DEFAULT_NAME), null);
        }
        return settings;
    }

    /**
     * Basic usage - instantiate executor only once and reuse its instance. But if both methods ('pre' and 'post') used
     * in the same executor and 'post' wants to process some data which was produced by 'pre' - in this case it's a good idea to
     * return a new instance here in order to prevent data rewrite by the concurrent executions.
     */
    @JsonIgnore
    public abstract @NotNull HookExecutor getExecutor();

    @JsonProperty("name")
    public String getName() {
        return getClass().getSimpleName();
    }

    @JsonProperty("enabled")
    public boolean isEnabled() {
        return loadSettings(false).isEnabled();
    }

    @JsonIgnore
    public abstract String getDefaultSettings();

    protected String getSettingsValue(String settingsId) {
        return StringUtils.getEmptyIfNullTrimmed(loadSettings(false).getPropertiesMap().get(settingsId));
    }

    @SuppressWarnings("unused")
    protected boolean isCommaSeparatedSettingsHasItem(String itemToCheck, String settingsName) {
        List<String> items = Arrays.asList(getSettingsValue(settingsName).split(","));
        return items.contains(itemToCheck) || items.contains(ALL_WILDCARD);
    }

    @SuppressWarnings("unused")
    protected boolean isCommaSeparatedSettingsHasItem(String itemToCheck, String settingsName, String projectId) {
        String itemsString = getSettingsValue(settingsName + DOT + projectId);
        if(StringUtils.isEmpty(itemsString)) {
            itemsString = getSettingsValue(settingsName + DOT + ALL_WILDCARD);
        }
        List<String> items = Arrays.asList(itemsString.split(","));
        return items.contains(itemToCheck) || items.contains(ALL_WILDCARD);
    }

    /**
     * Interceptor actions type
     */
    public enum ActionType {
        SAVE("save"),
        DELETE("delete");

        final String methodName;

        ActionType(String methodName) {
            this.methodName = methodName;
        }

        public String getMethodName() {
            return methodName;
        }

        public boolean canProcess(Method method) {
            return method != null && Objects.equals(method.getName(), methodName);
        }
    }

    /**
     * Interceptor item type
     */
    public enum ItemType {
        WORKITEM(IWorkItem.class),
        TESTRUN(ITestRun.class),
        PLAN(IPlan.class),
        MODULE(IModule.class);

        final Class<? extends IUniqueObject> itemClass;

        ItemType(Class<? extends IUniqueObject> itemClass) {
            this.itemClass = itemClass;
        }

        public boolean canProcess(Object object) {
            return object != null && itemClass.isAssignableFrom(object.getClass());
        }

    }
}
