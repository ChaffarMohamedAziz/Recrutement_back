package com.recrutement.recrutement.entities;


import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @Temporal(TemporalType.DATE)
    private Date date;

    @Temporal(TemporalType.DATE)
    private Date dateExpiration;

    private String categorie;
    private String description;
    private String localisation;
    private double salaire;
    private String devise;
    private Integer nombrePostes;
    private String experienceRequise;
    private String typeContrat;
    private String statut;

    @Column(columnDefinition = "TEXT")
    private String competencesJson;

    @ManyToOne
    @JoinColumn(name = "recruiter_id")
    private Recruiter recruiter;
}
