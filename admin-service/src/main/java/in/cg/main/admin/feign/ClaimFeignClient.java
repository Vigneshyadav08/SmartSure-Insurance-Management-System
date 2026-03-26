package in.cg.main.admin.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "claim-service")
public interface ClaimFeignClient {
    @PutMapping("/admin-internal/claims/{id}/status")
    Object updateClaimStatus(@PathVariable("id") Long id, @RequestParam("status") String status);

    @org.springframework.web.bind.annotation.GetMapping("/admin-internal/claims/{id}/download")
    byte[] downloadDocument(@PathVariable("id") Long id);

    @PutMapping("/admin-internal/claims/{id}/close")
    Object closeClaim(@PathVariable("id") Long id);
}
