package ch.sbb.polarion.extension.interceptor_hooks.only_assignee_can_delete;

import ch.sbb.polarion.extension.interceptor.model.ActionHook;
import ch.sbb.polarion.extension.interceptor.model.HookExecutor;
import ch.sbb.polarion.extension.interceptor.util.PropertiesUtils;
import com.polarion.alm.projects.model.IUser;
import com.polarion.alm.tracker.ITrackerService;
import com.polarion.alm.tracker.model.IWorkItem;
import com.polarion.core.util.logging.Logger;
import com.polarion.platform.core.PlatformContext;
import com.polarion.platform.persistence.model.IPObject;
import com.polarion.platform.persistence.model.IPObjectList;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

@SuppressWarnings({"unused", "unchecked", "rawtypes"})
public class OnlyAssigneeCanDeleteHook extends ActionHook implements HookExecutor {

    private static final String SETTINGS_PROJECTS = "projects";
    private static final String SETTINGS_ERROR_MESSAGE = "errorMessage";
    private static final String SETTINGS_DELETE_UNASSIGNED = "deleteUnassigned";
    private static final boolean DEFAULT_DELETE_UNASSIGNED = true;
    private static final String DEFAULT_ERROR_MESSAGE = "Only assignee user can delete WI!";
    private static final String VERSION = "2.0.0";
    private static final ITrackerService trackerService = PlatformContext.getPlatform().lookupService(ITrackerService.class);
    private static final Logger logger = Logger.getLogger(OnlyAssigneeCanDeleteHook.class);

    public OnlyAssigneeCanDeleteHook() {
        super(ItemType.WORKITEM, ActionType.DELETE, VERSION, "Control that only assignee user can delete WI." +
                " If Wi is unassigned then it can be removed or not dependent on value property " + SETTINGS_DELETE_UNASSIGNED);
    }

    @Override
    public String preAction(@NotNull IPObject object) {
        boolean deleteUnassigned = DEFAULT_DELETE_UNASSIGNED;
        String deleteUnassignedStringValue = getSettingsValue(SETTINGS_DELETE_UNASSIGNED);
        try {
            deleteUnassigned = Boolean.parseBoolean(deleteUnassignedStringValue);
        } catch (NumberFormatException e) {
            logger.error("Cannot parse boolean value '%s'".formatted(deleteUnassignedStringValue), e);
        }

        IWorkItem workItem = (IWorkItem) object;
        String returnMessage = null;
        if (workItem.getType() != null && workItem.getId() != null && isCommaSeparatedSettingsHasItem(workItem.getProjectId(), SETTINGS_PROJECTS)) {
            boolean allowToDelete = false;
            String currentUser = trackerService.getDataService().getSecurityService().getCurrentUser();
            IPObjectList assignees = workItem.getAssignees();
            if (assignees.isEmpty()) {
                allowToDelete = deleteUnassigned;
            } else {
                for (Iterator<IPObject> i = assignees.iterator(); i.hasNext() && !allowToDelete; ) {
                    IPObject user = i.next();
                    allowToDelete = currentUser.equals(((IUser) user).getId());
                }
            }
            if (!allowToDelete) {
                returnMessage = getSettingsValue(SETTINGS_ERROR_MESSAGE);
            }
        }

        return returnMessage;
    }

    @Override
    public @NotNull HookExecutor getExecutor() {
        return this; //there is no need to create a separate executor instance coz only 'pre' action used
    }

    @Override
    public String getDefaultSettings() {
        return PropertiesUtils.build(
                SETTINGS_PROJECTS, ALL_WILDCARD,
                SETTINGS_ERROR_MESSAGE, DEFAULT_ERROR_MESSAGE,
                SETTINGS_DELETE_UNASSIGNED, String.valueOf(DEFAULT_DELETE_UNASSIGNED)
        );
    }
}
