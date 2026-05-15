package com.recrutement.recrutement.repositories;

import com.recrutement.recrutement.entities.Subscription;
import com.recrutement.recrutement.entities.SubscriptionStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByRecruiter_Id(Long recruiterId);

    List<Subscription> findByStatusOrderByUpdatedAtDesc(SubscriptionStatus status);

    List<Subscription> findAllByOrderByUpdatedAtDesc();
}
