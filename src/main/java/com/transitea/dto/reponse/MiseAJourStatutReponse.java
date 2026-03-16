package com.transitea.dto.reponse;

import com.transitea.entite.enumeration.StatutColis;

import java.time.LocalDateTime;

public record MiseAJourStatutReponse(
        Long id,
        StatutColis statut,
        StatutColis ancienStatut,
        String localisation,
        String commentaire,
        Long utilisateurId,
        LocalDateTime dateCreation
) {
}
