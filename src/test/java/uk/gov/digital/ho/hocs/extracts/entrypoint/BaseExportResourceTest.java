package uk.gov.digital.ho.hocs.extracts.entrypoint;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.digital.ho.hocs.extracts.client.info.InfoClient;
import uk.gov.digital.ho.hocs.extracts.core.utils.JwtAuthHelper;


import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"local", "test"})
public abstract class BaseExportResourceTest {

    @Autowired
    protected TestRestTemplate restTemplate;


    @Autowired
    protected JwtAuthHelper jwtHelper;

    @LocalServerPort
    private int port;

    @MockBean
    protected InfoClient infoClient;

    protected String getExportUri(String uri, Object... options) {
        var formatted = String.format(uri, options);

        return "http://localhost:" + port + formatted;
    }

    protected String getFileName(String... options) {
        var optionsList = new ArrayList<>(Arrays.asList(options));
        optionsList.add(LocalDate.now().toString());

        var joinedName = String.join("-", optionsList);
        return joinedName + ".csv";
    }

    protected List<CSVRecord> getCSVRows(String csvBody) throws IOException {
        StringReader reader = new StringReader(csvBody);
        CSVParser csvParser = new CSVParser(reader,
                CSVFormat.EXCEL.builder().setSkipHeaderRecord(true).setTrim(true).build());
        return csvParser.getRecords();
    }

    protected HttpHeaders getAuthHeader(String subject, List<String> roles) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(HttpHeaders.AUTHORIZATION,  String.format("%s %s", "Bearer", jwtHelper.createJwt(subject, roles)));
        return new HttpHeaders(headers);
    }

}
