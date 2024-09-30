package ch.sbb.polarion.extension.interceptor_manager.osgi;

import ch.sbb.polarion.extension.interceptor_manager.model.IActionHook;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class HooksBundleActivator implements BundleActivator {
    private ServiceTracker<IActionHook, IActionHook> serviceTracker;

    @Override
    public void start(BundleContext bundleContext) {
        serviceTracker = new ServiceTracker<>(bundleContext, IActionHook.class, new HooksServiceTrackerCustomizer(bundleContext));
        serviceTracker.open();
    }

    @Override
    public void stop(BundleContext bundleContext) {
        if (serviceTracker != null) {
            serviceTracker.close();
        }
    }
}
