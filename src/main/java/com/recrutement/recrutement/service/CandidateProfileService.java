package com.recrutement.recrutement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recrutement.recrutement.dto.CandidateProfileRequest;
import com.recrutement.recrutement.dto.CandidateProfileResponse;
import com.recrutement.recrutement.entities.Candidate;
import com.recrutement.recrutement.entities.CV;
import com.recrutement.recrutement.entities.Role;
import com.recrutement.recrutement.entities.User;
import com.recrutement.recrutement.repositories.CVRepository;
import com.recrutement.recrutement.repositories.CandidateRepository;
import com.recrutement.recrutement.repositories.UserRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CandidateProfileService {
    private final CandidateRepository candidateRepository;
    private final CVRepository cvRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public CandidateProfileService(
            CandidateRepository candidateRepository,
            CVRepository cvRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper
    ) {
        this.candidateRepository = candidateRepository;
        this.cvRepository = cvRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public CandidateProfileResponse getCurrentProfile(User user) {
        Candidate candidate = getCurrentCandidate(user);
        return buildResponse(candidate);
    }

    public CandidateProfileResponse saveCurrentProfile(
            User user,
            CandidateProfileRequest request,
            MultipartFile profilePhoto,
            MultipartFile coverPhoto,
            MultipartFile cvFile
    ) {
        Candidate candidate = getCurrentCandidate(user);

        if (request != null) {
            candidate.setNom(nonEmpty(request.getFullName(), candidate.getNom()));
            candidate.setEmail(resolveEmail(candidate, request.getEmail()));
            candidate.setProfession(clean(request.getProfession()));
            candidate.setDateNaissance(parseDate(request.getBirthDate()));
            candidate.setNumTelephone(clean(request.getPhone()));
            candidate.setPosteRecherche(clean(request.getJobTitle()));
            candidate.setLocalisation(clean(request.getAddress()));
            candidate.setAdresse(clean(request.getAddress()));
            candidate.setGenre(clean(request.getGender()));
            candidate.setDescription(clean(request.getDescription()));

            if (request.getSocialLinks() != null) {
                candidate.setFacebookUrl(clean(request.getSocialLinks().getFacebook()));
                candidate.setInstagramUrl(clean(request.getSocialLinks().getInstagram()));
                candidate.setLinkedinUrl(clean(request.getSocialLinks().getLinkedin()));
                candidate.setGithubUrl(clean(request.getSocialLinks().getGithub()));
            }

            candidate.setEducationJson(writeEducationJson(request.getEducation()));
            candidate.setSkillsJson(writeSkillsJson(request.getSkills()));
        }

        if (hasFile(profilePhoto)) {
            candidate.setPhotoProfilNom(storeFile(candidate.getId(), profilePhoto, "profil"));
        }

        if (hasFile(coverPhoto)) {
            candidate.setPhotoCouvertureNom(storeFile(candidate.getId(), coverPhoto, "couverture"));
        }

        Candidate savedCandidate = candidateRepository.save(candidate);

        if (hasFile(cvFile)) {
            String storedCvName = storeFile(savedCandidate.getId(), cvFile, "cv");
            CV cv = cvRepository.findTopByCandidateOrderByDateImportDesc(savedCandidate).orElse(new CV());
            cv.setCandidate(savedCandidate);
            cv.setDateImport(new Date());
            cv.setNomFichier(storedCvName);
            cv.setTaille(formatFileSize(cvFile.getSize()));
            cvRepository.save(cv);
        }

        return buildResponse(savedCandidate);
    }

    private Candidate getCurrentCandidate(User user) {
        Candidate candidate = user.getId() == null
                ? null
                : candidateRepository.findById(user.getId()).orElse(null);

        if (candidate == null && user.getEmail() != null) {
            candidate = candidateRepository.findByEmail(user.getEmail());
        }

        if (candidate == null) {
            candidate = createMissingCandidateProfile(user);
        }

        if (candidate == null) {
            throw new RuntimeException("Profil candidat introuvable.");
        }
        return candidate;
    }

    private CandidateProfileResponse buildResponse(Candidate candidate) {
        CandidateProfileResponse response = new CandidateProfileResponse();
        response.setId(candidate.getId());
        response.setFullName(candidate.getNom());
        response.setProfession(candidate.getProfession());
        response.setEmail(candidate.getEmail());
        response.setBirthDate(candidate.getDateNaissance() == null ? "" : candidate.getDateNaissance().toString());
        response.setPhone(candidate.getNumTelephone());
        response.setJobTitle(candidate.getPosteRecherche());
        response.setAddress(candidate.getAdresse());
        response.setGender(candidate.getGenre());
        response.setDescription(candidate.getDescription());
        response.setEducationJson(defaultJson(candidate.getEducationJson(), "[]"));
        response.setSkillsJson(defaultJson(candidate.getSkillsJson(), "[]"));
        response.setFacebook(candidate.getFacebookUrl());
        response.setInstagram(candidate.getInstagramUrl());
        response.setLinkedin(candidate.getLinkedinUrl());
        response.setGithub(candidate.getGithubUrl());
        response.setProfilePhotoName(candidate.getPhotoProfilNom());
        response.setProfilePhotoUrl(buildImageDataUrl(candidate.getId(), candidate.getPhotoProfilNom()));
        response.setCoverPhotoName(candidate.getPhotoCouvertureNom());
        response.setCoverPhotoUrl(buildImageDataUrl(candidate.getId(), candidate.getPhotoCouvertureNom()));

        cvRepository.findTopByCandidateOrderByDateImportDesc(candidate).ifPresent(cv -> {
            response.setCvFileName(cv.getNomFichier());
            response.setCvFileSize(cv.getTaille());
        });

        return response;
    }

    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private String storeFile(Long candidateId, MultipartFile file, String prefix) {
        try {
            Path uploadDir = Paths.get("uploads", "candidates", String.valueOf(candidateId));
            Files.createDirectories(uploadDir);
            String originalName = file.getOriginalFilename() == null ? "fichier" : file.getOriginalFilename();
            String safeName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
            String storedName = prefix + "_" + System.currentTimeMillis() + "_" + safeName;
            Path targetPath = uploadDir.resolve(storedName);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return storedName;
        } catch (IOException ex) {
            throw new RuntimeException("Impossible d'enregistrer le fichier " + file.getOriginalFilename() + ".");
        }
    }

    private String writeEducationJson(List<CandidateProfileRequest.CandidateEducationRequest> education) {
        if (education == null || education.isEmpty()) {
            return "[]";
        }

        try {
            return objectMapper.writeValueAsString(education);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Impossible d'enregistrer la formation du candidat.");
        }
    }

    private String writeSkillsJson(List<CandidateProfileRequest.CandidateSkillRequest> skills) {
        if (skills == null || skills.isEmpty()) {
            return "[]";
        }

        try {
            return objectMapper.writeValueAsString(skills);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Impossible d'enregistrer les competences du candidat.");
        }
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String nonEmpty(String value, String fallback) {
        String cleaned = clean(value);
        return cleaned.isEmpty() ? fallback : cleaned;
    }

    private String resolveEmail(Candidate candidate, String value) {
        String cleaned = clean(value).toLowerCase();
        if (cleaned.isEmpty()) {
            return candidate.getEmail();
        }

        if (candidate.getEmail() != null && candidate.getEmail().equalsIgnoreCase(cleaned)) {
            return candidate.getEmail();
        }

        Optional<User> existingUser = userRepository.findByEmail(cleaned);
        if (existingUser.isPresent() && !existingUser.get().getId().equals(candidate.getId())) {
            throw new RuntimeException("Cette adresse e-mail est deja utilisee.");
        }

        return cleaned;
    }

    private LocalDate parseDate(String value) {
        String cleaned = clean(value);
        if (cleaned.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(cleaned);
        } catch (DateTimeParseException ex) {
            throw new RuntimeException("La date de naissance est invalide.");
        }
    }

    private String defaultJson(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private String formatFileSize(long sizeInBytes) {
        if (sizeInBytes < 1024) {
            return sizeInBytes + " o";
        }
        if (sizeInBytes < 1024 * 1024) {
            return (sizeInBytes / 1024) + " Ko";
        }
        return String.format("%.1f Mo", sizeInBytes / (1024d * 1024d));
    }

    private String buildImageDataUrl(Long candidateId, String fileName) {
        if (candidateId == null || fileName == null || fileName.isBlank()) {
            return "";
        }

        Path filePath = Paths.get("uploads", "candidates", String.valueOf(candidateId), fileName);
        if (!Files.exists(filePath)) {
            return "";
        }

        try {
            String contentType = Files.probeContentType(filePath);
            if (contentType == null || !contentType.startsWith("image/")) {
                contentType = guessImageContentType(fileName);
            }

            byte[] bytes = Files.readAllBytes(filePath);
            return "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (IOException ex) {
            return "";
        }
    }

    private String guessImageContentType(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        if (lowerCaseName.endsWith(".png")) {
            return "image/png";
        }
        if (lowerCaseName.endsWith(".gif")) {
            return "image/gif";
        }
        if (lowerCaseName.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg";
    }

    private Candidate createMissingCandidateProfile(User authenticatedUser) {
        Long userId = authenticatedUser.getId();
        String userEmail = authenticatedUser.getEmail();

        User persistedUser = null;
        if (userId != null) {
            persistedUser = userRepository.findById(userId).orElse(null);
        }

        if (persistedUser == null && userEmail != null && !userEmail.isBlank()) {
            persistedUser = userRepository.findByEmail(userEmail).orElse(null);
        }

        if (persistedUser == null) {
            return null;
        }

        if (persistedUser.getRole() != Role.CANDIDATE) {
            throw new RuntimeException("Seuls les comptes candidats peuvent enregistrer ce profil.");
        }

        Candidate candidate = new Candidate();
        candidate.setId(persistedUser.getId());
        candidate.setEmail(persistedUser.getEmail());
        candidate.setNom(persistedUser.getNom());
        candidate.setPassword(persistedUser.getPassword());
        candidate.setRole(persistedUser.getRole());
        candidate.setStatutCompte(persistedUser.getStatutCompte());
        candidate.setApprovalStatus(persistedUser.getApprovalStatus());
        candidate.setEmailVerified(persistedUser.getEmailverified());
        candidate.setActivationToken(persistedUser.getActivationToken());
        candidate.setResetPasswordToken(persistedUser.getResetPasswordToken());
        candidate.setResetPasswordTokenExpiresAt(persistedUser.getResetPasswordTokenExpiresAt());
        candidate.setNumTelephone("");
        candidate.setPosteRecherche("");
        candidate.setLocalisation("");
        candidate.setAdresse("");
        candidate.setGenre("");
        candidate.setDescription("");
        candidate.setProfession("");
        candidate.setEducationJson("[]");
        candidate.setSkillsJson("[]");
        return candidateRepository.save(candidate);
    }
}
