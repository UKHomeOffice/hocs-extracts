package uk.gov.digital.ho.hocs.extracts.entrypoint.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.digital.ho.hocs.extracts.entrypoint.dto.CreateExtractsDto;
import uk.gov.digital.ho.hocs.extracts.repository.AuditRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:export/cleandown.sql", config = @SqlConfig(transactionMode = ISOLATED))
public class ExtractsListenerTest extends BaseAwsSqsIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuditRepository auditRepository;

    @Test
    public void consumeMessageFromQueue() throws JsonProcessingException {
        CreateExtractsDto createExtractsDto = new CreateExtractsDto(UUID.randomUUID().toString(), "SERVICE", "{}",
                "NAMESPACE", LocalDateTime.now(), "TYPE", "USER");

        amazonSQSAsync.sendMessage(auditQueue, objectMapper.writeValueAsString(createExtractsDto));

        await().until(() -> getNumberOfMessagesOnQueue(auditQueue) == 0);
        await().until(() -> auditRepository.count() == 1);
    }

    @Test
    public void consumeMessageFromQueue_exceptionMakesMessageNotVisible() throws JsonProcessingException {
        CreateExtractsDto createExtractsDto = new CreateExtractsDto(null, null, null,
                null, null, null, null);

        amazonSQSAsync.sendMessage(auditQueue, objectMapper.writeValueAsString(createExtractsDto));

        await().until(() -> getNumberOfMessagesOnQueue(auditQueue) == 0);
        await().timeout(Duration.ofSeconds(20))
                .pollDelay(Duration.ofSeconds(10))
                .until(() -> getNumberOfMessagesOnQueue(auditQueueDlq) == 1);
    }

}
