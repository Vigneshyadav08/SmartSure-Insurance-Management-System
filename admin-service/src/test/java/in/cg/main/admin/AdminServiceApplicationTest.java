package in.cg.main.admin;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class AdminServiceApplicationTest {

    @Test
    void contextLoads() {
        // Verification that the Spring application context starts correctly
    }

    @Test
    void mainMethodTest() {
        assertDoesNotThrow(() -> AdminServiceApplication.main(new String[]{}));
    }
}
