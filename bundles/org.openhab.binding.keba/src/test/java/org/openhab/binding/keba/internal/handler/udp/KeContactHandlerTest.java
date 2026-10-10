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
package org.openhab.binding.keba.internal.handler.udp;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.openhab.binding.keba.internal.handler.KeContactProtocolHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests for the legacy UDP handler.
 *
 * @author Michael Weger - Initial contribution
 */
class KeContactHandlerTest {

    @Test
    void probesOnlyRequestedReportsAndRejectsMalformedResponses() {
        Thing thing = ThingBuilder.create(new ThingTypeUID("keba", "kecontact"), new ThingUID("keba:kecontact:probe"))
                .build();
        KeContactTransceiver transceiver = Objects.requireNonNull(mock(KeContactTransceiver.class));
        KeContactProtocolHandler.Listener listener = Objects
                .requireNonNull(mock(KeContactProtocolHandler.Listener.class));
        KeContactHandler handler = new KeContactHandler(thing, transceiver, new Configuration(Map.of()), listener);
        when(transceiver.send(eq("report 1"), eq(handler))).thenReturn(response("{\"ID\":1,\"Product\":\"P30\"}"));
        assertTrue(handler.readReport(1));
        verify(transceiver, never()).send(eq("report 2"), any());
        verify(transceiver, never()).send(eq("report 3"), any());
        when(transceiver.send(eq("report 1"), eq(handler))).thenReturn(response("TCH-OK"));
        assertFalse(handler.readReport(1));
        when(transceiver.send(eq("report 1"), eq(handler))).thenReturn(response("{\"ID\":2,\"State\":3}"));
        assertFalse(handler.readReport(1));
    }

    @Test
    void sendsDelayedCurrentAndUnlockCommandsThroughExistingChannels() {
        Thing thing = ThingBuilder
                .create(new ThingTypeUID("keba", "kecontact"), new ThingUID("keba:kecontact:commands")).build();
        KeContactTransceiver transceiver = Objects.requireNonNull(mock(KeContactTransceiver.class));
        KeContactProtocolHandler.Listener listener = Objects
                .requireNonNull(mock(KeContactProtocolHandler.Listener.class));
        KeContactHandler handler = new KeContactHandler(thing, transceiver, null, listener);
        when(transceiver.send(any(), eq(handler))).thenReturn(response("TCH-OK: done"));

        handler.handleCommand(new ChannelUID(thing.getUID(), "maxpresetcurrent"), new QuantityType<>(12, Units.AMPERE));
        verify(transceiver).send("currtime 12000 1", handler);
        handler.handleCommand(new ChannelUID(thing.getUID(), "maxpresetcurrentrange"), OnOffType.ON);
        verify(transceiver).send("currtime 63000 1", handler);
        handler.handleCommand(new ChannelUID(thing.getUID(), "unlockplug"), OnOffType.ON);
        verify(transceiver).send("unlock", handler);
        handler.handleCommand(new ChannelUID(thing.getUID(), "unlockplug"), OnOffType.OFF);
        verify(transceiver, times(1)).send("unlock", handler);
        verify(listener).stateUpdated("unlockplug", OnOffType.OFF);
    }

    @Test
    void udpRuleActionsSendExplicitCurrentDelayAndCompleteFailsafeTuple() {
        Thing thing = ThingBuilder
                .create(new ThingTypeUID("keba", "kecontact"), new ThingUID("keba:kecontact:udp-actions")).build();
        KeContactTransceiver transceiver = Objects.requireNonNull(mock(KeContactTransceiver.class));
        KeContactHandler handler = new KeContactHandler(thing, transceiver);
        when(transceiver.send(any(), eq(handler))).thenReturn(response("TCH-OK: done"));
        KeContactActions actions = new KeContactActions();
        actions.setThingHandler(handler);

        actions.setChargingCurrent(16000, 12);
        verify(transceiver).send("currtime 16000 12", handler);
        actions.setChargingCurrent(64000, 12);
        verify(transceiver, never()).send("currtime 64000 12", handler);

        actions.setFailsafe(12000, 30, true);
        verify(transceiver).send("failsafe 30 12000 1", handler);
        verify(transceiver, never()).send(eq("report 2"), eq(handler));
        actions.setFailsafe(4000, 30, true);
        actions.setFailsafe(12000, 4, true);
        verify(transceiver, never()).send("failsafe 30 4000 1", handler);
        verify(transceiver, never()).send("failsafe 4 12000 1", handler);
    }

