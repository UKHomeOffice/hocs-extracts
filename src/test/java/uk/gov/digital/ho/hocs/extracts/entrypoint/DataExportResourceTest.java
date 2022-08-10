package uk.gov.digital.ho.hocs.extracts.entrypoint;

import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.extracts.client.info.dto.CaseTypeDto;
import uk.gov.digital.ho.hocs.extracts.client.info.dto.SomuTypeDto;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpMethod.GET;

public class DataExportResourceTest extends BaseExportResourceTest {

    private HttpEntity<Object> authRequestEntity;

    @BeforeEach
    public void setup() {
        given(infoClient.getCaseTypes())
                .willReturn(Set.of(new CaseTypeDto("Test", "a1", "FOI")));
        authRequestEntity =
                new HttpEntity<>(null, getAuthHeader("export_client", List.of("FOI_EXPORT_USER")));
    }

    @Test
    public void exportTypeExport() throws IOException {
        given(infoClient.getCaseExportFields("FOI")).willReturn(new LinkedHashSet<>());

        ResponseEntity<String> result = restTemplate.exchange(
                getExportUri("/export/FOI?fromDate=2020-01-01&toDate=2022-01-01&exportType=CASE_DATA"),
                GET, authRequestEntity, String.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(getFileName("foi", "case_data"), result.getHeaders().getContentDisposition().getFilename());

        var rows = getCSVRows(result.getBody())
                .stream()
                .map(CSVRecord::toList)
                .collect(Collectors.toList());
        Assertions.assertEquals(1, rows.size());
    }

    @Test
    public void exportTypeReportFailsIfFromDateNotSpecified() {
        ResponseEntity<String> result = restTemplate.exchange(
                getExportUri("/export/FOI?toDate=2022-01-01&exportType=CASE_DATA"),
                GET, authRequestEntity, String.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void exportTypeReportFailsIfExportTypeNotSpecified() {
        ResponseEntity<String> result = restTemplate.exchange(
                getExportUri("/export/FOI?fromDate=2020-01-01&toDate=2022-01-01"),
                GET, authRequestEntity, String.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void exportTypeReportFailsIfExportTypeIsInvalid() {
        ResponseEntity<String> result = restTemplate.exchange(
                getExportUri("/export/FOI?fromDate=2020-01-01&toDate=2022-01-01&exportType=TEST"),
                GET,authRequestEntity, String.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void somuTypeExport() throws IOException {
        given(infoClient.getSomuType("FOI", "SOMU"))
                .willReturn(new SomuTypeDto(UUID.randomUUID(), "FOI", "SOMU", "{}", true));

        ResponseEntity<String> result = restTemplate.exchange(
                getExportUri("/export/somu/FOI?fromDate=2020-01-01&toDate=2022-01-01&somuType=SOMU"),
                GET, authRequestEntity, String.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(getFileName("foi", "SOMU"), result.getHeaders().getContentDisposition().getFilename());

        var rows = getCSVRows(result.getBody())
                .stream()
                .map(CSVRecord::toList)
                .collect(Collectors.toList());
        Assertions.assertEquals(1, rows.size());
    }

    @Test
    public void somuTypeReportFailsIfFromDateNotSpecified() {
        ResponseEntity<String> result = restTemplate.exchange(
                getExportUri("/export/somu/FOI?toDate=2022-01-01&somuType=SOMU"),
                GET, authRequestEntity, String.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void somuTypeReportFailsIfSomuTypeNotSpecified() {
        ResponseEntity<String> result = restTemplate.exchange(
                getExportUri("/export/somu/FOI?fromDate=2020-01-01&toDate=2022-01-01"),
                GET, authRequestEntity, String.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

}
