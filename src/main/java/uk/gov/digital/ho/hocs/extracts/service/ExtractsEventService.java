package uk.gov.digital.ho.hocs.extracts.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.extracts.core.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.extracts.repository.AuditRepository;
import uk.gov.digital.ho.hocs.extracts.repository.entity.AuditEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.extracts.core.LogEvent.EXTRACTS_EVENT_DELETED;
import static uk.gov.digital.ho.hocs.extracts.core.LogEvent.EVENT;

@Service
@Slf4j
public class ExtractsEventService {

    private final AuditRepository auditRepository;
    private static List<String> EXCLUDED_EVENT_TYPES = List.of(
            "CASE_SUMMARY_VIEWED",
            "CASE_VIEWED",
            "SOMU_ITEMS_VIEWED",
            "SOMU_ITEM_VIEWED",
            "STANDARD_LINE_VIEWED",
            "TEMPLATE_VIEWED");

    @Autowired
    public ExtractsEventService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public AuditEvent createExtractsEvent(String auditPayload, LocalDateTime auditTimestamp, String type, String userID) {
        return createExtractsEvent(null, null, auditPayload, auditTimestamp, type, userID);
    }

    public AuditEvent createExtractsEvent(UUID caseUUID, UUID stageUUID, String auditPayload, LocalDateTime auditTimestamp, String type, String userID) {
        if(type == null) {
            throw new EntityCreationException("Cannot create Extracts event without a type for case {} at timestamp: {}", caseUUID, auditTimestamp);
        }

        if(EXCLUDED_EVENT_TYPES.contains(type)) {
            log.debug("Ignoring message type {}", type);
            return null;
        }

        AuditEvent auditEvent = new AuditEvent(caseUUID, stageUUID, auditPayload, auditTimestamp, type, userID);
        auditRepository.save(auditEvent);
        log.debug("Created extracts event: UUID: {} at timestamp: {}", auditEvent.getUuid(), auditEvent.getAuditTimestamp());
        return auditEvent;
    }

    public Integer deleteCaseExtractsEvent(UUID caseUUID, Boolean deleted) {
        List<AuditEvent> audits = auditRepository.findAuditDataByCaseUUID(caseUUID);
        for (AuditEvent audit : audits) {
            audit.setDeleted(deleted);
            auditRepository.save(audit);
        }
        log.info("Set Deleted=({}) for {} extracts lines for caseUUID: {}", deleted, audits.size(), caseUUID, value(EVENT, EXTRACTS_EVENT_DELETED));
        return audits.size();
    }
}
