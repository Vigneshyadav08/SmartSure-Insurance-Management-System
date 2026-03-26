package in.cg.main.gateway.filter;

import in.cg.main.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired private JwtUtil jwtUtil;

    public AuthenticationFilter() { super(Config.class); }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            String method = exchange.getRequest().getMethod().name();

            if (isPublic(path, method)) return chain.filter(exchange);

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) 
                return error(exchange, HttpStatus.UNAUTHORIZED, "Please log in.");

            String token = authHeader.substring(7);
            try {
                jwtUtil.validateToken(token);
                String role = normalizeRole(jwtUtil.extractRole(token));
                if (!hasAccess(method, path, role)) 
                    return error(exchange, HttpStatus.FORBIDDEN, "Access denied.");
            } catch (Exception e) {
                return error(exchange, HttpStatus.UNAUTHORIZED, "Invalid session.");
            }

            return chain.filter(exchange);
        };
    }

    private boolean isPublic(String path, String method) {
        if ("GET".equals(method) && "/policies".equals(path)) return true;
        return path.contains("/auth/login") || path.contains("/auth/register") || 
               path.contains("/swagger-ui") || path.contains("/v3/api-docs");
    }

    private boolean hasAccess(String method, String path, String role) {
        if ("ADMIN".equals(role)) return true;
        if (path.startsWith("/admin/")) return false;
        
        if ("CUSTOMER".equals(role)) {
            if (method.equals("POST")) {
                return path.contains("/purchase") || path.equals("/claims/submit");
            }
            if (method.equals("GET")) {
                return path.startsWith("/policies") || path.startsWith("/claims");
            }
            return false;
        }
        return false;
    }

    private String normalizeRole(String role) {
        if (role == null) return "";
        return role.startsWith("ROLE_") ? role.substring(5) : role;
    }

    private Mono<Void> error(ServerWebExchange exchange, HttpStatus status, String msg) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(msg.getBytes())));
    }

    public static class Config {}
}
