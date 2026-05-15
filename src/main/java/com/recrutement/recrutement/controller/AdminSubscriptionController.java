package com.recrutement.recrutement.controller;

import com.recrutement.recrutement.dto.AdminSubscriptionRequest;
import com.recrutement.recrutement.dto.AdminSubscriptionResponse;
import com.recrutement.recrutement.service.SubscriptionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/subscriptions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminSubscriptionController {
    private final SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<List<AdminSubscriptionResponse>> getSubscriptions(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(subscriptionService.getAdminSubscriptions(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminSubscriptionResponse> getSubscription(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.getAdminSubscription(id));
    }

    @PostMapping
    public ResponseEntity<AdminSubscriptionResponse> createSubscription(@RequestBody AdminSubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService.createAdminSubscription(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminSubscriptionResponse> updateSubscription(@PathVariable Long id, @RequestBody AdminSubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService.updateAdminSubscription(id, request));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<AdminSubscriptionResponse> activateSubscription(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.activateSubscription(id));
    }

    @PutMapping("/{id}/suspend")
    public ResponseEntity<AdminSubscriptionResponse> suspendSubscription(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.suspendSubscription(id));
    }

    @PutMapping("/{id}/renew")
    public ResponseEntity<AdminSubscriptionResponse> renewSubscription(@PathVariable Long id, @RequestBody(required = false) AdminSubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService.renewSubscription(id, request));
    }
}
