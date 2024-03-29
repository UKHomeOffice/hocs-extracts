package uk.gov.digital.ho.hocs.extracts.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.digital.ho.hocs.extracts.core.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.extracts.core.LogEvent.EXCEPTION;
import static uk.gov.digital.ho.hocs.extracts.core.LogEvent.REST_CLIENT_EXCEPTION;

@Slf4j
@Component
public class RestHelper {

    private final String basicAuth;

    private final RestTemplate restTemplate;

    private final RequestData requestData;

    @Autowired
    public RestHelper(RestTemplate restTemplate, @Value("${hocs.basicauth}") String basicAuth, RequestData requestData) {
        this.restTemplate = restTemplate;
        this.basicAuth = basicAuth;
        this.requestData = requestData;
    }

    private static String getBasicAuth(String basicAuth) {
        return String.format("Basic %s", Base64.getEncoder().encodeToString(basicAuth.getBytes(StandardCharsets.UTF_8)));
    }

    public <R> R get(String rootUri, String endpoint, Class<R> type) {
        try {
            log.debug("Making GET request {}{}", rootUri, endpoint);
            return restTemplate
                    .exchange(rootUri.concat(endpoint), HttpMethod.GET, getAuthenticatedEntity(), type)
                    .getBody();
        } catch (Exception e) {
            log.error("Error in GET request {}{}", rootUri, endpoint, value(EVENT, REST_CLIENT_EXCEPTION), value(EXCEPTION, e));
            throw e;
        }
    }

    public <R> R get(String rootUri, String endpoint, ParameterizedTypeReference<R> type) {
        try {
            log.debug("Making GET request {}{}", rootUri, endpoint);
            return restTemplate
                    .exchange(rootUri.concat(endpoint), HttpMethod.GET, getAuthenticatedEntity(), type)
                    .getBody();
        } catch (Exception e) {
            log.error("Error in GET request {}{}", rootUri, endpoint, value(EVENT, REST_CLIENT_EXCEPTION), value(EXCEPTION, e));
            throw e;
        }
    }

    private HttpEntity<?> getAuthenticatedEntity() {
        return new HttpEntity<>(null, createAuthHeaders());
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(AUTHORIZATION, getBasicAuth(basicAuth));
        headers.add(RequestData.USER_ID_HEADER, requestData.getUserId());
        headers.add(RequestData.CORRELATION_ID_HEADER, requestData.getCorrelationId());
        return headers;
    }

}
