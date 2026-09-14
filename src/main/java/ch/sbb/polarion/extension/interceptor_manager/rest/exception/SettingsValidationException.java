package ch.sbb.polarion.extension.interceptor_manager.rest.exception;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Thrown when hook settings are rejected, carrying every problem found rather than only the first one.
 * <p>
 * Extends {@link IllegalArgumentException} so that a client which knows nothing about this type still
 * gets the generic 400 response: the joined message stays readable on its own.
 */
@Getter
public class SettingsValidationException extends IllegalArgumentException {

    public static final String MESSAGE_PREFIX = "Hook settings can not be saved:";

    private final transient List<String> validationErrors;

    public SettingsValidationException(@NotNull List<String> validationErrors) {
        super(MESSAGE_PREFIX + System.lineSeparator() + String.join(System.lineSeparator(), validationErrors));
        this.validationErrors = List.copyOf(validationErrors);
    }
}
