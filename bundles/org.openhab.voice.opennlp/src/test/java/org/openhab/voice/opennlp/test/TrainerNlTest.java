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

public class TrainerNlTest extends AbstractTrainerTest {

    @Test
    public void testGetStatus() throws Exception {

        this.trainer = new IntentTrainer("nl", skills, null, "alphanumeric");

        assertIsGetStatus("Wat is de temperatuur in de keuken?", "temperatuur", "keuken");
        assertIsGetStatus("Temperatuur in de keuken?", "temperatuur", "keuken");
        assertIsGetStatus("toon me de temperatuur in de keuken", "temperatuur", "keuken");
        assertIsGetStatus("wat is de temperatuur in de garage?", "temperatuur", "garage");
        assertIsGetStatus("temperatuur in de gang", "temperatuur", "gang");
        assertIsGetStatus("temperatuur op de eerste verdieping", "temperatuur", "eerste verdieping");
        assertIsGetStatus("vertel me de temperatuur van het zwembad", "temperatuur", "zwembad");
        assertIsGetStatus("lichten op zolder", "lichten", "zolder");
        assertIsGetStatus("lichten in de kelder", "lichten", "kelder");
        assertIsGetStatus("lichten in het tuinhuis", "lichten", "tuinhuis");
        assertIsGetStatus("gang lichten", "lichten", "gang");
        assertIsGetStatus("woonkamer verlichting", "verlichting", "woonkamer");
        assertIsGetStatus("keuken verlichting", "verlichting", "keuken");
        assertIsGetStatus("welke lampen staan aan?", "lampen", null);
        assertIsGetStatus("wat is de temperatuur in de keuken?", "temperatuur", "keuken");
        assertIsGetStatus("staat de voordeur open?", "voordeur", null);
        assertIsGetStatus("is de voordeur open? ", "voordeur", null);
        assertIsGetStatus("welke ramen staan open?", "ramen", null);
        assertIsGetStatus("staat het raam in de wc open?", "raam", "wc");
        assertIsGetStatus("verwarming in de keuken", "verwarming", "keuken");
        assertIsGetStatus("Wat is de temperatuur in de woonkamer?", "temperatuur", "woonkamer");
        assertIsGetStatus("Wat is de temperatuur in de kelder?", "temperatuur", "kelder");
    }

    @Test
    public void testActivateObjects() throws Exception {

        this.trainer = new IntentTrainer("nl", skills, null, "alphanumeric");

        assertIsActivate("zet de tv aan", "tv", null);
        assertIsActivate("doe het licht aan", "licht", null);
        assertIsActivate("doe het licht aan in de keuken", "licht", "keuken");
        assertIsActivate("doe het licht aan op zolder", "licht", "zolder");
        assertIsActivate("zet het licht aan op zolder", "licht", "zolder");
        assertIsActivate("zet het licht aan in de gang", "licht", "gang");
        assertIsActivate("zet het licht aan op de zolder", "licht", "zolder");
        assertIsActivate("zet het licht op de gang aan", "licht", "gang");
        assertIsActivate("zet de verwarmng aan", "verwarmng", null);

    }

    @Test
    public void testDeactivateObjects() throws Exception {

        this.trainer = new IntentTrainer("nl", skills, null, "alphanumeric");

        assertIsDeactivate("doe het licht uit", "licht", null);
        assertIsDeactivate("zet de tv uit", "tv", null);
        assertIsDeactivate("doe het licht uit, alsjeblieft", "licht", null);
        assertIsDeactivate("doe het licht uit", "licht", null);
        assertIsDeactivate("doe het licht uit in de keuken", "licht", "keuken");
        assertIsDeactivate("doe het licht uit op zolder", "licht", "zolder");
        assertIsDeactivate("zet het licht uit op zolder", "licht", "zolder");
        assertIsDeactivate("zet het licht uit in de gang", "licht", "gang");
        assertIsDeactivate("zet het licht uit op de zolder", "licht", "zolder");
        assertIsDeactivate("zet het licht op de gang uit", "licht", "gang");
        assertIsDeactivate("zet de verwarmng uit", "verwarmng", null);
    }

