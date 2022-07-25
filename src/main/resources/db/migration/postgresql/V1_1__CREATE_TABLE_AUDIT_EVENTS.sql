CREATE TABLE IF NOT EXISTS audit_events
(
    uuid                   UUID        NOT NULL,
    case_uuid              UUID,
    stage_uuid             UUID,
    audit_payload          JSONB,
    audit_timestamp        TIMESTAMP   NOT NULL,
    type                   TEXT        NOT NULL,
    user_id                TEXT        NOT NULL,
    case_type              text,
    deleted                BOOLEAN     NOT NULL DEFAULT FALSE,

    PRIMARY KEY (uuid, audit_timestamp)
) PARTITION BY RANGE (audit_timestamp);

--- annually
CREATE TABLE audit_events_2018 PARTITION OF audit_events
    FOR VALUES FROM ('2018-01-01') TO ('2019-01-01');

--- annually
CREATE TABLE audit_events_2019 PARTITION OF audit_events
    FOR VALUES FROM ('2019-01-01') TO ('2020-01-01');

--- annually
CREATE TABLE audit_events_2020 PARTITION OF audit_events
    FOR VALUES FROM ('2020-01-01') TO ('2021-01-01');

--- monthly
CREATE TABLE audit_events_2021_01 PARTITION OF audit_events
    FOR VALUES FROM ('2021-01-01') TO ('2021-02-01');

---
CREATE TABLE audit_events_2021_02 PARTITION OF audit_events
    FOR VALUES FROM ('2021-02-01') TO ('2021-03-01');

---
CREATE TABLE audit_events_2021_03 PARTITION OF audit_events
    FOR VALUES FROM ('2021-03-01') TO ('2021-04-01');

---
CREATE TABLE audit_events_2021_04 PARTITION OF audit_events
    FOR VALUES FROM ('2021-04-01') TO ('2021-05-01');

---
CREATE TABLE audit_events_2021_05 PARTITION OF audit_events
    FOR VALUES FROM ('2021-05-01') TO ('2021-06-01');

---
CREATE TABLE audit_events_2021_06 PARTITION OF audit_events
    FOR VALUES FROM ('2021-06-01') TO ('2021-07-01');

---
CREATE TABLE audit_events_2021_07 PARTITION OF audit_events
    FOR VALUES FROM ('2021-07-01') TO ('2021-08-01');

---
CREATE TABLE audit_events_2021_08 PARTITION OF audit_events
    FOR VALUES FROM ('2021-08-01') TO ('2021-09-01');

---
CREATE TABLE audit_events_2021_09 PARTITION OF audit_events
    FOR VALUES FROM ('2021-09-01') TO ('2021-10-01');

---
CREATE TABLE audit_events_2021_10 PARTITION OF audit_events
    FOR VALUES FROM ('2021-10-01') TO ('2021-11-01');

---
CREATE TABLE audit_events_2021_11 PARTITION OF audit_events
    FOR VALUES FROM ('2021-11-01') TO ('2021-12-01');

---
CREATE TABLE audit_events_2021_12 PARTITION OF audit_events
    FOR VALUES FROM ('2021-12-01') TO ('2022-01-01');

--- monthly
CREATE TABLE audit_events_2022_01 PARTITION OF audit_events
    FOR VALUES FROM ('2022-01-01') TO ('2022-02-01');

---
CREATE TABLE audit_events_2022_02 PARTITION OF audit_events
    FOR VALUES FROM ('2022-02-01') TO ('2022-03-01');

---
CREATE TABLE audit_events_2022_03 PARTITION OF audit_events
    FOR VALUES FROM ('2022-03-01') TO ('2022-04-01');

---
CREATE TABLE audit_events_2022_04 PARTITION OF audit_events
    FOR VALUES FROM ('2022-04-01') TO ('2022-05-01');

---
CREATE TABLE audit_events_2022_05 PARTITION OF audit_events
    FOR VALUES FROM ('2022-05-01') TO ('2022-06-01');

---
CREATE TABLE audit_events_2022_06 PARTITION OF audit_events
    FOR VALUES FROM ('2022-06-01') TO ('2022-07-01');

---
CREATE TABLE audit_events_2022_07 PARTITION OF audit_events
    FOR VALUES FROM ('2022-07-01') TO ('2022-08-01');

---
CREATE TABLE audit_events_2022_08 PARTITION OF audit_events
    FOR VALUES FROM ('2022-08-01') TO ('2022-09-01');

---
CREATE TABLE audit_events_2022_09 PARTITION OF audit_events
    FOR VALUES FROM ('2022-09-01') TO ('2022-10-01');

---
CREATE TABLE audit_events_2022_10 PARTITION OF audit_events
    FOR VALUES FROM ('2022-10-01') TO ('2022-11-01');

---
CREATE TABLE audit_events_2022_11 PARTITION OF audit_events
    FOR VALUES FROM ('2022-11-01') TO ('2022-12-01');

---
CREATE TABLE audit_events_2022_12 PARTITION OF audit_events
    FOR VALUES FROM ('2022-12-01') TO ('2023-01-01');

---

CREATE INDEX idx_audit_events_type_case_uuid ON audit_events (case_uuid, type) WHERE case_uuid IS NOT NULL;
CREATE INDEX idx_audit_events_type_case_type ON audit_events (case_type, type) WHERE case_type IS NOT NULL;
