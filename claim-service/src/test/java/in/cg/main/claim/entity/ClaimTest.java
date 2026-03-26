package in.cg.main.claim.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class ClaimTest {
    @Test
    void testGettersAndSetters() {
        Claim claim = new Claim();
        LocalDate now = LocalDate.now();
        
        claim.setId(1L);
        claim.setPolicyId(10L);
        claim.setCustomerUsername("user");
        claim.setDescription("desc");
        claim.setStatus("PENDING");
        claim.setDocumentPath("/path");
        claim.setClaimAmount(100.0);
        claim.setIncidentDate(now);
        claim.setClaimDate(now);
        
        assertEquals(1L, claim.getId());
        assertEquals(10L, claim.getPolicyId());
        assertEquals("user", claim.getCustomerUsername());
        assertEquals("desc", claim.getDescription());
        assertEquals("PENDING", claim.getStatus());
        assertEquals("/path", claim.getDocumentPath());
        assertEquals(100.0, claim.getClaimAmount());
        assertEquals(now, claim.getIncidentDate());
        assertEquals(now, claim.getClaimDate());
    }
}
