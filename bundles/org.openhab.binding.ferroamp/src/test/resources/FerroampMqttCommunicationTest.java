/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ferroamp.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.ferroamp.internal.api.FerroampMqttCommunication;

/**
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class FerroampMqttCommunicationTest {

    private FerroampMqttCommunication communication = new FerroampMqttCommunication("fakseUser", "fakePassword",
            "fakeHost", 1);

    private FerroAmpTestListener listener = new FerroAmpTestListener();

    @BeforeEach
    void setUp() {
        communication = new FerroampMqttCommunication("fakseUser", "fakePassword", "fakeHost", 1);
        communication.registerFerroAmpUpdateListener(listener);
    }

    @Test
    void testProcessMessage_ValidMessageForSso1() throws IOException {

        String messageJsonSso = DataUtil.fromFile("sso.json");

        communication.processMessage(FerroampBindingConstants.SSO_TOPIC, messageJsonSso.getBytes());

        assertNotNull(listener.keyValueMap);
        assertEquals(9, listener.keyValueMap.size());
        assertNotNull(listener.keyValueMap);
        assertEquals("29.766", listener.keyValueMap.get("0-temp"));
    }

    @Test
    void testProcessMessage_ValidMessageForSso2() throws IOException {
        String messageJsonSso = DataUtil.fromFile("multi_sso.json");
        FerroAmpTestListener listener = new FerroAmpTestListener();
        communication.registerFerroAmpUpdateListener(listener);

        communication.processMessage(FerroampBindingConstants.SSO_TOPIC, messageJsonSso.getBytes());

        assertNotNull(listener.keyValueMap);
        assertEquals(18, listener.keyValueMap.size());
        assertNotNull(listener.keyValueMap);
        assertEquals("19.166", listener.keyValueMap.get("1-temp"));
    }

    void testProcessMessage_ValidMessageForEhub() throws IOException {
        String jsonMessage = DataUtil.fromFile("ehub.json");

        communication.processMessage(FerroampBindingConstants.EHUB_TOPIC, jsonMessage.getBytes());

        assertNotNull(listener.keyValueMap);
        assertEquals("-126.28", listener.keyValueMap.get("ploadreactive.L2"));
    }

    @Test
    void testProcessMessage_InvalidMessage() {
        String messageJsonSso = "{}";

        assertThrows(IllegalStateException.class,
                () -> communication.processMessage(FerroampBindingConstants.SSO_TOPIC, messageJsonSso.getBytes()));
    }

    @Test
    void testProcessMessage_EhubTopic() {
        String messageJsonEhub = "{\"key\":{\"val\":\"value\"}}";
        FerroAmpTestListener listener = new FerroAmpTestListener();
        communication.registerFerroAmpUpdateListener(listener);

        communication.processMessage(FerroampBindingConstants.EHUB_TOPIC, messageJsonEhub.getBytes());

        assertNotNull(listener.keyValueMap);
        assertEquals("value", listener.keyValueMap.get("key"));
    }

    @Test
    void testProcessMessage_SsoTopic() {
        String messageJsonSso = "[{\"temp\":{\"val\":\"29.766\"}}]";
        FerroAmpTestListener listener = new FerroAmpTestListener();
        communication.registerFerroAmpUpdateListener(listener);

        communication.processMessage(FerroampBindingConstants.SSO_TOPIC, messageJsonSso.getBytes());

        assertNotNull(listener.keyValueMap);
        assertEquals(1, listener.keyValueMap.size());
        assertEquals("29.766", listener.keyValueMap.get("0-temp"));
    }

    @Test
    void testProcessMessage_EsoTopic() {
        String messageJsonEso = "{\"key\":{\"val\":\"value\"}}";

        communication.processMessage(FerroampBindingConstants.ESO_TOPIC, messageJsonEso.getBytes());

        assertNotNull(listener.keyValueMap);
        assertEquals("value", listener.keyValueMap.get("key"));
    }

    @Test
    void testProcessMessage_EsmTopic() {
        String messageJsonEsm = "{\"key\":{\"val\":\"value\"}}";

        communication.processMessage(FerroampBindingConstants.ESM_TOPIC, messageJsonEsm.getBytes());

        assertNotNull(listener.keyValueMap);
        assertEquals("value", listener.keyValueMap.get("key"));
    }

    @Test
    void testProcessMessage_UnknownTopic() {
        String unknownTopic = "unknown/topic";
        String messageJson = "{\"key\":{\"val\":\"value\"}}";

        communication.processMessage(unknownTopic, messageJson.getBytes());

        // No exception should be thrown, but no updates should occur
        assertTrue(listener.keyValueMap.isEmpty());

    }
}