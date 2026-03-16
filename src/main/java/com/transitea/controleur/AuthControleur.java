package com.transitea.controleur;

import com.transitea.dto.requete.ConnexionRequete;
import com.transitea.dto.requete.InscriptionRequete;
import com.transitea.dto.requete.RafraichissementRequete;
import com.transitea.dto.reponse.AuthReponse;
import com.transitea.dto.reponse.UtilisateurReponse;
import com.transitea.entite.Utilisateur;
import com.transitea.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
public class AuthControleur {

    private final AuthService authService;

    public AuthControleur(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthReponse> inscrire(
            @Valid @RequestBody InscriptionRequete requete) {

        AuthReponse reponse = authService.inscrire(requete);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthReponse> connecter(
            @Valid @RequestBody ConnexionRequete requete) {

        AuthReponse reponse = authService.connecter(requete);
        return ResponseEntity.ok(reponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthReponse> rafraichir(
            @Valid @RequestBody RafraichissementRequete requete) {

        AuthReponse reponse = authService.rafraichir(requete);
        return ResponseEntity.ok(reponse);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deconnecter(@Valid @RequestBody RafraichissementRequete requete) {
        authService.deconnecter(requete.refreshToken());
    }

    @GetMapping("/me")
    public ResponseEntity<UtilisateurReponse> obtenirProfil(
            @AuthenticationPrincipal Utilisateur utilisateurConnecte) {

        UtilisateurReponse reponse = authService.obtenirProfil(utilisateurConnecte);
        return ResponseEntity.ok(reponse);
    }
}
