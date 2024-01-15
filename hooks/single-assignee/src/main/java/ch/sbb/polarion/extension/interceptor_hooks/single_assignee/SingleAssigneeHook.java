package ch.sbb.polarion.extension.interceptor_hooks.single_assignee;

import ch.sbb.polarion.extension.interceptor.model.ActionHook;
import ch.sbb.polarion.extension.interceptor.util.PropertiesUtils;
import com.polarion.alm.projects.model.IUniqueObject;
import com.polarion.alm.tracker.model.IWorkItem;
import com.polarion.core.util.logging.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Save hook for control that user can add only single assignee to WI. PS-1695
 */
@SuppressWarnings("unused")
public class SingleAssigneeHook extends ActionHook {

    private static final String SETTINGS_PROJECTS_DESCRIPTION = "Comma-separated list of projects. Use * to process all.";
    private static final String SETTINGS_PROJECTS = "projects";
    private static final String SETTINGS_TYPES_DESCRIPTION = "Comma-separated list of types for particular project (e.g.: types.projectId1=task,defect). Use * to wildcard all projects or types (e.g. types.*=*).";
    private static final String SETTINGS_TYPES = "types";
    private static final String SETTINGS_ERROR_MESSAGE = "errorMessage";
    private static final String SETTINGS_ERROR_MESSAGE_DESCRIPTION = "Message which will be displayed in the negative case.";
    private static final String DEFAULT_ERROR_MESSAGE = "Only single assignee can be added to Work Item according project settings!";
    private static final String VERSION = "1.0.0";
    private static final Logger logger = Logger.getLogger(SingleAssigneeHook.class);

    public SingleAssigneeHook() {
        super(ItemType.WORKITEM, ActionType.SAVE, VERSION, "Save hook for control that user can add only single assignee to WI. PS-1695");
    }

    @Override
    public String processAction(@NotNull IUniqueObject object) {
        String returnMessage = null;
        IWorkItem workItem = (IWorkItem) object;
        if (workItem.getType() != null && workItem.getId() != null && !workItem.isUnresolvable() && workItem.isModified()) {
            try {
                if (isCommaSeparatedSettingsHasItem(workItem.getProjectId(), SETTINGS_PROJECTS) && isCommaSeparatedSettingsHasItem(workItem.getType().getId(), SETTINGS_TYPES, workItem.getProjectId())) {
                    returnMessage = getSettingsValue(SETTINGS_ERROR_MESSAGE);
                }
            } catch (Exception e) {
                returnMessage = "Cannot update WI due to Exception: " + (e.getMessage() != null ? e.getMessage() : e.getClass());
                logger.error("Hook processing error", e);
            }
        }

        return returnMessage;
    }

    @Override
    public String getDefaultSettings() {
        return PropertiesUtils.buildWithDescription(
                SETTINGS_PROJECTS_DESCRIPTION, SETTINGS_PROJECTS, ALL_WILDCARD,
                SETTINGS_TYPES_DESCRIPTION, SETTINGS_TYPES + DOT + ALL_WILDCARD, ALL_WILDCARD,
                SETTINGS_ERROR_MESSAGE_DESCRIPTION, SETTINGS_ERROR_MESSAGE, DEFAULT_ERROR_MESSAGE
        );
    }
}
