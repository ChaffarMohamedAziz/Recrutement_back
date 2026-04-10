package com.recrutement.recrutement.controller;

import com.recrutement.recrutement.dto.ApplicationResponse;
import com.recrutement.recrutement.dto.UpdateApplicationStatusRequest;
import com.recrutement.recrutement.entities.User;
import com.recrutement.recrutement.service.ApplicationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recruiter/candidatures")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RecruiterApplicationController {
    private final ApplicationService applicationService;

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getRecruiterApplications(
            Authentication authentication,
            @RequestParam(required = false) Long offerId,
            @RequestParam(required = false) Double minScore
    ) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(applicationService.getRecruiterApplications(currentUser, offerId, minScore));
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<ApplicationResponse> getRecruiterApplicationById(
            Authentication authentication,
            @PathVariable Long applicationId
    ) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(applicationService.getRecruiterApplicationById(currentUser, applicationId));
    }

    @PutMapping("/{applicationId}/status")
    public ResponseEntity<ApplicationResponse> updateRecruiterApplicationStatus(
            Authentication authentication,
            @PathVariable Long applicationId,
            @RequestBody UpdateApplicationStatusRequest request
    ) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(applicationService.updateRecruiterApplicationStatus(currentUser, applicationId, request.getStatus()));
    }
}
