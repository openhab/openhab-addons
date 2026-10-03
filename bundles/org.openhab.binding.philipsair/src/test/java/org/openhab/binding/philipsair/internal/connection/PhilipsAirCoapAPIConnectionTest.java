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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.config.CoapConfig;
import org.eclipse.californium.elements.config.Configuration;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.philipsair.internal.PhilipsAirConfiguration;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDataDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDeviceDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierWritableDataDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
        connection = new PhilipsAirCoapAPIConnection(config, notifications::add, null);
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
    public void standardCoapConfigurationIsNotChanged() {
        // the standard configuration is shared with the other bindings using Californium
        Configuration standard = Configuration.getStandard();

        assertNotEquals(CoapConfig.NO_DEDUPLICATOR, standard.get(CoapConfig.DEDUPLICATOR));
        assertNotEquals(20L, standard.get(CoapConfig.ACK_TIMEOUT, TimeUnit.SECONDS));
        assertNotEquals(65L, standard.get(CoapConfig.EXCHANGE_LIFETIME, TimeUnit.SECONDS));
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
    public void recentModelStatusIsTranslated() {
        assertTrue(connection.processNotification(encrypt(
                "{\"state\":{\"reported\":{\"D01S03\":\"Office\",\"D01S05\":\"AC3737/10\",\"D03102\":1,\"D03221\":8}}}"),
                URI));

        PhilipsAirPurifierDataDTO status = connection.getAirPurifierStatus("127.0.0.1");
        assertNotNull(status);
        assertEquals("1", status.getPower());
        assertEquals(8, status.getPm25());
        PhilipsAirPurifierDeviceDTO device = connection.getAirPurifierDevice("127.0.0.1");
        assertNotNull(device);
        assertEquals("AC3737/10", device.getModelId());
    }

    @Test
    public void invalidNotificationIsIgnored() {
        assertFalse(connection.processNotification(encrypt("{\"status\":\"success\"}"), URI));
        assertFalse(connection.processNotification("", URI));

        assertTrue(notifications.isEmpty());
        assertNull(connection.getAirPurifierStatus("127.0.0.1"));
    }

    /**
     * Creates a connection of which the device answers the counter sync with the given content.
     */
    private PhilipsAirCoapAPIConnection connectionAnswering(@Nullable String answer) {
        PhilipsAirConfiguration config = new PhilipsAirConfiguration();
        config.setHost("127.0.0.1");
        return new PhilipsAirCoapAPIConnection(config, notifications::add, null) {
            @Override
            @Nullable
            String requestSync() {
                return answer;
            }
        };
    }

    @Test
    public void deviceAnsweringTheSyncIsReachable() {
        PhilipsAirCoapAPIConnection standby = connectionAnswering("0000002A");
        try {
            assertTrue(standby.pingDevice());
        } finally {
            standby.dispose();
        }
    }

    @Test
    public void deviceWithoutValidSyncAnswerIsNotReachable() {
        for (String answer : new String[] { "", "0000", "zzzzzzzz", null }) {
            PhilipsAirCoapAPIConnection silent = connectionAnswering(answer);
            try {
                assertFalse(silent.pingDevice());
            } finally {
                silent.dispose();
            }
        }
    }

    private record Post(String path, String body) {
    }

    /**
     * A connection of which the exchanges with the device are answered by the test, and of which the clock is set by
     * the test.
     */
    private class FakeDevice extends PhilipsAirCoapAPIConnection {
        final List<Post> posts = new ArrayList<>();
        volatile @Nullable String syncAnswer = "00000020";
        volatile String controlAnswer = "{\"status\":\"success\"}";
        volatile long now = 1_000_000L;

        FakeDevice(@Nullable CoapProfile initialProfile) {
            super(newConfiguration(), notifications::add, initialProfile);
        }

        @Override
        String post(CoapClient client, String server, int port, String resourcePath, String body)
                throws ConnectorException, IOException {
            posts.add(new Post(resourcePath, body));
            String answer = resourcePath.endsWith("/sync") ? syncAnswer : controlAnswer;
            return answer != null ? answer : "";
        }

        @Override
        long currentTimeMillis() {
            return now;
        }

        List<Post> posts(String path) {
            return posts.stream().filter(post -> post.path().equals(path)).toList();
        }
    }

    private static PhilipsAirConfiguration newConfiguration() {
        PhilipsAirConfiguration config = new PhilipsAirConfiguration();
        config.setHost("127.0.0.1");
        config.setRefreshInterval(30);
        return config;
    }

    private static PhilipsAirPurifierWritableDataDTO powerOn() {
        PhilipsAirPurifierWritableDataDTO command = new PhilipsAirPurifierWritableDataDTO();
        command.setPower("1");
        return command;
    }

    private JsonObject decrypt(String body) {
        String json = PhilipsAirCoapCipher.decryptMsg(body, logger);
        assertFalse(json.isEmpty());
        return JsonParser.parseString(json).getAsJsonObject();
    }

    @Test
    public void commandIsSentInTheInitialProfileBeforeAnyStatus() {
        FakeDevice device = new FakeDevice(CoapProfile.BASIC_GEN3);
        try {
            assertEquals(CoapProfile.BASIC_GEN3, device.getDeviceProfile());

            device.sendCommand("power", powerOn());

            assertEquals(1, device.posts("/sys/dev/sync").size());
            assertEquals("00000001", device.posts("/sys/dev/sync").get(0).body());
            List<Post> control = device.posts("/sys/dev/control");
            assertEquals(1, control.size());
            String body = control.get(0).body();
            // the counter of the device plus one
            assertTrue(body.startsWith("00000021"));
            assertEquals(JsonParser.parseString(
                    "{\"state\":{\"desired\":{\"D03102\":1,\"CommandType\":\"app\",\"DeviceId\":\"\",\"EnduserId\":\"1\"}}}"),
                    decrypt(body));
        } finally {
            device.dispose();
        }
    }

    @Test
    public void commandWaitsForTheFirstStatusWithoutInitialProfile() {
        FakeDevice device = new FakeDevice(null);
        try {
            assertNull(device.getDeviceProfile());

            device.sendCommand("power", powerOn());

            assertTrue(device.posts.isEmpty());

            assertTrue(device.processNotification(encrypt(STATUS), URI));
            assertEquals(CoapProfile.CLASSIC, device.getDeviceProfile());
            device.sendCommand("power", powerOn());

            List<Post> control = device.posts("/sys/dev/control");
            assertEquals(1, control.size());
            assertEquals(JsonParser.parseString(
                    "{\"state\":{\"desired\":{\"pwr\":\"1\",\"CommandType\":\"app\",\"DeviceId\":\"\",\"EnduserId\":\"1\"}}}"),
                    decrypt(control.get(0).body()));
        } finally {
            device.dispose();
        }
    }

    @Test
    public void statusOfAnotherProfileReplacesTheInitialProfile() {
        FakeDevice device = new FakeDevice(CoapProfile.BASIC_GEN3);
        try {
            assertTrue(device.processNotification(
                    encrypt("{\"state\":{\"reported\":{\"D01S05\":\"AC3737/10\",\"D03102\":1,\"D03221\":8}}}"), URI));
            assertEquals(CoapProfile.AC3737, device.getDeviceProfile());
        } finally {
            device.dispose();
        }
    }

    @Test
    public void commandTheProfileCannotExpressIsNotSent() {
        FakeDevice device = new FakeDevice(CoapProfile.BASIC_GEN3);
        try {
            PhilipsAirPurifierWritableDataDTO command = new PhilipsAirPurifierWritableDataDTO();
            command.setMode("P");

            device.sendCommand("mode", command);

            assertTrue(device.posts.isEmpty());
        } finally {
            device.dispose();
        }
    }

    @Test
    public void commandIsNotSentWhenTheSyncFailed() {
        for (String answer : new String[] { "", "0000", "zzzzzzzz", null }) {
            FakeDevice device = new FakeDevice(CoapProfile.BASIC_GEN3);
            try {
                device.syncAnswer = answer;

                device.sendCommand("power", powerOn());

                assertEquals(1, device.posts("/sys/dev/sync").size());
                assertTrue(device.posts("/sys/dev/control").isEmpty());
            } finally {
                device.dispose();
            }
        }
    }

    @Test
    public void rejectedCommandIsSentOnce() {
        FakeDevice device = new FakeDevice(CoapProfile.BASIC_GEN3);
        try {
            device.controlAnswer = "{\"status\":\"failed\"}";

            device.sendCommand("power", powerOn());

            assertEquals(1, device.posts("/sys/dev/control").size());
        } finally {
            device.dispose();
        }
    }

    @Test
    public void staleStatusIsNotReportedUnlessTheDeviceAnswersPings() {
        FakeDevice device = new FakeDevice(null);
        try {
            assertTrue(device.processNotification(encrypt(STATUS), URI));
            assertNotNull(device.getAirPurifierStatus("127.0.0.1"));

            // the status is valid for at least a minute
            device.now += 59_999;
            assertNotNull(device.getAirPurifierStatus("127.0.0.1"));
            assertNotNull(device.getAirPurifierDevice("127.0.0.1"));
            assertNotNull(device.getAirPurifierFiltersStatus("127.0.0.1"));

            device.now += 1;
            assertNull(device.getAirPurifierStatus("127.0.0.1"));
            assertNull(device.getAirPurifierDevice("127.0.0.1"));
            assertNull(device.getAirPurifierFiltersStatus("127.0.0.1"));

            // a device that answers is still reachable, so its last status is still valid
            assertTrue(device.pingDevice());
            assertNotNull(device.getAirPurifierStatus("127.0.0.1"));
            device.now += 59_999;
            assertNotNull(device.getAirPurifierStatus("127.0.0.1"));
        } finally {
            device.dispose();
        }
    }

    @Test
    public void noNotificationAfterDispose() {
        connection.dispose();

        connection.processNotification(encrypt(STATUS), URI);

        assertTrue(notifications.isEmpty());
    }
}
