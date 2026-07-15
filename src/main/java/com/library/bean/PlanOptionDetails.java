package com.library.bean;

public class PlanOptionDetails {
	private Integer optionId;
	private Integer planId;
	private String name;
	private Double price;

	public Integer getOptionId() { return optionId; }
	public void setOptionId(Integer optionId) { this.optionId = optionId; }
	public Integer getPlanId() { return planId; }
	public void setPlanId(Integer planId) { this.planId = planId; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public Double getPrice() { return price; }
	public void setPrice(Double price) { this.price = price; }
}
