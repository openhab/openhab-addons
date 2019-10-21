/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.voice.opennlp.test;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.voice.text.Intent;
import org.junit.Test;
import org.openhab.voice.opennlp.internal.IntentTrainer;

public class TrainerFrTest extends AbstractTrainerTest {

    @Test
    public void testFR() throws Exception {
        Intent actual;
        this.trainer = new IntentTrainer("fr", skills, null, "alphanumeric");

        actual = interpret("montre le graphique de la consommation électrique pour les 2 derniers jours");
        assertEquals("get-history-daily", actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("2", actual.getEntities().get("period"));
        assertEquals("consommation électrique", actual.getEntities().get("object"));

        assertIsGetStatus("montre-moi la température du salon");
        assertIsGetStatus("Température du salon ?");
        assertIsGetStatus("lumières du couloir", "lumières", "couloir");
        assertIsGetStatus("lumières du salon", "lumières", "salon");
        assertIsGetStatus("lumière du salon", "lumière", "salon");
        assertIsGetStatus("lumière de la cuisine", "lumière", "cuisine");
        assertIsGetStatus("lumière de la chambre", "lumière", "chambre");
        assertIsGetStatus("lampes de la chambre", "lampes", "chambre");
        assertIsGetStatus("chauffage du couloir", "chauffage", "couloir");
        assertIsGetStatus("chauffage de la piscine", "chauffage", "piscine");
        assertIsGetStatus("lumières de la terrasse", "lumières", "terrasse");
        assertIsGetStatus("volets de la cuisine", "volets", "cuisine");
        assertIsGetStatus("fenêtre du séjour", "fenêtre", "séjour");
        assertIsGetStatus("lampes du couloir", "lampes", "couloir");
        assertIsGetStatus("chauffage dans la salle de bain", "chauffage", "salle de bain");
        assertIsGetStatus("lumières dans la salle de bain", "lumières", "salle de bain");
        assertIsGetStatus("lumières dans la salle de bain", "lumières", "salle de bain");
        assertIsGetStatus("ventilation dans la salle de bain", "ventilation", "salle de bain");
        assertIsGetStatus("température dans la chambre d'amis", "température", "chambre d amis");
        assertIsGetStatus("lumière dans la salle de jeux", "lumière", "salle de jeux");
        assertIsGetStatus("chauffage dans la buanderie", "chauffage", "buanderie");
        assertIsGetStatus("montre-moi l'état des lumières", "lumières", null);
        assertIsGetStatus("montre-moi l'état du thermostat", "thermostat", null);
        assertIsGetStatus("montre-moi l'état de la consommation électrique", "consommation électrique", null);
        assertIsGetStatus("montre-moi les lampes", "lampes", null);
        assertIsGetStatus("montre-moi le projecteur", "projecteur", null);
        assertIsGetStatus("montre-moi le lecteur dvd", "lecteur dvd", null);
        assertIsGetStatus("montre-moi le chauffage", "chauffage", null);
        assertIsGetStatus("montre-moi un peu le thermostat", "thermostat", null);
        assertIsGetStatus("montre-moi le volet du salon", "volet", "salon");
        assertIsGetStatus("montre-moi la température de la cuisine", "température", "cuisine");
        assertIsGetStatus("peux-tu me montrer le thermostat", "thermostat", null);
        assertIsGetStatus("chauffage au premier étage", "chauffage", "premier étage");
        assertIsGetStatus("chauffage au rez-de-chaussée", "chauffage", "rez de chaussée");
        assertIsGetStatus("lampe de la cage d'escalier", "lampe", "cage d escalier");
        assertIsGetStatus("volet dans la cage d'escalier", "volet", "cage d escalier");
        assertIsGetStatus("arrosage dans le jardin", "arrosage", "jardin");
        assertIsGetStatus("lampes du couloir", "lampes", "couloir");
        assertIsGetStatus("chauffage de la salle de bain", "chauffage", "salle de bain");

        assertIsActivate("active le thermostat dans le garage", "thermostat", "garage");
        assertIsActivate("active le chauffage dans le salon", "chauffage", "salon");
        assertIsActivate("allume la lampe de la cage d'escalier", "lampe", "cage d escalier");
        assertIsActivate("allume la lumière de la chambre", "lumière", "chambre");
        assertIsActivate("allume l'ampli", "ampli", null);
        assertIsActivate("allume la tv s'il te plaît", "tv", null);
        assertIsActivate("mets de la musique", "musique", null);
        assertIsActivate("mets moi de la musique", "musique", null);
        assertIsActivate("mets en route la climatisation", "climatisation", null);
        assertIsActivate("mets de la musique dans le salon", "musique", "salon");
        assertIsActivate("allume la lumière du couloir", "lumière", "couloir");
        assertIsActivate("allume la lumière du salon", "lumière", "salon");
        assertIsActivate("allume les lumières au premier étage", "lumières", "premier étage");
        assertIsActivate("allume le chauffage au rez-de-chaussée", "chauffage", "rez de chaussée");
        // assertIsActivate("mets du chauffage au rez-de-chaussée", "chauffage", "rez de chaussée");

        assertIsDeactivate("arrête le chauffage dans le salon", "chauffage", "salon");
        assertIsDeactivate("stoppe la ventilation", "ventilation", null);
        assertIsDeactivate("éteins les lumières dans la cuisine", "lumières", "cuisine");
        assertIsDeactivate("éteindre la lumière dans la salle à manger", "lumière", "salle à manger");
        assertIsDeactivate("arrêter l'air conditionné", "air conditionné", null);
        assertIsDeactivate("arrête l'arrosage du jardin", "arrosage", "jardin");
        assertIsDeactivate("arrête le volet du salon", "volet", "salon");
        assertIsDeactivate("éteins la lumière du bureau", "lumière", "bureau");
        assertIsDeactivate("stoppe la ventilation", "ventilation", null);
        assertIsDeactivate("stoppe la chaudière", "chaudière", null);
        assertIsDeactivate("je ne veux plus de chauffage dans le salon", "chauffage", "salon");
        assertIsDeactivate("arrête la musique", "musique", null);
        assertIsDeactivate("je ne veux plus de musique", "musique", null);

        assertIs(Skills.GET_HISTORY_HOURLY,
                "donne-moi un graphique de la température du salon pour les dernières heures");
        checkInterpretation(Skills.GET_HISTORY_DAILY, "température dans la salle de bain sur 3 jours", "température",
                "salle de bain");
        assertIsGetStatus("lampe dans la cage d'escalier");
        assertIsGetStatus("volet dans la cage d'escalier");
        assertIsGetStatus("arrosage dans le jardin");
        assertIsActivate("allume la lampe de la cage d'escalier");
        assertIsGetStatus("lampes du couloir");

        assertIs(Skills.GET_HISTORY_MONTHLY, "graphique mensuel de la température sur 2 mois");
        assertIs(Skills.GET_HISTORY_WEEKLY, "graphique de la température de la semaine");

        assertIsSetValue("mets la température du salon à 23");
        assertIsSetValue("mets le volume de la musique à 50 %");
        assertIsSetValue("mets le volume à 50 %");
        assertIsSetValue("change la lumière au rez-de-chaussée à 12%");
        assertIsSetValue("couleur rouge pour le bureau");
        assertIsSetValue("mets les lumières de la terrasse en jaune");
        assertIsSetValue("mets les lampes du balcon en orange");
        assertIsSetValue("mets la ventilation à 40 %");
        assertIsSetValue("change la couleur de la cuisine en bleu");
        assertIsSetValue("mets la musique à 40");
        assertIsSetValue("mets la musique dans la cuisine à 80");
        assertIsSetValue("mets les lumières dans le jardin en violet");
        assertIsSetValue("mets les lumières dans la buanderie à 40%");
        assertIsSetValue("mets les lumières dans le couloir à 20%");

        assertIs(Skills.CREATE_RULE, "programme un truc pour demain matin");
        assertIs(Skills.CREATE_RULE, "fais une règle à exécuter ce soir");
        assertIs(Skills.CREATE_RULE, "programmer une tâche pour dans 2 heures");
        assertIs(Skills.CREATE_RULE, "programme une tâche pour samedi à 10h45");
        assertIs(Skills.CREATE_RULE, "peux-tu ajouter une règle pour cet après midi");
    }
}
