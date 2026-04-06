package com.transitea.util;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class GenerateurCodeTrackingTest {

    @Test
    void generer_doitRespacterLeFormat_TRA_ANNEE_SUFFIXE() {
        String code = GenerateurCodeTracking.generer();

        assertThat(code).matches("TRA-\\d{4}-[A-Z0-9]{6}");
    }

    @Test
    void generer_doitContenir_LanneeEnCours() {
        String anneeAttendue = String.valueOf(LocalDate.now().getYear());

        String code = GenerateurCodeTracking.generer();

        assertThat(code).contains(anneeAttendue);
    }

    @Test
    void generer_doitProduire_UnCodeDe14Caracteres() {
        // TRA- (4) + YYYY (4) + - (1) + XXXXXX (6) = 15
        String code = GenerateurCodeTracking.generer();

        assertThat(code).hasSize(15);
    }

    @RepeatedTest(20)
    void generer_doitProduire_UnCodeUnique() {
        String code1 = GenerateurCodeTracking.generer();
        String code2 = GenerateurCodeTracking.generer();

        // Avec 36^6 = 2,1 milliards de combinaisons, la collision est quasi impossible
        assertThat(code1).isNotEqualTo(code2);
    }

    @Test
    void generer_doitUtiliser_UniquementDesCaracteresAutorises() {
        for (int i = 0; i < 100; i++) {
            String code = GenerateurCodeTracking.generer();
            String suffixe = code.substring(9); // apres "TRA-YYYY-"

            assertThat(suffixe).matches("[A-Z0-9]{6}");
        }
    }
}
