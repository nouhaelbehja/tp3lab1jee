package com.example.model;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Entity
@Table(name = "equipements")
public class Equipement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String nom;
    private String marque;
    private String description;
    @ManyToMany(mappedBy = "equipements")
    private Set<Salle> salles = new HashSet<>();

    protected Equipement() { }
    public Equipement(String nom, String marque, String description) {
        this.nom = Objects.requireNonNull(nom);
        this.marque = marque;
        this.description = description;
    }
    void ajouterSalle(Salle salle) { salles.add(salle); }
    void retirerSalle(Salle salle) { salles.remove(salle); }
    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getMarque() { return marque; }
    public String getDescription() { return description; }
    public Set<Salle> getSalles() { return Collections.unmodifiableSet(salles); }
}
