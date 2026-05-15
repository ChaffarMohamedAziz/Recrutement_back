package com.recrutement.recrutement.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_id", unique = true)
    private Recruiter recruiter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id")
    private Entreprise entreprise;

    @Enumerated(EnumType.STRING)
    private PlanType planType;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Temporal(TemporalType.DATE)
    private Date endDate;

    private Integer maxJobOffers;

    private Integer maxCandidateViews;

    private Boolean aiFeaturesEnabled;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @PrePersist
    void prePersist() {
        Date now = new Date();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (planType == null) {
            planType = PlanType.FREE;
        }
        if (status == null) {
            status = SubscriptionStatus.ACTIVE;
        }
        if (aiFeaturesEnabled == null) {
            aiFeaturesEnabled = Boolean.FALSE;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = new Date();
    }
}
