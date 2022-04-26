package uk.gov.digital.ho.hocs.audit.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.audit.client.info.InfoClient;
import uk.gov.digital.ho.hocs.audit.client.info.dto.TeamDto;
import uk.gov.digital.ho.hocs.audit.client.info.dto.UnitDto;
import uk.gov.digital.ho.hocs.audit.core.exception.AuditExportException;
import uk.gov.digital.ho.hocs.audit.service.domain.converter.HeaderConverter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_EXPORT_FAILURE;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.CSV_RECORD_EXPORT_FAILURE;
import static uk.gov.digital.ho.hocs.audit.core.LogEvent.EVENT;

@Slf4j
@Service
public class StaticUnitAndTeamService {

    private final HeaderConverter headerConverter;
    private final InfoClient infoClient;

    public StaticUnitAndTeamService(HeaderConverter headerConverter, InfoClient infoClient) {
        this.headerConverter = headerConverter;
        this.infoClient = infoClient;
    }

    public void export(PrintWriter writer, boolean convertHeader) throws IOException {
        var headers = getHeaders(convertHeader);

        try (var printer =
                     new CSVPrinter(writer, CSVFormat.Builder.create()
                             .setHeader(headers)
                             .setAutoFlush(true)
                             .build())) {
            Set<UnitDto> units = infoClient.getUnits();

            units.forEach(unit -> {
                Set<TeamDto> teams = infoClient.getTeamsForUnit(unit.getUuid());

                if (teams.isEmpty()) {
                    try {
                        printer.printRecord(unit.getUuid(), unit.getDisplayName(), null, null);
                    } catch (IOException e) {
                        throw new AuditExportException("Unable to parse record for reason {}", CSV_RECORD_EXPORT_FAILURE, e.getMessage());
                    }
                } else {
                    teams.forEach(team -> {
                        try {
                            printer.printRecord(unit.getUuid(), unit.getDisplayName(), team.getUuid(), team.getDisplayName());
                        } catch (IOException e) {
                            throw new AuditExportException("Unable to parse record for reason {}", CSV_RECORD_EXPORT_FAILURE, e.getMessage());
                        }
                    });
                }
            });
        } catch (IOException e) {
            log.error("Unable to export record for reason {}", e.getMessage(), value(EVENT, CSV_EXPORT_FAILURE));
        }
    }

    private String[] getHeaders(boolean convertHeaders) {
        String[] headers = new String[] {
                "unitUUID", "unitName", "teamUUID", "teamName"
        };

        if (convertHeaders) {
            return headerConverter.substitute(headers);
        }

        return headers;
    }




}
