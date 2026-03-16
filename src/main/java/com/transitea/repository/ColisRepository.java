package com.transitea.repository;

import com.transitea.entite.Colis;
import com.transitea.entite.Utilisateur;
import com.transitea.entite.enumeration.StatutColis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ColisRepository extends JpaRepository<Colis, Long> {

    Optional<Colis> findByCodeTrackingAndSupprimefalse(String codeTracking);

    Optional<Colis> findByUuidAndSupprimefalse(String uuid);

    Page<Colis> findByTransporteurAndSupprimefalse(Utilisateur transporteur, Pageable pageable);

    Page<Colis> findByTransporteurAndStatutActuelAndSupprimefalse(
            Utilisateur transporteur, StatutColis statut, Pageable pageable);

    @Query("SELECT c FROM Colis c WHERE c.transporteur = :transporteur " +
           "AND c.supprime = false " +
           "AND (LOWER(c.destinataireNom) LIKE LOWER(CONCAT('%', :recherche, '%')) " +
           "OR LOWER(c.codeTracking) LIKE LOWER(CONCAT('%', :recherche, '%')))")
    Page<Colis> rechercherParTransporteur(
            @Param("transporteur") Utilisateur transporteur,
            @Param("recherche") String recherche,
            Pageable pageable);

    List<Colis> findByTransporteurAndDateCreationBetweenAndSupprimefalse(
            Utilisateur transporteur, LocalDateTime debut, LocalDateTime fin);

    long countByTransporteurAndStatutActuelAndSupprimefalse(
            Utilisateur transporteur, StatutColis statut);

    @Query("SELECT c FROM Colis c WHERE c.supprime = false " +
           "AND c.dateCreation < :dateLimite")
    List<Colis> trouverColisAArchiver(@Param("dateLimite") LocalDateTime dateLimite);
}
