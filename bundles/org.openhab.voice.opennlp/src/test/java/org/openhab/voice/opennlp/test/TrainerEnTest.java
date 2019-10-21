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

public class TrainerEnTest extends AbstractTrainerTest {

    @Test
    public void testEN() throws Exception {
        this.trainer = new IntentTrainer("en", skills);

        assertIsGetStatus("Temperature in the kitchen?", "temperature", "kitchen");
        assertIsGetStatus("show me the temperature in the kitchen", "temperature", "kitchen");
        assertIsGetStatus("what's the temperature in the garage?", "temperature", "garage");
        assertIsGetStatus("temperature in the corridor", "temperature", "corridor");
        assertIsGetStatus("temperature on the first floor", "temperature", "first floor");
        assertIsGetStatus("tell me the temperature of the pool", "temperature", "pool");
        assertIsGetStatus("lights in the attic", "lights", "attic");
        assertIsGetStatus("lights in the basement", "lights", "basement");
        assertIsGetStatus("lights in the guest house", "lights", "guest house");
        assertIsGetStatus("corridor lights", "lights", "corridor");
        assertIsGetStatus("living room lights", "lights", "living room");
        assertIsGetStatus("kitchen lights", "lights", "kitchen");

        assertIsActivate("start the heating in the garage", "heating", "garage");
        assertIsActivate("start the boiler in the garage", "boiler", "garage");
        assertIsActivate("start the ac please!", "ac", null);
        assertIsActivate("put on some music please", "music", null);
        assertIsActivate("can you put some music in the kitchen please", "music", "kitchen");
        assertIsActivate("turn on the lights in the kitchen", "lights", "kitchen");
        assertIsActivate("please switch the lights in the kitchen on", "lights", "kitchen");
        assertIsActivate("i'd like some light in the bedroom", "light", "bedroom");
        assertIsActivate("i want some air conditioning in the bedroom", "air conditioning", "bedroom");

        assertIsDeactivate("please turn off the radiators", "radiators", null);
        assertIsDeactivate("deactivate the alarm", "alarm", null);
        assertIsDeactivate("stop the music in the living room", "music", "living room");
        assertIsDeactivate("i don't want music in the kitchen anymore", "music", "kitchen");
        assertIsDeactivate("i don't want any music in the kitchen anymore", "music", "kitchen");
        assertIsDeactivate("stop the washing machine", "washing machine", null);
        assertIsDeactivate("stop the air conditioning", "air conditioning", null);
        assertIsDeactivate("please could you stop the air conditioning", "air conditioning", null);
        assertIsDeactivate("no more lights please", "lights", null);

        checkInterpretation(Skills.GET_HISTORY_HOURLY,
                "show me a graph of the temperature in the living room for the last 3 hours", "temperature",
                "living room");
        checkInterpretation(Skills.GET_HISTORY_DAILY, "graph the water consumption for the last 2 days",
                "water consumption", null);
        checkInterpretation(Skills.GET_HISTORY_WEEKLY, "i'd like a chart of the humidity over 2 weeks", "humidity",
                null);
        checkInterpretation(Skills.GET_HISTORY_MONTHLY, "temperature in the downstairs corridor for the last month",
                "temperature", "downstairs corridor");
        checkInterpretation(Skills.GET_HISTORY_MONTHLY, "luminosity in the living room over 6 months", "luminosity",
                "living room");

        checkInterpretation(Skills.GET_HISTORY_LAST_CHANGES, "when was the alarm turned on for the last time?", "alarm",
                null);
        checkInterpretation(Skills.GET_HISTORY_LAST_CHANGES,
                "show me the last state changes of the window in the bedroom", "window", "bedroom");

        assertIsSetValue("set the volume in the living room to 25%");
        assertIsSetValue("change the lights in the kitchen to yellow");
        assertIsSetValue("change the corridor lights to 70%");
        assertIsSetValue("please set the kitchen lights to red");
        assertIsSetValue("set the music volume in the living room at 80%");
        assertIsSetValue("set the lights in the backyard at 80%");
        assertIsSetValue("set the lights of the pool to red");
        assertIsSetValue("set the lights in the child's room to 10%");
        assertIsSetValue("please put the lights in the child's room at 50%");
        assertIsSetValue("change the color the lights in blue");
        assertIsSetValue("set the lights in the living room to 35%");

        assertIs("create-rule", "i want to create a rule to run on Monday");
        assertIs("create-rule", "set up something to run later");
        assertIs("create-rule", "set up a new rule to run in 90 minutes");
        assertIs("create-rule", "set up a new rule to run tomorrow at 8");
        assertIs("create-rule", "set up a new rule to run on Wednesday at 11h30");
    }

}
