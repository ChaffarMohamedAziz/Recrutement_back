package com.recrutement.recrutement.repositories;

import com.recrutement.recrutement.entities.Candidature;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, Long> {
    Optional<Candidature> findByCandidate_IdAndOffre_Id(Long candidateId, Long offreId);

    List<Candidature> findByCandidate_IdOrderByDateDepotDesc(Long candidateId);

    List<Candidature> findByOffre_Recruiter_IdOrderByScoreCandidatDescDateDepotDesc(Long recruiterId);

    List<Candidature> findByOffre_Recruiter_IdAndOffre_IdOrderByScoreCandidatDescDateDepotDesc(Long recruiterId, Long offreId);

    Optional<Candidature> findByIdAndOffre_Recruiter_Id(Long candidatureId, Long recruiterId);

    void deleteByOffre_Recruiter_Id(Long recruiterId);
}
