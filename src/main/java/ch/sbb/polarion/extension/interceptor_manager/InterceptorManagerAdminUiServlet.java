package ch.sbb.polarion.extension.interceptor_manager;

import ch.sbb.polarion.extension.generic.GenericUiServlet;

import java.io.Serial;

public class InterceptorManagerAdminUiServlet extends GenericUiServlet {
    @Serial
    private static final long serialVersionUID = 1652935384860901702L;

    public InterceptorManagerAdminUiServlet() {
        super("interceptor-manager-admin");
    }
}