    @Test
    public void testHistoryHourly() throws Exception {

        Intent actual;
        this.trainer = new IntentTrainer("nl", skills, null, "alphanumeric");

        actual = interpret("Hoe is het verloop van de temperatuur in het afgelopen uur?");
        assertEquals(Skills.GET_HISTORY_HOURLY, actual.getName());
        assertEquals(1, actual.getEntities().size());
        assertEquals("temperatuur", actual.getEntities().get("object"));

        actual = interpret("Verloop van de temperatuur van het laatste uur");
        assertEquals(Skills.GET_HISTORY_HOURLY, actual.getName());
        assertEquals(1, actual.getEntities().size());
        assertEquals("temperatuur", actual.getEntities().get("object"));
    }

    @Test
    public void testHistoryDaily() throws Exception {

        Intent actual;
        this.trainer = new IntentTrainer("nl", skills, null, "alphanumeric");

        actual = interpret("Verloop van de temperatuur van de laatste 24 uur");
        assertEquals(Skills.GET_HISTORY_DAILY, actual.getName());
        assertEquals(1, actual.getEntities().size());
        assertEquals("temperatuur", actual.getEntities().get("object"));

        actual = interpret("Geschiedenis van de temperatuur van de afgelopen 24 uur");
        assertEquals(Skills.GET_HISTORY_DAILY, actual.getName());
        assertEquals(1, actual.getEntities().size());
        assertEquals("temperatuur", actual.getEntities().get("object"));

    }

