package ch.sbb.polarion.extension.interceptor.rest.controller;

import com.polarion.core.config.Configuration;
import com.polarion.core.config.IRestConfiguration;
import io.swagger.v3.oas.annotations.Hidden;
import org.jetbrains.annotations.NotNull;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Hidden
@Path("/swagger")
public class SwaggerController {
    private static final String URI_PATTERN = "$uri_replace";
    private static final String PERSISTENCE_PATTERN = "$persistence_replace";

    @Context
    private UriInfo uriInfo;

    @GET
    @Produces({MediaType.TEXT_HTML})
    @NotNull
    public Response swagger() throws IOException {
        IRestConfiguration configuration = Configuration.getInstance().rest();
        if (!(configuration.enabled() && configuration.swaggerUiEnabled())) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }

        try (InputStream inputStream = getClass().getResourceAsStream("/swagger_ui.html")) {
            URI uri = uriInfo.getAbsolutePath().resolve("swagger/definition.json");
            String html = new String(Objects.requireNonNull(inputStream).readAllBytes(), StandardCharsets.UTF_8)
                    .replace(URI_PATTERN, uri.toString())
                    .replace(PERSISTENCE_PATTERN, String.valueOf(configuration.swaggerUiPersistAuthorization()));
            return Response.status(Response.Status.OK).entity(html).build();
        }
    }
}
