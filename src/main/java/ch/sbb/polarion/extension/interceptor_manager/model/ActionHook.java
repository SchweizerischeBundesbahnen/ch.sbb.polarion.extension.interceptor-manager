package ch.sbb.polarion.extension.interceptor_manager.model;

import ch.sbb.polarion.extension.generic.settings.NamedSettings;
import ch.sbb.polarion.extension.generic.settings.NamedSettingsRegistry;
import ch.sbb.polarion.extension.generic.settings.SettingId;
import ch.sbb.polarion.extension.interceptor_manager.settings.HookModel;
import ch.sbb.polarion.extension.interceptor_manager.util.HookManifestUtils;
import ch.sbb.polarion.extension.interceptor_manager.util.PropertiesUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.polarion.alm.projects.model.IUniqueObject;
import com.polarion.alm.tracker.model.IModule;
import com.polarion.alm.tracker.model.IModuleComment;
import com.polarion.alm.tracker.model.IPlan;
import com.polarion.alm.tracker.model.ITestRun;
import com.polarion.alm.tracker.model.IWorkItem;
import com.polarion.alm.tracker.model.IWorkRecord;
import com.polarion.core.util.StringUtils;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Base class for a hook.
 */
@Data
@SuppressWarnings({"unused", "java:S5993"}) //leave public constructors
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


    /**
     * Called each time the settings are updated in the administration panel.
     *
     * @return null if the settings are valid and an error message if the settings are invalid (it will be displayed to the user)
     */
    public String validateSettings(HookModel model) {
        return null;
    }

    @NotNull
    protected String getSettingsValue(@NotNull String settingsId, String... selectors) {
        LinkedHashMap<String, String> valueMap = getSettingsValuesWithSelector(settingsId, selectors);
        return StringUtils.getEmptyIfNullTrimmed(valueMap.isEmpty() ? null : valueMap.entrySet().iterator().next().getValue());
    }

    protected boolean isCommaSeparatedSettingsHasItem(String itemToCheck, @NotNull String settingsId, String... selectors) {
        String itemsString = getSettingsValue(settingsId, selectors);
        return ALL_WILDCARD.equals(itemsString) || Stream.of(itemsString.split(",")).map(String::trim).anyMatch(s -> Objects.equals(s, itemToCheck));
    }

    protected Boolean getSettingsValueAsBoolean(@NotNull String settingsId, String... selectors) {
        String stringValue = getSettingsValue(settingsId, selectors);
        return stringValue.isEmpty() ? null : Boolean.valueOf(stringValue);
    }

    protected Integer getSettingsValueAsInt(@NotNull String settingsId, String... selectors) {
        String stringValue = getSettingsValue(settingsId, selectors);
        return stringValue.isEmpty() ? null : Integer.valueOf(stringValue);
    }

    protected List<String> getSettingsValueAsList(@NotNull String settingsId, String... selectors) {
        String stringValue = getSettingsValue(settingsId, selectors);
        return stringValue.isEmpty() ? new ArrayList<>() : Stream.of(stringValue.split(",")).map(String::trim).toList();
    }

    protected LinkedHashMap<String, String> getSettingsValuesWithSelector(@NotNull String settingsId, String... selectors) {
        return getSettingsValuesWithSelector(false, settingsId, selectors);
    }

    protected LinkedHashMap<String, String> getSettingsValuesWithSelector(boolean all, @NotNull String settingsId, String... selectors) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        String[] combinedSettingsIdWithSelectors = selectors == null ? new String[]{settingsId} :
                Stream.concat(Stream.of(settingsId), Arrays.stream(selectors)).toArray(String[]::new);
        List<String> selectorsCombinations = PropertiesUtils.generateSelectorsCombinations(combinedSettingsIdWithSelectors);
        for (String combination : selectorsCombinations) {
            String value = loadSettings(false).getPropertiesMap().get(combination);
            if (value != null) {
                result.put(combination, value);
                if (!all) {
                    break;
                }
            }
        }
        return result;
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
        MODULE(IModule.class),
        MODULE_COMMENT(IModuleComment.class),
        WORK_RECORD(IWorkRecord.class);

        final Class<? extends IUniqueObject> itemClass;

        ItemType(Class<? extends IUniqueObject> itemClass) {
            this.itemClass = itemClass;
        }

        public boolean canProcess(Object object) {
            return object != null && itemClass.isAssignableFrom(object.getClass());
        }

    }
}
