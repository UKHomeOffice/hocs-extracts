package uk.gov.digital.ho.hocs.extracts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

import javax.annotation.PreDestroy;

@Slf4j
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class HocsExtractsApplication {

	public static void main(String[] args) {
		SpringApplication.run(HocsExtractsApplication.class, args);
	}

	@PreDestroy
	public void stop() {
		log.info("Stopping gracefully");
	}

}
