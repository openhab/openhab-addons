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
package org.openhab.io.mcp.internal.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;

/**
 * Keeps a buffer of recent {@link LogEntry}s for the {@code get_logs} tool.
 *
 * <p>
 * The OSGi {@link LogReaderService} only keeps the last 100 entries, which is not enough history
 * to be useful. This class listens for new log entries and keeps up to 5000 of them, starting
 * with whatever the {@link LogReaderService} already has.
 *
 * <p>
 * Item state events are skipped because they would quickly fill the buffer on systems with many
 * items. Item commands, thing status changes, rule events and channel triggers are kept.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class LogRingBuffer implements LogListener {

    public static final int DEFAULT_CAPACITY = 5000;

    // Item state events are not stored, see the class comment.
    private static final Set<String> EXCLUDED_LOGGERS = Set.of("openhab.event.ItemStateEvent",
            "openhab.event.ItemStateUpdatedEvent", "openhab.event.ItemStateChangedEvent",
            "openhab.event.ItemStatePredictedEvent", "openhab.event.GroupItemStateChangedEvent",
            "openhab.event.GroupStateUpdatedEvent");

    private final LogReaderService logReaderService;
    private final int capacity;
    private final Deque<LogEntry> entries = new ArrayDeque<>();

    public LogRingBuffer(LogReaderService logReaderService) {
        this(logReaderService, DEFAULT_CAPACITY);
    }

    public LogRingBuffer(LogReaderService logReaderService, int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be greater than 0");
        }
        this.logReaderService = logReaderService;
        this.capacity = capacity;
        seed();
        logReaderService.addLogListener(this);
    }

    /**
     * Copies the entries the {@link LogReaderService} already holds into the buffer.
     */
    private void seed() {
        Enumeration<LogEntry> log = logReaderService.getLog();
        if (log == null) {
            return;
        }
        // getLog() returns newest-first; replay oldest-first so the deque stays chronological.
        List<LogEntry> newestFirst = new ArrayList<>();
        while (log.hasMoreElements()) {
            newestFirst.add(log.nextElement());
        }
        for (int i = newestFirst.size() - 1; i >= 0; i--) {
            logged(newestFirst.get(i));
        }
    }

    @Override
    public void logged(@Nullable LogEntry entry) {
        if (entry == null) {
            return;
        }
        String loggerName = entry.getLoggerName();
        if (loggerName != null && EXCLUDED_LOGGERS.contains(loggerName)) {
            return;
        }
        synchronized (entries) {
            if (entries.size() >= capacity) {
                entries.removeFirst();
            }
            entries.addLast(entry);
        }
    }

    /**
     * Returns a copy of the buffered entries, newest first.
     *
     * @return the buffered log entries, newest first
     */
    public List<LogEntry> snapshotNewestFirst() {
        List<LogEntry> snapshot;
        synchronized (entries) {
            snapshot = new ArrayList<>(entries);
        }
        Collections.reverse(snapshot);
        return snapshot;
    }

    /**
     * Stops listening for new log entries.
     */
    public void close() {
        logReaderService.removeLogListener(this);
    }
}
