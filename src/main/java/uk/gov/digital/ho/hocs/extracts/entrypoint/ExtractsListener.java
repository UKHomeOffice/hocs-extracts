package uk.gov.digital.ho.hocs.extracts.entrypoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.extracts.core.RequestData;
import uk.gov.digital.ho.hocs.extracts.entrypoint.dto.CreateExtractsDto;
import uk.gov.digital.ho.hocs.extracts.service.ExtractsEventService;

import java.util.Map;

@Service
public class ExtractsListener {

    private final ObjectMapper objectMapper;
    private final ExtractsEventService exportEventService;
    private final RequestData requestData;


    public ExtractsListener(ObjectMapper objectMapper,
                            ExtractsEventService exportEventService,
                            RequestData requestData) {
        this.objectMapper = objectMapper;
        this.exportEventService = exportEventService;
        this.requestData = requestData;
    }

    @SqsListener(value = "${aws.sqs.extracts.url}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void onAuditEvent(
            String message,
            @Headers Map<String, String> headers
    ) throws JsonProcessingException {
        try {
            requestData.parseMessageHeaders(headers);
            CreateExtractsDto createAuditEvent = objectMapper.readValue(message, CreateExtractsDto.class);
            exportEventService.createExtractsEvent(createAuditEvent.getCaseUUID(),
                    createAuditEvent.getStageUUID(),
                    createAuditEvent.getAuditPayload(),
                    createAuditEvent.getAuditTimestamp(),
                    createAuditEvent.getType(),
                    createAuditEvent.getUserID());
        } finally {
            requestData.clear();
        }
    }

}
