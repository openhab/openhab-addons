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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.LoggerFactory;

/**
 * A reader of the stream behind a server-sent event sink. Core's broadcaster is not used, as it does not tell
 * whether a send worked.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class SseStreamClient implements StreamClient {
    private final SseEventSink sink;
    private final Sse sse;
    private final AtomicInteger pending = new AtomicInteger();
    private final AtomicBoolean finished = new AtomicBoolean();

    SseStreamClient(SseEventSink sink, Sse sse) {
        this.sink = sink;
        this.sse = sse;
    }

    @Override
    public boolean send(String event, Object data) {
        if (finished.get() || sink.isClosed()) {
            close();
            return false;
        }
        OutboundSseEvent built = sse.newEventBuilder().name(event).mediaType(MediaType.APPLICATION_JSON_TYPE).data(data)
                .build();
        pending.incrementAndGet();
        try {
            sink.send(built).whenComplete((result, failure) -> {
                pending.decrementAndGet();
                if (failure != null) {
                    close();
                }
            });
            return true;
        } catch (RuntimeException e) {
            pending.decrementAndGet();
            close();
            return false;
        }
    }

    @Override
    public int pending() {
        return pending.get();
    }

    @Override
    public boolean finished() {
        return finished.get() || sink.isClosed();
    }

    @Override
    public void close() {
        if (finished.getAndSet(true)) {
            return;
        }
        try {
            sink.close();
        } catch (RuntimeException e) {
            LoggerFactory.getLogger(SseStreamClient.class).debug("KNX REST stream: the sink cannot be closed ({})",
                    e.getClass().getName());
        }
    }
}
