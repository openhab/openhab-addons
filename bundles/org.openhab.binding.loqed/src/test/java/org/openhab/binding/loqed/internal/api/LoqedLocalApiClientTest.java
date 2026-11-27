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
package org.openhab.binding.loqed.internal.api;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.loqed.internal.LoqedLocalConfiguration;

import com.google.gson.JsonObject;

/**
 * Tests LOQED's binary local command signature format.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class LoqedLocalApiClientTest {
    @Test
    public void signedCommandMatchesKnownVector() throws Exception {
        LoqedLocalApiClient client = createClient();

        byte[] expected = Base64.getDecoder()
                .decode("AAAAAAAAAAACBwAAAABlU/EABlSJLsSkSa0yBnGxx9fOAPnFCh4lK3HsM0ZNan2LPvYDAQI=");

        assertArrayEquals(expected,
                client.createSignedCommand("MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=", 3, 2, 1700000000L));
    }

    @Test
    public void webhookDeleteHashMatchesKnownVector() throws Exception {
        LoqedLocalApiClient client = createClient();

        assertEquals("19c993eb3fb1804186484e5a3d81a200d4abbe5b09a02383a4f6a6cc62737852",
                client.createWebhookDeleteHash(123, 1700000000L));
    }

    @Test
    public void identifiesStaleCallbackForSameRoute() {
        assertTrue(LoqedLocalApiClient.hasSameCallbackPath("http://192.0.2.10:8080/loqed/webhook/instance-a_localhome",
                "https://openhab.example/loqed/webhook/instance-a_localhome"));
        assertFalse(LoqedLocalApiClient.hasSameCallbackPath("http://192.0.2.10:8080/loqed/webhook/instance-b_localhome",
                "https://openhab.example/loqed/webhook/instance-a_localhome"));
    }

    @Test
    public void appliesLocalWebhookStatus() {
        LoqedLockData status = new LoqedLockData();
        status.online = true;
        JsonObject event = new JsonObject();
        event.addProperty("event_type", "STATE_CHANGED_NIGHT_LOCK");
        event.addProperty("requested_state", "NIGHT_LOCK");
        event.addProperty("battery_percentage", 74);
        event.addProperty("battery_type", 1);
        event.addProperty("ble_strength", -1);

        assertFalse(LoqedLocalApiClient.applyWebhook(status, event));

        assertEquals(BoltState.NIGHT_LOCK, status.boltState);
        assertEquals(74, status.batteryPercentage);
        assertEquals("nimh", status.batteryType);
        assertFalse(status.online);

        event = new JsonObject();
        event.addProperty("ble_strength", 42);
        assertTrue(LoqedLocalApiClient.applyWebhook(status, event));
        assertTrue(status.online);
        assertEquals(BoltState.UNKNOWN, status.boltState);
        assertEquals(-1, status.batteryPercentage);
    }

    @Test
    public void appliesMotorStallReachedState() {
        LoqedLockData status = new LoqedLockData();
        status.boltState = BoltState.NIGHT_LOCK;
        JsonObject event = new JsonObject();
        event.addProperty("event_type", "MOTOR_STALL");
        event.addProperty("requested_state", "UNKNOWN");

        LoqedLocalApiClient.applyWebhook(status, event);

        assertEquals(BoltState.UNKNOWN, status.boltState);
    }

    @Test
    public void appliesTouchToLockReachedState() {
        LoqedLockData status = new LoqedLockData();
        status.boltState = BoltState.DAY_LOCK;
        JsonObject event = new JsonObject();
        event.addProperty("event_type", "GO_TO_STATE_TOUCH_TO_LOCK");
        event.addProperty("requested_state", "NIGHT_LOCK");

        LoqedLocalApiClient.applyWebhook(status, event);

        assertEquals(BoltState.NIGHT_LOCK, status.boltState);
    }

    @Test
    public void rejectsMalformedStatusJson() {
        assertThrows(LoqedResponseException.class, () -> LoqedLocalApiClient.parseStatus("{"));
    }

    @Test
    public void leavesUnsupportedLocalStatusValuesUnknown() throws Exception {
        LoqedLockData status = LoqedLocalApiClient
                .parseStatus("{\"battery_percentage\":75,\"bolt_state\":\"night_lock\",\"lock_online\":1}");

        assertNull(status.partyMode);
        assertNull(status.guestAccessMode);
        assertNull(status.twistAssist);
        assertNull(status.touchToConnect);
    }

    private static LoqedLocalApiClient createClient() throws Exception {
        LoqedLocalConfiguration config = new LoqedLocalConfiguration();
        config.host = "192.0.2.1";
        config.bridgeKey = "YWJjZGVmMDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODk=";
        return new LoqedLocalApiClient(mock(HttpClient.class), config);
    }
}
