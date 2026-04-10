package com.recrutement.recrutement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recrutement.recrutement.dto.OffreCompetenceRequest;
import com.recrutement.recrutement.dto.OffreCompetenceResponse;
import com.recrutement.recrutement.dto.OffreRequest;
import com.recrutement.recrutement.dto.OffreResponse;
import com.recrutement.recrutement.entities.Candidate;
import com.recrutement.recrutement.entities.Offre;
import com.recrutement.recrutement.entities.Recruiter;
import com.recrutement.recrutement.entities.Role;
import com.recrutement.recrutement.entities.User;
import com.recrutement.recrutement.repositories.CandidateRepository;
import com.recrutement.recrutement.repositories.CandidatureRepository;
import com.recrutement.recrutement.repositories.OffreRepository;
import com.recrutement.recrutement.repositories.RecruiterRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class OfferService {
    private final OffreRepository offreRepository;
    private final RecruiterRepository recruiterRepository;
    private final CandidateRepository candidateRepository;
    private final CandidatureRepository candidatureRepository;
    private final MatchingService matchingService;
    private final ObjectMapper objectMapper;

    public OfferService(
            OffreRepository offreRepository,
            RecruiterRepository recruiterRepository,
            CandidateRepository candidateRepository,
            CandidatureRepository candidatureRepository,
            MatchingService matchingService,
            ObjectMapper objectMapper
    ) {
        this.offreRepository = offreRepository;
        this.recruiterRepository = recruiterRepository;
        this.candidateRepository = candidateRepository;
        this.candidatureRepository = candidatureRepository;
        this.matchingService = matchingService;
        this.objectMapper = objectMapper;
    }

    public List<OffreResponse> getAllOffers(User currentUser) {
        Candidate candidate = resolveCandidate(currentUser);
        return offreRepository.findAllByOrderByDateDesc().stream()
                .filter(this::isOfferActive)
                .map(offre -> toResponse(offre, candidate))
                .collect(Collectors.toList());
    }

    public OffreResponse getOfferById(Long id, User currentUser) {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre introuvable."));
        return toResponse(offre, resolveCandidate(currentUser));
    }

    public List<OffreResponse> getRecruiterOffers(User currentUser) {
        Recruiter recruiter = getCurrentRecruiter(currentUser);
        return offreRepository.findByRecruiter_IdOrderByDateDesc(recruiter.getId()).stream()
                .map(offre -> toResponse(offre, null))
                .collect(Collectors.toList());
    }

    public OffreResponse createOffer(User currentUser, OffreRequest request) {
        if (request == null) {
            throw new RuntimeException("Les informations de l'offre sont obligatoires.");
        }

        Recruiter recruiter = getCurrentRecruiter(currentUser);

        Offre offre = new Offre();
        applyRequest(offre, request);
        offre.setRecruiter(recruiter);
        offre.setDate(new Date());

        return toResponse(offreRepository.save(offre), null);
    }

    public OffreResponse updateOffer(User currentUser, Long offerId, OffreRequest request) {
        if (request == null) {
            throw new RuntimeException("Les informations de l'offre sont obligatoires.");
        }

        Recruiter recruiter = getCurrentRecruiter(currentUser);
        Offre offre = offreRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offre introuvable."));

        if (offre.getRecruiter() == null || !offre.getRecruiter().getId().equals(recruiter.getId())) {
            throw new RuntimeException("Vous ne pouvez modifier que vos propres offres.");
        }

        applyRequest(offre, request);
        return toResponse(offreRepository.save(offre), null);
    }

    private void applyRequest(Offre offre, OffreRequest request) {
        offre.setTitre(requireValue(request.getTitre(), "Le titre de l'offre est obligatoire."));
        offre.setCategorie(requireValue(request.getCategorie(), "La categorie de l'offre est obligatoire."));
        offre.setDescription(requireValue(request.getDescription(), "La description de l'offre est obligatoire."));
        offre.setLocalisation(requireValue(request.getLocalisation(), "La localisation est obligatoire."));
        offre.setTypeContrat(requireValue(request.getTypeContrat(), "Le type de contrat est obligatoire."));
        offre.setSalaire(request.getSalaire() == null ? 0d : request.getSalaire());
        offre.setDevise(defaultValue(request.getDevise(), "TND"));
        offre.setNombrePostes(request.getNombrePostes() == null ? 1 : request.getNombrePostes());
        offre.setExperienceRequise(defaultValue(request.getExperienceRequise(), "Non precisee"));
        offre.setStatut(defaultValue(request.getStatut(), "PUBLIEE"));
        offre.setDateExpiration(parseDate(request.getDateExpiration()));
        offre.setCompetencesJson(writeCompetencesJson(request.getCompetences()));
    }

    private OffreResponse toResponse(Offre offre, Candidate candidate) {
        OffreResponse response = new OffreResponse();
        response.setId(offre.getId());
        response.setTitre(offre.getTitre());
        response.setCategorie(offre.getCategorie());
        response.setDescription(offre.getDescription());
        response.setLocalisation(offre.getLocalisation());
        response.setSalaire(offre.getSalaire());
        response.setDevise(offre.getDevise());
        response.setNombrePostes(offre.getNombrePostes());
        response.setExperienceRequise(offre.getExperienceRequise());
        response.setTypeContrat(offre.getTypeContrat());
        response.setStatut(offre.getStatut());
        response.setDatePublication(formatDate(offre.getDate()));
        response.setDateExpiration(formatDate(offre.getDateExpiration()));
        response.setCompetences(readCompetencesJson(offre.getCompetencesJson()));

        if (offre.getRecruiter() != null) {
            response.setRecruiterId(offre.getRecruiter().getId());
            if (offre.getRecruiter().getEntreprise() != null) {
                response.setNomEntreprise(offre.getRecruiter().getEntreprise().getNomEntreprise());
            } else {
                response.setNomEntreprise(offre.getRecruiter().getNom());
            }
        }

        if (candidate != null) {
            response.setCompatibilityScore(matchingService.evaluate(candidate, offre).getScore());
            response.setAlreadyApplied(candidatureRepository.findByCandidate_IdAndOffre_Id(candidate.getId(), offre.getId()).isPresent());
            response.setApplicationStatus(candidatureRepository.findByCandidate_IdAndOffre_Id(candidate.getId(), offre.getId())
                    .map(candidature -> candidature.getStatut())
                    .orElse(null));
        } else {
            response.setCompatibilityScore(null);
            response.setAlreadyApplied(Boolean.FALSE);
            response.setApplicationStatus(null);
        }

        return response;
    }

    private Recruiter getCurrentRecruiter(User currentUser) {
        Recruiter recruiter = currentUser != null && currentUser.getId() != null
                ? recruiterRepository.findById(currentUser.getId()).orElse(null)
                : null;

        if (recruiter == null && currentUser != null) {
            recruiter = recruiterRepository.findByEmail(currentUser.getEmail());
        }

        if (recruiter == null) {
            recruiter = createMissingRecruiterProfile(currentUser);
        }

        if (recruiter == null) {
            throw new RuntimeException("Profil recruteur introuvable.");
        }
        return recruiter;
    }

    private Candidate resolveCandidate(User currentUser) {
        if (currentUser == null || currentUser.getRole() == null || !"CANDIDATE".equals(currentUser.getRole().name())) {
            return null;
        }

        if (currentUser.getId() != null) {
            Candidate candidate = candidateRepository.findById(currentUser.getId()).orElse(null);
            if (candidate != null) {
                return candidate;
            }
        }

        return candidateRepository.findByEmail(currentUser.getEmail());
    }

    private boolean isOfferActive(Offre offre) {
        String status = normalize(offre.getStatut()).toUpperCase(Locale.ROOT);
        if ("BROUILLON".equals(status) || "ARCHIVEE".equals(status) || "FERMEE".equals(status)) {
            return false;
        }

        if (offre.getDateExpiration() == null) {
            return true;
        }

        LocalDate expiration = toLocalDate(offre.getDateExpiration());

        return !expiration.isBefore(LocalDate.now());
    }

    private String writeCompetencesJson(List<OffreCompetenceRequest> competences) {
        List<OffreCompetenceRequest> safeList = competences == null ? new ArrayList<>() : competences.stream()
                .filter(item -> item != null && !normalize(item.getNom()).isEmpty())
                .peek(item -> {
                    item.setNom(normalize(item.getNom()));
                    item.setType(defaultValue(item.getType(), "OBLIGATOIRE"));
                    item.setNiveau(defaultValue(item.getNiveau(), "Intermediaire"));
                    item.setPonderation(item.getPonderation() == null ? 50 : item.getPonderation());
                })
                .collect(Collectors.toList());

        try {
            return objectMapper.writeValueAsString(safeList);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Impossible d'enregistrer les competences de l'offre.");
        }
    }

    private List<OffreCompetenceResponse> readCompetencesJson(String value) {
        if (value == null || value.isBlank()) {
            return new ArrayList<>();
        }

        try {
            List<OffreCompetenceRequest> items = objectMapper.readValue(value, new TypeReference<List<OffreCompetenceRequest>>() {
            });

            return items.stream().map(item -> {
                OffreCompetenceResponse response = new OffreCompetenceResponse();
                response.setCompetenceId(item.getCompetenceId());
                response.setNom(item.getNom());
                response.setType(item.getType());
                response.setPonderation(item.getPonderation());
                response.setNiveau(item.getNiveau());
                return response;
            }).collect(Collectors.toList());
        } catch (JsonProcessingException ex) {
            return new ArrayList<>();
        }
    }

    private Date parseDate(String value) {
        String normalized = normalize(value);
        if (normalized.isEmpty()) {
            return null;
        }

        try {
            LocalDate localDate = LocalDate.parse(normalized);
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException ex) {
            throw new RuntimeException("La date d'expiration est invalide.");
        }
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

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String requireValue(String value, String message) {
        String normalized = normalize(value);
        if (normalized.isEmpty()) {
            throw new RuntimeException(message);
        }
        return normalized;
    }

    private String defaultValue(String value, String fallback) {
        String normalized = normalize(value);
        return normalized.isEmpty() ? fallback : normalized;
    }

    private Recruiter createMissingRecruiterProfile(User authenticatedUser) {
        if (authenticatedUser == null) {
            return null;
        }

        if (authenticatedUser.getRole() != Role.RECRUITER) {
            throw new RuntimeException("Seuls les comptes recruteurs peuvent publier une offre.");
        }

        Recruiter recruiter = new Recruiter();
        recruiter.setId(authenticatedUser.getId());
        recruiter.setEmail(authenticatedUser.getEmail());
        recruiter.setNom(authenticatedUser.getNom());
        recruiter.setPassword(authenticatedUser.getPassword());
        recruiter.setRole(authenticatedUser.getRole());
        recruiter.setStatutCompte(authenticatedUser.getStatutCompte());
        recruiter.setApprovalStatus(authenticatedUser.getApprovalStatus());
        recruiter.setEmailVerified(authenticatedUser.getEmailverified());
        recruiter.setActivationToken(authenticatedUser.getActivationToken());
        recruiter.setResetPasswordToken(authenticatedUser.getResetPasswordToken());
        recruiter.setResetPasswordTokenExpiresAt(authenticatedUser.getResetPasswordTokenExpiresAt());
        recruiter.setFonction("");
        recruiter.setPoste("");
        recruiter.setDepartement("");
        return recruiterRepository.save(recruiter);
    }
}
