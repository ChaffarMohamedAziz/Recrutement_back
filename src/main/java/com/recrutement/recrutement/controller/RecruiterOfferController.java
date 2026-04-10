package com.recrutement.recrutement.controller;

import com.recrutement.recrutement.dto.OffreRequest;
import com.recrutement.recrutement.dto.OffreResponse;
import com.recrutement.recrutement.entities.User;
import com.recrutement.recrutement.service.OfferService;
import java.util.Map;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recruiter/offres")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RecruiterOfferController {
    private final OfferService offerService;

    @GetMapping
    public ResponseEntity<List<OffreResponse>> getMyOffers(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(offerService.getRecruiterOffers(currentUser));
    }

    @PostMapping
    public ResponseEntity<OffreResponse> createOffer(
            Authentication authentication,
            @RequestBody OffreRequest request
    ) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(offerService.createOffer(currentUser, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OffreResponse> updateOffer(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody OffreRequest request
    ) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(offerService.updateOffer(currentUser, id, request));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRecruiterOfferRuntimeException(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "message",
                ex.getMessage() == null || ex.getMessage().isBlank()
                        ? "Publication de l'offre impossible."
                        : ex.getMessage()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleRecruiterOfferException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message",
                "Une erreur technique est survenue lors de la publication de l'offre."
        ));
    }
}
