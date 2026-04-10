package com.recrutement.recrutement.controller;

import com.recrutement.recrutement.dto.CandidateProfileRequest;
import com.recrutement.recrutement.dto.CandidateProfileResponse;
import com.recrutement.recrutement.entities.User;
import com.recrutement.recrutement.service.CandidateProfileService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/candidate/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CandidateProfileController {
    private final CandidateProfileService candidateProfileService;

    @GetMapping
    public ResponseEntity<CandidateProfileResponse> getCurrentProfile(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(candidateProfileService.getCurrentProfile(currentUser));
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CandidateProfileResponse> saveCurrentProfile(
            Authentication authentication,
            @RequestPart("profile") CandidateProfileRequest request,
            @RequestPart(value = "profilePhoto", required = false) MultipartFile profilePhoto,
            @RequestPart(value = "coverPhoto", required = false) MultipartFile coverPhoto,
            @RequestPart(value = "cvFile", required = false) MultipartFile cvFile
    ) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(
                candidateProfileService.saveCurrentProfile(currentUser, request, profilePhoto, coverPhoto, cvFile)
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleProfileRuntimeException(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "message",
                ex.getMessage() == null || ex.getMessage().isBlank()
                        ? "Enregistrement du profil candidat impossible."
                        : ex.getMessage()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleProfileException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message",
                "Une erreur technique est survenue lors de l'enregistrement du profil candidat."
        ));
    }
}
