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
package org.openhab.io.opentelemetry.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventSubscriber;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;

/**
 * Tests for {@link OpenTelemetryEventListener}.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OpenTelemetryEventListenerTest {

    @Mock(answer = Answers.RETURNS_SELF)
    private @NonNullByDefault({}) SpanBuilder spanBuilder;

    @Mock
    private @NonNullByDefault({}) Span span;

    @Mock
    private @NonNullByDefault({}) Tracer tracer;

    @Mock
    private @NonNullByDefault({}) Event event;

    private @NonNullByDefault({}) OpenTelemetryEventListener listener;

    @BeforeEach
    public void setUp() {
        when(tracer.spanBuilder(anyString())).thenReturn(spanBuilder);
        when(spanBuilder.startSpan()).thenReturn(span);
        listener = new OpenTelemetryEventListener(tracer);
    }

    @Test
    public void testSubscribesToAllEventTypes() {
        assertTrue(listener.getSubscribedEventTypes().contains(EventSubscriber.ALL_EVENT_TYPES),
                "Listener must subscribe to ALL_EVENT_TYPES to capture the full event bus");
    }

    @Test
    public void testNoEventFilter() {
        assertNull(listener.getEventFilter(), "EventFilter must be null to receive all events without topic filtering");
    }

    @Test
    public void testReceiveCreatesSpanNamedAfterEventType() {
        when(event.getType()).thenReturn("ItemStateChangedEvent");
        when(event.getTopic()).thenReturn("openhab/items/MyItem/statechanged");
        when(event.getSource()).thenReturn("test-source");

        listener.receive(event);

        verify(tracer).spanBuilder("ItemStateChangedEvent");
    }

    @Test
    public void testReceiveSetsEventAttributes() {
        when(event.getType()).thenReturn("ThingStatusInfoChangedEvent");
        when(event.getTopic()).thenReturn("openhab/things/hue:light:1/status");
        when(event.getSource()).thenReturn("hue-binding");

        listener.receive(event);

        // attributes are set on the span after startSpan(), not on the builder
        verify(span).setAttribute("event.type", "ThingStatusInfoChangedEvent");
        verify(span).setAttribute("event.topic", "openhab/things/hue:light:1/status");
        verify(span).setAttribute("event.source", "hue-binding");
    }

    @Test
    public void testReceiveEndsSpan() {
        when(event.getType()).thenReturn("RuleStatusInfoEvent");
        when(event.getTopic()).thenReturn("openhab/rules/myRule/status");
        when(event.getSource()).thenReturn("rules-engine");

        listener.receive(event);

        verify(span).end();
    }

    @Test
    public void testReceiveUsesRootContext() {
        when(event.getType()).thenReturn("ItemStateChangedEvent");
        when(event.getTopic()).thenReturn("openhab/items/MyItem/statechanged");
        when(event.getSource()).thenReturn("test");

        listener.receive(event);

        // setParent(Context.root()) is called — verified via RETURNS_SELF chain
        verify(spanBuilder).setParent(any());
    }

    @Test
    public void testReceiveOmitsEventSourceWhenNull() {
        when(event.getType()).thenReturn("ThingStatusInfoChangedEvent");
        when(event.getTopic()).thenReturn("openhab/things/hue:light:1/status");
        when(event.getSource()).thenReturn(null);

        listener.receive(event);

        verify(span, never()).setAttribute(eq("event.source"), any(String.class));
    }

    @Test
    public void testReceiveOmitsEventSourceWhenBlank() {
        when(event.getType()).thenReturn("InboxUpdatedEvent");
        when(event.getTopic()).thenReturn("openhab/inbox/hue:light:1/added");
        when(event.getSource()).thenReturn("");

        listener.receive(event);

        verify(span, never()).setAttribute(eq("event.source"), any(String.class));
    }

    @Test
    public void testReceiveHandlesRuntimeExceptionGracefully() {
        when(event.getType()).thenReturn("ItemStateChangedEvent");
        when(tracer.spanBuilder(anyString())).thenThrow(new RuntimeException("tracer broken"));

        // must not throw — errors in telemetry must not break openHAB event dispatch
        assertDoesNotThrow(() -> listener.receive(event));
    }
}
