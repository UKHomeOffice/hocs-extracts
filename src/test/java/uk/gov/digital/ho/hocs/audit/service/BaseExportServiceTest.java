package uk.gov.digital.ho.hocs.audit.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.digital.ho.hocs.audit.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.audit.client.info.InfoClient;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@SpringBootTest
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:export/setup.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:export/cleandown.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public abstract class BaseExportServiceTest {

    @MockBean
    protected InfoClient infoClient;

    @MockBean
    protected CaseworkClient caseworkClient;

    protected List<CSVRecord> getCsvDataRows(String csvBody) throws IOException {
        StringReader reader = new StringReader(csvBody);
        CSVParser csvParser = new CSVParser(reader,
                CSVFormat.EXCEL.builder().setTrim(true).build());
        return csvParser.getRecords();
    }

    protected Map<String, Integer> getCsvHeaderRow(String csvBody) throws IOException {
        StringReader reader = new StringReader(csvBody);
        CSVParser csvParser = new CSVParser(reader,
                CSVFormat.EXCEL.builder().setSkipHeaderRecord(true).setTrim(true).build().withHeader());
        return csvParser.getHeaderMap();
    }

}
