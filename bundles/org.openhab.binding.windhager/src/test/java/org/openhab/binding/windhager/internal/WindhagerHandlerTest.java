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
package org.openhab.binding.windhager.internal;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.windhager.internal.WindhagerBindingConstants.THING_TYPE_BIOWIN2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Tests handler lifecycle behavior.
 *
 * @author Benjamin Utz - Handler lifecycle regression test
 */
@NonNullByDefault
class WindhagerHandlerTest {

    @Test
    void pollingResumesAfterThingUpdate() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicInteger nonceCounter = new AtomicInteger();
        CountDownLatch successfulPolls = new CountDownLatch(2);
        CountDownLatch onlineUpdates = new CountDownLatch(2);
        server.createContext("/", exchange -> handleRequest(exchange, nonceCounter, successfulPolls));
        server.start();

        HttpClient httpClient = new HttpClient();
        Thing thing = createThing(server.getAddress().getPort());
        WindhagerHandler handler = new WindhagerHandler(thing, httpClient);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        doAnswer(invocation -> {
            ThingStatusInfo statusInfo = invocation.getArgument(1);
            if (statusInfo.getStatus() == ThingStatus.ONLINE) {
                onlineUpdates.countDown();
            }
            return null;
        }).when(callback).statusUpdated(any(Thing.class), any(ThingStatusInfo.class));
        handler.setCallback(callback);

        try {
            handler.initialize();
            assertTrue(successfulPolls.await(5, TimeUnit.SECONDS));
            assertTrue(onlineUpdates.await(5, TimeUnit.SECONDS));

            Thing updatedThing = ThingBuilder.create(thing).withLabel("Updated").build();
            handler.thingUpdated(updatedThing);

            assertTrue(successfulPolls.await(5, TimeUnit.SECONDS));
            assertTrue(onlineUpdates.await(5, TimeUnit.SECONDS));
        } finally {
            handler.dispose();
            server.stop(0);
        }
    }

    private static Thing createThing(int port) {
        ThingUID thingUID = new ThingUID(THING_TYPE_BIOWIN2, "test");
        Thing thing = ThingBuilder.create(THING_TYPE_BIOWIN2, thingUID)
                .withConfiguration(new Configuration(Map.of("hostname", "127.0.0.1", "port", port, "username",
                        "test-user", "password", "test-password")))
                .withChannel(ChannelBuilder.create(new ChannelUID(thingUID, "flue-gas-temperature"))
                        .withType(new ChannelTypeUID("windhager", "biowin-value"))
                        .withConfiguration(new Configuration(Map.of("oid", "1/60/0/0/11/0", "refreshInterval", 1)))
                        .build())
                .build();
        return thing;
    }

    private static void handleRequest(HttpExchange exchange, AtomicInteger nonceCounter, CountDownLatch successfulPolls)
            throws IOException {
        if (exchange.getRequestHeaders().getFirst("Authorization") == null) {
            String nonce = "handler-test-nonce-" + nonceCounter.incrementAndGet();
            exchange.getResponseHeaders().add("WWW-Authenticate",
                    "Digest realm=\"BioWin test\", nonce=\"" + nonce + "\", algorithm=MD5, qop=\"auth\"");
            exchange.sendResponseHeaders(401, -1);
        } else {
            successfulPolls.countDown();
            byte[] body = "{\"value\":\"42\"}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
        }
        exchange.close();
    }
}