    @Test
    void udpStopUsesTheSuppliedRfidTagAndDoesNotReadReport100() {
        Thing thing = ThingBuilder.create(new ThingTypeUID("keba", "kecontact"), new ThingUID("keba:kecontact:stop"))
                .build();
        KeContactTransceiver transceiver = Objects.requireNonNull(mock(KeContactTransceiver.class));
        KeContactProtocolHandler.Listener listener = Objects
                .requireNonNull(mock(KeContactProtocolHandler.Listener.class));
        KeContactHandler handler = new KeContactHandler(thing, transceiver, null, listener);
        when(transceiver.send(eq("stop f287506300000000"), eq(handler))).thenReturn(response("TCH-OK: done"));

        handler.handleCommand(new ChannelUID(thing.getUID(), "udpstop"), new StringType(" f287506300000000 "));
        handler.handleCommand(new ChannelUID(thing.getUID(), "udpstop"), new StringType("0000000000000000"));
        handler.handleCommand(new ChannelUID(thing.getUID(), "udpstop"), new StringType("not-an-rfid-tag"));
        verify(transceiver).send("stop f287506300000000", handler);
        verify(transceiver, times(1)).send(eq("stop f287506300000000"), eq(handler));
        verify(transceiver, never()).send(eq("report 100"), eq(handler));
    }

    @Test
    void writesFailsafeCompoundCommandPreservingOtherSettingAndSaveFlag() {
        Thing thing = ThingBuilder
                .create(new ThingTypeUID("keba", "kecontact"), new ThingUID("keba:kecontact:failsafe")).build();
        KeContactTransceiver transceiver = Objects.requireNonNull(mock(KeContactTransceiver.class));
        KeContactProtocolHandler.Listener listener = Objects
                .requireNonNull(mock(KeContactProtocolHandler.Listener.class));
        KeContactHandler handler = new KeContactHandler(thing, transceiver, null, listener);
        when(transceiver.send(any(), eq(handler))).thenReturn(response("TCH-OK: done"));
        handler.onData(response("{\"ID\":2,\"State\":3,\"Curr FS\":6000,\"Tmo FS\":60}"));

        handler.handleCommand(new ChannelUID(thing.getUID(), "failsafecurrent"), new QuantityType<>(8, Units.AMPERE));
        verify(transceiver).send("failsafe 60 8000 0", handler);
        handler.handleCommand(new ChannelUID(thing.getUID(), "failsafetimeout"), new QuantityType<>(5, Units.SECOND));
        verify(transceiver).send("failsafe 5 8000 0", handler);
        handler.handleCommand(new ChannelUID(thing.getUID(), "failsafetimeout"), new QuantityType<>(4, Units.SECOND));
        verify(transceiver, never()).send("failsafe 4 8000 0", handler);
        handler.handleCommand(new ChannelUID(thing.getUID(), "failsafetimeout"), new QuantityType<>(30, Units.SECOND));
        verify(transceiver).send("failsafe 30 8000 0", handler);
        handler.handleCommand(new ChannelUID(thing.getUID(), "failsafepersist"), OnOffType.ON);
        verify(transceiver).send("failsafe 30 8000 1", handler);
        verify(listener).stateUpdated("failsafepersist", OnOffType.OFF);
    }

    @Test
    void readsReportTwoBeforeMergingAnUncachedFailsafeValue() {
        Thing thing = ThingBuilder
                .create(new ThingTypeUID("keba", "kecontact"), new ThingUID("keba:kecontact:failsafe-read")).build();
        KeContactTransceiver transceiver = Objects.requireNonNull(mock(KeContactTransceiver.class));
        KeContactProtocolHandler.Listener listener = Objects
                .requireNonNull(mock(KeContactProtocolHandler.Listener.class));
        KeContactHandler handler = new KeContactHandler(thing, transceiver, null, listener);
        when(transceiver.send(eq("report 2"), eq(handler)))
                .thenReturn(response("{\"ID\":2,\"State\":3,\"Curr FS\":6000,\"Tmo FS\":60}"));
        when(transceiver.send(eq("failsafe 60 8000 0"), eq(handler))).thenReturn(response("TCH-OK: done"));

        handler.handleCommand(new ChannelUID(thing.getUID(), "failsafecurrent"), new QuantityType<>(8, Units.AMPERE));

        verify(transceiver).send("report 2", handler);
        verify(transceiver).send("failsafe 60 8000 0", handler);
    }

    private static ByteBuffer response(String json) {
        return Objects.requireNonNull(ByteBuffer.wrap(json.getBytes(StandardCharsets.US_ASCII)));
    }

    @Test
    void ignoresShortProductValuesWithoutCrashingPollingParsing() {
        KeContactHandler handler = new KeContactHandler(mock(Thing.class), mock(KeContactTransceiver.class));
        ByteBuffer response = Objects.requireNonNull(ByteBuffer.wrap("{\"Product\":\"P30\"}".getBytes()));

        assertDoesNotThrow(() -> handler.onData(response));
    }

    @Test
    void ignoresUnknownProductSeriesWithoutCrashingPollingParsing() {
        KeContactHandler handler = new KeContactHandler(mock(Thing.class), mock(KeContactTransceiver.class));
        ByteBuffer response = Objects
                .requireNonNull(ByteBuffer.wrap("{\"Product\":\"KC-P30-123456Z-XXX\"}".getBytes()));

        assertDoesNotThrow(() -> handler.onData(response));
    }
}
