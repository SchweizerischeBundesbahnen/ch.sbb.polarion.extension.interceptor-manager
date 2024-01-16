package ch.sbb.polarion.extension.interceptor;

import ch.sbb.polarion.extension.interceptor.model.ActionHook;
import ch.sbb.polarion.extension.interceptor.model.HooksRegistry;
import com.polarion.alm.projects.model.IUniqueObject;
import com.polarion.core.util.exceptions.UserFriendlyRuntimeException;
import com.polarion.core.util.logging.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class ActionInvocationHandler implements InvocationHandler {

    private static final Logger logger = Logger.getLogger(ActionInvocationHandler.class);
    private static final Set<String> SUPPORTED_ACTIONS = Arrays.stream(ActionHook.ActionType.values()).map(ActionHook.ActionType::getMethodName).collect(Collectors.toSet());

    private final Object delegate;

    public ActionInvocationHandler(Object delegate) {
        this.delegate = delegate;
        HooksRegistry.HOOKS.refresh();
    }

    @Override
    @SuppressWarnings("squid:S112") //no need for dedicated exception instead of basic RuntimeException in this method
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String interruptReasonMessage = null;
        if (SUPPORTED_ACTIONS.contains(method.getName()) && args != null && args.length > 0 && args[0] instanceof IUniqueObject polarionObject) {
            Iterator<ActionHook> hooksIterator = HooksRegistry.HOOKS.list().stream()
                    .filter(h -> h.isEnabled() &&
                            h.getActionType().canProcess(method) &&
                            h.getItemTypes().stream().anyMatch(type -> type.canProcess(args[0]))
                    ).iterator();
            while (hooksIterator.hasNext() && interruptReasonMessage == null) {
                try {
                    interruptReasonMessage = hooksIterator.next().processAction(polarionObject);
                } catch (Exception e) {
                    logger.error("Error running interceptor for method: " + method, e);
                }
            }
        }

        // Delegate all calls to the base IDataService if no error messages.
        if (interruptReasonMessage == null) {
            try {
                return method.invoke(delegate, args);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Internal error in ITrackerService proxy.", e);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        } else {
            throw new UserFriendlyRuntimeException(interruptReasonMessage);
        }
    }
}
