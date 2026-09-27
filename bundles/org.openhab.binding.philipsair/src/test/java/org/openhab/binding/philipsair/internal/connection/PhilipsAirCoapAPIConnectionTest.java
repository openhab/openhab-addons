/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.philipsair.internal.connection;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.philipsair.internal.PhilipsAirConfiguration;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDataDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the handling of status notifications pushed by CoAP devices in {@link PhilipsAirCoapAPIConnection}.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class PhilipsAirCoapAPIConnectionTest {

    private static final String URI = "coap://127.0.0.1:5683/sys/dev/status";
    private static final String STATUS = "{\"state\":{\"reported\":{\"pwr\":\"1\",\"om\":\"2\",\"rh\":45}}}";

    private final Logger logger = LoggerFactory.getLogger(PhilipsAirCoapAPIConnectionTest.class);
    private final List<PhilipsAirAPIConnection> notifications = new ArrayList<>();

    private @NonNullByDefault({}) PhilipsAirCoapAPIConnection connection;

    @BeforeEach
    public void setUp() {
        PhilipsAirConfiguration config = new PhilipsAirConfiguration();
        config.setHost("127.0.0.1");
        connection = new PhilipsAirCoapAPIConnection(config, notifications::add);
    }

    @AfterEach
    public void tearDown() {
        connection.dispose();
    }

    private String encrypt(String message) {
        String encrypted = PhilipsAirCoapCipher.encryptedMsg(message, 0x10L, logger);
        assertNotNull(encrypted);
        return encrypted;
    }

    @Test
    public void noStatusBeforeFirstNotification() {
        assertNull(connection.getAirPurifierStatus("127.0.0.1"));
        assertNull(connection.getAirPurifierDevice("127.0.0.1"));
        assertNull(connection.getAirPurifierFiltersStatus("127.0.0.1"));
    }

    @Test
    public void notificationIsPassedToListener() {
        assertTrue(connection.processNotification(encrypt(STATUS), URI));

        assertEquals(List.of(connection), notifications);
        PhilipsAirPurifierDataDTO status = connection.getAirPurifierStatus("127.0.0.1");
        assertNotNull(status);
        assertEquals("1", status.getPower());
        assertEquals("2", status.getFanSpeed());
        assertEquals(45f, status.getHumidity());
    }

    @Test
    public void invalidNotificationIsIgnored() {
        assertFalse(connection.processNotification(encrypt("{\"status\":\"success\"}"), URI));
        assertFalse(connection.processNotification("", URI));

        assertTrue(notifications.isEmpty());
        assertNull(connection.getAirPurifierStatus("127.0.0.1"));
    }

    @Test
    public void noNotificationAfterDispose() {
        connection.dispose();

        connection.processNotification(encrypt(STATUS), URI);

        assertTrue(notifications.isEmpty());
    }
}
