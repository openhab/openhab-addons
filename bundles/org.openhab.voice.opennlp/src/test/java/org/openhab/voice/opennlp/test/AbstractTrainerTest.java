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

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.smarthome.core.voice.text.Intent;
import org.eclipse.smarthome.core.voice.text.InterpretationException;
import org.eclipse.smarthome.core.voice.text.InterpretationResult;
import org.eclipse.smarthome.core.voice.text.UnsupportedLanguageException;
import org.junit.Before;
import org.openhab.voice.opennlp.Skill;
import org.openhab.voice.opennlp.internal.IntentTrainer;

public class AbstractTrainerTest {

    IntentTrainer trainer = null;

    protected List<Skill> skills;

    public class Skills {
        public final static String GET_STATUS = "get-status";
        public final static String ACTIVATE_OBJECT = "activate-object";
        public final static String DEACTIVATE_OBJECT = "deactivate-object";
        public final static String GET_HISTORY_HOURLY = "get-history-hourly";
        public final static String GET_HISTORY_DAILY = "get-history-daily";
        public final static String GET_HISTORY_WEEKLY = "get-history-weekly";
        public final static String GET_HISTORY_MONTHLY = "get-history-monthly";
        public final static String GET_HISTORY_LAST_CHANGES = "get-history-last-changes";
        public final static String SET_VALUE = "set-value";
        public final static String CREATE_RULE = "create-rule";

    }

    @Before
    public void initializeMockSkills() {
        skills = new ArrayList<Skill>();

        skills.add(new MockSkill(Skills.GET_STATUS));
        skills.add(new MockSkill(Skills.ACTIVATE_OBJECT));
        skills.add(new MockSkill(Skills.DEACTIVATE_OBJECT));
        skills.add(new MockSkill(Skills.GET_HISTORY_HOURLY));
        skills.add(new MockSkill(Skills.GET_HISTORY_DAILY));
        skills.add(new MockSkill(Skills.GET_HISTORY_WEEKLY));
        skills.add(new MockSkill(Skills.GET_HISTORY_MONTHLY));
        skills.add(new MockSkill(Skills.GET_HISTORY_LAST_CHANGES));
        skills.add(new MockSkill(Skills.SET_VALUE));
        skills.add(new MockSkill(Skills.CREATE_RULE));
    }

    public class MockSkill implements Skill {

        private String intent;

        public MockSkill(String intent) {
            this.intent = intent;
        }

        @Override
        public String getIntentId() {
            return intent;
        }

        @Override
        public InputStream getTrainingData(String language) throws UnsupportedLanguageException {
            return MockSkill.class.getClassLoader().getResourceAsStream("train/" + language + "/" + intent + ".txt");
        }

        @Override
        public String interpretForVoice(Intent intent, String language) throws InterpretationException {
            return "OK";
        }

        @Override
        public InterpretationResult interpretForChat(Intent intent, String language) throws InterpretationException {
            return new InterpretationResult(language, "OK");
        }

        @Override
        public boolean isSuitableForChat() {
            return true;
        }

        @Override
        public boolean isSuitableForVoice() {
            return true;
        }
    }

    protected Intent interpret(String query) {
        System.out.println("----");
        System.out.println("\"" + query + "\"");
        System.out.println(new TreeMap<>(this.trainer.getScoreMap(query)).descendingMap().toString());
        Intent intent = this.trainer.interpret(query);
        System.out.println(intent.toString());
        return intent;
    }

    protected void checkInterpretation(String intentName, String query, String object, String location) {
        Intent actual = interpret(query);

        assertEquals(intentName, actual.getName());

        if (object != null) {
            assertTrue(actual.getEntities().containsKey("object"));
            assertEquals(object, actual.getEntities().get("object"));
        }

        if (location != null) {
            assertTrue(actual.getEntities().containsKey("location"));
            assertEquals(location, actual.getEntities().get("location"));
        }
    }

    protected void assertIs(String intentName, String query) {
        checkInterpretation(intentName, query, null, null);
    }

    protected void assertIsGetStatus(String query, String object, String location) {
        checkInterpretation(Skills.GET_STATUS, query, object, location);
    }

    protected void assertIsGetStatus(String query) {
        checkInterpretation(Skills.GET_STATUS, query, null, null);
    }

    protected void assertIsActivate(String query, String object, String location) {
        checkInterpretation(Skills.ACTIVATE_OBJECT, query, object, location);
    }

    protected void assertIsActivate(String query) {
        checkInterpretation(Skills.ACTIVATE_OBJECT, query, null, null);
    }

    protected void assertIsDeactivate(String query, String object, String location) {
        checkInterpretation(Skills.DEACTIVATE_OBJECT, query, object, location);
    }

    protected void assertIsDeactivate(String query) {
        checkInterpretation(Skills.DEACTIVATE_OBJECT, query, null, null);
    }

    protected void assertIsSetValue(String query, String object, String location) {
        checkInterpretation(Skills.SET_VALUE, query, object, location);
    }

    protected void assertIsSetValue(String query) {
        checkInterpretation(Skills.SET_VALUE, query, null, null);
    }

}