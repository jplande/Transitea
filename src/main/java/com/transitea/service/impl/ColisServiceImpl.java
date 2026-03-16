package com.transitea.service.impl;

import com.transitea.dto.requete.CreationColisRequete;
import com.transitea.dto.requete.MiseAJourStatutRequete;
import com.transitea.dto.reponse.ColisReponse;
import com.transitea.dto.reponse.ReponsePagee;
import com.transitea.entite.Colis;
import com.transitea.entite.MiseAJourStatut;
import com.transitea.entite.Utilisateur;
import com.transitea.entite.enumeration.Role;
import com.transitea.entite.enumeration.StatutColis;
import com.transitea.exception.AccesNonAutoriseException;
import com.transitea.exception.EntiteNonTrouveeException;
import com.transitea.mapper.ColisMapper;
import com.transitea.repository.ColisRepository;
import com.transitea.repository.MiseAJourStatutRepository;
import com.transitea.service.ColisService;
import com.transitea.utilitaire.GenerateurCodeTracking;
import com.transitea.utilitaire.ValidateurTransitionStatut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ColisServiceImpl implements ColisService {

    private static final Logger journal = LoggerFactory.getLogger(ColisServiceImpl.class);
    private static final int TENTATIVES_MAX_CODE_TRACKING = 5;

    private final ColisRepository colisRepository;
    private final MiseAJourStatutRepository miseAJourStatutRepository;
    private final ColisMapper colisMapper;

    public ColisServiceImpl(
            ColisRepository colisRepository,
            MiseAJourStatutRepository miseAJourStatutRepository,
            ColisMapper colisMapper) {
        this.colisRepository = colisRepository;
        this.miseAJourStatutRepository = miseAJourStatutRepository;
        this.colisMapper = colisMapper;
    }

    @Override
    public ColisReponse creer(CreationColisRequete requete, Utilisateur transporteur) {
        String codeTracking = genererCodeTrackingUnique();

        Colis colis = Colis.builder()
                .codeTracking(codeTracking)
                .transporteur(transporteur)
                .expediteurNom(requete.expediteurNom())
                .expediteurTelephone(requete.expediteurTelephone())
                .expediteurEmail(requete.expediteurEmail())
                .destinataireNom(requete.destinataireNom())
                .destinataireTelephone(requete.destinataireTelephone())
                .destinataireEmail(requete.destinataireEmail())
                .destinataireAdresse(requete.destinataireAdresse())
                .destinataireVille(requete.destinataireVille())
                .description(requete.description())
                .poids(requete.poids())
                .localId(requete.localId())
                .build();

        Colis colisSauvegarde = colisRepository.save(colis);

        enregistrerHistoriqueStatut(
                colisSauvegarde, null, StatutColis.ENREGISTRE, null, null, transporteur);

        journal.info("Colis cree avec le code : {}", codeTracking);
        return colisMapper.versReponse(colisSauvegarde);
    }

    @Override
    @Transactional(readOnly = true)
    public ReponsePagee<ColisReponse> lister(
            Utilisateur transporteur, StatutColis statut, Pageable pageable) {

        Page<Colis> page;

        if (statut != null) {
            page = colisRepository.findByTransporteurAndStatutActuelAndSupprimeFalse(
                    transporteur, statut, pageable);
        } else {
            page = colisRepository.findByTransporteurAndSupprimeFalse(transporteur, pageable);
        }

        Page<ColisReponse> pageReponse = page.map(colisMapper::versReponse);
        return ReponsePagee.depuis(pageReponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ColisReponse trouverParId(Long id, Utilisateur transporteur) {
        Colis colis = recupererColisOuEchouer(id);
        verifierAccesAuColis(colis, transporteur);

        List<MiseAJourStatut> historique =
                miseAJourStatutRepository.findByColisOrderByDateCreationAsc(colis);

        ColisReponse reponseBase = colisMapper.versReponse(colis);

        return new ColisReponse(
                reponseBase.id(),
                reponseBase.uuid(),
                reponseBase.codeTracking(),
                reponseBase.transporteurId(),
                reponseBase.transporteurNomComplet(),
                reponseBase.expediteurNom(),
                reponseBase.expediteurTelephone(),
                reponseBase.expediteurEmail(),
                reponseBase.destinataireNom(),
                reponseBase.destinataireTelephone(),
                reponseBase.destinataireEmail(),
                reponseBase.destinataireAdresse(),
                reponseBase.destinataireVille(),
                reponseBase.description(),
                reponseBase.poids(),
                reponseBase.statutActuel(),
                reponseBase.localId(),
                reponseBase.version(),
                reponseBase.dateCreation(),
                colisMapper.versMiseAJourReponses(historique)
        );
    }

    @Override
    public ColisReponse mettreAJourStatut(
            Long id, MiseAJourStatutRequete requete, Utilisateur utilisateur) {

        Colis colis = recupererColisOuEchouer(id);
        verifierAccesAuColis(colis, utilisateur);

        StatutColis ancienStatut = colis.getStatutActuel();
        ValidateurTransitionStatut.valider(ancienStatut, requete.statut());

        colis.setStatutActuel(requete.statut());
        Colis colusMisAJour = colisRepository.save(colis);

        enregistrerHistoriqueStatut(
                colusMisAJour,
                ancienStatut,
                requete.statut(),
                requete.localisation(),
                requete.commentaire(),
                utilisateur
        );

        journal.info("Statut du colis {} mis a jour : {} -> {}",
                colis.getCodeTracking(), ancienStatut, requete.statut());

        return colisMapper.versReponse(colusMisAJour);
    }

    @Override
    public void supprimer(Long id, Utilisateur transporteur) {
        Colis colis = recupererColisOuEchouer(id);
        verifierAccesAuColis(colis, transporteur);

        colis.setSupprime(true);
        colisRepository.save(colis);

        journal.info("Colis {} supprime (soft delete)", colis.getCodeTracking());
    }

    private Colis recupererColisOuEchouer(Long id) {
        return colisRepository.findById(id)
                .filter(c -> !c.getSupprime())
                .orElseThrow(() -> new EntiteNonTrouveeException("Colis", id));
    }

    private void verifierAccesAuColis(Colis colis, Utilisateur utilisateur) {
        boolean estAdmin = utilisateur.getRole() == Role.ADMIN;
        boolean estProprietaire = colis.getTransporteur().getId().equals(utilisateur.getId());

        if (!estAdmin && !estProprietaire) {
            throw new AccesNonAutoriseException();
        }
    }

    private void enregistrerHistoriqueStatut(
            Colis colis,
            StatutColis ancienStatut,
            StatutColis nouveauStatut,
            String localisation,
            String commentaire,
            Utilisateur utilisateur) {

        MiseAJourStatut historique = MiseAJourStatut.builder()
                .colis(colis)
                .ancienStatut(ancienStatut)
                .statut(nouveauStatut)
                .localisation(localisation)
                .commentaire(commentaire)
                .utilisateur(utilisateur)
                .build();

        miseAJourStatutRepository.save(historique);
    }

    private String genererCodeTrackingUnique() {
        for (int tentative = 0; tentative < TENTATIVES_MAX_CODE_TRACKING; tentative++) {
            String code = GenerateurCodeTracking.generer();
            if (colisRepository.findByCodeTrackingAndSupprimeFalse(code).isEmpty()) {
                return code;
            }
        }
        throw new IllegalStateException(
                "Impossible de generer un code tracking unique apres "
                + TENTATIVES_MAX_CODE_TRACKING + " tentatives");
    }
}
