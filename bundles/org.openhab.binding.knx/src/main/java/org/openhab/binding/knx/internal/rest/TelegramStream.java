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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.internal.monitor.SendOutcome;
import org.openhab.binding.knx.internal.monitor.TelegramFilter;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.binding.knx.internal.monitor.TelegramRecord;
import org.openhab.binding.knx.internal.rest.dto.ErrorDTO;
import org.openhab.binding.knx.internal.rest.dto.StreamAliveDTO;
import org.openhab.binding.knx.internal.rest.dto.StreamLostDTO;
import org.openhab.binding.knx.internal.rest.dto.StreamOpenDTO;
import org.openhab.binding.knx.internal.rest.dto.StreamOutcomeDTO;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Number 20, the live telegrams of the add-on's bridges for up to {@link #MAX_STREAMS} readers.
 * <p>
 * A bridge's first reader adds one listener to its monitor, the last one leaving removes it. The listener runs on
 * the bus threads and only notes that there is work, so a reader never holds up the bus or the monitor: the rows
 * are sent in passes on the timer, at most one per {@link #PASS_SPACING_MILLIS} per bridge, unless a reader whose
 * mark moved in a pass is still behind the rows the pass found and has room: then the next pass follows at once.
 * Each reader keeps the number of the last row it has gone past; a pass hands it what came since, at most as many
 * events as it has room for below {@link #MAX_PENDING} unwritten ones. A reader with a filter looks at up to
 * {@link #MAX_ROWS_PER_PASS} rows a pass to find its own. A reader that stays at that mark over two ticks is closed.
 * <p>
 * All state of the readers is guarded by one lock; sends hand the event over and do not wait for it to be written.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class TelegramStream {
    static final int MAX_STREAMS = 8;
    static final int MAX_PENDING = 256;
    static final int MAX_ROWS_PER_PASS = 5000;
    static final int MAX_SIGNALS = 1000;
    static final long PASS_SPACING_MILLIS = 200;
    static final long ALIVE_MILLIS = 10_000;
    /** The monitor filters a stream takes; the others of a query make no sense for rows that are still to come. */
    static final List<String> REFUSED_KEYS = List.of("from", "to", "context", "limit");

    static final String OPEN = "open";
    static final String TELEGRAM = "telegram";
    static final String OUTCOME = "outcome";
    static final String LOST = "lost";
    static final String ALIVE = "alive";
    static final String END = "end";

    /** The codes of an {@code end} event that are no error of a request. */
    enum End {
        SLOW_CLIENT("slow-client", "The reader fell too far behind and was closed."),
        BRIDGE_GONE("bridge-gone", "The bridge was removed or restarted."),
        SHUTDOWN("shutdown", "The KNX add-on is stopping.");

        final String code;
        final String text;

        End(String code, String text) {
            this.code = code;
            this.text = text;
        }
    }

    /** When the passes and the ticks run; the add-on uses openHAB's common scheduled pool. */
    interface Timer {
        /** {@return what cancels the task} */
        Runnable later(Runnable task, long delayMillis);

        /** {@return what cancels the task} */
        Runnable every(Runnable task, long periodMillis);

        /** {@return a time in milliseconds that only goes forward} */
        long now();

        static Timer of(ScheduledExecutorService pool) {
            return new Timer() {
                @Override
                public Runnable later(Runnable task, long delayMillis) {
                    ScheduledFuture<?> future = pool.schedule(task, delayMillis, TimeUnit.MILLISECONDS);
                    return () -> future.cancel(false);
                }

                @Override
                public Runnable every(Runnable task, long periodMillis) {
                    ScheduledFuture<?> future = pool.scheduleWithFixedDelay(task, periodMillis, periodMillis,
                            TimeUnit.MILLISECONDS);
                    return () -> future.cancel(false);
                }

                @Override
                public long now() {
                    return TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
                }
            };
        }
    }

    private record Signal(long number, SendOutcome outcome) {
    }

    /** One reader; guarded by {@code lock}. */
    private static final class Reader {
        final StreamClient client;
        final TelegramFilter filter;
        /** Whether a filter was given; the readers without one share the rows of their mark. */
        final boolean filtered;
        long lastSent;
        boolean fullAtLastTick;
        /** Rows it got as pending, whose outcome it is still to get; the oldest go first. */
        final Set<Long> pendingRows = new LinkedHashSet<>();

        Reader(StreamClient client, TelegramFilter filter, boolean filtered, long lastSent) {
            this.client = client;
            this.filter = filter;
            this.filtered = filtered;
            this.lastSent = lastSent;
        }

        int room() {
            return MAX_PENDING - client.pending();
        }

        void notePending(long number) {
            pendingRows.add(number);
            if (pendingRows.size() > MAX_SIGNALS) {
                Iterator<Long> oldest = pendingRows.iterator();
                oldest.next();
                oldest.remove();
            }
        }
    }

    private final Logger logger = LoggerFactory.getLogger(TelegramStream.class);
    private final KnxRestService service;
    private final Timer timer;
    private final Object lock = new Object();
    // guarded by lock
    private final Map<ThingUID, BridgeStream> bridges = new HashMap<>();
    private int readers;
    private boolean closed;
    private final Runnable cancelTick;

    TelegramStream(KnxRestService service, Timer timer) {
        this.service = service;
        this.timer = timer;
        this.cancelTick = timer.every(this::tick, ALIVE_MILLIS);
    }

    /**
     * Opens a stream for the client, or sends it one {@code end} event and closes it.
     *
     * @param parameters the monitor filters given, by key; null values count as not given
     * @param after the number of the last row the client has, null to start with the rows to come
     * @param monitorId the id of the monitor that number belongs to
     */
    void open(StreamClient client, @Nullable String bridge, Map<String, @Nullable String> parameters,
            @Nullable String after, @Nullable String monitorId) {
        try {
            synchronized (lock) {
                if (closed) {
                    end(client, End.SHUTDOWN.code, End.SHUTDOWN.text);
                    return;
                }
                openLocked(client, bridge, parameters, after, monitorId);
            }
        } catch (KnxRestException e) {
            end(client, e.code(), e.text());
        }
    }

    private void openLocked(StreamClient client, @Nullable String bridge, Map<String, @Nullable String> parameters,
            @Nullable String after, @Nullable String monitorId) {
        KNXBridgeBaseThingHandler handler = service.handler(bridge);
        TelegramMonitor monitor = handler.getTelegramMonitor();
        if (monitor.status().bufferSize() == 0) {
            throw new KnxRestException(KnxRestError.MONITOR_OFF);
        }
        for (String key : REFUSED_KEYS) {
            if (parameters.get(key) != null) {
                throw KnxRestException.badInput(key, "not a filter of the stream");
            }
        }
        TelegramFilter filter = service.filter(parameters).build();
        long afterNumber = after == null ? -1 : KnxRestService.number("after", after);
        // a reader that has gone would otherwise keep its place until a pass or a tick finds out
        for (BridgeStream open : List.copyOf(bridges.values())) {
            for (Reader reader : List.copyOf(open.readers)) {
                if (finished(reader)) {
                    open.drop(reader);
                }
            }
        }
        if (readers >= MAX_STREAMS) {
            throw new KnxRestException(KnxRestError.TOO_MANY_STREAMS);
        }

        ThingUID uid = handler.getThing().getUID();
        BridgeStream stream = bridges.get(uid);
        if (stream != null && !stream.monitor.equals(monitor)) {
            stream.endAll(End.BRIDGE_GONE);
            stream = null;
        }
        if (stream == null) {
            stream = new BridgeStream(uid, monitor);
            bridges.put(uid, stream);
            monitor.addListener(stream);
        }
        // read after the listener is in place, so a row appended meanwhile is signalled or counted here
        long lastNumber = monitor.lastNumber();
        String id = monitor.id();
        boolean reset = (monitorId != null && !monitorId.equals(id)) || afterNumber > lastNumber;
        long lastSent = reset ? 0 : afterNumber >= 0 ? afterNumber : lastNumber;

        StreamOpenDTO open = new StreamOpenDTO();
        open.lastNumber = lastNumber;
        open.monitorId = id;
        open.reset = reset ? Boolean.TRUE : null;
        boolean filtered = KnxRestService.FILTER_KEYS.stream().anyMatch(key -> parameters.get(key) != null);
        Reader reader = new Reader(client, filter, filtered, lastSent);
        stream.readers.add(reader);
        readers++;
        if (!send(reader, OPEN, open)) {
            stream.drop(reader);
            return;
        }
        stream.requestPass();
    }

    /** Ends every stream with {@code shutdown}, removes the listeners and refuses new streams. */
    void close() {
        synchronized (lock) {
            closed = true;
            cancelTick.run();
            for (BridgeStream stream : List.copyOf(bridges.values())) {
                stream.endAll(End.SHUTDOWN);
            }
        }
    }

    /**
     * Every {@link #ALIVE_MILLIS}: ends the streams of gone bridges and of monitors turned off, closes readers stuck
     * at the mark, alive.
     */
    private void tick() {
        try {
            synchronized (lock) {
                for (BridgeStream stream : List.copyOf(bridges.values())) {
                    stream.tick();
                }
            }
        } catch (RuntimeException e) {
            logger.warn("KNX REST stream: the tick failed ({})", e.getClass().getName());
        }
    }

    private static boolean send(Reader reader, String event, Object data) {
        try {
            return reader.client.send(event, data);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static boolean finished(Reader reader) {
        try {
            return reader.client.finished();
        } catch (RuntimeException e) {
            return true;
        }
    }

    /** Sends the last event and closes the client. */
    private void end(StreamClient client, String code, String text) {
        sendEnd(client, code, text);
        closeClient(client);
    }

    private void sendEnd(StreamClient client, String code, String text) {
        ErrorDTO error = new ErrorDTO();
        error.code = code;
        error.text = text;
        try {
            client.send(END, error);
        } catch (RuntimeException e) {
            logger.debug("KNX REST stream: the end event could not be sent ({})", e.getClass().getName());
        }
    }

    private void closeClient(StreamClient client) {
        try {
            client.close();
        } catch (RuntimeException e) {
            logger.debug("KNX REST stream: a reader could not be closed ({})", e.getClass().getName());
        }
    }

    /** The readers of one bridge, and the listener on its monitor. */
    private final class BridgeStream implements TelegramMonitor.Listener {
        final ThingUID uid;
        final TelegramMonitor monitor;
        /** Guarded by {@code lock}. */
        final List<Reader> readers = new ArrayList<>();
        /** Set from the moment a pass is scheduled until that pass has scheduled the next one or found none due. */
        private final AtomicBoolean scheduled = new AtomicBoolean();
        private volatile long lastPassAt;
        /** Guarded by itself; the bus threads add to it. */
        private final ArrayDeque<Signal> signals = new ArrayDeque<>();

        BridgeStream(ThingUID uid, TelegramMonitor monitor) {
            this.uid = uid;
            this.monitor = monitor;
            this.lastPassAt = timer.now() - PASS_SPACING_MILLIS;
        }

        @Override
        public void appended(long number) {
            requestPass();
        }

        @Override
        public void outcomeChanged(long number, SendOutcome outcome) {
            synchronized (signals) {
                if (signals.size() >= MAX_SIGNALS) {
                    signals.pollFirst();
                }
                signals.addLast(new Signal(number, outcome));
            }
            requestPass();
        }

        /** Schedules one pass if none is waiting or running; called on bus threads too, so it never waits. */
        void requestPass() {
            if (scheduled.compareAndSet(false, true)) {
                schedule(spacing());
            }
        }

        private long spacing() {
            return Math.max(0, lastPassAt + PASS_SPACING_MILLIS - timer.now());
        }

        /** Called with {@code scheduled} set, which the pass scheduled takes over. */
        private void schedule(long delay) {
            try {
                timer.later(this::pass, delay);
            } catch (RuntimeException e) {
                scheduled.set(false);
            }
        }

        private void pass() {
            synchronized (lock) {
                boolean settled = false;
                try {
                    passLocked();
                    settled = true;
                } catch (RuntimeException e) {
                    logger.warn("KNX REST stream: a pass failed ({})", e.getClass().getName());
                } finally {
                    if (!settled) {
                        scheduled.set(false);
                    }
                }
            }
        }

        /** Ends by scheduling the next pass, or by clearing {@code scheduled}. */
        private void passLocked() {
            lastPassAt = timer.now();
            List<Signal> taken;
            synchronized (signals) {
                taken = List.copyOf(signals);
                signals.clear();
            }
            if (!equals(bridges.get(uid)) || readers.isEmpty()) {
                scheduled.set(false);
                return;
            }
            long lastNumber = monitor.lastNumber();
            Map<Long, Integer> wanted = new HashMap<>();
            for (Reader reader : readers) {
                int room = reader.room();
                if (room > 0 && !reader.filtered) {
                    wanted.merge(reader.lastSent, Math.min(room, MAX_ROWS_PER_PASS), Math::max);
                }
            }
            Map<Long, List<TelegramRecord>> rowsAfter = new HashMap<>();
            wanted.forEach((mark, rows) -> rowsAfter.put(mark,
                    monitor.query(TelegramFilter.builder().afterNumber(mark).limit(rows).build())));
            List<Reader> moved = new ArrayList<>();
            for (Reader reader : List.copyOf(readers)) {
                long mark = reader.lastSent;
                if (!deliver(reader, rowsAfter.get(mark), lastNumber)) {
                    drop(reader);
                    continue;
                }
                if (reader.lastSent > mark) {
                    moved.add(reader);
                }
                for (Signal signal : taken) {
                    if (reader.pendingRows.remove(signal.number()) && !sendOutcome(reader, signal)) {
                        drop(reader);
                        break;
                    }
                }
            }
            long newest = monitor.lastNumber();
            boolean behind = false;
            for (Reader reader : readers) {
                if (reader.lastSent < lastNumber && moved.contains(reader) && reader.room() > 0) {
                    schedule(0);
                    return;
                }
                behind |= reader.lastSent < newest;
            }
            if (behind) {
                schedule(spacing());
                return;
            }
            scheduled.set(false);
            // a row or an outcome signalled after newest was read found the flag still set and asked for nothing
            if (monitor.lastNumber() > newest || hasSignals()) {
                requestPass();
            }
        }

        private boolean hasSignals() {
            synchronized (signals) {
                return !signals.isEmpty();
            }
        }

        /**
         * @param rowsAfterMark the oldest rows above the reader's mark, oldest first; null if it is filtered or had no
         *            room when they were asked for
         * @param lastNumber the monitor's last number, read before the rows
         * @return false if the reader is finished
         */
        private boolean deliver(Reader reader, @Nullable List<TelegramRecord> rowsAfterMark, long lastNumber) {
            int room = reader.room();
            if (room <= 0) {
                return true;
            }
            if (reader.filtered) {
                TelegramMonitor.Window window = monitor.window(reader.filter, reader.lastSent, room, MAX_ROWS_PER_PASS);
                return deliver(reader, window.rows(), window.first() > 0 ? window.first() : lastNumber + 1,
                        window.last());
            }
            return rowsAfterMark == null || deliver(reader, rowsAfterMark,
                    rowsAfterMark.isEmpty() ? lastNumber + 1 : rowsAfterMark.get(0).number(), 0);
        }

        /**
         * @param rows rows above the reader's mark, oldest first
         * @param first the number of the first row looked at, the monitor's last number + 1 if none was
         * @param lookedAt the number of the last row looked at to find the rows, 0 if that is the last of them
         * @return false if the reader is finished
         */
        private boolean deliver(Reader reader, List<TelegramRecord> rows, long first, long lookedAt) {
            if (first > reader.lastSent + 1) {
                StreamLostDTO lost = new StreamLostDTO();
                lost.count = first - reader.lastSent - 1;
                if (!send(reader, LOST, lost)) {
                    return false;
                }
                reader.lastSent = first - 1;
            }
            int room = reader.room();
            for (TelegramRecord row : rows) {
                if (reader.filter.matches(row)) {
                    if (room <= 0) {
                        if (lookedAt > 0) {
                            // the lost event took a place; the rows of the window before this one were looked at
                            reader.lastSent = row.number() - 1;
                        }
                        return true;
                    }
                    if (!send(reader, TELEGRAM, KnxDtoMapper.telegram(row))) {
                        return false;
                    }
                    room--;
                    if (row.outcome() == SendOutcome.PENDING) {
                        reader.notePending(row.number());
                    }
                }
                reader.lastSent = row.number();
            }
            reader.lastSent = Math.max(reader.lastSent, lookedAt);
            return true;
        }

        private boolean sendOutcome(Reader reader, Signal signal) {
            StreamOutcomeDTO outcome = new StreamOutcomeDTO();
            outcome.number = signal.number();
            outcome.outcome = signal.outcome().name();
            return send(reader, OUTCOME, outcome);
        }

        void tick() {
            KNXBridgeBaseThingHandler handler;
            try {
                handler = service.handler(uid.getAsString());
            } catch (KnxRestException e) {
                handler = null;
            }
            if (handler == null || !monitor.equals(handler.getTelegramMonitor())) {
                endAll(End.BRIDGE_GONE);
                return;
            }
            if (monitor.status().bufferSize() == 0) {
                endAll(KnxRestError.MONITOR_OFF.code(), KnxRestError.MONITOR_OFF.text());
                return;
            }
            for (Reader reader : List.copyOf(readers)) {
                if (reader.room() <= 0) {
                    if (reader.fullAtLastTick) {
                        sendEnd(reader.client, End.SLOW_CLIENT.code, End.SLOW_CLIENT.text);
                        drop(reader);
                        continue;
                    }
                    reader.fullAtLastTick = true;
                } else {
                    reader.fullAtLastTick = false;
                }
                StreamAliveDTO alive = new StreamAliveDTO();
                alive.lastNumber = reader.lastSent;
                if (!send(reader, ALIVE, alive)) {
                    drop(reader);
                }
            }
        }

        void endAll(End end) {
            endAll(end.code, end.text);
        }

        private void endAll(String code, String text) {
            for (Reader reader : List.copyOf(readers)) {
                sendEnd(reader.client, code, text);
                drop(reader);
            }
            detach();
        }

        /** Takes the reader out; the last one leaving removes the listener. */
        void drop(Reader reader) {
            if (readers.remove(reader)) {
                TelegramStream.this.readers--;
                closeClient(reader.client);
            }
            if (readers.isEmpty()) {
                detach();
            }
        }

        private void detach() {
            monitor.removeListener(this);
            bridges.remove(uid, this);
        }
    }
}
