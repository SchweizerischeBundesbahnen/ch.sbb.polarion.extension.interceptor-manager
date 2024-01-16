package ch.sbb.polarion.extension.interceptor.rest.filter;

import com.polarion.platform.core.PlatformContext;
import com.polarion.platform.security.ISecurityService;

import javax.security.auth.Subject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Secured
@Provider
public class LogoutFilter implements ContainerResponseFilter {

    private final ISecurityService securityService;

    public LogoutFilter(ISecurityService securityService) {
        this.securityService = securityService;
    }

    @SuppressWarnings("unused")
    public LogoutFilter() {
        this.securityService = PlatformContext.getPlatform().lookupService(ISecurityService.class);
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        Subject subject = (Subject) requestContext.getProperty(AuthenticationFilter.USER_SUBJECT);
        if (subject != null) {
            securityService.logout(subject);
        }
    }
}
