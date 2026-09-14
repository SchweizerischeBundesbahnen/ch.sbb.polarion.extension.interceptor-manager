package ch.sbb.polarion.extension.interceptor_manager.settings;

import ch.sbb.polarion.extension.generic.settings.SettingsModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HookModel extends SettingsModel {

    public static final String ENABLED = "ENABLED";
    public static final String HOOK_VERSION = "HOOK_VERSION";
    public static final String PROPERTIES = "PROPERTIES";

    private boolean enabled;
    private String hookVersion;
    private String properties;

    /**
     * The problems which make these settings unusable, empty when they are complete. Derived from the hook's
     * {@link ch.sbb.polarion.extension.interceptor_manager.model.RequireSettingEntries} declaration on every
     * read, never stored. Published on read only: a client which sends the model back must not be able to
     * declare its own settings valid.
     */
    @Builder.Default
    @Schema(description = "The problems which make these settings unusable, empty when they are complete")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<String> validationErrors = List.of();

    /**
     * The constructor as it was before validation errors existed, kept so that a hook which builds a model
     * itself still compiles. Validation errors are derived, so there is nothing to pass in here.
     */
    public HookModel(boolean enabled, String hookVersion, String properties) {
        this(enabled, hookVersion, properties, List.of());
    }

    /**
     * Null-safe by hand: {@code @NoArgsConstructor} leaves the field null, because {@code @Builder.Default}
     * moves the initializer above out of the constructors. Every read path goes through this getter.
     */
    public @NotNull List<String> getValidationErrors() {
        return validationErrors == null ? List.of() : validationErrors;
    }

    @Override
    protected String serializeModelData() {
        return serializeEntry(ENABLED, String.valueOf(enabled)) +
                serializeEntry(HOOK_VERSION, String.valueOf(hookVersion)) +
                serializeEntry(PROPERTIES, properties);
    }

    @Override
    protected void deserializeModelData(String serializedString) {
        enabled = Boolean.parseBoolean(deserializeEntry(ENABLED, serializedString));
        hookVersion = deserializeEntry(HOOK_VERSION, serializedString);
        properties = deserializeEntry(PROPERTIES, serializedString);
    }

    @JsonIgnore
    public Map<String, String> getPropertiesMap() {
        return properties == null ? new HashMap<>() : properties.lines()
                .filter(line -> line.contains("="))
                .map(line -> line.split("=", 2))
                .collect(Collectors.toMap(tokens -> tokens[0].trim(), tokens -> tokens[1].trim()));
    }
}