    @Test
    public void testHistoryWeekly() throws Exception {

        Intent actual;
        this.trainer = new IntentTrainer("nl", skills, null, "alphanumeric");

        actual = interpret("Verloop van de temperatuur van de laatste week");
        assertEquals(Skills.GET_HISTORY_WEEKLY, actual.getName());
        assertEquals(1, actual.getEntities().size());
        assertEquals("temperatuur", actual.getEntities().get("object"));

        actual = interpret("Verloop van de vochtigheid van vorige week");
        assertEquals(Skills.GET_HISTORY_WEEKLY, actual.getName());
        assertEquals(1, actual.getEntities().size());
        assertEquals("vochtigheid", actual.getEntities().get("object"));

        actual = interpret("toon het wekelijkse overzicht van de temperatuur voor de keuken");
        assertEquals(Skills.GET_HISTORY_WEEKLY, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("temperatuur", actual.getEntities().get("object"));
        assertEquals("keuken", actual.getEntities().get("location"));
    }

    @Test
    public void testHistoryMonthly() throws Exception {

        Intent actual;
        this.trainer = new IntentTrainer("nl", skills, null, "alphanumeric");

        actual = interpret("Geschiedenis van de temperatuur van de afgelopen maand");
        assertEquals(Skills.GET_HISTORY_MONTHLY, actual.getName());
        assertEquals(1, actual.getEntities().size());
        assertEquals("temperatuur", actual.getEntities().get("object"));

        actual = interpret("verloop van de temperatuur van de afgelopen maand");
        assertEquals(Skills.GET_HISTORY_MONTHLY, actual.getName());
        assertEquals(1, actual.getEntities().size());
        assertEquals("temperatuur", actual.getEntities().get("object"));

        actual = interpret("toon het maand overzicht van de temperatuur voor de keuken");
        assertEquals(Skills.GET_HISTORY_MONTHLY, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("temperatuur", actual.getEntities().get("object"));
        assertEquals("keuken", actual.getEntities().get("location"));
    }

    @Test
    public void testHistoryLastChanges() throws Exception {

        Intent actual;
        this.trainer = new IntentTrainer("nl", skills, null, "alphanumeric");

        actual = interpret("wanneer veranderde de temperatuur");
        assertEquals(Skills.GET_HISTORY_LAST_CHANGES, actual.getName());
        assertEquals(1, actual.getEntities().size());
        System.out.println(actual);
        assertEquals("temperatuur", actual.getEntities().get("object"));

        actual = interpret("wanneer werd het laatste alarm geactiveerd?");
        assertEquals(Skills.GET_HISTORY_LAST_CHANGES, actual.getName());
        assertEquals(1, actual.getEntities().size());
        assertEquals("alarm", actual.getEntities().get("object"));

        actual = interpret("wanneer is de status van het licht in de woonkamer voor het laatst veranderd?");
        assertEquals(Skills.GET_HISTORY_LAST_CHANGES, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("woonkamer", actual.getEntities().get("location"));
        assertEquals("licht", actual.getEntities().get("object"));
    }

    @Test
    public void testSetValue() throws Exception {
        Intent actual;
        this.trainer = new IntentTrainer("nl", skills, null, "alphanumeric");

        actual = interpret("zet de thermostaat op 21 graden.");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("thermostaat", actual.getEntities().get("object"));
        assertEquals("21", actual.getEntities().get("value"));

        actual = interpret("Verhoog de temperatuur tot 21 graden.");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("temperatuur", actual.getEntities().get("object"));
        assertEquals("21", actual.getEntities().get("value"));

        actual = interpret("Dim het licht tot 70%");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("licht", actual.getEntities().get("object"));
        assertEquals("70", actual.getEntities().get("value"));

        actual = interpret("Dim het licht in de woonkamer tot 70%");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(3, actual.getEntities().size());
        assertEquals("licht", actual.getEntities().get("object"));
        assertEquals("woonkamer", actual.getEntities().get("location"));
        assertEquals("70", actual.getEntities().get("value"));

        actual = interpret("Stel het volume in de woonkamer in op 50");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(3, actual.getEntities().size());
        assertEquals("volume", actual.getEntities().get("object"));
        assertEquals("woonkamer", actual.getEntities().get("location"));
        assertEquals("50", actual.getEntities().get("value"));

        actual = interpret("Kleur de lichten rood");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("lichten", actual.getEntities().get("object"));
        assertEquals("rood", actual.getEntities().get("color"));

        actual = interpret("Kleur de lampen oranje");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("lampen", actual.getEntities().get("object"));
        assertEquals("oranje", actual.getEntities().get("color"));

        actual = interpret("Kleur het licht in de keuken oranje");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(3, actual.getEntities().size());
        assertEquals("licht", actual.getEntities().get("object"));
        assertEquals("oranje", actual.getEntities().get("color"));
        assertEquals("keuken", actual.getEntities().get("location"));

        actual = interpret("zet de lampen in het tuinhuis op geel");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(3, actual.getEntities().size());
        assertEquals("lampen", actual.getEntities().get("object"));
        assertEquals("geel", actual.getEntities().get("color"));
        assertEquals("tuinhuis", actual.getEntities().get("location"));

        actual = interpret("Kleur het licht groen");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("licht", actual.getEntities().get("object"));
        assertEquals("groen", actual.getEntities().get("color"));

        actual = interpret("Kleur het licht cyaan");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("licht", actual.getEntities().get("object"));
        assertEquals("cyaan", actual.getEntities().get("color"));

        actual = interpret("Kleur het licht blauw");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("licht", actual.getEntities().get("object"));
        assertEquals("blauw", actual.getEntities().get("color"));

        actual = interpret("Kleur het licht paars");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("licht", actual.getEntities().get("object"));
        assertEquals("paars", actual.getEntities().get("color"));

        actual = interpret("Kleur het licht wit");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("licht", actual.getEntities().get("object"));
        assertEquals("wit", actual.getEntities().get("color"));

    }

    @Test
    public void testCreateRule() throws Exception {
        Intent actual;
        this.trainer = new IntentTrainer("nl", skills, null, "alphanumeric");

        actual = interpret("stel een timer in");
        assertEquals(Skills.CREATE_RULE, actual.getName());

        actual = interpret("Maak een regel die om 8 uur afgaat");
        assertEquals(Skills.CREATE_RULE, actual.getName());

    }

}
