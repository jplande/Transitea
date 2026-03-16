package com.transitea.service;

import com.transitea.dto.requete.ConnexionRequete;
import com.transitea.dto.requete.InscriptionRequete;
import com.transitea.dto.requete.RafraichissementRequete;
import com.transitea.dto.reponse.AuthReponse;
import com.transitea.dto.reponse.UtilisateurReponse;
import com.transitea.entite.Utilisateur;

public interface AuthService {

    AuthReponse inscrire(InscriptionRequete requete);

    AuthReponse connecter(ConnexionRequete requete);

    AuthReponse rafraichir(RafraichissementRequete requete);

    void deconnecter(String refreshToken);

    UtilisateurReponse obtenirProfil(Utilisateur utilisateur);
}
