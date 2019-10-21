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

import org.junit.Test;
import org.openhab.voice.opennlp.internal.IntentTrainer;

public class TrainerItTest extends AbstractTrainerTest {

    @Test
    public void testIT() throws Exception {
        this.trainer = new IntentTrainer("it", skills, null, "alphanumeric");

        assertIsGetStatus("Temperatura in cucina?", "temperatura", "cucina");
        assertIsGetStatus("mostrami la temperatura in cucina", "temperatura", "cucina");
        assertIsGetStatus("Qual è la temperatura in garage?", "temperatura", "garage");
        assertIsGetStatus("temperatura in corridoio", "temperatura", "corridoio");
        assertIsGetStatus("temperatura al primo piano", "temperatura", "primo piano");
        assertIsGetStatus("dimmi la temperatura della piscina", "temperatura", "piscina");
        assertIsGetStatus("luci dell'attico", "luci", "attico");
        assertIsGetStatus("luci al piano terra", "luci", "piano terra");
        assertIsGetStatus("luci nello studio", "luci", "studio");
        assertIsGetStatus("luci del corridoio", "luci", "corridoio");
        assertIsGetStatus("luci del soggiorno", "luci", "soggiorno");
        assertIsGetStatus("luci del giardino", "luci", "giardino");

        assertIsActivate("accendi il riscaldamento nel garage", "riscaldamento", "garage");
        assertIsActivate("accendi la pompa della piscina", "pompa", "piscina");
        assertIsActivate("accendi l'aria condizionata per favore", "aria condizionata", null);
        assertIsActivate("metti un po' di musica per favore", "musica", null);
        assertIsActivate("puoi mettere un po' di musica in cucina per favore?", "musica", "cucina");
        assertIsActivate("accendi le luci in cucina", "luci", "cucina");
        assertIsActivate("per favore accendi le luci della cucina", "luci", "cucina");
        assertIsActivate("vorrei un po' di luce in bagno", "luce", "bagno");
        assertIsActivate("voglio un po' d'aria condizionata in cucina", "aria condizionata", "cucina");

        // assertIsDeactivate("spegni i radiatori", "radiatori", null);
        assertIsDeactivate("disattiva l'allarme", "allarme", null);
        assertIsDeactivate("spegni la musica in soggiorno", "musica", "soggiorno");
        assertIsDeactivate("non voglio più musica in cucina", "musica", "cucina");
        assertIsDeactivate("ferma la lavatrice", "lavatrice", null);
        assertIsDeactivate("spegni l'aria condizionata", "aria condizionata", null);
        assertIsDeactivate("per favore potresti spegnere l'aria condizionata?", "aria condizionata", null);
        assertIsDeactivate("spegni le luci", "luci", null);

        checkInterpretation(Skills.GET_HISTORY_HOURLY,
                "mostrami un grafico della temperatura del soggiorno delle ultime 3 ore", "temperatura", "soggiorno");
        checkInterpretation(Skills.GET_HISTORY_DAILY, "grafico del consumo di acqua degli ultimi 2 giorni",
                "consumo di acqua", null);
        checkInterpretation(Skills.GET_HISTORY_WEEKLY, "vorrei un grafico dell'umidità deglle ultime 2 settimane",
                "umidità", null);
        checkInterpretation(Skills.GET_HISTORY_MONTHLY, "temperatura del piano di sotto nell'ultimo mese",
                "temperatura", "piano di sotto");
        checkInterpretation(Skills.GET_HISTORY_MONTHLY, "luminosità del soggiorno negli ultimi 6 mesi", "luminosità",
                "soggiorno");

        checkInterpretation(Skills.GET_HISTORY_LAST_CHANGES, "quando è scattato l'allarme l'ultima volta?", "allarme",
                null);
        checkInterpretation(Skills.GET_HISTORY_LAST_CHANGES,
                "mostrami gli ultimi cambi di stato della finestra della camera", "finestra", "camera");

        assertIsSetValue("imposta il volume in soggiorno al 25%");
        assertIsSetValue("cambia le luci della cucina in giallo");
        assertIsSetValue("metti le luci del corridoio al 70%");
        assertIsSetValue("per favore imposta le luci della cucina a rosso");
        assertIsSetValue("metti il volume della musica del soggiorno al 80%");
        assertIsSetValue("imposta le luci del giardino al 80%");
        assertIsSetValue("imposta le luci della piscina a rosso");
        assertIsSetValue("imposta le luci della cameretta al 10%");
        assertIsSetValue("cambia il colore delle luci in blu");
        assertIsSetValue("imposta le luci del soggiorno al 35%");

        assertIs("create-rule", "voglio creare una regola che si attivi di lunedì");
        assertIs("create-rule", "metti una regola che si attivi più tardi");
        assertIs("create-rule", "imposta una regola che si attivi fra 90 minuti");
        assertIs("create-rule", "imposta una nuova regola per domani alle 8");
        assertIs("create-rule", "imposta una nuova regola per tutti i mercoledì alle 11h30");
    }
}
