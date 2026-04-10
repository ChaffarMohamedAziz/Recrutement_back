package com.recrutement.recrutement.service;

import com.recrutement.recrutement.dto.ApplicationResponse;
import com.recrutement.recrutement.entities.Candidate;
import com.recrutement.recrutement.entities.Candidature;
import com.recrutement.recrutement.entities.Entreprise;
import com.recrutement.recrutement.entities.Offre;
import com.recrutement.recrutement.entities.Recruiter;
import com.recrutement.recrutement.entities.User;
import com.recrutement.recrutement.repositories.CandidateRepository;
import com.recrutement.recrutement.repositories.CandidatureRepository;
import com.recrutement.recrutement.repositories.OffreRepository;
import com.recrutement.recrutement.repositories.RecruiterRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {
    private static final String STATUS_A_TRIER = "A_TRIER";
    private static final String STATUS_ENTRETIEN = "ENTRETIEN";
    private static final String STATUS_RETENU = "RETENU";
    private static final String STATUS_REFUSE = "REFUSE";

    private final CandidateRepository candidateRepository;
    private final RecruiterRepository recruiterRepository;
    private final OffreRepository offreRepository;
    private final CandidatureRepository candidatureRepository;
    private final MatchingService matchingService;

    public ApplicationService(
            CandidateRepository candidateRepository,
            RecruiterRepository recruiterRepository,
            OffreRepository offreRepository,
            CandidatureRepository candidatureRepository,
            MatchingService matchingService
    ) {
        this.candidateRepository = candidateRepository;
        this.recruiterRepository = recruiterRepository;
        this.offreRepository = offreRepository;
        this.candidatureRepository = candidatureRepository;
        this.matchingService = matchingService;
    }

    @Transactional
    public ApplicationResponse applyToOffer(User currentUser, Long offerId) {
        Candidate candidate = getCurrentCandidate(currentUser);
        Offre offer = getActiveOffer(offerId);

        candidatureRepository.findByCandidate_IdAndOffre_Id(candidate.getId(), offerId)
                .ifPresent(existing -> {
                    throw new RuntimeException("Vous avez deja postule a cette offre.");
                });

        MatchingService.MatchResult matchResult = matchingService.evaluate(candidate, offer);

        Candidature candidature = Candidature.builder()
                .candidate(candidate)
                .offre(offer)
                .dateDepot(new Date())
                .statut(STATUS_A_TRIER)
                .scoreCandidat(matchResult.getScore())
                .build();

        return toResponse(candidatureRepository.save(candidature), matchResult);
    }

    public List<ApplicationResponse> getCandidateApplications(User currentUser) {
        Candidate candidate = getCurrentCandidate(currentUser);
        return candidatureRepository.findByCandidate_IdOrderByDateDepotDesc(candidate.getId()).stream()
                .map(candidature -> toResponse(candidature, null))
                .collect(Collectors.toList());
    }

    public List<ApplicationResponse> getRecruiterApplications(User currentUser, Long offerId, Double minScore) {
        Recruiter recruiter = getCurrentRecruiter(currentUser);
        List<Candidature> candidatures = offerId == null
                ? candidatureRepository.findByOffre_Recruiter_IdOrderByScoreCandidatDescDateDepotDesc(recruiter.getId())
                : candidatureRepository.findByOffre_Recruiter_IdAndOffre_IdOrderByScoreCandidatDescDateDepotDesc(recruiter.getId(), offerId);

        double effectiveMinScore = minScore == null ? 0d : minScore;

        return candidatures.stream()
                .filter(candidature -> candidature.getScoreCandidat() >= effectiveMinScore)
                .map(candidature -> toResponse(candidature, null))
                .collect(Collectors.toList());
    }

    public ApplicationResponse getRecruiterApplicationById(User currentUser, Long applicationId) {
        Recruiter recruiter = getCurrentRecruiter(currentUser);
        Candidature candidature = candidatureRepository.findByIdAndOffre_Recruiter_Id(applicationId, recruiter.getId())
                .orElseThrow(() -> new RuntimeException("Candidature introuvable."));
        return toResponse(candidature, null);
    }

    @Transactional
    public ApplicationResponse updateRecruiterApplicationStatus(User currentUser, Long applicationId, String status) {
        Recruiter recruiter = getCurrentRecruiter(currentUser);
        Candidature candidature = candidatureRepository.findByIdAndOffre_Recruiter_Id(applicationId, recruiter.getId())
                .orElseThrow(() -> new RuntimeException("Candidature introuvable."));

        candidature.setStatut(normalizeStatus(status));
        return toResponse(candidatureRepository.save(candidature), null);
    }

    public boolean hasApplied(Long offerId, Long candidateId) {
        if (offerId == null || candidateId == null) {
            return false;
        }
        return candidatureRepository.findByCandidate_IdAndOffre_Id(candidateId, offerId).isPresent();
    }

    public String getApplicationStatus(Long offerId, Long candidateId) {
        if (offerId == null || candidateId == null) {
            return null;
        }
        return candidatureRepository.findByCandidate_IdAndOffre_Id(candidateId, offerId)
                .map(Candidature::getStatut)
                .orElse(null);
    }

    private ApplicationResponse toResponse(Candidature candidature, MatchingService.MatchResult matchResult) {
        Candidate candidate = candidature.getCandidate();
        Offre offer = candidature.getOffre();
        MatchingService.MatchResult effectiveMatch = matchResult != null ? matchResult : matchingService.evaluate(candidate, offer);

        ApplicationResponse response = new ApplicationResponse();
        response.setId(candidature.getId());
        response.setOfferId(offer == null ? null : offer.getId());
        response.setOfferTitle(offer == null ? "" : safe(offer.getTitre()));
        response.setCompanyName(resolveCompanyName(offer == null ? null : offer.getRecruiter()));
        response.setOfferLocation(offer == null ? "" : safe(offer.getLocalisation()));
        response.setContractType(offer == null ? "" : safe(offer.getTypeContrat()));
        response.setAppliedAt(formatDate(candidature.getDateDepot()));
        response.setStatus(normalizeStatus(candidature.getStatut()));
        response.setScore(roundScore(candidature.getScoreCandidat()));
        response.setCandidateId(candidate == null ? null : candidate.getId());
        response.setCandidateName(candidate == null ? "" : safe(candidate.getNom()));
        response.setCandidateEmail(candidate == null ? "" : safe(candidate.getEmail()));
        response.setCandidateJobTitle(candidate == null ? "" : safe(nonEmpty(candidate.getPosteRecherche(), candidate.getProfession())));
        response.setCandidateLocation(candidate == null ? "" : safe(candidate.getLocalisation()));
        response.setCandidateExperience(candidate == null ? 0 : candidate.getExperience());
        response.setCandidateSummary(candidate == null ? "" : safe(candidate.getDescription()));
        response.setMatchingSkills(effectiveMatch.getMatchingSkills());
        response.setMissingSkills(effectiveMatch.getMissingSkills());
        response.setSkills(effectiveMatch.getSkills());
        return response;
    }

    private Candidate getCurrentCandidate(User currentUser) {
        Candidate candidate = currentUser.getId() == null
                ? null
                : candidateRepository.findById(currentUser.getId()).orElse(null);

        if (candidate == null) {
            candidate = candidateRepository.findByEmail(currentUser.getEmail());
        }

        if (candidate == null) {
            throw new RuntimeException("Profil candidat introuvable.");
        }
        return candidate;
    }

    private Recruiter getCurrentRecruiter(User currentUser) {
        Recruiter recruiter = recruiterRepository.findByEmail(currentUser.getEmail());
        if (recruiter == null) {
            throw new RuntimeException("Profil recruteur introuvable.");
        }
        return recruiter;
    }

    private Offre getActiveOffer(Long offerId) {
        Offre offer = offreRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offre introuvable."));

        if (!isOfferActive(offer)) {
            throw new RuntimeException("Cette offre n'est plus active.");
        }

        return offer;
    }

    private boolean isOfferActive(Offre offer) {
        String status = safe(offer.getStatut()).toUpperCase(Locale.ROOT);
        if ("BROUILLON".equals(status) || "ARCHIVEE".equals(status) || "FERMEE".equals(status)) {
            return false;
        }

        Date expirationDate = offer.getDateExpiration();
        if (expirationDate == null) {
            return true;
        }

        LocalDate expiry = toLocalDate(expirationDate);
        return !expiry.isBefore(LocalDate.now());
    }

    private String normalizeStatus(String status) {
        String normalized = safe(status).toUpperCase(Locale.ROOT);
        if (STATUS_ENTRETIEN.equals(normalized)) {
            return STATUS_ENTRETIEN;
        }
        if (STATUS_RETENU.equals(normalized)) {
            return STATUS_RETENU;
        }
        if (STATUS_REFUSE.equals(normalized)) {
            return STATUS_REFUSE;
        }
        return STATUS_A_TRIER;
    }

    private String resolveCompanyName(Recruiter recruiter) {
        if (recruiter == null) {
            return "";
        }

        Entreprise entreprise = recruiter.getEntreprise();
        if (entreprise != null && entreprise.getNomEntreprise() != null && !entreprise.getNomEntreprise().isBlank()) {
            return entreprise.getNomEntreprise();
        }

        return safe(recruiter.getNom());
    }

    private String formatDate(Date value) {
        if (value == null) {
            return "";
        }

        return toLocalDate(value).toString();
    }

    private LocalDate toLocalDate(Date value) {
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }

        return value.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private double roundScore(double value) {
        return Math.round(value * 10d) / 10d;
    }

    private String nonEmpty(String primary, String fallback) {
        String normalized = safe(primary);
        return normalized.isEmpty() ? safe(fallback) : normalized;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
