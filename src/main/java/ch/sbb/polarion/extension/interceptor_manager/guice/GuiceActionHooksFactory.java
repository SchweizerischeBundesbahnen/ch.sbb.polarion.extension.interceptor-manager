package ch.sbb.polarion.extension.interceptor_manager.guice;

import ch.sbb.polarion.extension.interceptor_manager.model.IActionHook;
import com.google.inject.Inject;
import com.polarion.core.util.logging.Logger;
import com.polarion.platform.guice.internal.GuicePlatform;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class GuiceActionHooksFactory {
    private static final Logger logger = Logger.getLogger(GuiceActionHooksFactory.class);
    private final Set<IActionHook> actionHooks = new HashSet<>();

    private GuiceActionHooksFactory() {}

    public static GuiceActionHooksFactory build() {
        GuiceActionHooksFactory actionHooksFactory = new GuiceActionHooksFactory();
        GuicePlatform.tryInjectMembers(actionHooksFactory);
        return actionHooksFactory;
    }

    @Inject
    @SuppressWarnings("unused")
    public void setActionHooks(Set<IActionHook> actionHooks) {
        if (actionHooks != null) {
            this.actionHooks.addAll(actionHooks);
            logger.info("Added guice action hooks: " + actionHooks.size());
        }
    }
}
