package com.recrutement.recrutement.dto;

public class AdminSubscriptionResponse {
    private Long id;
    private Long recruiterId;
    private String recruiterName;
    private String recruiterEmail;
    private Long entrepriseId;
    private String entrepriseName;
    private String planType;
    private String status;
    private String startDate;
    private String endDate;
    private Integer maxJobOffers;
    private Integer maxCandidateViews;
    private Boolean aiFeaturesEnabled;
    private String createdAt;
    private String updatedAt;
    private String abonnementActif;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRecruiterId() { return recruiterId; }
    public void setRecruiterId(Long recruiterId) { this.recruiterId = recruiterId; }
    public String getRecruiterName() { return recruiterName; }
    public void setRecruiterName(String recruiterName) { this.recruiterName = recruiterName; }
    public String getRecruiterEmail() { return recruiterEmail; }
    public void setRecruiterEmail(String recruiterEmail) { this.recruiterEmail = recruiterEmail; }
    public Long getEntrepriseId() { return entrepriseId; }
    public void setEntrepriseId(Long entrepriseId) { this.entrepriseId = entrepriseId; }
    public String getEntrepriseName() { return entrepriseName; }
    public void setEntrepriseName(String entrepriseName) { this.entrepriseName = entrepriseName; }
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
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public String getAbonnementActif() { return abonnementActif; }
    public void setAbonnementActif(String abonnementActif) { this.abonnementActif = abonnementActif; }
}
