package uk.gov.digital.ho.hocs.audit.export;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.audit.application.LogEvent;
import uk.gov.digital.ho.hocs.audit.auditdetails.exception.AuditExportException;
import uk.gov.digital.ho.hocs.audit.auditdetails.model.AuditData;
import uk.gov.digital.ho.hocs.audit.auditdetails.repository.AuditRepository;
import uk.gov.digital.ho.hocs.audit.export.dto.AuditPayload;
import uk.gov.digital.ho.hocs.audit.export.infoclient.InfoClient;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.application.LogEvent.*;

@Slf4j
@Service
public class ExportService {

    private ObjectMapper mapper;
    private AuditRepository auditRepository;
    private InfoClient infoClient;
    public static final String[] CASE_DATA_EVENTS = {"CASE_CREATED", "CASE_UPDATED"};
    public static final String[] TOPIC_EVENTS = {"CASE_TOPIC_CREATED", "CASE_TOPIC_DELETED"};
    public static final String[] CORRESPONDENT_EVENTS = {"CORRESPONDENT_DELETED", "CORRESPONDENT_CREATED"};
    public static final String[] ALLOCATION_EVENTS = {"STAGE_ALLOCATED_TO_TEAM"};

    public ExportService(AuditRepository auditRepository, ObjectMapper mapper, InfoClient infoClient) {
        this.auditRepository = auditRepository;
        this.mapper = mapper;
        this.infoClient = infoClient;
    }

    @Transactional(readOnly = true)
    public void auditExport(LocalDate from, LocalDate to, OutputStream output, String caseType, ExportType exportType) throws IOException {
        OutputStream buffer = new BufferedOutputStream(output);
        OutputStreamWriter outputWriter = new OutputStreamWriter(buffer, "UTF-8");
        String caseTypeCode = infoClient.getCaseTypes().stream().filter(e -> e.getType().equals(caseType)).findFirst().get().getShortCode();
        switch(exportType) {
            case CASE_DATA:
                caseDataExport(from, to, outputWriter, caseTypeCode, caseType);
                break;
            case TOPICS:
                topicExport(from, to, outputWriter, caseTypeCode);
                break;
            case CORRESPONDENTS:
                correspondentExport(from, to, outputWriter, caseTypeCode);
                break;
            case ALLOCATIONS:
                allocationExport(from, to, outputWriter, caseTypeCode);
                break;
            default:
                throw new AuditExportException("Unknown export type requests");
        }
    }

