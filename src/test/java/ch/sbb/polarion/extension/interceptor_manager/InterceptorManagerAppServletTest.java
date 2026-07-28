package ch.sbb.polarion.extension.interceptor_manager;

import ch.sbb.polarion.extension.generic.GenericUiServlet;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InterceptorManagerAppServletTest {

    /**
     * The webapp name has to match the context registered in plugin.xml and the paths hivemodule.xml
     * opens; a mismatch serves nothing and stays invisible until an administration page is opened.
     */
    @Test
    void servesTheReactAppWebapp() throws Exception {
        InterceptorManagerAppServlet servlet = new InterceptorManagerAppServlet();

        Field field = GenericUiServlet.class.getDeclaredField("webAppName");
        field.setAccessible(true);

        assertEquals("interceptor-manager-app", field.get(servlet));
    }
}
