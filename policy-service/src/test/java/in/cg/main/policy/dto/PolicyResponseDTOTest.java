package in.cg.main.policy.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PolicyResponseDTOTest {
    @Test
    void testGettersAndSetters() {
        PolicyResponseDTO dto = new PolicyResponseDTO();
        dto.setId(1L);
        dto.setName("Term Life");
        dto.setPremium(500.0);
        dto.setCoverage(100000.0);
        dto.setMonths(12);

        assertEquals(1L, dto.getId());
        assertEquals("Term Life", dto.getName());
        assertEquals(500.0, dto.getPremium());
        assertEquals(100000.0, dto.getCoverage());
        assertEquals(12, dto.getMonths());
    }
}
