CREATE TABLE IF NOT EXISTS audit_event
(
    uuid                   UUID        NOT NULL,
    case_uuid              UUID,
    stage_uuid             UUID,
    audit_payload          JSONB,
    audit_timestamp        TIMESTAMP   NOT NULL,
    type                   TEXT        NOT NULL,
    user_id                TEXT        NOT NULL,
    case_type              text,
    deleted                BOOLEAN    NOT NULL DEFAULT FALSE,

    PRIMARY KEY (uuid, audit_timestamp),
    CONSTRAINT audit_event_uuid_idempotent UNIQUE (uuid, audit_timestamp)
    ) PARTITION BY RANGE (audit_timestamp);

--- annually
CREATE TABLE audit_event_2018 PARTITION OF audit_event
    FOR VALUES FROM ('2018-01-01') TO ('2019-01-01');

--- annually
CREATE TABLE audit_event_2019 PARTITION OF audit_event
    FOR VALUES FROM ('2019-01-01') TO ('2020-01-01');

---
CREATE TABLE audit_event_2020 PARTITION OF audit_event
    FOR VALUES FROM ('2020-01-01') TO ('2021-01-01');

--- quarterly
CREATE TABLE audit_event_2021_1 PARTITION OF audit_event
    FOR VALUES FROM ('2021-01-01') TO ('2021-04-01');

---
CREATE TABLE audit_event_2021_2 PARTITION OF audit_event
    FOR VALUES FROM ('2021-04-01') TO ('2021-07-01');

---
CREATE TABLE audit_event_2021_3 PARTITION OF audit_event
    FOR VALUES FROM ('2021-07-01') TO ('2021-10-01');

---
CREATE TABLE audit_event_2021_4 PARTITION OF audit_event
    FOR VALUES FROM ('2021-10-01') TO ('2022-01-01');

--- monthly
CREATE TABLE audit_event_2022_1 PARTITION OF audit_event
    FOR VALUES FROM ('2022-01-01') TO ('2022-02-01');

---
CREATE TABLE audit_event_2022_2 PARTITION OF audit_event
    FOR VALUES FROM ('2022-02-01') TO ('2022-03-01');

---
CREATE TABLE audit_event_2022_3 PARTITION OF audit_event
    FOR VALUES FROM ('2022-03-01') TO ('2022-04-01');

---
CREATE TABLE audit_event_2022_4 PARTITION OF audit_event
    FOR VALUES FROM ('2022-04-01') TO ('2022-05-01');

---
CREATE TABLE audit_event_2022_5 PARTITION OF audit_event
    FOR VALUES FROM ('2022-05-01') TO ('2022-06-01');

---
CREATE TABLE audit_event_2022_6 PARTITION OF audit_event
    FOR VALUES FROM ('2022-06-01') TO ('2022-07-01');

---
CREATE TABLE audit_event_2022_7 PARTITION OF audit_event
    FOR VALUES FROM ('2022-07-01') TO ('2022-08-01');

---
CREATE TABLE audit_event_2022_8 PARTITION OF audit_event
    FOR VALUES FROM ('2022-08-01') TO ('2022-09-01');

---
CREATE TABLE audit_event_2022_9 PARTITION OF audit_event
    FOR VALUES FROM ('2022-09-01') TO ('2022-10-01');

---
CREATE TABLE audit_event_2022_10 PARTITION OF audit_event
    FOR VALUES FROM ('2022-10-01') TO ('2022-11-01');

---
CREATE TABLE audit_event_2022_11 PARTITION OF audit_event
    FOR VALUES FROM ('2022-11-01') TO ('2022-12-01');

---
CREATE TABLE audit_event_2022_12 PARTITION OF audit_event
    FOR VALUES FROM ('2022-12-01') TO ('2023-01-01');

---

CREATE INDEX idx_audit_events_type_case_uuid ON audit_event (case_uuid, type) WHRRE case_uuid IS NOT NULL;
CREATE INDEX idx_audit_events_type_case_type ON audit_event (case_type, type) WHERE case_type IS NOT NULL;
