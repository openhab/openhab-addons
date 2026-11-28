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
package org.openhab.binding.knx.internal.rest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.rest.dto.StreamAliveDTO;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

/**
 * Checks the client of a real event stream: how it builds an event, counts the sends that have not been written,
 * and gives up when the sink is closed or a send fails.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class SseStreamClientTest {
    private Sse sse = mock(Sse.class);
    private OutboundSseEvent.Builder builder = mock(OutboundSseEvent.Builder.class);
    private SseEventSink sink = mock(SseEventSink.class);
    private OutboundSseEvent event = mock(OutboundSseEvent.class);

    @BeforeEach
    void setUp() {
        sse = mock(Sse.class);
        builder = mock(OutboundSseEvent.Builder.class, RETURNS_SELF);
        sink = mock(SseEventSink.class);
        event = mock(OutboundSseEvent.class);
        when(sse.newEventBuilder()).thenReturn(builder);
        when(builder.build()).thenReturn(event);
    }

    @Test
    void anEventIsNamedJsonAndCountedUntilItIsWritten() {
        CompletableFuture<Object> written = new CompletableFuture<>();
        doReturn(written).when(sink).send(any());
        SseStreamClient client = new SseStreamClient(sink, sse);
        StreamAliveDTO alive = new StreamAliveDTO();

        assertTrue(client.send("alive", alive));

        verify(builder).name("alive");
        verify(builder).mediaType(MediaType.APPLICATION_JSON_TYPE);
        verify(builder).data(alive);
        verify(sink).send(event);
        assertEquals(1, client.pending());
        written.complete("written");
        assertEquals(0, client.pending());
    }

    @Test
    void aFailedSendClosesTheSinkAndEndsTheClient() {
        CompletableFuture<Object> written = new CompletableFuture<>();
        doReturn(written).when(sink).send(any());
        SseStreamClient client = new SseStreamClient(sink, sse);
        assertTrue(client.send("alive", new StreamAliveDTO()));

        written.completeExceptionally(new IllegalStateException("made-up failure"));

        assertEquals(0, client.pending());
        verify(sink).close();
        assertFalse(client.send("alive", new StreamAliveDTO()));
        verify(sink, times(1)).send(any());
    }

    @Test
    void aClosedSinkTakesNoEvent() {
        when(sink.isClosed()).thenReturn(true);
        SseStreamClient client = new SseStreamClient(sink, sse);

        assertFalse(client.send("alive", new StreamAliveDTO()));

        verify(sink, never()).send(any());
    }

    @Test
    void itIsFinishedOnceClosedOrAfterAFailedSendOrWithAClosedSinkAndNotBefore() {
        CompletableFuture<Object> written = new CompletableFuture<>();
        doReturn(written).when(sink).send(any());
        SseStreamClient failing = new SseStreamClient(sink, sse);
        assertFalse(failing.finished());
        assertTrue(failing.send("alive", new StreamAliveDTO()));
        assertFalse(failing.finished());
        written.completeExceptionally(new IllegalStateException("made-up failure"));
        assertTrue(failing.finished());

        SseStreamClient closed = new SseStreamClient(mock(SseEventSink.class), sse);
        closed.close();
        assertTrue(closed.finished());

        SseEventSink closedSink = mock(SseEventSink.class);
        when(closedSink.isClosed()).thenReturn(true);
        assertTrue(new SseStreamClient(closedSink, sse).finished());
    }

    @Test
    void askingWhetherItIsFinishedSendsNothing() {
        SseStreamClient client = new SseStreamClient(sink, sse);

        client.finished();

        verify(sink, never()).send(any());
        verify(sink, never()).close();
    }

    @Test
    void aSendThatThrowsEndsTheClient() {
        when(sink.send(any())).thenThrow(new IllegalStateException("made-up failure"));
        SseStreamClient client = new SseStreamClient(sink, sse);

        assertFalse(client.send("alive", new StreamAliveDTO()));

        assertEquals(0, client.pending());
        verify(sink).close();
    }
}
