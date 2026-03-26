package in.cg.main.policy.dto;

public class PolicyResponseDTO {
    private Long id;
    private String name;
    private Double premium;
    private Double coverage;
    private Double deductibleAmount;
    private int months;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPremium() { return premium; }
    public void setPremium(Double premium) { this.premium = premium; }
    public Double getCoverage() { return coverage; }
    public void setCoverage(Double coverage) { this.coverage = coverage; }
    public Double getDeductibleAmount() { return deductibleAmount; }
    public void setDeductibleAmount(Double deductibleAmount) { this.deductibleAmount = deductibleAmount; }
    public int getMonths() { return months; }
    public void setMonths(int months) { this.months = months; }
}
