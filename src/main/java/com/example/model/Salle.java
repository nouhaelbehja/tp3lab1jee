package com.example.model;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Entity
@Table(name = "salles")
public class Salle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String nom;
    @Column(nullable = false)
    private int capacite;
    private String batiment;
    private int etage;
    private boolean accessiblePmr;
    @OneToMany(mappedBy = "salle")
    private Set<Reservation> reservations = new HashSet<>();
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "salle_equipement", joinColumns = @JoinColumn(name = "salle_id"),
            inverseJoinColumns = @JoinColumn(name = "equipement_id"))
    private Set<Equipement> equipements = new HashSet<>();

    protected Salle() { }
    public Salle(String nom, int capacite, String batiment, int etage, boolean accessiblePmr) {
        this.nom = Objects.requireNonNull(nom);
        setCapacite(capacite);
        this.batiment = batiment;
        this.etage = etage;
        this.accessiblePmr = accessiblePmr;
    }
    public void addEquipement(Equipement equipement) {
        equipements.add(Objects.requireNonNull(equipement));
        equipement.ajouterSalle(this);
    }
    public void removeEquipement(Equipement equipement) {
        if (equipements.remove(equipement)) { equipement.retirerSalle(this); }
    }
    void ajouterReservation(Reservation reservation) { reservations.add(reservation); }
    void retirerReservation(Reservation reservation) { reservations.remove(reservation); }
    public Long getId() { return id; }
    public String getNom() { return nom; }
    public int getCapacite() { return capacite; }
    public String getBatiment() { return batiment; }
    public int getEtage() { return etage; }
    public boolean isAccessiblePmr() { return accessiblePmr; }
    public Set<Equipement> getEquipements() { return Collections.unmodifiableSet(equipements); }
    public Set<Reservation> getReservations() { return Collections.unmodifiableSet(reservations); }
    public void setCapacite(int capacite) {
        if (capacite <= 0) { throw new IllegalArgumentException("La capacité doit être positive."); }
        this.capacite = capacite;
    }
}
