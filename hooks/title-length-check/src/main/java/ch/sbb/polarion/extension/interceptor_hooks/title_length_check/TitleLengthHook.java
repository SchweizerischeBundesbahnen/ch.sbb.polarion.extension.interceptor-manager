package ch.sbb.polarion.extension.interceptor_hooks.title_length_check;

import ch.sbb.polarion.extension.interceptor.model.ActionHook;
import ch.sbb.polarion.extension.interceptor.util.PropertiesUtils;
import com.polarion.alm.projects.model.IUniqueObject;
import com.polarion.alm.tracker.model.IWorkItem;
import com.polarion.core.util.logging.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("unused")
public class TitleLengthHook extends ActionHook {

    private static final String SETTINGS_ERROR_MESSAGE = "errorMessage";
    private static final String SETTINGS_MAX_LENGTH = "titleMaxLength";
    private static final String MAX_LENGTH_VARIABLE = "{%s}".formatted(SETTINGS_MAX_LENGTH);
    private static final String DEFAULT_ERROR_MESSAGE = "Title length is over the limit (" + MAX_LENGTH_VARIABLE + " symbols). Please correct it before saving";
    private static final int DEFAULT_MAX_LENGTH = 256;
    private static final String VERSION = "1.0.0";
    private static final Logger logger = Logger.getLogger(TitleLengthHook.class);

    public TitleLengthHook() {
        super(List.of(ItemType.WORKITEM, ItemType.MODULE), ActionType.SAVE, VERSION, "Validates title length");
    }

    @Override
    public String processAction(@NotNull IUniqueObject object) {
        int maxLength = DEFAULT_MAX_LENGTH;
        String maxLengthStringValue = getSettingsValue(SETTINGS_MAX_LENGTH);
        try {
            maxLength = Integer.parseInt(maxLengthStringValue);
        } catch (NumberFormatException e) {
            logger.error("Cannot parse max length value '%s'".formatted(maxLengthStringValue), e);
        }
        return ((IWorkItem) object).getTitle().length() > maxLength ? getSettingsValue(SETTINGS_ERROR_MESSAGE).replace(MAX_LENGTH_VARIABLE, String.valueOf(maxLength)) : null;
    }

    @Override
    public String getDefaultSettings() {
        return PropertiesUtils.build(
                SETTINGS_ERROR_MESSAGE, DEFAULT_ERROR_MESSAGE,
                SETTINGS_MAX_LENGTH, String.valueOf(DEFAULT_MAX_LENGTH)
        );
    }
}
