package com.example.service;

import com.example.model.*;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

/** Les opérations d'écriture doivent être appelées dans une transaction active. */
public class ReservationService {
    private final EntityManager em;

    public ReservationService(EntityManager em) { this.em = em; }

    public boolean estDisponible(Salle salle, LocalDateTime debut, LocalDateTime fin) {
        if (debut == null || fin == null || !debut.isBefore(fin)) {
            throw new IllegalArgumentException("Période invalide.");
        }
        return em.createQuery("select count(r) from Reservation r where r.salle = :salle "
                + "and r.statut <> :annulee and r.dateDebut < :fin and r.dateFin > :debut", Long.class)
                .setParameter("salle", salle).setParameter("annulee", StatutReservation.ANNULEE)
                .setParameter("debut", debut).setParameter("fin", fin).getSingleResult() == 0;
    }

    public Reservation reserver(Utilisateur utilisateur, Salle salle, String motif,
                                 LocalDateTime debut, LocalDateTime fin, int participants) {
        if (utilisateur == null || salle == null) {
            throw new IllegalArgumentException("Utilisateur et salle obligatoires.");
        }
        if (!estDisponible(salle, debut, fin)) {
            throw new IllegalArgumentException("La salle est déjà réservée sur cette période.");
        }
        Reservation reservation = new Reservation(motif, debut, fin, participants);
        reservation.setSalle(salle);
        utilisateur.addReservation(reservation);
        em.persist(reservation);
        return reservation;
    }

    public List<Reservation> reservationsDe(Utilisateur utilisateur) {
        return em.createQuery("select r from Reservation r where r.utilisateur = :utilisateur "
                + "order by r.dateDebut", Reservation.class)
                .setParameter("utilisateur", utilisateur).getResultList();
    }

    public List<Salle> sallesAvecEquipement(String nom) {
        return em.createQuery("select distinct s from Salle s join s.equipements e "
                + "where lower(e.nom) = lower(:nom) order by s.nom", Salle.class)
                .setParameter("nom", nom).getResultList();
    }
}
