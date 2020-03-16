package uk.gov.digital.ho.hocs.audit.auditdetails.exception;


public class EntityPermissionException extends RuntimeException {

    public EntityPermissionException(String msg, Object... args) {
        super(String.format(msg, args));
    }
}