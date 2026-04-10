package com.recrutement.recrutement.service;

import com.recrutement.recrutement.dto.CompetenceRequest;
import com.recrutement.recrutement.dto.CompetenceResponse;
import com.recrutement.recrutement.entities.Competence;
import com.recrutement.recrutement.repositories.CompetenceRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CompetenceService {
    private final CompetenceRepository competenceRepository;

    public CompetenceService(CompetenceRepository competenceRepository) {
        this.competenceRepository = competenceRepository;
    }

    public List<CompetenceResponse> getCompetences(String query, String type) {
        String normalizedQuery = normalize(query);
        String normalizedType = normalize(type);

        List<Competence> items;
        if (!normalizedQuery.isEmpty() && !normalizedType.isEmpty()) {
            items = competenceRepository.findByNomContainingIgnoreCaseAndTypeIgnoreCaseOrderByNomAsc(normalizedQuery, normalizedType);
        } else if (!normalizedQuery.isEmpty()) {
            items = competenceRepository.findByNomContainingIgnoreCaseOrderByTypeAscNomAsc(normalizedQuery);
        } else if (!normalizedType.isEmpty()) {
            items = competenceRepository.findByTypeIgnoreCaseOrderByNomAsc(normalizedType);
        } else {
            items = competenceRepository.findAllByOrderByTypeAscNomAsc();
        }

        return items.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public CompetenceResponse createCompetence(CompetenceRequest request) {
        String nom = normalizeRequired(request.getNom(), "Le nom de la competence est obligatoire.");
        String type = normalizeRequired(request.getType(), "La categorie de la competence est obligatoire.");

        if (competenceRepository.existsByNomIgnoreCase(nom)) {
            throw new RuntimeException("Une competence avec ce nom existe deja.");
        }

        Competence competence = Competence.builder()
                .nom(nom)
                .type(type)
                .description(normalize(request.getDescription()))
                .build();

        return toResponse(competenceRepository.save(competence));
    }

    public CompetenceResponse updateCompetence(Long id, CompetenceRequest request) {
        Competence competence = competenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Competence introuvable."));

        String nom = normalizeRequired(request.getNom(), "Le nom de la competence est obligatoire.");
        String type = normalizeRequired(request.getType(), "La categorie de la competence est obligatoire.");

        if (competenceRepository.existsByNomIgnoreCaseAndIdNot(nom, id)) {
            throw new RuntimeException("Une competence avec ce nom existe deja.");
        }

        competence.setNom(nom);
        competence.setType(type);
        competence.setDescription(normalize(request.getDescription()));

        return toResponse(competenceRepository.save(competence));
    }

    public void deleteCompetence(Long id) {
        Competence competence = competenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Competence introuvable."));

        competenceRepository.delete(competence);
    }

    private CompetenceResponse toResponse(Competence competence) {
        CompetenceResponse response = new CompetenceResponse();
        response.setId(competence.getId());
        response.setNom(competence.getNom());
        response.setType(competence.getType());
        response.setDescription(competence.getDescription());
        return response;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeRequired(String value, String errorMessage) {
        String normalized = normalize(value);
        if (normalized.isEmpty()) {
            throw new RuntimeException(errorMessage);
        }
        return normalized;
    }
}
