package uk.gov.digital.ho.hocs.extracts.repository.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@IdClass(CompositeKey.class)
@Table(name = "audit_events")
@NoArgsConstructor
public class AuditEvent implements Serializable {

    @Id
    @Column(name = "uuid")
    @Getter
    private UUID uuid;

    @Column(name = "case_uuid")
    @Getter
    private UUID caseUUID;

    @Column(name = "stage_uuid")
    @Getter
    private UUID stageUUID;

    @Getter
    @Column(name = "audit_payload")
    private String auditPayload;

    @Id
    @Column(name = "auditTimestamp")
    @Getter
    private LocalDateTime auditTimestamp;

    @Column(name = "type")
    @Getter
    private String type;

    @Column(name = "user_id")
    @Getter
    private String userID;

    @Column(name = "case_type")
    @Getter
    private String caseType;

    @Column(name = "deleted")
    @Getter
    @Setter
    private Boolean deleted;

    public AuditEvent(String auditPayload, LocalDateTime auditTimestamp, String type, String userID) {
        this.uuid = UUID.randomUUID();
        this.auditPayload = auditPayload;
        this.auditTimestamp = auditTimestamp;
        this.type = type;
        this.userID = userID;
        if(caseUUID != null) {
            this.caseType = caseUUID.toString().substring(34);
        }
        this.deleted = false;
    }

    public AuditEvent(UUID caseUUID, UUID stageUUID, String auditPayload, LocalDateTime auditTimestamp, String type, String userID) {
        this(auditPayload, auditTimestamp, type, userID);
        this.caseUUID = caseUUID;
        this.stageUUID = stageUUID;
        if(caseUUID != null) {
            this.caseType = caseUUID.toString().substring(34);
        }
    }

}
