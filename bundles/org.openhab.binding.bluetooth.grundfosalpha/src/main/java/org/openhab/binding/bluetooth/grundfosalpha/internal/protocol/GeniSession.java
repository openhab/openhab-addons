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
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serializes GENI exchanges and read-modify-write transactions within a Bluetooth connection.
 * Replies do not carry the requested data ID/sub-ID, so a timeout invalidates the session.
 * <p>
 * The writer, listener and connection failure callbacks are never invoked while holding the session lock, so callers
 * may hold their own locks while calling into the session.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class GeniSession {

    private final static int REQUEST_DELAY_MS = 700;

    private final Logger logger = LoggerFactory.getLogger(GeniSession.class);
    private final ScheduledExecutorService scheduler;
    private final Function<byte[], CompletionStage<?>> writer;
    private final BiConsumer<Map<GeniMeasurand, BigDecimal>, BooleanSupplier> listener;
    private final Runnable connectionFailed;
    private final Deque<Request> queue = new ArrayDeque<>();

    private @Nullable ScheduledFuture<?> sendFuture;
    private @Nullable ScheduledFuture<?> timeoutFuture;
    private @Nullable Exchange pending;
    private boolean active;
    private boolean closed;
    private boolean failed;
    private long generation;

    /**
     * @param listener receives decoded values together with a check telling whether the session that produced them
     *            is still current. Values are delivered outside the session lock, so a listener that must not publish
     *            stale values should perform this check while holding the lock it also holds when stopping or closing
     *            the session.
     */
    public GeniSession(ScheduledExecutorService scheduler, Function<byte[], CompletionStage<?>> writer,
            BiConsumer<Map<GeniMeasurand, BigDecimal>, BooleanSupplier> listener, Runnable connectionFailed) {
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

    private void sendNext(long generation) {
        Exchange exchange;
        synchronized (this) {
            if (generation != this.generation) {
                return;
            }
            sendFuture = null;
            if (!active || pending != null || queue.isEmpty()) {
                return;
            }
            Request request = queue.removeFirst();
            exchange = new Exchange(request, request.readRequest.request(), false);
            begin(exchange);
        }
        sendFragment(exchange, 0);
    }

    private void sendPrepared(Exchange exchange) {
        synchronized (this) {
            if (!active || !exchange.equals(pending)) {
                return;
            }
            sendFuture = null;
            begin(exchange);
        }
        sendFragment(exchange, 0);
    }

    private void begin(Exchange exchange) {
        pending = exchange;
        timeoutFuture = scheduler.schedule(() -> fail(exchange, "Timed out waiting for GENI reply"), 5,
                TimeUnit.SECONDS);
    }

    private void sendFragment(Exchange exchange, int offset) {
        if (offset == exchange.frame.length) {
            completeWrite(exchange);
            return;
        }
        synchronized (this) {
            if (!active || !exchange.equals(pending)) {
                return;
            }
        }
        // The default BLE ATT payload is 20 bytes. Await each write before sending the next fragment.
        // The write is issued outside the lock; if the session is stopped meanwhile, at most this fragment is sent
        // on the connection being closed, and its completion is ignored.
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

    private void completeWrite(Exchange exchange) {
        Runnable delivery;
        synchronized (this) {
            if (!active || !exchange.equals(pending)) {
                return;
            }
            exchange.written = true;
            delivery = finish(exchange);
        }
        if (delivery != null) {
            delivery.run();
        }
    }

    public void accept(byte[] notification) {
        Runnable delivery;
        synchronized (this) {
            Exchange exchange = pending;
            if (!active || exchange == null || exchange.received || !exchange.decoder.addPacket(notification)) {
                return;
            }
            exchange.received = true;
            delivery = finish(exchange);
        }
        if (delivery != null) {
            delivery.run();
        }
    }

    /**
     * Completes the exchange and advances the session. Must be called while holding the session lock.
     *
     * @return the delivery of decoded values to run after releasing the lock, or {@code null} if there is none
     */
    private @Nullable Runnable finish(Exchange exchange) {
        if (!exchange.written || !exchange.received || !exchange.equals(pending)) {
            return null;
        }
        cancelTimeout();
        pending = null;
        Request request = exchange.request;
        if (exchange.write) {
            if (!exchange.decoder.isWriteAcknowledged()) {
                logger.debug("Pump rejected {} write; reading back its current setting", request.readRequest);
            }
            queue.addFirst(new Request(request.readRequest, null, 0));
            scheduleNext();
            return null;
        }
        Map<GeniMeasurand, BigDecimal> values = exchange.decoder.decode();
        ModeSetting setting = request.setting;
        byte[] data = exchange.decoder.objectData();
        if (setting != null && data != null) {
            // Keep the read-modify-write together, before any queued reads or other mode commands.
            Exchange write = new Exchange(request, setting.writeRequest(data, request.value), true);
            pending = write;
            sendFuture = scheduler.schedule(() -> sendPrepared(write), REQUEST_DELAY_MS, TimeUnit.MILLISECONDS);
        } else {
            if (setting != null) {
                logger.debug("Cannot write {} without a supported settings object", setting);
            }
            scheduleNext();
        }
        long generation = this.generation;
        return () -> listener.accept(values, () -> isCurrent(generation));
    }

    private synchronized boolean isCurrent(long generation) {
        return active && generation == this.generation;
    }

    private void fail(Exchange exchange, String reason) {
        synchronized (this) {
            if (!active || !exchange.equals(pending)) {
                return;
            }
            logger.debug("{}; reconnecting to discard uncorrelated replies", reason);
            stop();
            failed = true;
        }
        connectionFailed.run();
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
