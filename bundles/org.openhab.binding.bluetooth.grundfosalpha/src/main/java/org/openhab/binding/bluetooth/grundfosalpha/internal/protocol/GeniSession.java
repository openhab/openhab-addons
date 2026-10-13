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
package org.openhab.binding.bluetooth.grundfosalpha.internal.protocol;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serializes GENI exchanges and read-modify-write transactions within a Bluetooth connection.
 * Replies do not carry the requested data ID/sub-ID, so a timeout invalidates the session.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class GeniSession {

    private final static int REQUEST_DELAY_MS = 700;

    private final Logger logger = LoggerFactory.getLogger(GeniSession.class);
    private final ScheduledExecutorService scheduler;
    private final Function<byte[], CompletionStage<?>> writer;
    private final Consumer<Map<GeniMeasurand, BigDecimal>> listener;
    private final Runnable connectionFailed;
    private final Deque<Request> queue = new ArrayDeque<>();

    private @Nullable ScheduledFuture<?> sendFuture;
    private @Nullable ScheduledFuture<?> timeoutFuture;
    private @Nullable Exchange pending;
    private boolean active;
    private boolean closed;
    private boolean failed;
    private long generation;

    public GeniSession(ScheduledExecutorService scheduler, Function<byte[], CompletionStage<?>> writer,
            Consumer<Map<GeniMeasurand, BigDecimal>> listener, Runnable connectionFailed) {
        this.scheduler = scheduler;
        this.writer = writer;
        this.listener = listener;
        this.connectionFailed = connectionFailed;
    }

    public synchronized void start() {
        if (!closed && !failed) {
            active = true;
        }
    }

    public synchronized void close() {
        closed = true;
        stop();
    }

    public synchronized void stop() {
        active = false;
        failed = false;
        generation++;
        pending = null;
        queue.clear();
        ScheduledFuture<?> sendFuture = this.sendFuture;
        if (sendFuture != null) {
            sendFuture.cancel(false);
            this.sendFuture = null;
        }
        cancelTimeout();
    }

    public synchronized void read(GeniReadRequest request) {
        enqueue(new Request(request, null, 0));
    }

    public synchronized void write(ModeSetting setting, int value) {
        if (!setting.accepts(value)) {
            throw new IllegalArgumentException("Unsupported mode value: " + value);
        }
        // Coalesce unsent commands for this setting; an in-progress transaction finishes first.
        queue.removeIf(request -> request.setting == setting);
        enqueue(new Request(setting.readRequest(), setting, value));
    }

    private void enqueue(Request request) {
        if (active && !queue.contains(request)) {
            queue.addLast(request);
            scheduleNext();
        }
    }

    private void scheduleNext() {
        if (active && pending == null && sendFuture == null && !queue.isEmpty()) {
            long generation = this.generation;
            sendFuture = scheduler.schedule(() -> sendNext(generation), REQUEST_DELAY_MS, TimeUnit.MILLISECONDS);
        }
    }

    private synchronized void sendNext(long generation) {
        if (generation != this.generation) {
            return;
        }
        sendFuture = null;
        if (!active || pending != null || queue.isEmpty()) {
            return;
        }
        Request request = queue.removeFirst();
        send(new Exchange(request, request.readRequest.request(), false));
    }

    private void send(Exchange exchange) {
        pending = exchange;
        timeoutFuture = scheduler.schedule(() -> fail(exchange, "Timed out waiting for GENI reply"), 5,
                TimeUnit.SECONDS);
        sendFragment(exchange, 0);
    }

    private synchronized void sendFragment(Exchange exchange, int offset) {
        if (!active || !exchange.equals(pending)) {
            return;
        }
        if (offset == exchange.frame.length) {
            exchange.written = true;
            finish(exchange);
            return;
        }
        // The default BLE ATT payload is 20 bytes. Await each write before sending the next fragment.
        int end = Math.min(offset + 20, exchange.frame.length);
        try {
            writer.apply(Arrays.copyOfRange(exchange.frame, offset, end)).whenComplete((ignored, failure) -> {
                if (failure != null) {
                    fail(exchange, "Bluetooth characteristic write failed");
                } else {
                    sendFragment(exchange, end);
                }
            });
        } catch (RuntimeException e) {
            fail(exchange, "Could not write Bluetooth characteristic");
        }
    }

    public synchronized void accept(byte[] notification) {
        Exchange exchange = pending;
        if (active && exchange != null && !exchange.received && exchange.decoder.addPacket(notification)) {
            exchange.received = true;
            finish(exchange);
        }
    }

    private void finish(Exchange exchange) {
        if (!exchange.written || !exchange.received || !exchange.equals(pending)) {
            return;
        }
        cancelTimeout();
        Request request = exchange.request;
        if (exchange.write) {
            pending = null;
            if (!exchange.decoder.isWriteAcknowledged()) {
                logger.debug("Pump rejected {} write; reading back its current setting", request.readRequest);
            }
            queue.addFirst(new Request(request.readRequest, null, 0));
        } else {
            Map<GeniMeasurand, BigDecimal> values = exchange.decoder.decode();
            listener.accept(values);
            if (!active || !exchange.equals(pending)) {
                return;
            }
            pending = null;
            ModeSetting setting = request.setting;
            byte[] data = exchange.decoder.objectData();
            if (setting != null) {
                if (data != null) {
                    // Keep the read-modify-write together, before any queued reads or other mode commands.
                    Exchange write = new Exchange(request, setting.writeRequest(data, request.value), true);
                    pending = write;
                    sendFuture = scheduler.schedule(() -> sendPrepared(write), REQUEST_DELAY_MS, TimeUnit.MILLISECONDS);
                    return;
                }
                logger.debug("Cannot write {} without a supported settings object", setting);
            }
        }
        scheduleNext();
    }

    private synchronized void sendPrepared(Exchange exchange) {
        if (active && exchange.equals(pending)) {
            sendFuture = null;
            send(exchange);
        }
    }

    private synchronized void fail(Exchange exchange, String reason) {
        if (active && exchange.equals(pending)) {
            logger.debug("{}; reconnecting to discard uncorrelated replies", reason);
            stop();
            failed = true;
            connectionFailed.run();
        }
    }

    private void cancelTimeout() {
        ScheduledFuture<?> timeoutFuture = this.timeoutFuture;
        if (timeoutFuture != null) {
            timeoutFuture.cancel(false);
            this.timeoutFuture = null;
        }
    }

    private record Request(GeniReadRequest readRequest, @Nullable ModeSetting setting, int value) {
    }

    private static final class Exchange {
        final Request request;
        final byte[] frame;
        final boolean write;
        final GeniResponseDecoder decoder;
        boolean written;
        boolean received;

        Exchange(Request request, byte[] frame, boolean write) {
            this.request = request;
            this.frame = frame;
            this.write = write;
            decoder = new GeniResponseDecoder(write ? null : request.readRequest);
        }
    }
}
