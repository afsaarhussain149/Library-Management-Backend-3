package com.library.bean;

import java.util.List;

public class PlanCatalogDetails {
	private Integer planId;
	private Integer hours;
	private List<PlanOptionDetails> options;

	public Integer getPlanId() { return planId; }
	public void setPlanId(Integer planId) { this.planId = planId; }
	public Integer getHours() { return hours; }
	public void setHours(Integer hours) { this.hours = hours; }
	public List<PlanOptionDetails> getOptions() { return options; }
	public void setOptions(List<PlanOptionDetails> options) { this.options = options; }
}
