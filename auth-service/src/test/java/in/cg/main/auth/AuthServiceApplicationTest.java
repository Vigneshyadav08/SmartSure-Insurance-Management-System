package in.cg.main.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import in.cg.main.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
class AuthServiceApplicationTest {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
        // Basic check to ensure the context starts up
    }
}
