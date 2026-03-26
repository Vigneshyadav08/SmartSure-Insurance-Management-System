package in.cg.main.claim.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class ClaimRequestDTO {
	@NotNull(message = "Policy ID is required")
	private Long policyId;
	@NotBlank(message = "Customer username is required")
	private String customerUsername;
	@NotBlank(message = "Description is required")
	private String description;
    @NotNull(message = "Claim amount is required")
    private Double claimAmount;
    @NotNull(message = "Incident date is required")
    private LocalDate incidentDate;

    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }
    public String getCustomerUsername() { return customerUsername; }
    public void setCustomerUsername(String customerUsername) { this.customerUsername = customerUsername; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getClaimAmount() { return claimAmount; }
    public void setClaimAmount(Double claimAmount) { this.claimAmount = claimAmount; }
    public LocalDate getIncidentDate() { return incidentDate; }
    public void setIncidentDate(LocalDate incidentDate) { this.incidentDate = incidentDate; }
}
