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
package org.openhab.binding.ipcamera.internal.servlet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for per-client MJPEG stream lifecycle.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
class StreamOutputTest {
    @Test
    void idleStreamTimesOut() throws Exception {
        StreamOutput stream = newStreamOutput(Duration.ofMillis(1));

        assertThrows(SocketTimeoutException.class, stream::sendFrame);
    }

    @Test
    void closeAllStreamsUnblocksWaitingClientAndRemovesStream() throws Exception {
        OpenStreams streams = new OpenStreams();
        StreamOutput stream = newStreamOutput(Duration.ofSeconds(30));
        streams.addStream(stream);
        CountDownLatch started = new CountDownLatch(1);
        AtomicBoolean closedByDispose = new AtomicBoolean();
        Thread client = new Thread(() -> {
            started.countDown();
            try {
                stream.sendFrame();
            } catch (IOException e) {
                closedByDispose.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        client.start();

        try {
            assertTrue(started.await(2, TimeUnit.SECONDS));
            assertTrue(awaitTimedWaiting(client));
            streams.closeAllStreams();
            client.join(TimeUnit.SECONDS.toMillis(2));

            assertFalse(client.isAlive());
            assertTrue(closedByDispose.get());
            assertTrue(streams.isEmpty());
        } finally {
            streams.closeAllStreams();
            client.interrupt();
            client.join(TimeUnit.SECONDS.toMillis(2));
        }
    }

    private boolean awaitTimedWaiting(Thread thread) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (thread.getState() != Thread.State.TIMED_WAITING && System.nanoTime() < deadline) {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
        }
        return thread.getState() == Thread.State.TIMED_WAITING;
    }

    private StreamOutput newStreamOutput(Duration timeout) throws IOException {
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
        return new StreamOutput(response, timeout);
    }
}
