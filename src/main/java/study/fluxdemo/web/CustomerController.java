package study.fluxdemo.web;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import study.fluxdemo.domain.Customer;
import study.fluxdemo.domain.CustomerRepository;

import java.time.Duration;

@RestController
public class CustomerController {
	private final CustomerRepository customerRepository;
	private final Sinks.Many<Customer> sink;

	// A 요청 -> Flux -> Stream
	// B 요청 -> Flux -> Stream
	// -> Flux.merge -> sink

	public CustomerController(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
		this.sink = Sinks.many().multicast().onBackpressureBuffer(); // 새로 들어온 데이터만 받음
	}

	@GetMapping("/customers")
	public Flux<Customer> findAll() {
		return customerRepository.findAll().log();
	}

	@GetMapping(value = "/customers-stream", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
	public Flux<Customer> findAllStream() {
		return customerRepository.findAll()
				.delayElements(Duration.ofSeconds(1)).log();
	}

	@GetMapping("/flux")
	public Flux<Integer> flux() {
		return Flux.just(1, 2, 3, 4, 5).delayElements(Duration.ofSeconds(1)).log();
	}

	@GetMapping(value = "/fluxstream", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
	public Flux<Integer> fluxstream() {
		return Flux.just(1, 2, 3, 4, 5).delayElements(Duration.ofSeconds(1)).log();
	}

	@GetMapping("/customer/{id}")
	public Mono<Customer> findById(@PathVariable Long id) {
		return customerRepository.findById(id).log();
	}

	// 1. Flux를 리턴하는 경우
	@GetMapping(value = "/customers/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<Customer> findAllSSE() {
		return customerRepository.findAll()
				.delayElements(Duration.ofSeconds(1)).log();
	}

	// 2. ServerSentEvent를 리턴하는 경우 (실시간 응답상태 유지)
	@GetMapping("/customers/sse2") // produces = MediaType.TEXT_EVENT_STREAM_VALUE 생략 가능
	public Flux<ServerSentEvent<Customer>> findAllSSE2() {
		return sink.asFlux().map(c -> ServerSentEvent.builder(c).build())
				.doOnCancel(() -> {
					sink.asFlux().blockLast(); // 호출을 cancel 할 경우 마지막 데이터임을 알려줌. 클아이언트가 request 재요청 가능함
				});
	}

	@PostMapping("/customer")
	public Mono<Customer> save() {
		return customerRepository.save(new Customer("aaa", "BBB"))
				.doOnNext(c -> {
					sink.tryEmitNext(c);
				}).log();
	}
}
