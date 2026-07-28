package ch.sbb.polarion.extension.interceptor_manager;

import ch.sbb.polarion.extension.generic.GenericUiServlet;

import java.io.Serial;

public class InterceptorManagerAppServlet extends GenericUiServlet {
    @Serial
    private static final long serialVersionUID = 3894567162055409521L;

    public InterceptorManagerAppServlet() {
        super("interceptor-manager-app");
    }
}
