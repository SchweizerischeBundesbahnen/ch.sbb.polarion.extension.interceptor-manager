package ch.sbb.polarion.extension.interceptor_manager.rest.model;

import ch.sbb.polarion.extension.generic.rest.model.ErrorEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

/**
 * An {@link ErrorEntity} which also lists the individual problems, so that the administration page can
 * render them one by one instead of showing a single run-on message.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Details about a settings validation error")
public class ValidationErrorEntity extends ErrorEntity {

    @Schema(description = "The problems which make the settings invalid", example = "[\"Settings must contain the entry \\\"projects\\\"\"]")
    private final List<String> validationErrors;

    public ValidationErrorEntity(String message, List<String> validationErrors) {
        super(message);
        this.validationErrors = List.copyOf(validationErrors);
    }
}
