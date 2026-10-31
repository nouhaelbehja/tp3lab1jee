package com.example;

import com.example.model.*;
import com.example.service.ReservationService;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.function.Consumer;

public class App {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("gestion-reservations-pu");
        try {
            executerScenario(emf);
        } finally {
            emf.close();
        }
    }

    private static void executerScenario(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            Utilisateur utilisateur = new Utilisateur("Nouha Exemple", "nouha@example.com",
                    "0600000000", "Informatique");
            Salle atlas = new Salle("Atlas", 30, "Innovation", 1, true);
            Salle oasis = new Salle("Oasis", 15, "Formation", 0, true);
            Equipement projecteur = new Equipement("Projecteur", "Epson", "Projection Full HD");
            atlas.addEquipement(projecteur);
            atlas.addEquipement(new Equipement("Tableau interactif", "Samsung", "Écran tactile"));
            oasis.addEquipement(projecteur);
            LocalDateTime debut = LocalDateTime.of(2026, 11, 10, 9, 0);
            Reservation premiere = new Reservation("Atelier JPA", debut, debut.plusHours(2), 20);
            premiere.setSalle(atlas);
            utilisateur.addReservation(premiere);

            System.out.println("\n=== 1. Création par cascades ===");
            transaction(em, manager -> {
                manager.persist(utilisateur); // utilisateur -> réservation -> salle -> équipements
                manager.persist(oasis); // réutilise le même équipement partagé
            });
            bilan(em);
            Long utilisateurId = utilisateur.getId();
            Long reservationId = premiere.getId();
            Long salleId = atlas.getId();
            em.clear();

            System.out.println("\n=== 2. Requêtes JPQL et disponibilité ===");
            ReservationService service = new ReservationService(em);
            Utilisateur user = em.find(Utilisateur.class, utilisateurId);
            Salle salle = em.find(Salle.class, salleId);
            service.reservationsDe(user).forEach(r -> System.out.println(r.getMotif() + " : " + r.getDateDebut()));
            service.sallesAvecEquipement("Projecteur").forEach(s -> System.out.println("Salle équipée : " + s.getNom()));
            System.out.println("Créneau occupé disponible : " + service.estDisponible(salle, debut, debut.plusHours(1)));
            System.out.println("Créneau suivant disponible : " + service.estDisponible(salle, debut.plusHours(2), debut.plusHours(3)));

            System.out.println("\n=== 3. Modification et nouvelle réservation ===");
            transaction(em, manager -> {
                user.setDepartement("Recherche et développement");
                manager.find(Reservation.class, reservationId).setStatut(StatutReservation.EN_ATTENTE);
                service.reserver(user, salle, "Réunion de projet", debut.plusHours(3), debut.plusHours(4), 10);
            });
            System.out.println("Département : " + user.getDepartement());
            System.out.println("Statut : " + em.find(Reservation.class, reservationId).getStatut());
            bilan(em);

            System.out.println("\n=== 4. Suppression orpheline ===");
            transaction(em, manager -> user.removeReservation(manager.find(Reservation.class, reservationId)));
            em.clear();
            System.out.println("Réservation supprimée : " + (em.find(Reservation.class, reservationId) == null));
            bilan(em);

            System.out.println("\n=== 5. Suppression de l'utilisateur et de ses réservations ===");
            transaction(em, manager -> manager.remove(manager.find(Utilisateur.class, utilisateurId)));
            em.clear();
            bilan(em);
            System.out.println("Les salles et les équipements partagés sont conservés.");
        } finally {
            em.close();
        }
    }

    private static void transaction(EntityManager em, Consumer<EntityManager> action) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            action.accept(em);
            tx.commit();
        } catch (RuntimeException exception) {
            if (tx.isActive()) { tx.rollback(); }
            System.err.println("Transaction annulée : " + exception.getMessage());
            throw exception;
        }
    }

    private static void bilan(EntityManager em) {
        for (String entite : new String[]{"Utilisateur", "Reservation", "Salle", "Equipement"}) {
            Long total = em.createQuery("select count(e) from " + entite + " e", Long.class).getSingleResult();
            System.out.println(entite + " : " + total);
        }
    }
}
