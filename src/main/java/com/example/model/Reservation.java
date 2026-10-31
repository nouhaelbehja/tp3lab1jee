package com.example.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String motif;
    @Column(nullable = false)
    private LocalDateTime dateDebut;
    @Column(nullable = false)
    private LocalDateTime dateFin;
    private int nombreParticipants;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private StatutReservation statut = StatutReservation.CONFIRMEE;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;
    // PERSIST propage la création, mais jamais la suppression d'une salle partagée.
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;

    protected Reservation() { }
    public Reservation(String motif, LocalDateTime debut, LocalDateTime fin, int participants) {
        this.motif = motif;
        this.dateDebut = debut;
        this.dateFin = fin;
        this.nombreParticipants = participants;
        valider();
    }
    @PrePersist @PreUpdate
    private void valider() {
        if (motif == null || motif.trim().isEmpty()) {
            throw new IllegalArgumentException("Le motif est obligatoire.");
        }
        if (dateDebut == null || dateFin == null || !dateDebut.isBefore(dateFin)) {
            throw new IllegalArgumentException("Le début doit précéder la fin.");
        }
        if (nombreParticipants <= 0 || (salle != null && nombreParticipants > salle.getCapacite())) {
            throw new IllegalArgumentException("Le nombre de participants dépasse la capacité ou est invalide.");
        }
    }
    void associerUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
    public void setSalle(Salle salle) {
        if (salle != null && nombreParticipants > salle.getCapacite()) {
            throw new IllegalArgumentException("Capacité de la salle insuffisante.");
        }
        if (this.salle != null) { this.salle.retirerReservation(this); }
        this.salle = salle;
        if (salle != null) { salle.ajouterReservation(this); }
    }
    public void setStatut(StatutReservation statut) {
        this.statut = java.util.Objects.requireNonNull(statut);
    }
    public Long getId() { return id; }
    public String getMotif() { return motif; }
    public LocalDateTime getDateDebut() { return dateDebut; }
    public LocalDateTime getDateFin() { return dateFin; }
    public int getNombreParticipants() { return nombreParticipants; }
    public StatutReservation getStatut() { return statut; }
    public Utilisateur getUtilisateur() { return utilisateur; }
    public Salle getSalle() { return salle; }
}
