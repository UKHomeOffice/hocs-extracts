package uk.gov.digital.ho.hocs.extracts.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.extracts.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.extracts.client.casework.dto.GetCorrespondentOutlineResponse;
import uk.gov.digital.ho.hocs.extracts.client.info.InfoClient;
import uk.gov.digital.ho.hocs.extracts.client.info.dto.CaseTypeActionDto;
import uk.gov.digital.ho.hocs.extracts.client.info.dto.CaseTypeDto;
import uk.gov.digital.ho.hocs.extracts.client.info.dto.EntityDto;
import uk.gov.digital.ho.hocs.extracts.client.info.dto.TeamDto;
import uk.gov.digital.ho.hocs.extracts.client.info.dto.UnitDto;
import uk.gov.digital.ho.hocs.extracts.client.info.dto.UserDto;
import uk.gov.digital.ho.hocs.extracts.core.utils.ZonedDateTimeConverter;
import uk.gov.digital.ho.hocs.extracts.entrypoint.dto.ExtractsPayload;
import uk.gov.digital.ho.hocs.extracts.repository.AuditRepository;
import uk.gov.digital.ho.hocs.extracts.repository.entity.AuditEvent;
import uk.gov.digital.ho.hocs.extracts.service.domain.ExportType;
import uk.gov.digital.ho.hocs.extracts.service.domain.converter.ExportDataConverter;
import uk.gov.digital.ho.hocs.extracts.service.domain.converter.HeaderConverter;
import uk.gov.digital.ho.hocs.extracts.service.domain.converter.MalformedDateConverter;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CaseDataExportService extends CaseDataDynamicExportService {

    static final String[] EVENTS = {"CASE_CREATED", "CASE_UPDATED", "CASE_COMPLETED"};
    private static final Map<String, String[]> ENTITY_LISTS = Map.of(
            "MPAM", new String[]{
                    "MPAM_ENQUIRY_SUBJECTS",
                    "MPAM_ENQUIRY_REASONS_ALL",
                    "MPAM_BUS_UNITS_ALL"},
            "MTS", new String[]{
                    "MPAM_ENQUIRY_SUBJECTS",
                    "MPAM_ENQUIRY_REASONS_ALL",
                    "MPAM_BUS_UNITS_ALL"
            });

    public CaseDataExportService(ObjectMapper objectMapper, AuditRepository auditRepository, InfoClient infoClient,
                                 CaseworkClient caseworkClient, HeaderConverter headerConverter,
                                 MalformedDateConverter malformedDateConverter) {
        super(objectMapper, auditRepository, infoClient, caseworkClient, headerConverter, malformedDateConverter);
    }

    @Override
    public ExportType getExportType() {
        return ExportType.CASE_DATA;
    }

    @Override
    protected String[] parseData(AuditEvent audit,
                                 ZonedDateTimeConverter zonedDateTimeConverter, ExportDataConverter exportDataConverter,
                                 String[] additionalHeaders)
            throws JsonProcessingException {
        ExtractsPayload.CaseData caseData = objectMapper.readValue(audit.getAuditPayload(), ExtractsPayload.CaseData.class);

        List<String> data = new ArrayList<>();

        data.add(zonedDateTimeConverter.convert(audit.getAuditTimestamp()));
        data.add(audit.getType());
        data.add(exportDataConverter.convertValue(audit.getUserID()));
        data.add(exportDataConverter.convertCaseUuid(audit.getCaseUUID()));
        data.add(caseData.getReference());
        data.add(caseData.getType());
        data.add(Objects.toString(caseData.getCaseDeadline(), ""));
        data.add(exportDataConverter.convertValue(Objects.toString(caseData.getPrimaryCorrespondent(), "")));
        data.add(exportDataConverter.convertValue(Objects.toString(caseData.getPrimaryTopic(), "")));

        if (caseData.getData() != null) {
            for (String field : additionalHeaders) {
                data.add(exportDataConverter.convertValue(caseData.getData().getOrDefault(field, "")));
            }
        }

        return data.toArray(new String[0]);
    }

    @Override
    @Transactional
    public void export(LocalDate from, LocalDate to, OutputStream outputStream, String caseType,
                       boolean convert, boolean convertHeader, ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        var caseTypeDto = getCaseTypeCode(caseType);

        var data = getData(from, to, caseTypeDto.getShortCode(), EVENTS);
        var dataConverter = getDataConverter(convert, caseTypeDto);

        printData(outputStream, zonedDateTimeConverter, dataConverter, convertHeader, caseTypeDto, data);
    }

    @Override
    protected String[] getHeaders() {
        return new String[] {
                "timestamp", "event", "userId", "caseUuid", "reference",
                "caseType", "deadline", "primaryCorrespondent", "primaryTopic"
        };
    }

    @Override
    protected String[] getAdditionalHeaders(CaseTypeDto caseType) {
        return infoClient.getCaseExportFields(caseType.getType()).toArray(String[]::new);
    }

    @Override
    protected ExportDataConverter getDataConverter(boolean convert, CaseTypeDto caseType) {
        if (!convert) {
            return new ExportDataConverter();
        }

        Map<String, String> uuidToName = new HashMap<>();
        Map<String, String> entityListItemToName = new HashMap<>();

        uuidToName.putAll(infoClient.getUsers().stream()
                .collect(Collectors.toMap(UserDto::getId, UserDto::getUsername)));
        uuidToName.putAll(infoClient.getAllTeams().stream()
                .collect(Collectors.toMap(team -> team.getUuid().toString(), TeamDto::getDisplayName)));
        uuidToName.putAll(infoClient.getUnits().stream()
                .collect(Collectors.toMap(UnitDto::getUuid, UnitDto::getDisplayName)));
        caseworkClient.getAllCaseTopics()
                .forEach(
                        topic -> uuidToName.putIfAbsent(topic.getTopicUUID().toString(), topic.getTopicText())
                );
        uuidToName.putAll(caseworkClient.getAllCorrespondents().stream()
                .collect(Collectors.toMap(corr -> corr.getUuid().toString(), GetCorrespondentOutlineResponse::getFullname)));
        uuidToName.putAll(infoClient.getCaseTypeActions().stream()
                .collect(Collectors.toMap(action -> action.getUuid().toString(), CaseTypeActionDto::getActionLabel)));

        for (String listName : ENTITY_LISTS.getOrDefault(caseType.getType(), new String[0])) {
            Set<EntityDto> entities = infoClient.getEntitiesForList(listName);
            entities.forEach(e -> entityListItemToName.put(e.getSimpleName(), e.getData().getTitle()));
        }

        return new ExportDataConverter(uuidToName, entityListItemToName, caseType.getShortCode(), auditRepository);
    }

    @Override
    protected Stream<AuditEvent> getData(LocalDate from, LocalDate to, String caseTypeCode, String[] events) {
        LocalDateTime peggedTo = to.isBefore(LocalDate.now()) ? LocalDateTime.of(to, LocalTime.MAX) : LocalDateTime.now();

        if (peggedTo.toLocalDate().equals(LocalDate.now())) {
            return auditRepository.findAuditEventLatestEventsAfterDate(LocalDateTime.of(
                    from, LocalTime.MIN), LocalDateTime.now(), events, caseTypeCode);
        }
        return auditRepository.findLastAuditDataByDateRangeAndEvents(LocalDateTime.of(
                from, LocalTime.MIN), peggedTo,
                events, caseTypeCode);
    }
}
