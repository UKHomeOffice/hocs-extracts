package uk.gov.digital.ho.hocs.extracts.entrypoint;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.extracts.client.info.dto.CaseTypeDto;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpMethod.GET;

public class StaticExportResourceTest extends BaseExportResourceTest {

    private HttpEntity<Object> authRequestEntity;

    @BeforeEach
    public void setup() {
        authRequestEntity =
                new HttpEntity<>(null, getAuthHeader("export_client", List.of("DCU_EXPORT_USER")));
    }
    @Test
    public void topicExportTest() throws IOException {
        given(infoClient.getTopics()).willReturn(Set.of());

        ResponseEntity<String> result = restTemplate.exchange(getExportUri("/export/topics"),
                GET, authRequestEntity, String.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(getFileName("topics"), result.getHeaders().getContentDisposition().getFilename());

        var rows = getCSVRows(result.getBody());
        Assertions.assertEquals(1, rows.size());
    }

    @Test
    public void teamExportTest() throws IOException {
        given(infoClient.getTeams()).willReturn(Set.of());

        ResponseEntity<String> result = restTemplate.exchange(getExportUri("/export/teams"),
                GET, authRequestEntity, String.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(getFileName("teams"), result.getHeaders().getContentDisposition().getFilename());

        var rows = getCSVRows(result.getBody());

        Assertions.assertEquals(1, rows.size());
    }

    @Test
    public void unitTeamExportTest() throws IOException {
        given(infoClient.getUnits()).willReturn(Set.of());

        ResponseEntity<String> result = restTemplate.exchange(getExportUri("/export/units/teams"),
                GET, authRequestEntity, String.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(getFileName("units_teams"), result.getHeaders().getContentDisposition().getFilename());

        var rows = getCSVRows(result.getBody());
        Assertions.assertEquals(1, rows.size());
    }

    @Test
    public void userExportTest() throws IOException {
        given(infoClient.getUsers()).willReturn(Set.of());

        ResponseEntity<String> result = restTemplate.exchange(getExportUri("/export/users"),
                GET, authRequestEntity, String.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(getFileName("users"), result.getHeaders().getContentDisposition().getFilename());

        var rows = getCSVRows(result.getBody());
        Assertions.assertEquals(1, rows.size());
    }

    @Test
    public void topicsWithTeamsExportTest() throws IOException {
        given(infoClient.getTopicsWithTeams("TEST")).willReturn(Set.of());

        ResponseEntity<String> result = restTemplate.exchange(getExportUri("/export/topics/TEST/teams"),
                GET, authRequestEntity, String.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(getFileName("test", "topics_teams"), result.getHeaders().getContentDisposition().getFilename());

        var rows = getCSVRows(result.getBody());
        Assertions.assertEquals(1, rows.size());
    }

}
