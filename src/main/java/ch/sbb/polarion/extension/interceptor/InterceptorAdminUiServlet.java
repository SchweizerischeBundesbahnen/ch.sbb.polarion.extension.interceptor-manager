package ch.sbb.polarion.extension.interceptor;

import com.polarion.core.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;

public class InterceptorAdminUiServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(InterceptorAdminUiServlet.class);

    @Serial
    private static final long serialVersionUID = 4323903250755251706L;
    private static final String WEB_APP_NAME = "interceptor-admin";

    private static void setContentType(@NotNull String uri, @NotNull HttpServletResponse response) {
        if (uri.endsWith(".js")) {
            response.setContentType("text/javascript");
        } else if (uri.endsWith(".html")) {
            response.setContentType("text/html");
        } else if (uri.endsWith(".png")) {
            response.setContentType("image/png");
        } else if (uri.endsWith(".css")) {
            response.setContentType("text/css");
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();
        String relativeUri = uri.substring("/polarion/".length());

        if (relativeUri.startsWith(WEB_APP_NAME + "/ui/")) {
            serveResource(response, relativeUri.substring((WEB_APP_NAME + "/ui").length()));
        }
    }

    private void serveResource(@NotNull HttpServletResponse response, @NotNull String uri) {
        try (InputStream inputStream = getServletContext().getResourceAsStream(uri)) {

            if (inputStream == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                try (ServletOutputStream outputStream = response.getOutputStream()) {
                    setContentType(uri, response);
                    IOUtils.copy(inputStream, outputStream);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
