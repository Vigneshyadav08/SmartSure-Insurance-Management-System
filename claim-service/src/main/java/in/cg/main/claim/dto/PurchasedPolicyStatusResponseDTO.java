package in.cg.main.claim.dto;

public class PurchasedPolicyStatusResponseDTO {
    private String status;
    private Double deductibleAmount;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getDeductibleAmount() { return deductibleAmount; }
    public void setDeductibleAmount(Double deductibleAmount) { this.deductibleAmount = deductibleAmount; }
}
