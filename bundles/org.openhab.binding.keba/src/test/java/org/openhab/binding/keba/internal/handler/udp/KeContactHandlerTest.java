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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.openhab.binding.keba.internal.handler.KeContactProtocolHandler;
import org.openhab.core.config.core.Configuration;
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
