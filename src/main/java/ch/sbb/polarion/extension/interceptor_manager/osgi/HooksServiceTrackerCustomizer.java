package ch.sbb.polarion.extension.interceptor_manager.osgi;

import ch.sbb.polarion.extension.interceptor_manager.model.IActionHook;
import ch.sbb.polarion.extension.interceptor_manager.model.HooksRegistry;
import com.polarion.core.util.logging.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class HooksServiceTrackerCustomizer implements ServiceTrackerCustomizer<IActionHook, IActionHook> {
    private final BundleContext context;
    private static final Logger logger = Logger.getLogger(HooksServiceTrackerCustomizer.class);

    public HooksServiceTrackerCustomizer(BundleContext context) {
        this.context = context;
    }

    @Override
    public IActionHook addingService(ServiceReference<IActionHook> serviceReference) {
        IActionHook actionHook = context.getService(serviceReference);
        HooksRegistry.HOOKS.addHook(actionHook);
        logger.info("Added service: " + serviceReference.toString());
        return actionHook;
    }

    @Override
    public void removedService(ServiceReference<IActionHook> serviceReference, IActionHook o) {
        HooksRegistry.HOOKS.refresh();
        logger.info("Removed service: " + serviceReference.toString());
    }

    @Override
    public void modifiedService(ServiceReference<IActionHook> serviceReference, IActionHook o) {
        HooksRegistry.HOOKS.refresh();
    }
}
