package uk.gov.digital.ho.hocs.extracts.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.digital.ho.hocs.extracts.core.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.extracts.repository.AuditRepository;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:export/cleandown.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ExtractsEventServiceTest {

    private final String correlationID = "CORRELATION_ID";
    private final String raisingService = "RAISING_SERVICE";
    private final String auditPayload = "{\"Test1\":\"Value1\"}";
    private final String namespace = "NAMESPACE";
    private final LocalDateTime dateTime = LocalDateTime.now();
    private final String auditType = "TYPE";
    private final String userID = "USER";

    @Autowired
    private ExtractsEventService extractsEventService;

    @Autowired
    private AuditRepository auditRepository;

    @Test
    public void shouldCreateAudit() {
        extractsEventService.createExtractsEvent(auditPayload,
                dateTime,
                auditType,
                userID);

        Assertions.assertEquals(1, auditRepository.count());
    }

    @Test
    public void shouldNotCreateWithNullTimestamp() {
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            extractsEventService.createExtractsEvent(auditPayload,
                    null,
                    auditType,
                    userID);
        });
    }

    @Test
    public void shouldNotCreateWithNullType() {
        Assertions.assertThrows(EntityCreationException.class, () -> {
            extractsEventService.createExtractsEvent(auditPayload,
                    dateTime,
                    null,
                    userID);
        });
    }

    @Test
    public void shouldNotCreateWithNullUser() {
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            extractsEventService.createExtractsEvent(auditPayload,
                    dateTime,
                    auditType,
                    null);
        });
    }

    @Test
    public void shouldCreateAuditWhenAuditPayloadIsInvalid() {
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            extractsEventService.createExtractsEvent("\"Test\" \"Test\"",
                    dateTime,
                    auditType,
                    userID);
        });
    }

    @Test
    public void shouldCreateAuditWhenAuditPayloadIsEmpty() {
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            extractsEventService.createExtractsEvent("",
                    dateTime,
                    auditType,
                    userID);
        });
    }

    @Test
    public void shouldCreateAuditWhenAuditPayloadIsNull() {
        extractsEventService.createExtractsEvent(null,
                dateTime,
                auditType,
                userID);

        Assertions.assertEquals(1, auditRepository.count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CASE_SUMMARY_VIEWED", "CASE_VIEWED", "SOMU_ITEMS_VIEWED", "SOMU_ITEM_VIEWED", "STANDARD_LINE_VIEWED", "TEMPLATE_VIEWED"})
    public void shouldNotCreateIgnoredEventTypes(String value) {
        extractsEventService.createExtractsEvent(auditPayload,
                dateTime,
                value,
                userID);

        Assertions.assertEquals(0, auditRepository.findAll().stream().filter(e-> e.getType().equals(value)).count());
    }

    @Test
    public void deleteCaseAuditShouldMarkAsDeleted() {
        UUID caseUuid = UUID.randomUUID();

        // setup case preparation
        extractsEventService.createExtractsEvent(caseUuid, UUID.randomUUID(), null,
                dateTime, auditType, userID);

        extractsEventService.deleteCaseExtractsEvent(caseUuid, true);

        var audits = auditRepository.findAuditDataByCaseUUID(caseUuid);
        Assertions.assertEquals(1, audits.size());
        Assertions.assertTrue(audits.get(0).getDeleted());
    }

}
