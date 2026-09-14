package ch.sbb.polarion.extension.interceptor_manager.rest.exception.mapper;

import ch.sbb.polarion.extension.interceptor_manager.rest.exception.SettingsValidationException;
import ch.sbb.polarion.extension.interceptor_manager.rest.model.ValidationErrorEntity;
import com.polarion.core.util.logging.Logger;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps rejected hook settings to 400 with the list of problems. Registered next to the generic
 * IllegalArgumentException mapper, which stays in charge of every other illegal argument.
 */
@Provider
public class SettingsValidationExceptionMapper implements ExceptionMapper<SettingsValidationException> {
    private static final Logger logger = Logger.getLogger(SettingsValidationExceptionMapper.class);

    @Override
    public Response toResponse(SettingsValidationException e) {
        logger.error("Invalid hook settings: " + e.getMessage(), e);
        return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
                .entity(new ValidationErrorEntity(e.getMessage(), e.getValidationErrors()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
