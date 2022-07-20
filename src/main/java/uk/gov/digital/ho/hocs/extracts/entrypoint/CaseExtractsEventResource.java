package uk.gov.digital.ho.hocs.extracts.entrypoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.extracts.entrypoint.dto.DeleteCaseExtractsDto;
import uk.gov.digital.ho.hocs.extracts.entrypoint.dto.DeleteCaseAuditResponse;
import uk.gov.digital.ho.hocs.extracts.service.ExtractsEventService;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
class CaseExtractsEventResource {
    private final ExtractsEventService exportEventService;

    @Autowired
    public CaseExtractsEventResource(ExtractsEventService exportEventService) {
        this.exportEventService = exportEventService;
    }

    @PostMapping(value = "/extracts/case/{caseUUID}/delete", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<DeleteCaseAuditResponse> deleteCaseAudit(@PathVariable UUID caseUUID, @RequestBody DeleteCaseExtractsDto request) {
        Integer auditCount = exportEventService.deleteCaseExtractsEvent(caseUUID, request.getDeleted());
        return ResponseEntity.ok(DeleteCaseAuditResponse.from(caseUUID, request, auditCount));
    }

}
