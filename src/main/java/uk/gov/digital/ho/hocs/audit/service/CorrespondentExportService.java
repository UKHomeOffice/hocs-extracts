package uk.gov.digital.ho.hocs.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.audit.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.client.casework.dto.GetCorrespondentOutlineResponse;
import uk.gov.digital.ho.hocs.audit.client.info.InfoClient;
import uk.gov.digital.ho.hocs.audit.client.info.dto.UserDto;
import uk.gov.digital.ho.hocs.audit.core.utils.ZonedDateTimeConverter;
import uk.gov.digital.ho.hocs.audit.entrypoint.dto.AuditPayload;
import uk.gov.digital.ho.hocs.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.repository.entity.AuditEvent;
import uk.gov.digital.ho.hocs.audit.service.domain.ExportType;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.ExportDataConverter;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.HeaderConverter;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.MalformedDateConverter;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CorrespondentExportService extends DynamicExportService {

    private static final String[] EVENTS = {"CORRESPONDENT_DELETED", "CORRESPONDENT_CREATED", "CORRESPONDENT_UPDATED"};

    public CorrespondentExportService(ObjectMapper objectMapper, AuditRepository auditRepository, InfoClient infoClient,
                                      CaseworkClient caseworkClient, HeaderConverter headerConverter,
                                      MalformedDateConverter malformedDateConverter) {
        super(objectMapper, auditRepository, infoClient, caseworkClient, headerConverter, malformedDateConverter);
    }

    @Override
    public ExportType getExportType() {
        return ExportType.CORRESPONDENTS;
    }

    @Override
    protected String[] parseData(AuditEvent audit, String caseType, ZonedDateTimeConverter zonedDateTimeConverter, ExportDataConverter exportDataConverter) throws JsonProcessingException {
        AuditPayload.Correspondent correspondentData = objectMapper.readValue(audit.getAuditPayload(), AuditPayload.Correspondent.class);

        List<String> data = new ArrayList<>();
        data.add(zonedDateTimeConverter.convert(audit.getAuditTimestamp()));
        data.add(audit.getType());
        data.add(exportDataConverter.convertValue(audit.getUserID()));
        data.add(exportDataConverter.convertCaseUuid(audit.getCaseUUID()));
        data.add(exportDataConverter.convertValue(correspondentData.getUuid().toString()));
        data.add(correspondentData.getFullname());
        data.add(correspondentData.getOrganisation());

        if (correspondentData.getAddress() != null) {
            data.add(correspondentData.getAddress().getAddress1());
            data.add(correspondentData.getAddress().getAddress2());
            data.add(correspondentData.getAddress().getAddress3());
            data.add(correspondentData.getAddress().getCountry());
            data.add(correspondentData.getAddress().getPostcode());
        } else {
            data.addAll(List.of("", "", "", "", ""));
        }

        data.add(correspondentData.getTelephone());
        data.add(correspondentData.getEmail());
        data.add(correspondentData.getReference());
        data.add(correspondentData.getExternalKey());
        return data.toArray(new String[0]);
    }

    @Override
    protected Stream<AuditEvent> getData(LocalDate from, LocalDate to, String caseType, String[] events) {
        LocalDate peggedTo = to.isAfter(LocalDate.now()) ? LocalDate.now() : to;

        return auditRepository.findAuditDataByDateRangeAndEvents(LocalDateTime.of(
                        from, LocalTime.MIN), LocalDateTime.of(peggedTo, LocalTime.MAX),
                events, getCaseTypeCode(caseType));
    }

    @Override
    public void export(LocalDate from, LocalDate to, PrintWriter writer, String caseType, boolean convert, boolean convertHeader, ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        var dataConverter = getDataConverter(convert, caseType);
        var data = getData(from, to, caseType, EVENTS);

        printData(writer, zonedDateTimeConverter, dataConverter, convertHeader, caseType, data);
    }

    @Override
    protected String[] getHeaders() {
        return new String[]{"timestamp", "event", "userId", "caseUuid",
                "correspondentUuid", "fullname", "organisation", "address1", "address2",
                "address3", "country", "postcode", "telephone", "email",
                "reference", "externalKey"};
    }

    @Override
    protected ExportDataConverter getDataConverter(boolean convert, String caseType) {
        if (!convert) {
            return new ExportDataConverter();
        }

        Map<String, String> uuidToName = new HashMap<>();

        uuidToName.putAll(infoClient.getUsers().stream()
                .collect(Collectors.toMap(UserDto::getId, UserDto::getUsername)));
        uuidToName.putAll(caseworkClient.getAllActiveCorrespondents().stream()
                .collect(Collectors.toMap(corr -> corr.getUuid().toString(), GetCorrespondentOutlineResponse::getFullname)));

        return new ExportDataConverter(uuidToName, Collections.emptyMap(), caseworkClient);
    }
}
