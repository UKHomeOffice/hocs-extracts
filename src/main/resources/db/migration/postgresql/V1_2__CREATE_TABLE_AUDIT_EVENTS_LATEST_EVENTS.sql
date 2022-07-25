CREATE TABLE IF NOT EXISTS audit_events_latest
(
    uuid                   UUID        NOT NULL,
    case_uuid              UUID,
    stage_uuid             UUID,
    audit_payload          JSONB,
    audit_timestamp        TIMESTAMP   NOT NULL,
    type                   TEXT        NOT NULL,
    user_id                TEXT        NOT NULL,
    case_type              TEXT,
    deleted                BOOLEAN     NOT NULL DEFAULT FALSE,

    PRIMARY KEY (uuid),
    CONSTRAINT audit_events_latest_case_uuid_type UNIQUE (case_uuid, type)
);

CREATE INDEX IF NOT EXISTS audit_events_latest_type_case_type ON audit_events_latest (case_type, type);


CREATE OR REPLACE FUNCTION upsertLatestAuditEvents() RETURNS TRIGGER AS
$BODY$
BEGIN
INSERT INTO audit_events_latest
(uuid, case_uuid, stage_uuid, audit_payload, audit_timestamp, type, user_id, case_type)
VALUES (NEW.uuid, NEW.case_uuid, NEW.stage_uuid, NEW.audit_payload, NEW.audit_timestamp, NEW.type, NEW.user_id, NEW.case_type)
    ON CONFLICT (case_uuid, type) DO UPDATE
                                         SET uuid = EXCLUDED.uuid, stage_uuid = EXCLUDED.stage_uuid, audit_payload = EXCLUDED.audit_payload,
                                         audit_timestamp = EXCLUDED.audit_timestamp, user_id = EXCLUDED.user_id
                                     WHERE audit_events_latest.audit_timestamp < excluded.audit_timestamp;

RETURN NEW;
END;
$BODY$
language plpgsql;

CREATE OR REPLACE FUNCTION updateLatestAuditEventsDeleted() RETURNS TRIGGER AS
$BODY$
BEGIN
UPDATE audit_events_latest
SET deleted = NEW.deleted
WHERE uuid = NEW.uuid;

RETURN NEW;
END;
$BODY$
language plpgsql;

CREATE TRIGGER auditEventLatestTypesTrigger
    AFTER INSERT ON audit_events
    FOR EACH ROW
    WHEN (NEW.type in ('CASE_CREATED', 'CASE_UPDATED', 'CASE_COMPLETED'))
    EXECUTE PROCEDURE upsertLatestAuditEvents();

CREATE TRIGGER auditEventUpdateDeletedFlag
    AFTER UPDATE ON audit_events
    FOR EACH ROW
    WHEN (OLD.deleted <> NEW.deleted)
    EXECUTE PROCEDURE updateLatestAuditEventsDeleted();
