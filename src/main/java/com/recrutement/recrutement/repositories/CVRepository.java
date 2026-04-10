package com.recrutement.recrutement.repositories;

import com.recrutement.recrutement.entities.Candidate;
import com.recrutement.recrutement.entities.CV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CVRepository extends JpaRepository<CV, Long> {
    Optional<CV> findTopByCandidateOrderByDateImportDesc(Candidate candidate);
}
