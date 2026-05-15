package com.recrutement.recrutement.service;

import com.recrutement.recrutement.dto.AdminSubscriptionRequest;
import com.recrutement.recrutement.dto.AdminSubscriptionResponse;
import com.recrutement.recrutement.entities.Entreprise;
import com.recrutement.recrutement.entities.PlanType;
import com.recrutement.recrutement.entities.Recruiter;
import com.recrutement.recrutement.entities.Subscription;
import com.recrutement.recrutement.entities.SubscriptionStatus;
import com.recrutement.recrutement.repositories.EntrepriseRepository;
import com.recrutement.recrutement.repositories.OffreRepository;
import com.recrutement.recrutement.repositories.RecruiterRepository;
import com.recrutement.recrutement.repositories.SubscriptionRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubscriptionService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.FRANCE);

    private final SubscriptionRepository subscriptionRepository;
    private final RecruiterRepository recruiterRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final OffreRepository offreRepository;

    public SubscriptionService(
            SubscriptionRepository subscriptionRepository,
            RecruiterRepository recruiterRepository,
            EntrepriseRepository entrepriseRepository,
            OffreRepository offreRepository
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.recruiterRepository = recruiterRepository;
        this.entrepriseRepository = entrepriseRepository;
        this.offreRepository = offreRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminSubscriptionResponse> getAdminSubscriptions(String status) {
        if (safe(status).isBlank()) {
            return subscriptionRepository.findAllByOrderByUpdatedAtDesc().stream().map(this::toResponse).toList();
        }
        SubscriptionStatus subscriptionStatus = SubscriptionStatus.valueOf(safe(status).toUpperCase(Locale.ROOT));
        return subscriptionRepository.findByStatusOrderByUpdatedAtDesc(subscriptionStatus).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AdminSubscriptionResponse getAdminSubscription(Long id) {
        return toResponse(getExistingSubscription(id));
    }

    @Transactional
    public AdminSubscriptionResponse createAdminSubscription(AdminSubscriptionRequest request) {
        Recruiter recruiter = getRecruiter(request.getRecruiterId());
        if (subscriptionRepository.findByRecruiter_Id(recruiter.getId()).isPresent()) {
            throw new RuntimeException("Ce recruteur possede deja un abonnement.");
        }
        Subscription subscription = Subscription.builder().recruiter(recruiter).build();
        applyRequest(subscription, request, true);
        syncEntrepriseAbonnement(subscription);
        return toResponse(subscriptionRepository.save(subscription));
    }

    @Transactional
    public AdminSubscriptionResponse updateAdminSubscription(Long id, AdminSubscriptionRequest request) {
        Subscription subscription = getExistingSubscription(id);
        applyRequest(subscription, request, false);
        syncEntrepriseAbonnement(subscription);
        return toResponse(subscriptionRepository.save(subscription));
    }

    @Transactional
    public AdminSubscriptionResponse activateSubscription(Long id) {
        Subscription subscription = getExistingSubscription(id);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        if (subscription.getStartDate() == null) {
            subscription.setStartDate(new Date());
        }
        if (subscription.getEndDate() == null) {
            subscription.setEndDate(toDate(LocalDate.now().plusDays(30)));
        }
        syncEntrepriseAbonnement(subscription);
        return toResponse(subscriptionRepository.save(subscription));
    }

    @Transactional
    public AdminSubscriptionResponse suspendSubscription(Long id) {
        Subscription subscription = getExistingSubscription(id);
        subscription.setStatus(SubscriptionStatus.SUSPENDED);
        syncEntrepriseAbonnement(subscription);
        return toResponse(subscriptionRepository.save(subscription));
    }

    @Transactional
    public AdminSubscriptionResponse renewSubscription(Long id, AdminSubscriptionRequest request) {
        Subscription subscription = getExistingSubscription(id);
        LocalDate baseDate = subscription.getEndDate() == null
                ? LocalDate.now()
                : subscription.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int additionalDays = request != null && request.getAdditionalDays() != null ? Math.max(1, request.getAdditionalDays()) : 30;
        subscription.setEndDate(toDate(baseDate.plusDays(additionalDays)));
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        syncEntrepriseAbonnement(subscription);
        return toResponse(subscriptionRepository.save(subscription));
    }

    @Transactional(readOnly = true)
    public Optional<Subscription> findByRecruiterId(Long recruiterId) {
        return subscriptionRepository.findByRecruiter_Id(recruiterId)
                .map(this::markExpiredIfNeededReadonly);
    }

    @Transactional
    public void assertRecruiterCanPublishOffer(Recruiter recruiter, boolean creation) {
        Subscription subscription = getApplicableSubscription(recruiter);
        if (subscription == null) {
            return;
        }
        assertSubscriptionActive(subscription, "publier une offre");
        if (!creation) {
            return;
        }
        int maxJobOffers = subscription.getMaxJobOffers() == null ? 0 : subscription.getMaxJobOffers();
        if (maxJobOffers <= 0) {
            throw new RuntimeException("Votre abonnement ne permet pas de publier d'offres actuellement.");
        }
        long currentOffers = offreRepository.findByRecruiter_IdOrderByDateDesc(recruiter.getId()).stream()
                .filter(offre -> !"ARCHIVEE".equalsIgnoreCase(safe(offre.getStatut())))
                .count();
        if (currentOffers >= maxJobOffers) {
            throw new RuntimeException("Votre plan " + subscription.getPlanType().name() + " limite la publication a " + maxJobOffers + " offre(s).");
        }
    }

    @Transactional
    public void assertRecruiterCanUseAiFeatures(Recruiter recruiter, String featureLabel) {
        Subscription subscription = getApplicableSubscription(recruiter);
        if (subscription == null) {
            return;
        }
        assertSubscriptionActive(subscription, featureLabel);
        if (!Boolean.TRUE.equals(subscription.getAiFeaturesEnabled())) {
            throw new RuntimeException("Votre abonnement actuel n'inclut pas " + featureLabel + ".");
        }
    }

    private Subscription getApplicableSubscription(Recruiter recruiter) {
        if (recruiter == null || recruiter.getId() == null) {
            return null;
        }
        return subscriptionRepository.findByRecruiter_Id(recruiter.getId())
                .map(this::markExpiredIfNeeded)
                .orElse(null);
    }

    private Subscription markExpiredIfNeededReadonly(Subscription subscription) {
        if (subscription == null || subscription.getEndDate() == null || subscription.getStatus() == SubscriptionStatus.EXPIRED) {
            return subscription;
        }
        LocalDate endDate = subscription.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (endDate.isBefore(LocalDate.now())) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
        }
        return subscription;
    }

    private Subscription markExpiredIfNeeded(Subscription subscription) {
        Subscription updated = markExpiredIfNeededReadonly(subscription);
        if (updated != null && updated.getId() != null && updated.getStatus() == SubscriptionStatus.EXPIRED) {
            syncEntrepriseAbonnement(updated);
            return subscriptionRepository.save(updated);
        }
        return updated;
    }

    private void assertSubscriptionActive(Subscription subscription, String actionLabel) {
        if (subscription.getStatus() == SubscriptionStatus.SUSPENDED) {
            throw new RuntimeException("Cet abonnement est suspendu. Impossible de " + actionLabel + ".");
        }
        if (subscription.getStatus() == SubscriptionStatus.EXPIRED) {
            throw new RuntimeException("Cet abonnement a expire. Impossible de " + actionLabel + ".");
        }
    }

    private void applyRequest(Subscription subscription, AdminSubscriptionRequest request, boolean creating) {
        if (request == null) {
            throw new RuntimeException("Les informations d'abonnement sont obligatoires.");
        }
        Recruiter recruiter = creating ? getRecruiter(request.getRecruiterId()) : subscription.getRecruiter();
        Entreprise entreprise = request.getEntrepriseId() != null
                ? entrepriseRepository.findById(request.getEntrepriseId()).orElse(null)
                : recruiter.getEntreprise();

        subscription.setRecruiter(recruiter);
        subscription.setEntreprise(entreprise);
        subscription.setPlanType(resolvePlanType(request.getPlanType()));
        subscription.setStatus(resolveStatus(request.getStatus()));
        subscription.setStartDate(parseDate(request.getStartDate(), LocalDate.now()));
        subscription.setEndDate(parseDate(request.getEndDate(), LocalDate.now().plusDays(defaultDurationDays(subscription.getPlanType()))));
        subscription.setMaxJobOffers(resolveMaxJobOffers(request.getMaxJobOffers(), subscription.getPlanType()));
        subscription.setMaxCandidateViews(resolveMaxCandidateViews(request.getMaxCandidateViews(), subscription.getPlanType()));
        subscription.setAiFeaturesEnabled(request.getAiFeaturesEnabled() != null ? request.getAiFeaturesEnabled() : defaultAiFeatures(subscription.getPlanType()));
        if (subscription.getCreatedAt() == null) {
            subscription.setCreatedAt(new Date());
        }
        subscription.setUpdatedAt(new Date());
    }

    private void syncEntrepriseAbonnement(Subscription subscription) {
        Entreprise entreprise = subscription.getEntreprise();
        if (entreprise == null && subscription.getRecruiter() != null) {
            entreprise = subscription.getRecruiter().getEntreprise();
        }
        if (entreprise == null) {
            return;
        }
        entreprise.setAbonnementActif(subscription.getStatus() == SubscriptionStatus.ACTIVE ? "OUI" : "NON");
        entrepriseRepository.save(entreprise);
    }

    private AdminSubscriptionResponse toResponse(Subscription subscription) {
        AdminSubscriptionResponse response = new AdminSubscriptionResponse();
        response.setId(subscription.getId());
        response.setRecruiterId(subscription.getRecruiter() == null ? null : subscription.getRecruiter().getId());
        response.setRecruiterName(subscription.getRecruiter() == null ? "" : safe(subscription.getRecruiter().getNom()));
        response.setRecruiterEmail(subscription.getRecruiter() == null ? "" : safe(subscription.getRecruiter().getEmail()));
        response.setEntrepriseId(subscription.getEntreprise() == null ? null : subscription.getEntreprise().getIdEntreprise());
        response.setEntrepriseName(subscription.getEntreprise() == null ? "" : safe(subscription.getEntreprise().getNomEntreprise()));
        response.setPlanType(subscription.getPlanType() == null ? "" : subscription.getPlanType().name());
        response.setStatus(subscription.getStatus() == null ? "" : subscription.getStatus().name());
        response.setStartDate(formatDate(subscription.getStartDate()));
        response.setEndDate(formatDate(subscription.getEndDate()));
        response.setMaxJobOffers(subscription.getMaxJobOffers());
        response.setMaxCandidateViews(subscription.getMaxCandidateViews());
        response.setAiFeaturesEnabled(Boolean.TRUE.equals(subscription.getAiFeaturesEnabled()));
        response.setCreatedAt(formatDateTime(subscription.getCreatedAt()));
        response.setUpdatedAt(formatDateTime(subscription.getUpdatedAt()));
        response.setAbonnementActif(subscription.getEntreprise() == null ? "" : safe(subscription.getEntreprise().getAbonnementActif()));
        return response;
    }

    private Subscription getExistingSubscription(Long id) {
        return subscriptionRepository.findById(id)
                .map(this::markExpiredIfNeeded)
                .orElseThrow(() -> new RuntimeException("Abonnement introuvable."));
    }

    private Recruiter getRecruiter(Long recruiterId) {
        if (recruiterId == null) {
            throw new RuntimeException("Le recruteur est obligatoire.");
        }
        return recruiterRepository.findById(recruiterId)
                .orElseThrow(() -> new RuntimeException("Recruteur introuvable."));
    }

    private PlanType resolvePlanType(String value) {
        if (safe(value).isBlank()) {
            return PlanType.FREE;
        }
        return PlanType.valueOf(safe(value).toUpperCase(Locale.ROOT));
    }

    private SubscriptionStatus resolveStatus(String value) {
        if (safe(value).isBlank()) {
            return SubscriptionStatus.ACTIVE;
        }
        return SubscriptionStatus.valueOf(safe(value).toUpperCase(Locale.ROOT));
    }

    private Date parseDate(String value, LocalDate fallback) {
        if (safe(value).isBlank()) {
            return toDate(fallback);
        }
        return toDate(LocalDate.parse(value, DATE_FORMATTER));
    }

    private Date toDate(LocalDate localDate) {
        return java.sql.Date.valueOf(localDate);
    }

    private int resolveMaxJobOffers(Integer value, PlanType planType) {
        if (value != null) {
            return Math.max(0, value);
        }
        return switch (planType) {
            case FREE -> 1;
            case STANDARD -> 10;
            case PREMIUM -> 100;
        };
    }

    private int resolveMaxCandidateViews(Integer value, PlanType planType) {
        if (value != null) {
            return Math.max(0, value);
        }
        return switch (planType) {
            case FREE -> 25;
            case STANDARD -> 250;
            case PREMIUM -> 5000;
        };
    }

    private boolean defaultAiFeatures(PlanType planType) {
        return planType == PlanType.PREMIUM;
    }

    private int defaultDurationDays(PlanType planType) {
        return switch (planType) {
            case FREE -> 30;
            case STANDARD -> 90;
            case PREMIUM -> 365;
        };
    }

    private String formatDate(Date date) {
        return date == null ? "" : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(DATE_FORMATTER);
    }

    private String formatDateTime(Date date) {
        return date == null ? "" : DATE_TIME_FORMATTER.format(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
