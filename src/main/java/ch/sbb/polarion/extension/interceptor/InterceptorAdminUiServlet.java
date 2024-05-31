package ch.sbb.polarion.extension.interceptor;

import ch.sbb.polarion.extension.generic.GenericUiServlet;

import java.io.Serial;

public class InterceptorAdminUiServlet extends GenericUiServlet {
    @Serial
    private static final long serialVersionUID = 1652935384860901702L;

    public InterceptorAdminUiServlet() {
        super("interceptor-admin");
    }
}
