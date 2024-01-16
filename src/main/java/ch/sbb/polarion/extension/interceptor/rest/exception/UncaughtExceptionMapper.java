package ch.sbb.polarion.extension.interceptor.rest.exception;

import ch.sbb.polarion.extension.interceptor.rest.filter.LogoutFilter;
import com.polarion.core.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Default mapper for all unmapped exceptions.
 * If you delete this mapper then {@link LogoutFilter}
 * will stop working.
 */
@Provider
public class UncaughtExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger logger = Logger.getLogger(UncaughtExceptionMapper.class);

    public Response toResponse(Throwable throwable) {
        if (throwable instanceof WebApplicationException webapplicationexception) {

            //this block covers cases when the specific WebApplicationException was thrown but
            //there is no explicit mapper for it (e.g. NotAuthorizedException)
            return webapplicationexception.getResponse();
        } else {
            logger.error("Uncaught exception", throwable);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                    .entity(throwable.getMessage())
                    .build();
        }
    }
}
