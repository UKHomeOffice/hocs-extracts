package uk.gov.digital.ho.hocs.extracts.repository.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class CompositeKey implements Serializable {
    private UUID uuid;
    private LocalDateTime auditTimestamp;
}
