package com.transitea.service;

import com.transitea.dto.requete.CreationColisRequete;
import com.transitea.dto.requete.MiseAJourStatutRequete;
import com.transitea.dto.reponse.ColisReponse;
import com.transitea.dto.reponse.ReponsePagee;
import com.transitea.entite.Utilisateur;
import com.transitea.entite.enumeration.StatutColis;
import org.springframework.data.domain.Pageable;

public interface ColisService {

    ColisReponse creer(CreationColisRequete requete, Utilisateur transporteur);

    ReponsePagee<ColisReponse> lister(Utilisateur transporteur, StatutColis statut, Pageable pageable);

    ColisReponse trouverParId(Long id, Utilisateur transporteur);

    ColisReponse mettreAJourStatut(Long id, MiseAJourStatutRequete requete, Utilisateur utilisateur);

    void supprimer(Long id, Utilisateur transporteur);
}
