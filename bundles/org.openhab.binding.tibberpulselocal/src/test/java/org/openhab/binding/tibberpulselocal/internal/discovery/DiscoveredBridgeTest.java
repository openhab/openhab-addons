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
package org.openhab.binding.tibberpulselocal.internal.discovery;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.binding.tibberpulselocal.internal.TibberPulseLocalBindingConstants.*;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.tibberpulselocal.internal.handler.BridgeHandler;
import org.openhab.binding.tibberpulselocal.internal.handler.BridgeHandler.StreamConnection;
import org.openhab.binding.tibberpulselocal.internal.sml.SmlFrameBuilder;
import org.openhab.binding.tibberpulselocal.internal.stream.StreamListener;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

/**
 * Tests the scenarios of the {@code discovery} spec that join bridge and meter discovery: "Meter below a discovered
 * bridge" and "Running stream undisturbed". The {@code bridge} thing is built from the participant result, as the
 * framework does when the user accepts an entry.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(30)
class DiscoveredBridgeTest {

    private static final long WAIT_MILLIS = 10_000;
    private static final String DEVICE_A = "aaaaaaaaaaaaaaaa";
    private static final String PASSWORD = "s3cret-Pw";

    private final BridgeMDNSDiscoveryParticipant participant = new BridgeMDNSDiscoveryParticipant();
    private final List<StreamListener> listeners = new CopyOnWriteArrayList<>();
    private final BlockingQueue<DiscoveryResult> results = new LinkedBlockingQueue<>();
    private @Nullable Bridge bridge;
    private @Nullable BridgeHandler handler;
    private @Nullable MeterDiscoveryService service;

    @BeforeEach
    void setUp() throws Exception {
        ServiceInfo info = mock(ServiceInfo.class);
        when(info.getName()).thenReturn("Tibber Bridge Web Server");
        when(info.getServer()).thenReturn("Bridge01234567.local.");
        when(info.getInet4Addresses()).thenReturn(
                new Inet4Address[] { (Inet4Address) InetAddress.getByAddress(new byte[] { -64, 0, 2, 20 }) });

        // What the framework does with an accepted entry: the configuration parameter comes from the entry, the
        // password from the user.
        DiscoveryResult entry = participant.createResult(info);
        assertNotNull(entry);
        Object offeredHostname = entry.getProperties().get(CONFIG_HOSTNAME);
        assertNotNull(offeredHostname);
        Bridge b = BridgeBuilder.create(THING_TYPE_BRIDGE, entry.getThingUID())
                .withConfiguration(new Configuration(Map.of(CONFIG_HOSTNAME, offeredHostname, "password", PASSWORD)))
                .build();
        BridgeHandler h = new BridgeHandler(b, (host, password, name, listener) -> {
            listeners.add(listener);
            return new StreamConnection() {
                @Override
                public void start() {
                }

                @Override
                public void stop() {
                }
            };
        });
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        doAnswer(invocation -> {
            b.setStatusInfo(invocation.getArgument(1, ThingStatusInfo.class));
            return null;
        }).when(callback).statusUpdated(any(), any());
        h.setCallback(callback);
        b.setHandler(h);
        h.initialize();
        listeners.get(0).onStreamActive();
        MeterDiscoveryService s = new MeterDiscoveryService();
        s.setThingHandler(h);
        s.addDiscoveryListener(new DiscoveryListener() {
            @Override
            public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
                results.add(result);
            }

            @Override
            public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
            }

            @Override
            public @Nullable Collection<ThingUID> removeOlderResults(DiscoveryService source, Instant timestamp,
                    @Nullable Collection<ThingTypeUID> thingTypeUIDs, @Nullable ThingUID bridgeUID) {
                return null;
            }
        });
        bridge = b;
        handler = h;
        service = s;
    }

    @AfterEach
    void tearDown() {
        MeterDiscoveryService s = service;
        if (s != null) {
            s.deactivate();
            s.dispose();
        }
        BridgeHandler h = handler;
        if (h != null) {
            h.dispose();
        }
    }

    private static byte[] message(String deviceId) {
        byte[] frame = new SmlFrameBuilder().signed(SmlFrameBuilder.POWER, SmlFrameBuilder.UNIT_W, 0, 100, 8)
                .unsigned(SmlFrameBuilder.ENERGY_IMPORT, SmlFrameBuilder.UNIT_WH, -1, BigInteger.valueOf(1), 8).build();
        return SmlFrameBuilder.concat(("<device:" + deviceId + " topic:\"sml\">").getBytes(StandardCharsets.US_ASCII),
                frame);
    }

    private <T> T required(@Nullable T value) {
        assertNotNull(value);
        return value;
    }

    @Test
    void whenTheBridgeThingWasCreatedFromAnEntryThenTheMeterEntryIsBelowItsThingUid() throws Exception {
        MeterDiscoveryService s = required(service);
        s.initialize();

        listeners.get(0).onMessage(message(DEVICE_A));

        DiscoveryResult result = results.poll(WAIT_MILLIS, TimeUnit.MILLISECONDS);
        assertNotNull(result);
        assertEquals(new ThingUID("tibberpulselocal:bridge:bridge01234567"), required(bridge).getUID());
        assertEquals(new ThingUID("tibberpulselocal:meter:bridge01234567:aaaaaaaaaaaaaaaa"), result.getThingUID());
        assertEquals(required(bridge).getUID(), result.getBridgeUID());
        assertEquals(DEVICE_A, result.getProperties().get(CONFIG_DEVICE_ID));
        assertEquals("192.0.2.20", required(bridge).getConfiguration().get(CONFIG_HOSTNAME));
    }
}
