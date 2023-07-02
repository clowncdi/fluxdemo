package study.fluxdemo.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import study.fluxdemo.domain.Customer;
import study.fluxdemo.domain.CustomerRepository;

import static org.mockito.Mockito.when;

//통합테스트용
//@SpringBootTest
//@AutoConfigureWebTestClient

@WebFluxTest
public class CustomerControllerTest {

	@MockBean
	CustomerRepository customerRepository;

	@Autowired
	private WebTestClient webClient; // 비동기로 http 요청

	@Test
	void 한건찾기_테스트() {
		// given
		Customer customer = new Customer("Jack", "Bauer");

		// stub -> 행동 지시
		when(customerRepository.findById(1L))
				.thenReturn(Mono.just(customer));

		webClient.get().uri("/customer/{id}", 1L)
				.exchange()
				.expectBody()
				.jsonPath("$.firstName").isEqualTo("Jack")
				.jsonPath("$.lastName").isEqualTo("Bauer");

	}
}
