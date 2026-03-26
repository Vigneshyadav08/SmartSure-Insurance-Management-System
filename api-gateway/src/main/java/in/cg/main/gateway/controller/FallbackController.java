package in.cg.main.gateway.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
@Tag(name = "API Gateway")
public class FallbackController {

    @RequestMapping("/auth")
    public Mono<String> authServiceFallback() {
        return Mono.just("Authentication Service is busy or down. Please try again after some time.");
    }

    @RequestMapping("/policy")
    public Mono<String> policyServiceFallback() {
        return Mono.just("Policy Service is taking too long to respond. Please try again later.");
    }

    @RequestMapping("/claim")
    public Mono<String> claimServiceFallback() {
        return Mono.just("Claim Service is currently unavailable. Please try again later.");
    }

    @RequestMapping("/admin")
    public Mono<String> adminServiceFallback() {
        return Mono.just("Admin Service is facing issues. Administrative actions are pending.");
    }
}
