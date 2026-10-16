package com.example;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.service.SalleService;
import com.example.service.SalleServiceImpl;
import com.example.util.PaginationResult;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.List;

public class App {

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println(" TP 5 : Requêtes avancées avec JPA - Salles, Multi-critères & Pagination  ");
        System.out.println("==========================================================================");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("SallePU");
        EntityManager em = emf.createEntityManager();

        try {
            // 1. Initialisation des données de test
            initData(em);

            // 2. Initialisation du service avec injection manuelle de l'EntityManager
            SalleService salleService = new SalleServiceImpl();
            ((SalleServiceImpl) salleService).setEntityManager(em);

            // ---------------------------------------------------------------------
            // TEST 1 : Salles disponibles par créneau horaire (Requête JPQL)
            // ---------------------------------------------------------------------
            System.out.println("\n--------------------------------------------------------------------------");
            System.out.println(" [TEST 1] Requête JPQL : Salles disponibles sur un créneau horaire");
            System.out.println("--------------------------------------------------------------------------");

            LocalDateTime debut = LocalDateTime.of(2024, 6, 1, 9, 0);
            LocalDateTime fin = LocalDateTime.of(2024, 6, 1, 11, 0);

            System.out.println("Créneau recherché : du " + debut + " au " + fin);
            List<Salle> sallesDisponibles = salleService.getAvailableRooms(debut, fin);

            System.out.println("Nombre de salles disponibles : " + sallesDisponibles.size());
            for (Salle salle : sallesDisponibles) {
                System.out.println("  ✓ " + salle.getNom() + " (Capacité : " + salle.getCapacite() + " personnes)");
            }

            // ---------------------------------------------------------------------
            // TEST 2 : Recherche multi-critères dynamique (API Criteria)
            // ---------------------------------------------------------------------
            System.out.println("\n--------------------------------------------------------------------------");
            System.out.println(" [TEST 2] Recherche multi-critères dynamique avec l'API Criteria");
            System.out.println("--------------------------------------------------------------------------");

            String motCleNom = "Salle";
            Integer capaciteMinimale = 25;
            System.out.println("Critères : nom contient '" + motCleNom + "', capacité minimale >= " + capaciteMinimale +
                               ", disponible entre " + debut + " et " + fin);

            List<Salle> resultatsCriteres = salleService.searchRooms(motCleNom, capaciteMinimale, debut, fin, 1, 10);
            System.out.println("Résultats de la recherche multi-critères :");
            for (Salle salle : resultatsCriteres) {
                System.out.println("  ✓ " + salle.getNom() + " - Capacité : " + salle.getCapacite());
            }

            // ---------------------------------------------------------------------
            // TEST 3 : Pagination côté serveur (setFirstResult & setMaxResults)
            // ---------------------------------------------------------------------
            System.out.println("\n--------------------------------------------------------------------------");
            System.out.println(" [TEST 3] Pagination côté serveur avec PaginationResult");
            System.out.println("--------------------------------------------------------------------------");

            int pageSize = 3;
            // Page 1
            PaginationResult<Salle> page1 = salleService.searchRoomsPaginated(null, null, null, null, 1, pageSize);
            System.out.println("Page " + page1.getCurrentPage() + "/" + page1.getTotalPages() +
                               " (Total éléments : " + page1.getTotalItems() + ", Taille page : " + page1.getPageSize() + ") :");
            for (Salle s : page1.getItems()) {
                System.out.println("  - [ID=" + s.getId() + "] " + s.getNom() + " (" + s.getCapacite() + " places)");
            }

            // Page 2
            PaginationResult<Salle> page2 = salleService.searchRoomsPaginated(null, null, null, null, 2, pageSize);
            System.out.println("\nPage " + page2.getCurrentPage() + "/" + page2.getTotalPages() +
                               " (Total éléments : " + page2.getTotalItems() + ", Taille page : " + page2.getPageSize() + ") :");
            for (Salle s : page2.getItems()) {
                System.out.println("  - [ID=" + s.getId() + "] " + s.getNom() + " (" + s.getCapacite() + " places)");
            }

            // Page 3
            PaginationResult<Salle> page3 = salleService.searchRoomsPaginated(null, null, null, null, 3, pageSize);
            System.out.println("\nPage " + page3.getCurrentPage() + "/" + page3.getTotalPages() +
                               " (Total éléments : " + page3.getTotalItems() + ", Taille page : " + page3.getPageSize() + ") :");
            for (Salle s : page3.getItems()) {
                System.out.println("  - [ID=" + s.getId() + "] " + s.getNom() + " (" + s.getCapacite() + " places)");
            }

            System.out.println("\n==========================================================================");
            System.out.println(" [SUCCÈS] Tous les tests du TP 5 ont été exécutés avec succès !           ");
            System.out.println("==========================================================================");

        } finally {
            em.close();
            emf.close();
        }
    }

    private static void initData(EntityManager em) {
        em.getTransaction().begin();

        // Création des salles
        Salle s1 = new Salle("Salle Java", 30);
        Salle s2 = new Salle("Salle Python", 50);
        Salle s3 = new Salle("Salle Spring", 20);
        Salle s4 = new Salle("Amphi Turing", 120);
        Salle s5 = new Salle("Salle Hibernate", 15);
        Salle s6 = new Salle("Salle Docker", 40);
        Salle s7 = new Salle("Salle Kubernetes", 25);

        em.persist(s1);
        em.persist(s2);
        em.persist(s3);
        em.persist(s4);
        em.persist(s5);
        em.persist(s6);
        em.persist(s7);

        // Création des réservations pour simuler les occupations
        // Réservation 1 : Salle Java réservée le 2024-06-01 de 09:00 à 11:00
        Reservation r1 = new Reservation(s1,
                LocalDateTime.of(2024, 6, 1, 9, 0),
                LocalDateTime.of(2024, 6, 1, 11, 0));

        // Réservation 2 : Amphi Turing réservé le 2024-06-01 de 10:00 à 12:00 (chevauchement partiel)
        Reservation r2 = new Reservation(s4,
                LocalDateTime.of(2024, 6, 1, 10, 0),
                LocalDateTime.of(2024, 6, 1, 12, 0));

        // Réservation 3 : Salle Python réservée l'après-midi de 14:00 à 16:00
        Reservation r3 = new Reservation(s2,
                LocalDateTime.of(2024, 6, 1, 14, 0),
                LocalDateTime.of(2024, 6, 1, 16, 0));

        em.persist(r1);
        em.persist(r2);
        em.persist(r3);

        em.getTransaction().commit();
        System.out.println("[DB] Données de test initialisées (7 Salles, 3 Réservations).");
    }
}
