package ch.sbb.polarion.extension.interceptor_hooks.plan_save;

import ch.sbb.polarion.extension.interceptor.model.ActionHook;
import ch.sbb.polarion.extension.interceptor.model.HookExecutor;
import ch.sbb.polarion.extension.interceptor.util.PropertiesUtils;
import com.polarion.alm.tracker.model.IPlan;
import com.polarion.alm.tracker.model.IWorkItem;
import com.polarion.core.util.logging.Logger;
import com.polarion.platform.persistence.model.IPObject;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;

@SuppressWarnings("unused")
public class PlanSaveHook extends ActionHook implements HookExecutor {

    private static final String SETTINGS_PROJECTS = "projects";
    private static final String SETTINGS_TEMPLATES = "templates";
    private static final String VERSION = "2.0.0";
    private static final Logger logger = Logger.getLogger(PlanSaveHook.class);

    public PlanSaveHook() {
        super(ItemType.PLAN, ActionType.SAVE, VERSION, "Hook intercepting Plan save operation for adding WIs to parent plan (propagating)");
    }

    @Override
    public void postAction(@NotNull IPObject object) {
        IPlan plan = (IPlan) object;

        logger.debug("Processing Plan: " + plan.getId());
        if (!plan.isTemplate() && isCommaSeparatedSettingsHasItem(plan.getProjectId(), SETTINGS_PROJECTS)) {
            if (plan.getTemplate() != null && isCommaSeparatedSettingsHasItem(plan.getTemplate().getId(), SETTINGS_TEMPLATES)
                    && plan.getParent() != null && plan.getParent().getTemplate() != null && isCommaSeparatedSettingsHasItem(plan.getParent().getTemplate().getId(), SETTINGS_TEMPLATES)) {
                IPlan parentPlan = plan.getParent();
                logger.debug("Parent Plan: " + parentPlan.getId());
                LinkedHashSet<IWorkItem> parentPlanItems = parentPlan.getItems();
                boolean isModified = false;
                for (IWorkItem wi : plan.getItems()) {
                    if (!parentPlanItems.contains(wi)) {
                        logger.debug("Adding WI: " + wi.getId());
                        parentPlan.addRecord(wi);
                        isModified = true;
                    }
                }
                if (isModified) {
                    parentPlan.save();
                }
            } else {
                logger.debug("Unsupported plan type: " + plan.getTemplate().getId() + " Supporting: " + getSettingsValue(SETTINGS_TEMPLATES));
            }
        } else {
            logger.debug("Unsupported project: " + plan.getProjectId() + " Supporting: " + getSettingsValue(SETTINGS_PROJECTS));
        }
    }

    @Override
    public @NotNull HookExecutor getExecutor() {
        return this; //there is no need to create a separate executor instance coz only 'post' action used
    }

    @Override
    public String getDefaultSettings() {
        return PropertiesUtils.build(
                SETTINGS_PROJECTS, ALL_WILDCARD,
                SETTINGS_TEMPLATES, ALL_WILDCARD
        );
    }
}
