package in.cg.main.policy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class PolicyDTO {
	@NotBlank(message = "Policy name is required")
	private String name;
	@Positive(message = "Coverage must be greater than zero")
	private double coverage;
	@Positive(message = "Premium must be greater than zero")
	private double premium;
	@Positive(message = "Deductible amount must be positive")
	private double deductibleAmount;
	@Min(value = 1, message = "Duration must be at least 1 month")
	private int months;

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public double getCoverage() { return coverage; }
	public void setCoverage(double coverage) { this.coverage = coverage; }
	public double getPremium() { return premium; }
	public void setPremium(double premium) { this.premium = premium; }
	public double getDeductibleAmount() { return deductibleAmount; }
	public void setDeductibleAmount(double deductibleAmount) { this.deductibleAmount = deductibleAmount; }
	public int getMonths() { return months; }
	public void setMonths(int months) { this.months = months; }
}
