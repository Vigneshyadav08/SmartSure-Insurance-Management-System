package in.cg.main.claim.entity;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity
@Table(name="claims")
public class Claim {
    public Claim() {
        // Default constructor for JPA
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long policyId;
    private String customerUsername;
    private String description;
    private String status; // PENDING, APPROVED, REJECTED
    private String documentPath;
    private Double claimAmount;
    private Double deductibleAmount;
    private Double payoutAmount;
    private LocalDate incidentDate;
    private LocalDate claimDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }
    public String getCustomerUsername() { return customerUsername; }
    public void setCustomerUsername(String customerUsername) { this.customerUsername = customerUsername; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }
    public Double getClaimAmount() { return claimAmount; }
    public void setClaimAmount(Double claimAmount) { this.claimAmount = claimAmount; }
    public Double getDeductibleAmount() { return deductibleAmount; }
    public void setDeductibleAmount(Double deductibleAmount) { this.deductibleAmount = deductibleAmount; }
    public Double getPayoutAmount() { return payoutAmount; }
    public void setPayoutAmount(Double payoutAmount) { this.payoutAmount = payoutAmount; }
    public LocalDate getIncidentDate() { return incidentDate; }
    public void setIncidentDate(LocalDate incidentDate) { this.incidentDate = incidentDate; }
    public LocalDate getClaimDate() { return claimDate; }
    public void setClaimDate(LocalDate claimDate) { this.claimDate = claimDate; }
}
