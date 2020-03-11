package uk.gov.digital.ho.hocs.audit.application;

public enum LogEvent {

    AUDIT_EVENT_CREATED,
    AUDIT_RECORD_NOT_FOUND,
    AUDIT_STARTUP_FAILURE,
    INVALID_AUDIT_PALOAD_STORED,
    AUDIT_EVENT_CREATION_FAILED,
    UNCAUGHT_EXCEPTION,
    REST_HELPER_GET,
    INFO_CLIENT_GET_TEAMS_SUCCESS,
    INFO_CLIENT_GET_TEAM_SUCCESS,
    INFO_CLIENT_GET_CASE_TYPES_SUCCESS,
    INFO_CLIENT_GET_USERS_SUCCESS,
    INFO_CLIENT_GET_USER_SUCCESS,
    INFO_CLIENT_GET_EXPORT_FIELDS_SUCCESS,
    INFO_CLIENT_GET_EXPORT_VIEWS_SUCCESS,
    INFO_CLIENT_GET_EXPORT_VIEW_SUCCESS,
    INFO_CLIENT_GET_EXPORT_VIEW_FAILURE,
    CSV_EXPORT_START,
    CSV_EXPORT_COMPETE,
    CSV_EXPORT_FAILURE, INFO_CLIENT_GET_TOPIC_SUCCESS;

    public static final String EVENT = "event_id";
    public static final String EXCEPTION = "exception";
}
