package in.cg.main.admin.service;

import in.cg.main.admin.feign.ClaimFeignClient;
import in.cg.main.admin.feign.PolicyFeignClient;
import in.cg.main.admin.repository.ReportRepository;
import in.cg.main.admin.entity.Report;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * WHY: This test suite ensures that the AdminService correctly orchestrates
 * cross-service actions and handles dependencies properly.
 * 
 * WHAT: It verifies mocks for ClaimFeignClient, PolicyFeignClient, and ReportRepository.
 * 
 * HOW: Uses Mockito to mock interface behavior and verify that the 
 * service methods call the correct underlying feign clients or repositories.
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private ClaimFeignClient claimFeignClient;
    @Mock private PolicyFeignClient policyFeignClient;
    @Mock private ReportRepository reportRepository;
    private AdminService service;

    @BeforeEach
    void setUp() {
        // WHY: Manual instantiation ensures that we control the injection process
        // and can easily add new horizontal dependencies as the service grows.
        service = new AdminService(claimFeignClient, policyFeignClient, reportRepository);
    }

    @Test
    void getPurchasedPolicies_shouldCallPolicyFeign() {
        // WHY: Verification that admins can audit user policies through the
        // inter-service feign client.
        service.getPurchasedPolicies("alice");
        verify(policyFeignClient).getPurchasedPolicies("alice");
    }

    @Test
    void expirePolicy_shouldCallPolicyFeign() {
        // WHY: Verification that admins can manually trigger the terminal 
        // lifecycle stage of a policy for administrative reasons.
        service.expirePolicy(1L);
        verify(policyFeignClient).expirePolicy(1L);
    }

    @Test
    void approveClaim_shouldCallFeignWithApprovedStatus() {
        // WHY: Verification that the admin service correctly delegates the approval
        // action to the claim service via inter-service communication.
        service.approveClaim(1L);
        verify(claimFeignClient).updateClaimStatus(1L, "APPROVED");
    }

    @Test
    void rejectClaim_shouldCallFeignWithRejectedStatus() {
        // WHY: Verification that the rejection workflow is properly integrated
        // and communicates the correct terminal status to the claim service.
        service.rejectClaim(1L);
        verify(claimFeignClient).updateClaimStatus(1L, "REJECTED");
    }

    @Test
    void downloadClaimDocument_shouldCallFeignAndReturnBytes() {
        // WHY: Verification that admins can retrieve documents for manual review
        // through the cross-service feign client proxy.
        byte[] mockData = "PDF Content".getBytes();
        when(claimFeignClient.downloadDocument(1L)).thenReturn(mockData);
        assertArrayEquals(mockData, service.downloadClaimDocument(1L));
    }

    @Test
    void claimServiceFallback_shouldReturnErrorMessage() {
        // WHY: Resilience testing to ensure the system provides a graceful
        // degradation message when the claim service is unresponsive.
        String msg = (String) service.claimServiceFallback(1L, new RuntimeException("Down"));
        assertTrue(msg.contains("unavailable"));
    }

    @Test
    void generateReport_shouldSaveAndReturnReport() {
        // WHY: Verification of the auditing capability, ensuring that system reports
        // are correctly generated and timestamped for record keeping.
        when(reportRepository.save(any())).thenReturn(new Report());
        assertNotNull(service.generateReport());
    }
        
    @Test
    void closeClaim_shouldCallFeignWithClosedStatus() {
        // WHY: Verification that the terminal closure stage is correctly 
        // proxied to the claim service for final archiving.
        service.closeClaim(1L);
        verify(claimFeignClient).closeClaim(1L);
    }
}
