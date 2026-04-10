package com.recrutement.recrutement.controller;

import com.recrutement.recrutement.dto.MessageResponse;
import com.recrutement.recrutement.dto.RegisterResponse;
import com.recrutement.recrutement.dto.UserProfileResponse;
import com.recrutement.recrutement.dto.UserSummaryResponse;
import com.recrutement.recrutement.service.AuthService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {
    private final AuthService authService;

    @GetMapping("/recruiters")
    public ResponseEntity<List<RegisterResponse>> getRecruiterAccounts() {
        return ResponseEntity.ok(authService.getRecruiterAccounts());
    }

    @PostMapping("/recruiters/{id}/approve")
    public ResponseEntity<RegisterResponse> approveRecruiter(@PathVariable Long id) {
        RegisterResponse response = authService.approveRecruiter(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/recruiters/{id}/reject")
    public ResponseEntity<MessageResponse> rejectRecruiter(@PathVariable Long id) {
        MessageResponse response = authService.rejectRecruiter(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @DeleteMapping("/recruiters/{id}")
    public ResponseEntity<MessageResponse> deleteRecruiter(@PathVariable Long id) {
        authService.deleteRecruiterAccount(id);
        return ResponseEntity.ok(new MessageResponse(true, "Recruteur supprime avec succes."));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserSummaryResponse>> getUsers(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(authService.getUsers(query));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(authService.getUserById(id));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(authService.deleteUser(id));
    }
}
