package com.transitea.dto.reponse;

import com.transitea.entite.enumeration.Role;
import com.transitea.entite.enumeration.StatutUtilisateur;

import java.time.LocalDateTime;

public record UtilisateurReponse(
        Long id,
        String uuid,
        String nom,
        String prenom,
        String email,
        String telephone,
        Role role,
        StatutUtilisateur statut,
        LocalDateTime dateCreation
) {
}
