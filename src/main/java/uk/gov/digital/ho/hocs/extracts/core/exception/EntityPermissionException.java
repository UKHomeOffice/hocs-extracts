package uk.gov.digital.ho.hocs.extracts.core.exception;


public class EntityPermissionException extends RuntimeException {

    public EntityPermissionException(String msg, Object... args) {
        super(String.format(msg, args));
    }
}
