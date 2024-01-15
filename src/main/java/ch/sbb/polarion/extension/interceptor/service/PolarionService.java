package ch.sbb.polarion.extension.interceptor.service;

import ch.sbb.polarion.extension.interceptor.util.RequestContextUtil;
import com.polarion.platform.core.PlatformContext;
import com.polarion.platform.security.ISecurityService;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.Callable;

@Getter
public class PolarionService {

    protected final ISecurityService securityService;

    public PolarionService() {
        securityService = PlatformContext.getPlatform().lookupService(ISecurityService.class);
    }

    public PolarionService(@NotNull ISecurityService securityService) {
        this.securityService = securityService;
    }

    @SneakyThrows
    public <T> T callPrivileged(Callable<T> callable) {
        return securityService.doAsUser(RequestContextUtil.getUserSubject(), (PrivilegedExceptionAction<T>) callable::call);
    }

    @SneakyThrows
    public void callPrivileged(Runnable runnable) {
        securityService.doAsUser(RequestContextUtil.getUserSubject(), (PrivilegedAction<Object>) () -> {
                    runnable.run();
                    return null;
                }
        );
    }
}