package uk.gov.digital.ho.hocs.extracts.entrypoint.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class CreateExtractsDto {

    @JsonProperty("correlation_id")
    private String correlationID;

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty(value = "stageUUID")
    private UUID stageUUID;

    @JsonProperty(value = "raising_service")
    private String raisingService;

    @JsonProperty(value = "audit_payload")
    private String auditPayload;

    @JsonProperty(value = "namespace")
    private String namespace;

    @JsonProperty(value = "audit_timestamp")
    private LocalDateTime auditTimestamp;

    @JsonProperty(value = "type")
    private String type;

    @JsonProperty(value = "user_id")
    private String userID;

    public CreateExtractsDto(String correlationID, String raisingService, String auditPayload, String namespace, LocalDateTime auditTimestamp, String type, String userID){
        this.correlationID = correlationID;
        this.raisingService = raisingService;
        this.auditPayload = auditPayload;
        this.namespace = namespace;
        this.auditTimestamp = auditTimestamp;
        this.type = type;
        this.userID = userID;
    }
}
