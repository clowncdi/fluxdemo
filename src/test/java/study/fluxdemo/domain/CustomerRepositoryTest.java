package study.fluxdemo.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;
import study.fluxdemo.config.DBinit;

@DataR2dbcTest
@Import(DBinit.class)
public class CustomerRepositoryTest {

	@Autowired
	private CustomerRepository customerRepository;

	@Test
	void 한건찾기_테스트() {
		StepVerifier
				.create(customerRepository.findById(1L))
				.expectNextMatches(c -> c.getFirstName().equals("Jack"))
				.expectComplete()
				.verify();
	}
}
