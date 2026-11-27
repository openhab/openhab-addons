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
package org.openhab.binding.ipcamera.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.CHANNEL_START_STREAM;
import static org.openhab.core.library.types.OnOffType.ON;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.FFmpegFormat;
import org.openhab.binding.ipcamera.internal.handler.IpCameraHandler;

/**
 * Tests for FFmpeg process lifecycle management.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
class FfmpegTest {
    @Test
    void concurrentStartsLaunchOnlyOneProcess() throws Exception {
        AtomicInteger starts = new AtomicInteger();
        CountDownLatch processStarted = new CountDownLatch(1);
        FakeProcess process = new FakeProcess();
        Ffmpeg ffmpeg = newFfmpeg(command -> {
            starts.incrementAndGet();
            processStarted.countDown();
            return process;
        });

        Thread firstStart = new Thread(ffmpeg::startConverting);
        Thread secondStart = new Thread(ffmpeg::startConverting);
        firstStart.start();
        assertTrue(processStarted.await(2, TimeUnit.SECONDS));
        secondStart.start();
        secondStart.join(TimeUnit.SECONDS.toMillis(2));
        firstStart.join(TimeUnit.SECONDS.toMillis(2));
        assertFalse(firstStart.isAlive());
        assertFalse(secondStart.isAlive());
        ffmpeg.stopConverting();
        assertEquals(1, starts.get());
        assertFalse(process.isAlive());
    }

    @Test
    void hlsStartAndDisposeDoNotDeadlock() throws Exception {
        IpCameraHandler handler = mock(IpCameraHandler.class);
        CountDownLatch cleanupHasHandlerMonitor = new CountDownLatch(1);
        CountDownLatch allowCleanup = new CountDownLatch(1);
        AtomicInteger starts = new AtomicInteger();
        Ffmpeg ffmpeg = new Ffmpeg(handler, FFmpegFormat.HLS, "ffmpeg", "", "rtsp://camera/stream", "", "output.m3u8",
                "user", "secret", command -> {
                    starts.incrementAndGet();
                    return new FakeProcess();
                });
        doAnswer(invocation -> {
            synchronized (handler) {
                return null;
            }
        }).when(handler).setChannelState(CHANNEL_START_STREAM, ON);

        Thread cleanup = new Thread(() -> {
            synchronized (handler) {
                cleanupHasHandlerMonitor.countDown();
                try {
                    allowCleanup.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                ffmpeg.dispose();
            }
        });
        cleanup.setDaemon(true);
        cleanup.start();
        Thread start = new Thread(ffmpeg::startConverting);
        start.setDaemon(true);
        try {
            assertTrue(cleanupHasHandlerMonitor.await(2, TimeUnit.SECONDS));
            start.start();
            assertTrue(awaitBlocked(start, 2, TimeUnit.SECONDS));
        } finally {
            allowCleanup.countDown();
        }
        cleanup.join(TimeUnit.SECONDS.toMillis(2));
        if (start.getState() != Thread.State.NEW) {
            start.join(TimeUnit.SECONDS.toMillis(2));
        }

        assertFalse(cleanup.isAlive());
        assertFalse(start.isAlive());
        assertEquals(0, starts.get());
    }

    @Test
    void stopConvertingTerminatesProcessOnClientDisconnect() throws Exception {
        CountDownLatch processStarted = new CountDownLatch(1);
        FakeProcess process = new FakeProcess();
        Ffmpeg ffmpeg = newFfmpeg(command -> {
            processStarted.countDown();
            return process;
        });

        ffmpeg.startConverting();
        assertTrue(processStarted.await(2, TimeUnit.SECONDS));
        ffmpeg.stopConverting();

        assertFalse(process.isAlive());
        assertFalse(ffmpeg.isAlive());
    }

    @Test
    void closedErrorStreamTerminatesStillRunningProcess() throws Exception {
        CountDownLatch processStarted = new CountDownLatch(1);
        FakeProcess process = new FakeProcess();
        Ffmpeg ffmpeg = newFfmpeg(command -> {
            processStarted.countDown();
            return process;
        });

        ffmpeg.startConverting();
        assertTrue(processStarted.await(2, TimeUnit.SECONDS));
        process.closeErrorStream();
        assertTrue(process.waitFor(2, TimeUnit.SECONDS));

        assertFalse(process.isAlive());
        ffmpeg.dispose();
    }

    @Test
    void failedProcessStartCanBeDisposedWithoutRestarting() throws Exception {
        AtomicInteger starts = new AtomicInteger();
        CountDownLatch startAttempted = new CountDownLatch(1);
        Ffmpeg ffmpeg = newFfmpeg(command -> {
            starts.incrementAndGet();
            startAttempted.countDown();
            throw new IOException("process start failed");
        });

        ffmpeg.startConverting();
        assertTrue(startAttempted.await(2, TimeUnit.SECONDS));
        ffmpeg.dispose();
        ffmpeg.startConverting();

        assertEquals(1, starts.get());
        assertFalse(ffmpeg.isAlive());
    }

    @Test
    void disposeTerminatesProcessAndPreventsFurtherStarts() throws Exception {
        AtomicInteger starts = new AtomicInteger();
        CountDownLatch processStarted = new CountDownLatch(1);
        FakeProcess process = new FakeProcess();
        Ffmpeg ffmpeg = newFfmpeg(command -> {
            starts.incrementAndGet();
            processStarted.countDown();
            return process;
        });

        ffmpeg.startConverting();
        assertTrue(processStarted.await(2, TimeUnit.SECONDS));
        ffmpeg.dispose();
        ffmpeg.startConverting();

        assertEquals(1, starts.get());
        assertFalse(process.isAlive());
        assertFalse(ffmpeg.isAlive());
    }

    private static boolean awaitBlocked(Thread thread, long timeout, TimeUnit unit) {
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        while (thread.isAlive() && thread.getState() != Thread.State.BLOCKED && System.nanoTime() < deadline) {
            Thread.yield();
        }
        return thread.getState() == Thread.State.BLOCKED;
    }

    private static Ffmpeg newFfmpeg(Ffmpeg.ProcessStarter processStarter) {
        return new Ffmpeg(mock(IpCameraHandler.class), FFmpegFormat.MJPEG, "ffmpeg", "", "rtsp://camera/stream", "",
                "output.jpg", "user", "secret", processStarter);
    }

    private static final class FakeProcess extends Process {
        private final CountDownLatch terminated = new CountDownLatch(1);
        private final InputStream errorStream = new BlockingInputStream();

        @Override
        public OutputStream getOutputStream() {
            return OutputStream.nullOutputStream();
        }

        @Override
        public InputStream getInputStream() {
            return InputStream.nullInputStream();
        }

        @Override
        public InputStream getErrorStream() {
            return errorStream;
        }

        @Override
        public int waitFor() throws InterruptedException {
            terminated.await();
            return 0;
        }

        @Override
        public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
            return terminated.await(timeout, unit);
        }

        @Override
        public int exitValue() {
            if (isAlive()) {
                throw new IllegalThreadStateException();
            }
            return 0;
        }

        @Override
        public void destroy() {
            terminated.countDown();
        }

        @Override
        public Process destroyForcibly() {
            destroy();
            return this;
        }

        @Override
        public boolean isAlive() {
            return terminated.getCount() > 0;
        }

        private void closeErrorStream() throws IOException {
            errorStream.close();
        }
    }

    private static final class BlockingInputStream extends InputStream {
        private final CountDownLatch closed = new CountDownLatch(1);

        @Override
        public int read() throws IOException {
            try {
                closed.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting for stream data", e);
            }
            return -1;
        }

        @Override
        public void close() {
            closed.countDown();
        }
    }
}
