package com.recrutement.recrutement.repositories;

import com.recrutement.recrutement.entities.Offre;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OffreRepository extends JpaRepository<Offre, Long> {
    List<Offre> findAllByOrderByDateDesc();

    List<Offre> findByRecruiter_IdOrderByDateDesc(Long recruiterId);

    void deleteByRecruiter_Id(Long recruiterId);
}
