package com.recrutement.recrutement.dto;

public class AdminSubscriptionRequest {
    private Long recruiterId;
    private Long entrepriseId;
    private String planType;
    private String status;
    private String startDate;
    private String endDate;
    private Integer maxJobOffers;
    private Integer maxCandidateViews;
    private Boolean aiFeaturesEnabled;
    private Integer additionalDays;

    public Long getRecruiterId() { return recruiterId; }
    public void setRecruiterId(Long recruiterId) { this.recruiterId = recruiterId; }
    public Long getEntrepriseId() { return entrepriseId; }
    public void setEntrepriseId(Long entrepriseId) { this.entrepriseId = entrepriseId; }
    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public Integer getMaxJobOffers() { return maxJobOffers; }
    public void setMaxJobOffers(Integer maxJobOffers) { this.maxJobOffers = maxJobOffers; }
    public Integer getMaxCandidateViews() { return maxCandidateViews; }
    public void setMaxCandidateViews(Integer maxCandidateViews) { this.maxCandidateViews = maxCandidateViews; }
    public Boolean getAiFeaturesEnabled() { return aiFeaturesEnabled; }
    public void setAiFeaturesEnabled(Boolean aiFeaturesEnabled) { this.aiFeaturesEnabled = aiFeaturesEnabled; }
    public Integer getAdditionalDays() { return additionalDays; }
    public void setAdditionalDays(Integer additionalDays) { this.additionalDays = additionalDays; }
}