    void caseDataExport(LocalDate from, LocalDate to, OutputStreamWriter outputWriter, String caseTypeCode, String caseType) throws IOException {
        log.info("Exporting CASE_DATA to CSV", value(EVENT, CSV_EXPORT_START));
        List<String> headers = Stream.of("timestamp", "event" ,"userId", "caseUuid", "reference","caseType", "deadline", "primaryCorrespondent", "primaryTopic").collect(Collectors.toList());
        LinkedHashSet<String> caseDataHeaders = infoClient.getCaseExportFields(caseType);
        headers.addAll(caseDataHeaders);



        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[headers.size()])))) {
            Stream<AuditData> data = auditRepository.findAuditDataByDateRangeAndEvents(LocalDateTime.of(
                    from, LocalTime.MIN), LocalDateTime.of(to, LocalTime.MAX),
                    CASE_DATA_EVENTS, caseTypeCode);
            data.forEach((audit) -> {
                try {
                    printer.printRecord(parseCaseDataAuditPayload(audit, caseDataHeaders));
                    outputWriter.flush();
                } catch (Exception e) {
                    log.error("Unable to parse record for audit {} for reason {}", audit.getUuid(), e.getMessage(), value(LogEvent.EVENT, CSV_EXPORT_FAILURE));
                }
            });
            log.info("Export CASE_DATA to CSV Complete", value(EVENT, CSV_EXPORT_COMPETE));
        }
    }

    private String[] parseCaseDataAuditPayload(AuditData audit, LinkedHashSet<String> caseDataHeaders) throws IOException {
        List<String> data = new ArrayList<>();
        AuditPayload.CaseData caseData = mapper.readValue(audit.getAuditPayload(), AuditPayload.CaseData.class);
        data.add(audit.getAuditTimestamp().toString());
        data.add(audit.getType());
        data.add(audit.getUserID());
        data.add(Objects.toString(audit.getCaseUUID()));
        data.add(caseData.getReference());
        data.add(caseData.getType());
        data.add(Objects.toString(caseData.getCaseDeadline(), ""));
        data.add(Objects.toString(caseData.getPrimaryCorrespondent(),""));
        data.add(Objects.toString(caseData.getPrimaryTopic(),""));

        if(caseData.getData() !=null) {
            for (String field : caseDataHeaders) {
                data.add(caseData.getData().getOrDefault(field, ""));
            }
        }
        return data.toArray(new String[data.size()]);
    }

    void topicExport(LocalDate from, LocalDate to, OutputStreamWriter outputWriter, String caseTypeCode) throws IOException {
        log.info("Exporting TOPIC to CSV", value(EVENT, CSV_EXPORT_START));
        List<String> headers = Stream.of("timestamp", "event" ,"userId", "caseUuid", "topicUuid", "topic").collect(Collectors.toList());
        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[headers.size()])))) {
            Stream<AuditData> data = auditRepository.findAuditDataByDateRangeAndEvents(LocalDateTime.of(
                    from, LocalTime.MIN), LocalDateTime.of(to, LocalTime.MAX),
                    TOPIC_EVENTS, caseTypeCode);
            data.forEach((audit) -> {
                try {
                    printer.printRecord(parseTopicAuditPayload(audit));
                    outputWriter.flush();
                } catch (IOException e) {
                    log.error("Unable to parse record for audit {} for reason {}", audit.getUuid(), e.getMessage(), value(LogEvent.EVENT, CSV_EXPORT_FAILURE));
                }
            });
            log.info("Export TOPIC to CSV Complete", value(EVENT, CSV_EXPORT_COMPETE));
        }
    }

    private List<String> parseTopicAuditPayload(AuditData audit) throws IOException {
        List<String> data = new ArrayList<>();
        AuditPayload.Topic topicData = mapper.readValue(audit.getAuditPayload(), AuditPayload.Topic.class);
        data.add(audit.getAuditTimestamp().toString());
        data.add(audit.getType());
        data.add(audit.getUserID());
        data.add(Objects.toString(audit.getCaseUUID(), ""));
        data.add(topicData.getTopicUuid().toString());
        data.add(topicData.getTopicName());
        return data;
    }

    void correspondentExport(LocalDate from, LocalDate to, OutputStreamWriter outputWriter, String caseTypeCode) throws IOException {
        log.info("Exporting CORRESPONDENT to CSV", value(EVENT, CSV_EXPORT_START));
        List<String> headers = Stream.of("timestamp", "event" ,"userId","caseUuid",
                "correspondentUuid", "fullname", "address1", "address2",
                "address3", "country", "postcode", "telephone", "email",
                "reference").collect(Collectors.toList());

        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[headers.size()])))) {

            Stream<AuditData> data = auditRepository.findAuditDataByDateRangeAndEvents(LocalDateTime.of(
                    from, LocalTime.MIN), LocalDateTime.of(to, LocalTime.MAX),
                    CORRESPONDENT_EVENTS, caseTypeCode);

            data.forEach((audit) -> {
                try {
                    printer.printRecord(parseCorrespondentAuditPayload(audit));
                    outputWriter.flush();
                } catch (IOException e) {
                    log.error("Unable to parse record for audit {} for reason {}", audit.getUuid(), e.getMessage(), value(LogEvent.EVENT, CSV_EXPORT_FAILURE));
                }
            });
            log.info("Export CORRESPONDENT to CSV Complete", value(EVENT, CSV_EXPORT_COMPETE));
        }
    }

    private List<String> parseCorrespondentAuditPayload(AuditData audit) throws IOException {
        List<String> data = new ArrayList<>();
        AuditPayload.Correspondent correspondentData = mapper.readValue(audit.getAuditPayload(), AuditPayload.Correspondent.class);
        data.add(audit.getAuditTimestamp().toString());
        data.add(audit.getType());
        data.add(audit.getUserID());
        data.add(Objects.toString(audit.getCaseUUID(), ""));
        data.add(correspondentData.getUuid().toString());
        data.add(correspondentData.getFullname());

        if(correspondentData.getAddress() != null) {
            data.add(correspondentData.getAddress().getAddress1());
            data.add(correspondentData.getAddress().getAddress2());
            data.add(correspondentData.getAddress().getAddress3());
            data.add(correspondentData.getAddress().getCountry());
            data.add(correspondentData.getAddress().getPostcode());
        }

        data.add(correspondentData.getTelephone());
        data.add(correspondentData.getEmail());
        data.add(correspondentData.getReference());

        return data;
    }

    void allocationExport(LocalDate from, LocalDate to, OutputStreamWriter outputWriter, String caseTypeCode) throws IOException {
        log.info("Exporting ALLOCATION to CSV", value(EVENT, CSV_EXPORT_START));
        List<String> headers = Stream.of("timestamp", "event" ,"userId","caseUuid","stage", "teamUuid").collect(Collectors.toList());
        try (CSVPrinter printer = new CSVPrinter(outputWriter, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[headers.size()])))) {
            Stream<AuditData> data = auditRepository.findAuditDataByDateRangeAndEvents(LocalDateTime.of(
                    from, LocalTime.MIN), LocalDateTime.of(to, LocalTime.MAX),
                    ALLOCATION_EVENTS, caseTypeCode);
            data.forEach((audit) -> {
                try {
                    printer.printRecord(parseAllocationAuditPayload(audit));
                    outputWriter.flush();
                } catch (IOException e) {
                    log.error("Unable to parse record for audit {} for reason {}", audit.getUuid(), e.getMessage(), value(LogEvent.EVENT, CSV_EXPORT_FAILURE));
                }
            });
            log.info("Export ALLOCATION to CSV Complete", value(EVENT, CSV_EXPORT_COMPETE));
        }
    }

    private List<String> parseAllocationAuditPayload(AuditData audit) throws IOException {
        List<String> data = new ArrayList<>();
        AuditPayload.StageTeamAllocation allocationData = mapper.readValue(audit.getAuditPayload(), AuditPayload.StageTeamAllocation.class);
        data.add(audit.getAuditTimestamp().toString());
        data.add(audit.getType());
        data.add(audit.getUserID());
        data.add(Objects.toString(audit.getCaseUUID(), ""));
        data.add(allocationData.getStage());
        data.add(Objects.toString(allocationData.getTeamUUID(), ""));
        return data;
    }
}

