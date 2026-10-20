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
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.openhab.binding.keba.internal.handler.KeContactProtocolHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.UnDefType;

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
    void report100PublishesSessionLifecycleAndUptimeBasedDuration() {
        Thing thing = ThingBuilder.create(new ThingTypeUID("keba", "kecontact"), new ThingUID("keba:kecontact:session"))
                .build();
        KeContactTransceiver transceiver = Objects.requireNonNull(mock(KeContactTransceiver.class));
        KeContactProtocolHandler.Listener listener = Objects
                .requireNonNull(mock(KeContactProtocolHandler.Listener.class));
        KeContactHandler handler = new KeContactHandler(thing, transceiver, null, listener);

        handler.onData(response("""
                {"ID":100,"Session ID":4294967295,"started":"2026-10-20 10:00:00.000",
                 "started[s]":100,"ended[s]":0,"reason":0,"Sec":160}
                """));
        verify(listener).stateUpdated("sessionid", new DecimalType(4294967295L));
        verify(listener).stateUpdated("session", OnOffType.ON);
        verify(listener).stateUpdated("sessionstart", new DateTimeType(Instant.parse("2026-10-20T10:00:00Z")));
        verify(listener).stateUpdated("sessionduration", new QuantityType<>(60, Units.SECOND));

        handler.onData(response("""
                {"ID":100,"Session ID":4294967295,"started":"2026-10-20 10:00:00.000",
                 "started[s]":100,"ended[s]":190,"reason":1,"Sec":250}
                """));
        verify(listener).stateUpdated("session", OnOffType.OFF);
        verify(listener).stateUpdated("sessionduration", new QuantityType<>(90, Units.SECOND));

        handler.onData(response("""
                {"ID":100,"Session ID":1,"started":"100","started[s]":100,"ended[s]":0,"reason":0,"Sec":200}
                """));
        verify(listener).stateUpdated("sessionstart", UnDefType.UNDEF);
        verify(listener).stateUpdated("sessionduration", new QuantityType<>(100, Units.SECOND));

        handler.onData(response("""
                {"ID":100,"Session ID":0,"started":"0","started[s]":0,"ended[s]":0,"reason":0,"Sec":20}
                """));
        verify(listener, times(2)).stateUpdated("session", OnOffType.OFF);
        verify(listener).stateUpdated("sessionstart", UnDefType.UNDEF);
        verify(listener, never()).stateUpdated("sessionduration", UnDefType.UNDEF);
        verify(listener, never()).stateUpdated("sessionid", new DecimalType(0));
    }

    @Test
    void report100RolloverReadsCompletedSessionWithoutClearingItems() {
        Thing thing = ThingBuilder
                .create(new ThingTypeUID("keba", "kecontact"), new ThingUID("keba:kecontact:session-rollover")).build();
        KeContactTransceiver transceiver = Objects.requireNonNull(mock(KeContactTransceiver.class));
        KeContactProtocolHandler.Listener listener = Objects
                .requireNonNull(mock(KeContactProtocolHandler.Listener.class));
        KeContactHandler handler = new KeContactHandler(thing, transceiver, null, listener);
        ByteBuffer empty = response("""
                {"ID":100,"Session ID":0,"started":"0","started[s]":0,"ended[s]":0,"reason":0,
                 "E pres":0,"RFID tag":"0000000000000000","RFID class":"0000000000000000"}
                """);
        when(transceiver.send("report 100", handler)).thenReturn(response("""
                {"ID":100,"Session ID":42,"started":"2026-10-20 10:00:00.000",
                 "started[s]":100,"ended[s]":0,"reason":0,"Sec":160}
                """), empty, empty);
        when(transceiver.send("report 101", handler)).thenReturn(response("""
                {"ID":101,"Session ID":42,"started":"2026-10-20 10:00:00.000",
                 "started[s]":100,"ended[s]":190,"reason":1,"Sec":250,"E pres":1234,
                 "RFID tag":"1234567890abcdef","RFID class":"0000000000000001","Curr HW":0}
                """), null);

        assertTrue(handler.readReport(100));
        clearInvocations(listener);
        assertTrue(handler.readReport(100));
        verify(transceiver).send("report 101", handler);
        verify(listener, times(2)).stateUpdated("session", OnOffType.OFF);
        verify(listener).stateUpdated("sessionid", new DecimalType(42));
        verify(listener).stateUpdated("sessionstart", new DateTimeType(Instant.parse("2026-10-20T10:00:00Z")));
        verify(listener).stateUpdated("sessionduration", new QuantityType<>(90, Units.SECOND));
        verify(listener).stateUpdated("sessionconsumption", new QuantityType<>(123.4, Units.WATT_HOUR));
        verify(listener).stateUpdated("sessionrfidtag", new StringType("1234567890abcdef"));
        verify(listener).stateUpdated("sessionrfidclass", new StringType("0000000000000001"));
        verify(listener, never()).stateUpdated(eq("maxsystemcurrent"), any());
        verify(listener, never()).stateUpdated(any(), eq(UnDefType.UNDEF));
        verify(listener, never()).stateUpdated("sessionid", new DecimalType(0));
        verify(listener, never()).stateUpdated("sessionconsumption", new QuantityType<>(0, Units.WATT_HOUR));

        clearInvocations(listener);
        assertTrue(handler.readReport(100));
        verify(listener).stateUpdated("session", OnOffType.OFF);
        verify(listener, never()).stateUpdated(eq("sessionstart"), any());
        verify(listener, never()).stateUpdated(eq("sessionduration"), any());
        verify(listener, never()).stateUpdated(eq("sessionid"), any());

        clearInvocations(listener);
        handler.onData(response("{\"ID\":100,\"Session ID\":42,\"started\":\"invalid\"}"));
        handler.onData(response("{\"ID\":3,\"E pres\":0}"));
        verify(listener, never()).stateUpdated(eq("sessionstart"), any());
        verify(listener, never()).stateUpdated(eq("sessionduration"), any());
        verify(listener, never()).stateUpdated(eq("sessionconsumption"), any());

        clearInvocations(listener);
        handler.onData(response("""
                {"ID":100,"Session ID":43,"started":"2026-10-20 11:00:00.000",
                 "started[s]":300,"ended[s]":0,"reason":0,"Sec":300,"E pres":0}
                """));
        verify(listener).stateUpdated("session", OnOffType.ON);
        verify(listener).stateUpdated("sessionid", new DecimalType(43));
        verify(listener).stateUpdated("sessionstart", new DateTimeType(Instant.parse("2026-10-20T11:00:00Z")));
        verify(listener).stateUpdated("sessionduration", new QuantityType<>(0, Units.SECOND));
        verify(listener).stateUpdated("sessionconsumption", new QuantityType<>(0, Units.WATT_HOUR));
        handler.onData(response("{\"ID\":3,\"E pres\":100}"));
        verify(listener).stateUpdated("sessionconsumption", new QuantityType<>(10, Units.WATT_HOUR));
    }

    @Test
    void report100LeavesInvalidSessionTimesUndefined() {
        Thing thing = ThingBuilder
                .create(new ThingTypeUID("keba", "kecontact"), new ThingUID("keba:kecontact:invalid-session-time"))
                .build();
        KeContactTransceiver transceiver = Objects.requireNonNull(mock(KeContactTransceiver.class));
        KeContactProtocolHandler.Listener listener = Objects
                .requireNonNull(mock(KeContactProtocolHandler.Listener.class));
        KeContactHandler handler = new KeContactHandler(thing, transceiver, null, listener);

        handler.onData(response("""
                {"ID":100,"Session ID":1,"started":"2026-02-30 10:00:00.000",
                 "started[s]":200,"ended[s]":0,"reason":0,"Sec":100}
                """));
        handler.onData(response("""
                {"ID":100,"Session ID":2,"started":null,"started[s]":"invalid","ended[s]":0,"reason":0,"Sec":300}
                """));
        handler.onData(response("""
                {"ID":100,"Session ID":3,"started":"300","started[s]":300,"ended[s]":0,"reason":1,"Sec":400}
                """));
        handler.onData(response("{\"ID\":100,\"Session ID\":4}"));

        verify(listener, times(4)).stateUpdated("sessionstart", UnDefType.UNDEF);
        verify(listener, times(4)).stateUpdated("sessionduration", UnDefType.UNDEF);
        verify(listener, times(2)).stateUpdated("session", OnOffType.ON);
        verify(listener).stateUpdated("session", OnOffType.OFF);
        verify(listener).stateUpdated("session", UnDefType.UNDEF);
    }

    @Test
    void outputAcceptsSwitchCommandsAndIntegerOutputModes() {
        Thing thing = ThingBuilder.create(new ThingTypeUID("keba", "kecontact"), new ThingUID("keba:kecontact:output"))
                .build();
        KeContactTransceiver transceiver = Objects.requireNonNull(mock(KeContactTransceiver.class));
        KeContactHandler handler = new KeContactHandler(thing, transceiver);
        ChannelUID output = new ChannelUID(thing.getUID(), "output");

        handler.handleCommand(output, OnOffType.ON);
        handler.handleCommand(output, OnOffType.OFF);
        handler.handleCommand(output, new DecimalType(0));
        handler.handleCommand(output, new DecimalType(1));
        handler.handleCommand(output, new DecimalType(10));
        handler.handleCommand(output, new DecimalType(150));
        handler.handleCommand(output, new DecimalType(151));
        for (String value : new String[] { "-1", "2", "9", "10.5", "2147483648" }) {
            handler.handleCommand(output, new DecimalType(value));
        }
        handler.handleCommand(output, new StringType("10"));

        verify(transceiver, times(2)).send("output 0", handler);
        verify(transceiver, times(2)).send("output 1", handler);
        verify(transceiver).send("output 10", handler);
        verify(transceiver).send("output 150", handler);
        verify(transceiver).send("output 151", handler);
        verify(transceiver, times(7)).send(any(), eq(handler));
    }

    @Test
    void outputReadbackPreservesNumericOutputModes() {
        Thing thing = ThingBuilder
                .create(new ThingTypeUID("keba", "kecontact"), new ThingUID("keba:kecontact:output-readback")).build();
        KeContactTransceiver transceiver = Objects.requireNonNull(mock(KeContactTransceiver.class));
        KeContactProtocolHandler.Listener listener = Objects
                .requireNonNull(mock(KeContactProtocolHandler.Listener.class));
        KeContactHandler handler = new KeContactHandler(thing, transceiver, null, listener);

        for (int output : new int[] { 0, 1, 10, 150 }) {
            handler.onData(response("{\"ID\":2,\"Output\":" + output + "}"));
            verify(listener).stateUpdated("output", new DecimalType(output));
        }
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
        ByteBuffer response = Objects
                .requireNonNull(ByteBuffer.wrap("{\"Product\":\"P30\"}".getBytes(StandardCharsets.US_ASCII)));

        assertDoesNotThrow(() -> handler.onData(response));
    }

    @Test
    void ignoresUnknownProductSeriesWithoutCrashingPollingParsing() {
        KeContactHandler handler = new KeContactHandler(mock(Thing.class), mock(KeContactTransceiver.class));
        ByteBuffer response = Objects.requireNonNull(
                ByteBuffer.wrap("{\"Product\":\"KC-P30-123456Z-XXX\"}".getBytes(StandardCharsets.US_ASCII)));

        assertDoesNotThrow(() -> handler.onData(response));
    }
}
