package ch.sbb.polarion.extension.interceptor_manager.rest.exception.mapper;

import ch.sbb.polarion.extension.interceptor_manager.rest.exception.SettingsValidationException;
import ch.sbb.polarion.extension.interceptor_manager.rest.model.ValidationErrorEntity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The shape of this response is the contract the administration page and the system tests read: a 400 whose
 * body carries the problems one by one, so the page can list them instead of showing one run-on message.
 */
class SettingsValidationExceptionMapperTest {

    private final SettingsValidationExceptionMapper mapper = new SettingsValidationExceptionMapper();

    @Test
    void rejectedSettingsAreReportedAsBadRequest() {
        Response response = mapper.toResponse(new SettingsValidationException(List.of("first problem")));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    }

    @Test
    void everyProblemIsCarriedSeparately() {
        List<String> problems = List.of("first problem", "second problem");

        Response response = mapper.toResponse(new SettingsValidationException(problems));

        ValidationErrorEntity entity = assertInstanceOf(ValidationErrorEntity.class, response.getEntity());
        assertEquals(problems, entity.getValidationErrors());
    }

    @Test
    void theMessageStaysReadableOnItsOwn() {
        // A client which knows nothing about validationErrors still gets every problem, in the message.
        Response response = mapper.toResponse(new SettingsValidationException(List.of("first problem", "second problem")));

        ValidationErrorEntity entity = (ValidationErrorEntity) response.getEntity();
        assertEquals(SettingsValidationException.MESSAGE_PREFIX + System.lineSeparator() + "first problem" + System.lineSeparator() + "second problem",
                entity.getMessage());
    }

    @Test
    void theReportedProblemsCanNotBeChangedThroughTheEntity() {
        ValidationErrorEntity entity = (ValidationErrorEntity) mapper.toResponse(new SettingsValidationException(List.of("first problem"))).getEntity();
        List<String> reported = entity.getValidationErrors();

        assertThrows(UnsupportedOperationException.class, () -> reported.add("another problem"));
    }
}
