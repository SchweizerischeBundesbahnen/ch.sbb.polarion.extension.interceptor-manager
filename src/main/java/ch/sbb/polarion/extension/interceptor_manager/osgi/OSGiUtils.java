package ch.sbb.polarion.extension.interceptor_manager.osgi;

import com.polarion.core.util.logging.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OSGiUtils {
    private static final Logger logger = Logger.getLogger(OSGiUtils.class);

    private OSGiUtils() {}

    public static <T> List<T> lookupOSGiService(Class<T> serviceInterface) {
        Bundle bundle = FrameworkUtil.getBundle(OSGiUtils.class);
        if (bundle == null) {
            logger.warn("Cannot get OSGi bundle for interceptor manager via classloader: " + OSGiUtils.class.getClassLoader());
            return List.of();
        }
        BundleContext bundleContext = bundle.getBundleContext();
        if (bundleContext == null) {
            logger.warn("Bundle context is not yet initialized for bundle: " + bundle.getSymbolicName() + ". Check Bundle-ActivationPolicy: lazy");
            return List.of();
        }
        try {
            ServiceReference<?>[] serviceReferences = bundleContext.getServiceReferences(serviceInterface.getCanonicalName(),null);
            if (serviceReferences != null) {
                //noinspection unchecked
                return (List<T>) Arrays.stream(serviceReferences)
                        .map(bundleContext::getService)
                        .collect(Collectors.toList());
            } else {
                return List.of();
            }
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException("Invalid syntax of OSGi interface class name: " + e.getMessage(), e);
        }
    }
}
