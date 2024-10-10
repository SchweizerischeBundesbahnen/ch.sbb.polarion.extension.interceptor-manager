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
import io.swagger.v3.oas.annotations.media.Schema;
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
public abstract class ActionHook implements IActionHook {

    public static final String ALL_WILDCARD = "*";
    public static final String DOT = ".";

    @JsonIgnore
    @Schema(hidden = true)
    protected HookModel settings;

    @Schema(description = "List of item types associated with the hook")
    private List<ItemType> itemTypes;

    @Schema(description = "Action type associated with the hook")
    private ActionType actionType;

    @Schema(description = "Version of the hook")
    private String version;

    @Schema(description = "Description of the hook")
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

    @Override
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
    @Override
    public abstract @NotNull HookExecutor getExecutor();

    @JsonProperty("name")
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @JsonProperty("enabled")
    @Override
    public boolean isEnabled() {
        return loadSettings(false).isEnabled();
    }

    @JsonIgnore
    @Override
    public abstract String getDefaultSettings();


    /**
     * Called each time the settings are updated in the administration panel.
     *
     * @return null if the settings are valid and an error message if the settings are invalid (it will be displayed to the user)
     */
    @SuppressWarnings("java:S1172") // parameter 'model' will be used by subclasses
    @Override
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
    @Schema(description = "Represents the type of actions that can be intercepted")
    public enum ActionType {
        @Schema(description = "Action to save an item")
        SAVE("save"),

        @Schema(description = "Action to delete an item")
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
    @Schema(description = "Represents the type of items that can be intercepted")
    public enum ItemType {
        @Schema(description = "A work item")
        WORKITEM(IWorkItem.class),

        @Schema(description = "A test run item")
        TESTRUN(ITestRun.class),

        @Schema(description = "A plan item")
        PLAN(IPlan.class),

        @Schema(description = "A module item")
        MODULE(IModule.class),

        @Schema(description = "A comment associated with a module")
        MODULE_COMMENT(IModuleComment.class),

        @Schema(description = "A work record item")
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
