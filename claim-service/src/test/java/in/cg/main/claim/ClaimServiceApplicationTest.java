package in.cg.main.claim;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClaimServiceApplicationTest {

    @Test
    void contextLoads() {
        // Verification that the Spring application context starts correctly
    }

    @Test
    void mainMethodTest() {
        assertDoesNotThrow(() -> ClaimServiceApplication.main(new String[]{"--server.port=0"}));
    }
}
