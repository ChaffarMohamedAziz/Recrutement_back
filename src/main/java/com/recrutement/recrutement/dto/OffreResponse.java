package com.recrutement.recrutement.dto;

import java.util.List;

public class OffreResponse {
    private Long id;
    private String titre;
    private String categorie;
    private String description;
    private String localisation;
    private double salaire;
    private String devise;
    private Integer nombrePostes;
    private String experienceRequise;
    private String typeContrat;
    private String statut;
    private String datePublication;
    private String dateExpiration;
    private String nomEntreprise;
    private Long recruiterId;
    private Double compatibilityScore;
    private Boolean alreadyApplied;
    private String applicationStatus;
    private List<OffreCompetenceResponse> competences;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public double getSalaire() {
        return salaire;
    }

    public void setSalaire(double salaire) {
        this.salaire = salaire;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public Integer getNombrePostes() {
        return nombrePostes;
    }

    public void setNombrePostes(Integer nombrePostes) {
        this.nombrePostes = nombrePostes;
    }

    public String getExperienceRequise() {
        return experienceRequise;
    }

    public void setExperienceRequise(String experienceRequise) {
        this.experienceRequise = experienceRequise;
    }

    public String getTypeContrat() {
        return typeContrat;
    }

    public void setTypeContrat(String typeContrat) {
        this.typeContrat = typeContrat;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(String datePublication) {
        this.datePublication = datePublication;
    }

    public String getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(String dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public String getNomEntreprise() {
        return nomEntreprise;
    }

    public void setNomEntreprise(String nomEntreprise) {
        this.nomEntreprise = nomEntreprise;
    }

    public Long getRecruiterId() {
        return recruiterId;
    }

    public void setRecruiterId(Long recruiterId) {
        this.recruiterId = recruiterId;
    }

    public Double getCompatibilityScore() {
        return compatibilityScore;
    }

    public void setCompatibilityScore(Double compatibilityScore) {
        this.compatibilityScore = compatibilityScore;
    }

    public Boolean getAlreadyApplied() {
        return alreadyApplied;
    }

    public void setAlreadyApplied(Boolean alreadyApplied) {
        this.alreadyApplied = alreadyApplied;
    }

    public String getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(String applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public List<OffreCompetenceResponse> getCompetences() {
        return competences;
    }

    public void setCompetences(List<OffreCompetenceResponse> competences) {
        this.competences = competences;
    }
}
