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

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventFilter;
import org.openhab.core.events.EventSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;

/**
 * Subscribes to the entire openHAB event bus and emits a flat OTLP span per event.
 *
 * <p>
 * Spans are flat by design: openHAB's event bus is fire-and-forget (async dispatch on a
 * dedicated handler thread), so there is no propagated call context to attach to. {@link Context#root()}
 * is used as the parent so spans are always root spans even if a stale context is active on
 * the handler thread. Volume can be controlled via {@code tracesSamplingRatio} in the config.
 *
 * <p>
 * The handler is intentionally non-blocking: span construction and hand-off to the
 * {@code BatchSpanProcessor} are O(1) operations; all I/O happens asynchronously.
 *
 * <p>
 * Registered and unregistered dynamically by {@link OpenTelemetryService} via the OSGi service
 * registry — not a {@code @Component}.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
public class OpenTelemetryEventListener implements EventSubscriber {
    private final Logger logger = LoggerFactory.getLogger(OpenTelemetryEventListener.class);

    private final Tracer tracer;

    public OpenTelemetryEventListener(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return Set.of(EventSubscriber.ALL_EVENT_TYPES);
    }

    @Override
    public @Nullable EventFilter getEventFilter() {
        return null;
    }

    @Override
    public void receive(Event event) {
        try {
            Span span = tracer.spanBuilder(event.getType()) //
                    .setParent(Context.root()) // explicitly flat — not a child of any active span
                    .setSpanKind(SpanKind.INTERNAL) //
                    .startSpan();
            try {
                span.setAttribute("event.type", event.getType());
                span.setAttribute("event.topic", event.getTopic());
                String source = event.getSource();
                if (source != null && !source.isBlank()) {
                    span.setAttribute("event.source", source);
                }
            } finally {
                span.end();
            }
        } catch (RuntimeException e) {
            logger.trace("Failed to emit span for event {}: {}", event.getType(), e.getMessage());
        }
    }
}
