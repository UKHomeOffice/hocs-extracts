package uk.gov.digital.ho.hocs.extracts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PreDestroy;

@Slf4j
@SpringBootApplication
public class HocsExtractsApplication {

	public static void main(String[] args) {
		SpringApplication.run(HocsExtractsApplication.class, args);
	}

	@PreDestroy
	public void stop() {
		log.info("Stopping gracefully");
	}

}
