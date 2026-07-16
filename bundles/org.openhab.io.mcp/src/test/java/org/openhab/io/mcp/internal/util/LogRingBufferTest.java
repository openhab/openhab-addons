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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogReaderService;

/**
 * Tests for {@link LogRingBuffer}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class LogRingBufferTest {

    @Mock
    @Nullable
    LogReaderService logReaderService;

    private static <T> T requireNonNull(@Nullable T value) {
        assertNotNull(value);
        return value;
    }

    private LogEntry mockEntry(long sequence, String loggerName, String message) {
        LogEntry e = mock(LogEntry.class);
        lenient().when(e.getSequence()).thenReturn(sequence);
        lenient().when(e.getLoggerName()).thenReturn(loggerName);
        lenient().when(e.getMessage()).thenReturn(message);
        return e;
    }

    @Test
    void seedsFromLogReaderServiceHistory() {
        LogEntry newer = mockEntry(2, "x", "b");
        LogEntry older = mockEntry(1, "x", "a");
        // getLog() returns newest-first, like pax-logging does.
        when(logReaderService.getLog()).thenReturn(Collections.enumeration(List.of(newer, older)));

        LogRingBuffer buffer = new LogRingBuffer(requireNonNull(logReaderService), 10);
        List<LogEntry> snapshot = buffer.snapshotNewestFirst();
        assertEquals(2, snapshot.size());
        assertEquals(2, snapshot.get(0).getSequence());
        assertEquals(1, snapshot.get(1).getSequence());
    }

    @Test
    void evictsOldestWhenCapacityExceeded() {
        when(logReaderService.getLog()).thenReturn(Collections.emptyEnumeration());

        LogRingBuffer buffer = new LogRingBuffer(requireNonNull(logReaderService), 3);
        for (int i = 1; i <= 5; i++) {
            buffer.logged(mockEntry(i, "x", "m" + i));
        }

        List<LogEntry> snapshot = buffer.snapshotNewestFirst();
        assertEquals(3, snapshot.size());
        assertEquals(5, snapshot.get(0).getSequence());
        assertEquals(4, snapshot.get(1).getSequence());
        assertEquals(3, snapshot.get(2).getSequence());
    }

    @Test
    void excludesItemStateTelemetryEvents() {
        when(logReaderService.getLog()).thenReturn(Collections.emptyEnumeration());

        LogRingBuffer buffer = new LogRingBuffer(requireNonNull(logReaderService), 10);
        buffer.logged(mockEntry(1, "openhab.event.ItemStateEvent", "t"));
        buffer.logged(mockEntry(2, "openhab.event.ItemStateUpdatedEvent", "t"));
        buffer.logged(mockEntry(3, "openhab.event.ItemStateChangedEvent", "t"));
        buffer.logged(mockEntry(4, "openhab.event.ItemStatePredictedEvent", "t"));
        buffer.logged(mockEntry(5, "openhab.event.GroupItemStateChangedEvent", "t"));
        buffer.logged(mockEntry(6, "openhab.event.GroupStateUpdatedEvent", "t"));
        buffer.logged(mockEntry(7, "openhab.event.ItemCommandEvent", "kept"));
        buffer.logged(mockEntry(8, "openhab.event.ThingStatusInfoChangedEvent", "kept"));
        buffer.logged(mockEntry(9, "org.openhab.binding.foo", "kept"));

        List<LogEntry> snapshot = buffer.snapshotNewestFirst();
        assertEquals(3, snapshot.size());
        assertEquals(9, snapshot.get(0).getSequence());
        assertEquals(8, snapshot.get(1).getSequence());
        assertEquals(7, snapshot.get(2).getSequence());
    }

    @Test
    void rejectsNonPositiveCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new LogRingBuffer(requireNonNull(logReaderService), 0));
        assertThrows(IllegalArgumentException.class, () -> new LogRingBuffer(requireNonNull(logReaderService), -1));
    }

    @Test
    void registersAndUnregistersListener() {
        when(logReaderService.getLog()).thenReturn(Collections.emptyEnumeration());

        LogRingBuffer buffer = new LogRingBuffer(requireNonNull(logReaderService), 10);
        verify(logReaderService).addLogListener(buffer);

        buffer.close();
        verify(logReaderService).removeLogListener(buffer);
    }
}
