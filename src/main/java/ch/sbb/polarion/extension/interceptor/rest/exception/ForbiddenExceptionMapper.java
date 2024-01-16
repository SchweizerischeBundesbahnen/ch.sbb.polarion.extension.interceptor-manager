package ch.sbb.polarion.extension.interceptor.rest.exception;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ForbiddenExceptionMapper implements ExceptionMapper<ForbiddenException> {

    public Response toResponse(ForbiddenException e) {
        return Response.status(Response.Status.FORBIDDEN.getStatusCode())
                .entity(e.getMessage())
                .build();
    }
}
