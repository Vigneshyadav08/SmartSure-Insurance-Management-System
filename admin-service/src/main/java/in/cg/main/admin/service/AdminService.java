package in.cg.main.admin.service;

import in.cg.main.admin.entity.Report;
import in.cg.main.admin.feign.ClaimFeignClient;
import in.cg.main.admin.feign.PolicyFeignClient;
import in.cg.main.admin.repository.ReportRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class AdminService {
    private final ClaimFeignClient claimFeignClient;
    private final PolicyFeignClient policyFeignClient;
    private final ReportRepository reportRepository;

    public AdminService(ClaimFeignClient claimFeignClient, PolicyFeignClient policyFeignClient, ReportRepository reportRepository) {
        this.claimFeignClient = claimFeignClient;
        this.policyFeignClient = policyFeignClient;
        this.reportRepository = reportRepository;
    }

    @CircuitBreaker(name = "policyService", fallbackMethod = "policyServiceFallback")
    public Object getPurchasedPolicies(String username) {
        return policyFeignClient.getPurchasedPolicies(username);
    }

    @CircuitBreaker(name = "policyService", fallbackMethod = "policyServiceFallback")
    public String expirePolicy(Long id) {
        return policyFeignClient.expirePolicy(id);
    }

    public Object policyServiceFallback(Object arg, Exception e) {
        return "Resilience Triggered: Policy Service is currently unavailable. Error: " + e.getMessage();
    }

    @CircuitBreaker(name = "claimService", fallbackMethod = "claimServiceFallback")
    @Retry(name = "claimService")
    @Bulkhead(name = "claimService")
    public Object approveClaim(Long claimId) {
        return claimFeignClient.updateClaimStatus(claimId, "APPROVED");
    }

    @CircuitBreaker(name = "claimService", fallbackMethod = "claimServiceFallback")
    @Retry(name = "claimService")
    @Bulkhead(name = "claimService")
    public Object rejectClaim(Long claimId) {
        return claimFeignClient.updateClaimStatus(claimId, "REJECTED");
    }

    public Object claimServiceFallback(Long claimId, Exception e) {
        return "Resilience Triggered for claim ID " + claimId + ": Claim Service is currently unavailable. Please try again later. Error: " + e.getMessage();
    }

    @CircuitBreaker(name = "claimService", fallbackMethod = "claimServiceFallback")
    public byte[] downloadClaimDocument(Long claimId) {
        return claimFeignClient.downloadDocument(claimId);
    }

    @CircuitBreaker(name = "claimService", fallbackMethod = "claimServiceFallback")
    public Object closeClaim(Long claimId) {
        return claimFeignClient.closeClaim(claimId);
    }

    public Report generateReport() {
        Report report = new Report();
        report.setContent("System Operations Report Generated");
        report.setGeneratedAt(LocalDateTime.now());
        return reportRepository.save(report);
    }
}
