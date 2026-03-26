package in.cg.main.claim.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class ClaimResponseDTOTest {
    @Test
    void testGettersAndSetters() {
        ClaimResponseDTO dto = new ClaimResponseDTO();
        LocalDate now = LocalDate.now();
        
        dto.setId(1L);
        dto.setPolicyId(10L);
        dto.setCustomerUsername("user");
        dto.setDescription("desc");
        dto.setStatus("PENDING");
        dto.setDocumentPath("/path");
        dto.setClaimAmount(100.0);
        dto.setIncidentDate(now);
        dto.setClaimDate(now);
        
        assertEquals(1L, dto.getId());
        assertEquals(10L, dto.getPolicyId());
        assertEquals("user", dto.getCustomerUsername());
        assertEquals("desc", dto.getDescription());
        assertEquals("PENDING", dto.getStatus());
        assertEquals("/path", dto.getDocumentPath());
        assertEquals(100.0, dto.getClaimAmount());
        assertEquals(now, dto.getIncidentDate());
        assertEquals(now, dto.getClaimDate());
    }
}
