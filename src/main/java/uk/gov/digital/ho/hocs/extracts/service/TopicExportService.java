package uk.gov.digital.ho.hocs.extracts.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.extracts.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.extracts.client.info.InfoClient;
import uk.gov.digital.ho.hocs.extracts.client.info.dto.CaseTypeDto;
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
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TopicExportService extends DynamicExportService {

    private static final String[] EVENTS = { "CASE_TOPIC_CREATED", "CASE_TOPIC_DELETED" };

    public TopicExportService(ObjectMapper objectMapper, AuditRepository auditRepository, InfoClient infoClient, CaseworkClient caseworkClient, HeaderConverter headerConverter, MalformedDateConverter malformedDateConverter) {
        super(objectMapper, auditRepository, infoClient, caseworkClient, headerConverter, malformedDateConverter);
    }

    @Override
    public ExportType getExportType() {
        return ExportType.TOPICS;
    }

    @Override
    protected String[] parseData(AuditEvent audit, ZonedDateTimeConverter zonedDateTimeConverter, ExportDataConverter exportDataConverter)
            throws JsonProcessingException {
        ExtractsPayload.Topic topicData = objectMapper.readValue(audit.getAuditPayload(), ExtractsPayload.Topic.class);

        return new String[]{
                zonedDateTimeConverter.convert(audit.getAuditTimestamp()),
                audit.getType(),
                exportDataConverter.convertValue(audit.getUserID()),
                exportDataConverter.convertCaseUuid(audit.getCaseUUID()),
                exportDataConverter.convertValue(Objects.toString(topicData.getTopicUuid(), "")),
                topicData.getTopicName()
        };
    }

    @Override
    protected Stream<AuditEvent> getData(LocalDate from, LocalDate to, String caseTypeCode, String[] events) {
        LocalDate peggedTo = to.isAfter(LocalDate.now()) ? LocalDate.now() : to;

        return auditRepository.findAuditDataByDateRangeAndEvents(LocalDateTime.of(
                        from, LocalTime.MIN), LocalDateTime.of(peggedTo, LocalTime.MAX),
                events, caseTypeCode);
    }

    @Override
    public void export(LocalDate from, LocalDate to, OutputStream outputStream, String caseType, boolean convert, boolean convertHeader, ZonedDateTimeConverter zonedDateTimeConverter) throws IOException {
        var caseTypeCode = getCaseTypeCode(caseType);

        var dataConverter = getDataConverter(convert, caseTypeCode);
        var data = getData(from, to, caseTypeCode.getShortCode(), EVENTS);

        printData(outputStream, zonedDateTimeConverter, dataConverter, convertHeader, data);
    }

    @Override
    protected String[] getHeaders() {
        return new String[] {"timestamp", "event", "userId", "caseUuid", "topicUuid", "topic"};
    }

    @Override
    protected ExportDataConverter getDataConverter(boolean convert, CaseTypeDto caseType) {
        if (!convert) {
            return new ExportDataConverter();
        }

        Map<String, String> uuidToName = infoClient.getUsers().stream()
                .collect(Collectors.toMap(UserDto::getId, UserDto::getUsername));

        caseworkClient.getAllCaseTopics()
                .forEach(
                        topic -> uuidToName.putIfAbsent(topic.getTopicUUID().toString(), topic.getTopicText())
                );

        return new ExportDataConverter(uuidToName, Collections.emptyMap(), caseType.getShortCode(), auditRepository);
    }
}
