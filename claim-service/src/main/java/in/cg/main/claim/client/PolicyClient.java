package in.cg.main.claim.client;

import in.cg.main.claim.dto.PurchasedPolicyStatusResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "policy-service")
public interface PolicyClient {

    @GetMapping("/policies/purchase/status")
    PurchasedPolicyStatusResponseDTO getPurchasedPolicyStatus(
            @RequestParam("username") String username, 
            @RequestParam("policyId") Long policyId
    );
}
