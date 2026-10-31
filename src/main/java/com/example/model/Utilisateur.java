package com.example.model;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Entity
@Table(name = "utilisateurs")
public class Utilisateur {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String nom;
    private String telephone;
    private String departement;
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Reservation> reservations = new HashSet<>();

    protected Utilisateur() { }

    public Utilisateur(String nom, String email, String telephone, String departement) {
        this.nom = Objects.requireNonNull(nom);
        this.email = Objects.requireNonNull(email);
        this.telephone = telephone;
        this.departement = departement;
    }

    public void addReservation(Reservation reservation) {
        Objects.requireNonNull(reservation);
        if (reservation.getUtilisateur() != null && reservation.getUtilisateur() != this) {
            throw new IllegalArgumentException("Une réservation ne peut pas changer de propriétaire.");
        }
        reservations.add(reservation);
        reservation.associerUtilisateur(this);
    }

    public void removeReservation(Reservation reservation) {
        if (reservations.remove(reservation)) {
            reservation.associerUtilisateur(null);
            reservation.setSalle(null);
        }
    }

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getEmail() { return email; }
    public String getTelephone() { return telephone; }
    public String getDepartement() { return departement; }
    public Set<Reservation> getReservations() { return Collections.unmodifiableSet(reservations); }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setDepartement(String departement) { this.departement = departement; }
}
