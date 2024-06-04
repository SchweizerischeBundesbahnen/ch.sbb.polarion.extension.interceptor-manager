package ch.sbb.polarion.extension.interceptor;

import ch.sbb.polarion.extension.interceptor.model.ActionHook;
import ch.sbb.polarion.extension.interceptor.model.HookExecutor;
import ch.sbb.polarion.extension.interceptor.model.HooksRegistry;
import com.polarion.core.util.StringUtils;
import com.polarion.core.util.exceptions.UserFriendlyRuntimeException;
import com.polarion.core.util.logging.Logger;
import com.polarion.platform.persistence.model.IPObject;

import java.util.List;

@SuppressWarnings({"unused", "unchecked"})
public class DataServiceInterceptor {

    private static final Logger logger = Logger.getLogger(DataServiceInterceptor.class);

    public List<HookExecutor> getHookExecutors(boolean saveAction, IPObject polarionObject) {
        return HooksRegistry.HOOKS.list().stream()
                .filter(h -> h.isEnabled() &&
                        h.getActionType().equals(saveAction ? ActionHook.ActionType.SAVE : ActionHook.ActionType.DELETE) &&
                        h.getItemTypes().stream().anyMatch(type -> type.canProcess(polarionObject))
                ).map(ActionHook::getExecutor).toList();
    }

    public void callExecutors(Object executorsListObject, boolean pre, IPObject polarionObject) {
        List<HookExecutor> executors = (List<HookExecutor>) executorsListObject;
        executors.forEach(executor -> {
            String interruptReasonMessage = null;
            try {
                if (pre) {
                    interruptReasonMessage = executor.preAction(polarionObject);
                } else {
                    executor.postAction(polarionObject);
                }
            } catch (Exception e) {
                logger.error("Error running hook executor", e);
            }
            if (!StringUtils.isEmpty(interruptReasonMessage)) {
                throw new UserFriendlyRuntimeException(interruptReasonMessage);
            }
        });
    }

}
