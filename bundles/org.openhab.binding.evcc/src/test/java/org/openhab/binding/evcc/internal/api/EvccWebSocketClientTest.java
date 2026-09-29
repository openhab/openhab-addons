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
package org.openhab.binding.evcc.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Tests for {@link EvccWebSocketClient}, focused on verifying that only the initial
 * "welcome" message received after a (re)connect is treated as a full state, while
 * every subsequent message - including multi-key deltas that carry nested objects or
 * arrays ("multi-shard" deltas) - is routed as a partial update.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccWebSocketClientTest {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final List<JsonObject> fullStates = new ArrayList<>();
    private final Map<String, JsonElement> partialUpdates = new LinkedHashMap<>();
    private EvccWebSocketClient client = createClient();

    private EvccWebSocketClient createClient() {
        return new EvccWebSocketClient("ws://evcc/ws", scheduler, fullStates::add,
                (key, value) -> partialUpdates.put(key, value), () -> {
                }, () -> {
                });
    }

    @BeforeEach
    public void setup() {
        fullStates.clear();
        partialUpdates.clear();
        client = createClient();
    }

    @AfterEach
    public void tearDown() {
        client.stop();
        scheduler.shutdownNow();
    }

    @Test
    public void onlyWelcomeMessageIsTreatedAsFullStateAndLaterMultiShardDeltaIsPartial() {
        client.handleConnected(mock(Session.class));

        JsonObject welcomeGrid = new JsonObject();
        welcomeGrid.addProperty("power", 100);
        JsonObject welcome = new JsonObject();
        welcome.add("grid", welcomeGrid);
        welcome.addProperty("pvPower", 500);

        client.handleMessage(welcome);

        assertEquals(1, fullStates.size());
        assertEquals(welcome, fullStates.get(0));
        assertTrue(partialUpdates.isEmpty());

        // A later delta carrying multiple shards (a nested object and an array) must not be
        // mistaken for a new full state, even though it structurally resembles one.
        JsonObject deltaGrid = new JsonObject();
        deltaGrid.addProperty("power", 200);
        JsonArray forecastShard = new JsonArray();
        forecastShard.add(1);
        JsonObject multiShardDelta = new JsonObject();
        multiShardDelta.add("grid", deltaGrid);
        multiShardDelta.add("someForecastShard", forecastShard);

        client.handleMessage(multiShardDelta);

        assertEquals(1, fullStates.size());
        assertEquals(deltaGrid, partialUpdates.get("grid"));
        assertEquals(forecastShard, partialUpdates.get("someForecastShard"));
    }

    @Test
    public void reconnectAwaitsANewWelcomeMessage() {
        client.handleConnected(mock(Session.class));
        JsonObject firstWelcome = new JsonObject();
        firstWelcome.addProperty("pvPower", 500);
        client.handleMessage(firstWelcome);
        assertEquals(1, fullStates.size());

        client.handleClosed(1000, "test");
        client.handleConnected(mock(Session.class));

        JsonObject secondWelcome = new JsonObject();
        secondWelcome.addProperty("pvPower", 600);
        client.handleMessage(secondWelcome);

        assertEquals(2, fullStates.size());
        assertEquals(secondWelcome, fullStates.get(1));
    }
}
