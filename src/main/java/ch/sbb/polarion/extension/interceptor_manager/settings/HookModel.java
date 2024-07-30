package ch.sbb.polarion.extension.interceptor_manager.settings;

import ch.sbb.polarion.extension.generic.settings.SettingsModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
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
