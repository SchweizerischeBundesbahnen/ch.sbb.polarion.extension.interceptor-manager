package ch.sbb.polarion.extension.interceptor_manager.osgi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OSGiUtilsTest {
    @Mock
    private Bundle bundle;
    @Mock
    private BundleContext bundleContext;
    @Mock
    private ServiceReference<ITestService> serviceReference1;
    @Mock
    private ServiceReference<ITestService> serviceReference2;
    @Mock
    private ITestService service1;
    @Mock
    private ITestService service2;

    @Test
    public void shouldLookupServices() throws InvalidSyntaxException {
        try (MockedStatic<FrameworkUtil> osgiFramework = mockStatic(FrameworkUtil.class)) {
            osgiFramework.when(() -> FrameworkUtil.getBundle(OSGiUtils.class)).thenReturn(bundle);
            when(bundle.getBundleContext()).thenReturn(bundleContext);
            ServiceReference<?>[] serviceRefs = {serviceReference1, serviceReference2};
            when(bundleContext.getServiceReferences(ITestService.class.getCanonicalName(), null)).thenReturn(serviceRefs);
            when(bundleContext.getService(serviceReference1)).thenReturn(service1);
            when(bundleContext.getService(serviceReference2)).thenReturn(service2);

            List<ITestService> services = OSGiUtils.lookupOSGiService(ITestService.class);
            assertThat(services).hasSize(2);
            assertThat(services).contains(service1);
            assertThat(services).contains(service2);
        }
    }

    private interface ITestService {}
}