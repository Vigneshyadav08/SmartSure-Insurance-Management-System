package in.cg.main.policy.entity;

import jakarta.persistence.*;

@Entity
@Table(name="purchased_policies")
public class PurchasedPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long policyId;
    private String customerUsername;
    private String status; // CREATED, ACTIVE, CANCELLED, EXPIRY
    private Double deductibleAmount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }
    public String getCustomerUsername() { return customerUsername; }
    public void setCustomerUsername(String customerUsername) { this.customerUsername = customerUsername; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getDeductibleAmount() { return deductibleAmount; }
    public void setDeductibleAmount(Double deductibleAmount) { this.deductibleAmount = deductibleAmount; }
}
