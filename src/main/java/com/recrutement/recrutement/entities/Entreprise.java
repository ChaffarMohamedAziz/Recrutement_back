package com.recrutement.recrutement.entities;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "entreprises")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Entreprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idEntreprise ;

    @Column(nullable = false)
    private String nomEntreprise;

    @Column(nullable = false)
    private String secteur;

    @Column(nullable = false)
    private String adresse;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String abonnementActif;

    @Column(nullable = false)
    private String description;

    private String siteWeb;
}
