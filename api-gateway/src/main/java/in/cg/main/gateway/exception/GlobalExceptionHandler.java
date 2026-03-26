package in.cg.main.gateway.exception;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

// This class catches any unhandled errors in the gateway and returns a clean message
// Without this, users would see a raw Spring error page
@Component
@Order(-1) // This makes it run before Spring's default error handler
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        // Step 1: Decide what status code and message to show
        HttpStatus status;
        String message;

        String errorMsg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";

        if (errorMsg.contains("missing") || errorMsg.contains("unauthorized")
                || errorMsg.contains("expired") || errorMsg.contains("jwt")
                || errorMsg.contains("log in")) {
            // Token is missing or invalid → 401
            status = HttpStatus.UNAUTHORIZED;
            message = ex.getMessage();

        } else if (errorMsg.contains("access denied") || errorMsg.contains("forbidden")
                || errorMsg.contains("reserved") || errorMsg.contains("available for")) {
            // Role is not allowed → 403
            status = HttpStatus.FORBIDDEN;
            message = ex.getMessage();

        } else {
            // Any other unexpected error
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "Something went wrong. Please try again later.";
        }

        // Step 2: Write the message as plain text in the response
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);

        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
