package in.cg.main.policy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class PolicyServiceApplicationTest {

    @Test
    void contextLoads() {
    }

    @Test
    void mainMethodTest() {
        assertDoesNotThrow(() -> PolicyServiceApplication.main(new String[]{}));
    }
}
