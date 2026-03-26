package in.cg.main.policy.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PolicyDTOTest {
    @Test
    void testGettersAndSetters() {
        PolicyDTO dto = new PolicyDTO();
        dto.setName("Term Life");
        dto.setCoverage(100000.0);
        dto.setPremium(500.0);
        dto.setMonths(12);

        assertEquals("Term Life", dto.getName());
        assertEquals(100000.0, dto.getCoverage());
        assertEquals(500.0, dto.getPremium());
        assertEquals(12, dto.getMonths());
    }
}
