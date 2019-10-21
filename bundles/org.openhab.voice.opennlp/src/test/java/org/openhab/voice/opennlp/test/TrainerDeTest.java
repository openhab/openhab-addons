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

public class TrainerDeTest extends AbstractTrainerTest {

    @Test
    public void testActivateObjects() throws Exception {

        Intent actual;
        this.trainer = new IntentTrainer("de", skills);

        actual = interpret("mach den Fernseher an");
        assertEquals(Skills.ACTIVATE_OBJECT, actual.getName());
        assertEquals(1, actual.getEntities().size());
        assertEquals("fernseher", actual.getEntities().get("object"));

        actual = interpret("bitte mache das Licht an");
        assertEquals(Skills.ACTIVATE_OBJECT, actual.getName());
        assertEquals(1, actual.getEntities().size());
        assertEquals("licht", actual.getEntities().get("object"));

        actual = interpret("mache die Lampe im Wohnzimmer an");
        assertEquals(Skills.ACTIVATE_OBJECT, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("lampe", actual.getEntities().get("object"));
        assertEquals("wohnzimmer", actual.getEntities().get("location"));

        actual = interpret("mach im Wohnzimmer das Licht an");
        assertEquals(Skills.ACTIVATE_OBJECT, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("licht", actual.getEntities().get("object"));
        assertEquals("wohnzimmer", actual.getEntities().get("location"));
    }

    @Test
    public void testDeactivateObjects() throws Exception {

        Intent actual;
        this.trainer = new IntentTrainer("de", skills);

        actual = interpret("mach das Licht aus");
        assertEquals(Skills.DEACTIVATE_OBJECT, actual.getName());
        assertEquals(1, actual.getEntities().size());
        assertEquals("licht", actual.getEntities().get("object"));

        actual = interpret("mach den Fernseher aus");
        assertEquals(Skills.DEACTIVATE_OBJECT, actual.getName());
        assertEquals(1, actual.getEntities().size());
        assertEquals("fernseher", actual.getEntities().get("object"));

        actual = interpret("bitte mache das Licht aus");
        assertEquals(Skills.DEACTIVATE_OBJECT, actual.getName());
        assertEquals(1, actual.getEntities().size());
        assertEquals("licht", actual.getEntities().get("object"));
    }

    @Test
    public void testGetStatus() throws Exception {

        Intent actual;
        this.trainer = new IntentTrainer("de", skills);

        actual = interpret("Heizung in der Küche");
        assertEquals(Skills.GET_STATUS, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("heizung", actual.getEntities().get("object"));
        assertEquals("küche", actual.getEntities().get("location"));

        actual = interpret("wie hoch ist die Temperatur im Wohnzimmer");
        assertEquals(Skills.GET_STATUS, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("temperatur", actual.getEntities().get("object"));
        assertEquals("wohnzimmer", actual.getEntities().get("location"));

        actual = interpret("wie hoch ist die Temperatur im Keller");
        assertEquals(Skills.GET_STATUS, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("temperatur", actual.getEntities().get("object"));
        assertEquals("keller", actual.getEntities().get("location"));

        actual = interpret("wie ist die Temperatur im Kinderzimmer");
        assertEquals(Skills.GET_STATUS, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("temperatur", actual.getEntities().get("object"));
        assertEquals("kinderzimmer", actual.getEntities().get("location"));

        actual = interpret("ist draußen das Licht an?");
        assertEquals(Skills.GET_STATUS, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("licht", actual.getEntities().get("object"));
        assertEquals("draußen", actual.getEntities().get("location"));

        actual = interpret("ist das Licht draußen an?");
        assertEquals(Skills.GET_STATUS, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("licht", actual.getEntities().get("object"));
        assertEquals("draußen", actual.getEntities().get("location"));

        actual = interpret("ist das Licht oben an?");
        assertEquals(Skills.GET_STATUS, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("licht", actual.getEntities().get("object"));
        assertEquals("oben", actual.getEntities().get("location"));

        actual = interpret("wie ist das Licht draußen?");
        assertEquals(Skills.GET_STATUS, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("licht", actual.getEntities().get("object"));
        assertEquals("draußen", actual.getEntities().get("location"));

        actual = interpret("ist der Bluetooth-Speaker im Schlafzimmer an?");
        assertEquals(Skills.GET_STATUS, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("bluetooth-speaker", actual.getEntities().get("object"));
        assertEquals("schlafzimmer", actual.getEntities().get("location"));

        actual = interpret("läuft der Fernseher im Kinderzimmer?");
        assertEquals(Skills.GET_STATUS, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("fernseher", actual.getEntities().get("object"));
        assertEquals("kinderzimmer", actual.getEntities().get("location"));

        actual = interpret("ist das Garagentor offen?");
        assertEquals(Skills.GET_STATUS, actual.getName());
        assertEquals(1, actual.getEntities().size());
        assertEquals("garagentor", actual.getEntities().get("object"));

        actual = interpret("ist die Balkontüre offen?");
        assertEquals(Skills.GET_STATUS, actual.getName());
        assertEquals(1, actual.getEntities().size());
        assertEquals("balkontüre", actual.getEntities().get("object"));
    }

    @Test
    public void testHistoryHourly() throws Exception {

        Intent actual;
        this.trainer = new IntentTrainer("de", skills, null, "alphanumeric");

        actual = interpret("wie ist der Verlauf der Temperatur der letzten Stunde?");
        assertEquals(Skills.GET_HISTORY_HOURLY, actual.getName());
        assertEquals(1, actual.getEntities().size());
        assertEquals("temperatur", actual.getEntities().get("object"));

        actual = interpret("Verlauf der Temperatur der letzten Stunde");
        assertEquals(Skills.GET_HISTORY_HOURLY, actual.getName());
        assertEquals(1, actual.getEntities().size());
        assertEquals("temperatur", actual.getEntities().get("object"));
    }

    @Test
    public void testHistoryDaily() throws Exception {

        Intent actual;
        this.trainer = new IntentTrainer("de", skills, null, "alphanumeric");

        actual = interpret("Verlauf der Temperatur der letzten 24 Stunden");
        assertEquals(Skills.GET_HISTORY_DAILY, actual.getName());
        assertEquals(1, actual.getEntities().size());
        assertEquals("temperatur", actual.getEntities().get("object"));

        actual = interpret("Verlauf der Luftfeuchtigkeit der letzten 24 Stunden");
        assertEquals(Skills.GET_HISTORY_DAILY, actual.getName());
        assertEquals(1, actual.getEntities().size());
        assertEquals("luftfeuchtigkeit", actual.getEntities().get("object"));

    }

    @Test
    public void testHistoryWeekly() throws Exception {

        Intent actual;
        this.trainer = new IntentTrainer("de", skills, null, "alphanumeric");

        actual = interpret("Verlauf der Temperatur der letzten Woche");
        assertEquals(Skills.GET_HISTORY_WEEKLY, actual.getName());
        assertEquals(1, actual.getEntities().size());
        assertEquals("temperatur", actual.getEntities().get("object"));

        actual = interpret("Verlauf der Luftfeuchtigkeit der letzten Woche");
        assertEquals(Skills.GET_HISTORY_WEEKLY, actual.getName());
        assertEquals(1, actual.getEntities().size());
        assertEquals("luftfeuchtigkeit", actual.getEntities().get("object"));

        actual = interpret("zeige die Wochenübersicht der Temperatur für die Küche");
        assertEquals(Skills.GET_HISTORY_WEEKLY, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("temperatur", actual.getEntities().get("object"));
        assertEquals("küche", actual.getEntities().get("location"));
    }

    @Test
    public void testHistoryMonthly() throws Exception {

        Intent actual;
        this.trainer = new IntentTrainer("de", skills, null, "alphanumeric");

        actual = interpret("Verlauf der Temperatur des letzten Monats");
        assertEquals(Skills.GET_HISTORY_MONTHLY, actual.getName());
        assertEquals(1, actual.getEntities().size());
        assertEquals("temperatur", actual.getEntities().get("object"));

        actual = interpret("Verlauf der Luftfeuchtigkeit des letzten Monats");
        assertEquals(Skills.GET_HISTORY_MONTHLY, actual.getName());
        assertEquals(1, actual.getEntities().size());
        assertEquals("luftfeuchtigkeit", actual.getEntities().get("object"));

        actual = interpret("Monatsübersicht der Temperatur für die Küche");
        assertEquals(Skills.GET_HISTORY_MONTHLY, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("temperatur", actual.getEntities().get("object"));
        assertEquals("küche", actual.getEntities().get("location"));
    }

    @Test
    public void testHistoryLastChanges() throws Exception {

        Intent actual;
        this.trainer = new IntentTrainer("de", skills, null, "alphanumeric");

        actual = interpret("wann hat sich die Temperatur zuletzt geändert?");
        assertEquals(Skills.GET_HISTORY_LAST_CHANGES, actual.getName());
        assertEquals(1, actual.getEntities().size());
        assertEquals("temperatur", actual.getEntities().get("object"));

        actual = interpret("wann wurde der letzte Alarm ausgelöst?");
        assertEquals(Skills.GET_HISTORY_LAST_CHANGES, actual.getName());
        assertEquals(1, actual.getEntities().size());
        assertEquals("alarm", actual.getEntities().get("object"));

        actual = interpret("wann wurde der Status vom Licht im Wohnzimmer zuletzt geändert?");
        assertEquals(Skills.GET_HISTORY_LAST_CHANGES, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("wohnzimmer", actual.getEntities().get("location"));
        assertEquals("licht", actual.getEntities().get("object"));
    }

    @Test
    public void testSetValueOfTemperature() throws Exception {
        Intent actual;
        this.trainer = new IntentTrainer("de", skills, null, "alphanumeric");

        actual = interpret("stell den Thermostat auf 21 Grad.");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("thermostat", actual.getEntities().get("object"));
        assertEquals("21", actual.getEntities().get("value"));

        actual = interpret("Temperatur auf 21 Grad erhöhen.");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("temperatur", actual.getEntities().get("object"));
        assertEquals("21", actual.getEntities().get("value"));
    }

    @Test
    public void testSetValueOfLights() throws Exception {
        Intent actual;
        this.trainer = new IntentTrainer("de", skills, null, "alphanumeric");

        actual = interpret("Dimme das Licht auf 70%");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("licht", actual.getEntities().get("object"));
        assertEquals("70", actual.getEntities().get("value"));

        actual = interpret("Dimme das Licht auf 10%");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("licht", actual.getEntities().get("object"));
        assertEquals("10", actual.getEntities().get("value"));

        actual = interpret("Dimme das Licht im Wohnzimmer auf 70%");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(3, actual.getEntities().size());
        assertEquals("licht", actual.getEntities().get("object"));
        assertEquals("wohnzimmer", actual.getEntities().get("location"));
        assertEquals("70", actual.getEntities().get("value"));

        actual = interpret("Dimme die Beleuchtung auf 100%");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("beleuchtung", actual.getEntities().get("object"));
        assertEquals("100", actual.getEntities().get("value"));

        actual = interpret("Dimme die Beleuchtung im Bad auf 100%");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(3, actual.getEntities().size());
        assertEquals("beleuchtung", actual.getEntities().get("object"));
        assertEquals("bad", actual.getEntities().get("location"));
        assertEquals("100", actual.getEntities().get("value"));
    }

    @Test
    public void testSetValueOfSpeaker() throws Exception {
        Intent actual;
        this.trainer = new IntentTrainer("de", skills, null, "alphanumeric");

        actual = interpret("Regle im Wohnzimmer die Lautstärke auf 50");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(3, actual.getEntities().size());
        assertEquals("lautstärke", actual.getEntities().get("object"));
        assertEquals("wohnzimmer", actual.getEntities().get("location"));
        assertEquals("50", actual.getEntities().get("value"));

        actual = interpret("Stelle die Lautstärke im Büro auf 8");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(3, actual.getEntities().size());
        assertEquals("lautstärke", actual.getEntities().get("object"));
        assertEquals("büro", actual.getEntities().get("location"));
        assertEquals("8", actual.getEntities().get("value"));

        actual = interpret("Lautstärke im Büro auf 8");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(3, actual.getEntities().size());
        assertEquals("lautstärke", actual.getEntities().get("object"));
        assertEquals("büro", actual.getEntities().get("location"));
        assertEquals("8", actual.getEntities().get("value"));

        actual = interpret("stelle Lautstärke im Büro auf 8%");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(3, actual.getEntities().size());
        assertEquals("lautstärke", actual.getEntities().get("object"));
        assertEquals("büro", actual.getEntities().get("location"));
        assertEquals("8", actual.getEntities().get("value"));
    }

    @Test
    public void testSetValueOfLightColor() throws Exception {
        Intent actual;
        this.trainer = new IntentTrainer("de", skills, null, "alphanumeric");

        actual = interpret("Färbe das Licht im Wohnzimmer grün");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(3, actual.getEntities().size());
        assertEquals("licht", actual.getEntities().get("object"));
        assertEquals("wohnzimmer", actual.getEntities().get("location"));
        assertEquals("grün", actual.getEntities().get("value"));
    }

    @Test
    public void testSetValueOfShutter() throws Exception {
        Intent actual;
        this.trainer = new IntentTrainer("de", skills, null, "alphanumeric");

        actual = interpret("fahre die Jalousien hoch");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(2, actual.getEntities().size());
        assertEquals("jalousien", actual.getEntities().get("object"));
        assertEquals("hoch", actual.getEntities().get("value"));

        actual = interpret("regle die Jalousien in der Küche auf 50%");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(3, actual.getEntities().size());
        assertEquals("jalousien", actual.getEntities().get("object"));
        assertEquals("küche", actual.getEntities().get("location"));
        assertEquals("50", actual.getEntities().get("value"));

        actual = interpret("regle die Jalousien in der Küche auf 0%");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(3, actual.getEntities().size());
        assertEquals("jalousien", actual.getEntities().get("object"));
        assertEquals("küche", actual.getEntities().get("location"));
        assertEquals("0", actual.getEntities().get("value"));

        actual = interpret("fahre die Jalousien in der Küche auf 50%");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(3, actual.getEntities().size());
        assertEquals("jalousien", actual.getEntities().get("object"));
        assertEquals("küche", actual.getEntities().get("location"));
        assertEquals("50", actual.getEntities().get("value"));

        actual = interpret("stell die Rolladen in der Küche auf 50%");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(3, actual.getEntities().size());
        assertEquals("rolladen", actual.getEntities().get("object"));
        assertEquals("küche", actual.getEntities().get("location"));
        assertEquals("50", actual.getEntities().get("value"));

        actual = interpret("stell den Rolladen in der Küche auf 0%");
        assertEquals(Skills.SET_VALUE, actual.getName());
        assertEquals(3, actual.getEntities().size());
        assertEquals("rolladen", actual.getEntities().get("object"));
        assertEquals("küche", actual.getEntities().get("location"));
        assertEquals("0", actual.getEntities().get("value"));
    }

    @Test
    public void testCreateRule() throws Exception {
        Intent actual;
        this.trainer = new IntentTrainer("de", skills, null, "alphanumeric");

        actual = interpret("einen Timer stellen");
        assertEquals(Skills.CREATE_RULE, actual.getName());

        actual = interpret("Erstelle eine Regel, die um 8 Uhr läuft.");
        assertEquals(Skills.CREATE_RULE, actual.getName());

    }
}
