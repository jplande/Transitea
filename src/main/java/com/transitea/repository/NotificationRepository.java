package com.transitea.repository;

import com.transitea.entite.Colis;
import com.transitea.entite.Notification;
import com.transitea.entite.enumeration.StatutNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByColisAndStatut(Colis colis, StatutNotification statut);

    List<Notification> findByStatutAndNbTentativesLessThan(
            StatutNotification statut, int nbTentativesMax);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.dateCreation < :dateLimite")
    int supprimerNotificationsAnciennes(@Param("dateLimite") LocalDateTime dateLimite);
}
