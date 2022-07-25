package uk.gov.digital.ho.hocs.extracts.core.exception;

public class EntityCreationException extends RuntimeException {

    public EntityCreationException(String msg, Object... args) {
        super(String.format(msg, args));
    }
}
