package in.cg.main.auth.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String secret = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        String token = jwtUtil.generateToken("testuser", "ROLE_USER");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        String token = jwtUtil.generateToken("testuser", "ROLE_USER");
        assertEquals("testuser", jwtUtil.extractUsername(token));
    }

    @Test
    void extractRole_shouldReturnCorrectRole() {
        String token = jwtUtil.generateToken("testuser", "ROLE_USER");
        assertEquals("ROLE_USER", jwtUtil.extractRole(token));
    }

    @Test
    void validateToken_shouldNotThrowForValidToken() {
        String token = jwtUtil.generateToken("testuser", "ROLE_USER");
        assertDoesNotThrow(() -> jwtUtil.validateToken(token));
    }
}
