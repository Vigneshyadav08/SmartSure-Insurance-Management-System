package in.cg.main.policy.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PolicyTest {
    @Test
    void testGettersAndSetters() {
        Policy policy = new Policy();
        policy.setId(1L);
        policy.setName("Term Life");
        policy.setPremium(500.0);
        policy.setCoverage(100000.0);
        policy.setMonths(12);

        assertEquals(1L, policy.getId());
        assertEquals("Term Life", policy.getName());
        assertEquals(500.0, policy.getPremium());
        assertEquals(100000.0, policy.getCoverage());
        assertEquals(12, policy.getMonths());
    }
}
