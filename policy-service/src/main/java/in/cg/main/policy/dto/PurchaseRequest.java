package in.cg.main.policy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
public class PurchaseRequest {
    @NotNull(message = "Policy ID is required")
    private Long policyId;
    @NotBlank(message = "Customer username is required")
    private String customerUsername;

    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }
    public String getCustomerUsername() { return customerUsername; }
    public void setCustomerUsername(String customerUsername) { this.customerUsername = customerUsername; }
}
