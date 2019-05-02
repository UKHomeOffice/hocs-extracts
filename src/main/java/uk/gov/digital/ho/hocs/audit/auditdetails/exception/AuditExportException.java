package uk.gov.digital.ho.hocs.audit.auditdetails.exception;

public class AuditExportException extends RuntimeException {

    public AuditExportException(String msg, Object... args) {
        super(String.format(msg, args));
    }
}