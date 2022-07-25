INSERT INTO audit_events
    (uuid, case_uuid, stage_uuid, audit_payload, audit_timestamp, type, user_id, case_type)
VALUES ('00000000-0000-0000-0000-000000000000', '10000000-0000-0000-0000-000000000000', '20000000-0000-0000-0000-000000000000',
        '{ "data": {}, "reference": "TEST" }',
        '2020-01-01 00:00:00.000000', 'CASE_CREATED', '40000000-0000-0000-0000-000000000000', 'a1'),

       ('00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000000', '20000000-0000-0000-0000-000000000000',
        '{ "data": { "PreviousCaseReference": "TEST-1" }, "reference": "TEST" }',
        '2020-01-01 01:00:00.000000', 'CASE_UPDATED', '40000000-0000-0000-0000-000000000000', 'a1'),

       ('00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000000', '20000000-0000-0000-0000-000000000001',
        '{ "data": { "PreviousCaseReference": "TEST-1"}, "reference": "TEST" }',
        TIMESTAMP 'today', 'CASE_UPDATED', '40000000-0000-0000-0000-000000000000', 'a1'),

       ('00000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002',
        '{ "data": {}, "reference": "TEST2" }',
        '2020-01-01 00:00:00.000000', 'CASE_CREATED', '40000000-0000-0000-0000-000000000001', 'a2'),

       ('00000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000000', '20000000-0000-0000-0000-000000000000',
        '{"stage": "TEST", "deadline": "2020-01-31", "stageUUID": "20000000-0000-0000-0000-000000000000", "allocatedToUUID": "40000000-0000-0000-0000-000000000000", "deadlineWarning": "2020-01-31"}',
        '2020-01-01 01:00:00.000000', 'STAGE_ALLOCATED_TO_TEAM', '40000000-0000-0000-0000-000000000000', 'a1');
