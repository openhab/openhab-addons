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
package org.openhab.binding.speedtest.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.speedtest.internal.dto.ResultContainer;
import org.openhab.binding.speedtest.internal.dto.ResultsContainerServerList;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.RefreshType;

import com.google.gson.Gson;

class SpeedtestHandlerTest {
    private static final ResultContainer RESULT = new Gson().fromJson("""
            {
              "type": "result",
              "timestamp": "2026-10-02T10:00:00Z",
              "ping": { "jitter": "1.0", "latency": "10.0" },
              "download": { "bandwidth": "125000", "bytes": "1000", "elapsed": "1000" },
              "upload": { "bandwidth": "62500", "bytes": "500", "elapsed": "1000" },
              "isp": "Example ISP",
              "interface": { "internalIp": "192.0.2.1", "externalIp": "198.51.100.1" },
              "server": { "id": 1, "name": "Example", "location": "Test City" },
              "result": { "persisted": false }
            }
            """, ResultContainer.class);

    private Thing thing;
    private ThingHandlerCallback callback;
    private TimeZoneProvider timeZoneProvider;

    @BeforeEach
    void setUp() {
        thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID("speedtest:speedtest:test"));
        when(thing.getStatus()).thenReturn(ThingStatus.OFFLINE);
        callback = mock(ThingHandlerCallback.class);
        timeZoneProvider = mock(TimeZoneProvider.class);
        when(timeZoneProvider.getTimeZone()).thenReturn(ZoneId.of("UTC"));
    }

    @Test
    void staleSuccessfulRunDoesNotUpdateChannelsOrStatus() throws Exception {
        BlockingSpeedtestHandler handler = createHandler(RESULT);

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Future<?> run = executor.submit(() -> handler.getSpeed(0));
            handler.awaitRequest();
            handler.dispose();
            handler.releaseRequest();
            run.get(1, TimeUnit.SECONDS);
        }

        verify(callback, never()).stateUpdated(any(), any());
        verify(callback, never()).statusUpdated(any(), any());
    }

    @Test
    void disposedManualTriggerDoesNotPublishOffState() throws Exception {
        BlockingSpeedtestHandler handler = createHandler(RESULT);
        ChannelUID triggerChannel = new ChannelUID(thing.getUID(), SpeedtestBindingConstants.TRIGGER_TEST);

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Future<?> trigger = executor.submit(() -> handler.handleCommand(triggerChannel, OnOffType.ON));
            handler.awaitRequest();
            handler.dispose();
            handler.releaseRequest();
            trigger.get(1, TimeUnit.SECONDS);
        }

        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    void disposeWaitsForInProgressChannelPublication() throws Exception {
        BlockingSpeedtestHandler handler = createHandler(RESULT);
        CountDownLatch channelUpdateStarted = new CountDownLatch(1);
        CountDownLatch releaseChannelUpdate = new CountDownLatch(1);
        AtomicInteger channelUpdateCount = new AtomicInteger();
        org.mockito.Mockito.doAnswer(invocation -> {
            if (channelUpdateCount.incrementAndGet() == 1) {
                channelUpdateStarted.countDown();
                releaseChannelUpdate.await();
            }
            return null;
        }).when(callback).stateUpdated(any(), any());

        try (ExecutorService measurementExecutor = Executors.newSingleThreadExecutor();
                ExecutorService disposalExecutor = Executors.newSingleThreadExecutor()) {
            Future<?> measurement = measurementExecutor.submit(() -> handler.getSpeed(0));
            handler.awaitRequest();
            handler.releaseRequest();
            assertTrue(channelUpdateStarted.await(1, TimeUnit.SECONDS));

            CountDownLatch disposeStarted = new CountDownLatch(1);
            Future<?> disposal = disposalExecutor.submit(() -> {
                disposeStarted.countDown();
                handler.dispose();
            });
            try {
                assertTrue(disposeStarted.await(1, TimeUnit.SECONDS));
                assertThrows(TimeoutException.class, () -> disposal.get(1, TimeUnit.SECONDS));
            } finally {
                releaseChannelUpdate.countDown();
            }

            disposal.get(1, TimeUnit.SECONDS);
            int publishedChannelsAtDispose = channelUpdateCount.get();
            measurement.get(1, TimeUnit.SECONDS);
            assertEquals(publishedChannelsAtDispose, channelUpdateCount.get());
        }
    }

    @Test
    void disposeWaitsForRefreshChannelPublication() throws Exception {
        BlockingSpeedtestHandler handler = createHandler(RESULT);
        try (ExecutorService measurementExecutor = Executors.newSingleThreadExecutor()) {
            Future<?> measurement = measurementExecutor.submit(() -> handler.getSpeed(0));
            handler.awaitRequest();
            handler.releaseRequest();
            measurement.get(1, TimeUnit.SECONDS);
        }

        CountDownLatch channelUpdateStarted = new CountDownLatch(1);
        CountDownLatch releaseChannelUpdate = new CountDownLatch(1);
        org.mockito.Mockito.doAnswer(invocation -> {
            channelUpdateStarted.countDown();
            releaseChannelUpdate.await();
            return null;
        }).when(callback).stateUpdated(any(), any());

        ChannelUID channelUID = mock(ChannelUID.class);
        when(channelUID.getId()).thenReturn("refresh-test");
        try (ExecutorService refreshExecutor = Executors.newSingleThreadExecutor();
                ExecutorService disposalExecutor = Executors.newSingleThreadExecutor()) {
            Future<?> refresh = refreshExecutor.submit(() -> handler.handleCommand(channelUID, RefreshType.REFRESH));
            assertTrue(channelUpdateStarted.await(1, TimeUnit.SECONDS));

            CountDownLatch disposeStarted = new CountDownLatch(1);
            Future<?> disposal = disposalExecutor.submit(() -> {
                disposeStarted.countDown();
                handler.dispose();
            });
            try {
                assertTrue(disposeStarted.await(1, TimeUnit.SECONDS));
                assertThrows(TimeoutException.class, () -> disposal.get(1, TimeUnit.SECONDS));
            } finally {
                releaseChannelUpdate.countDown();
            }

            disposal.get(1, TimeUnit.SECONDS);
            refresh.get(1, TimeUnit.SECONDS);
        }
    }

    @Test
    void initializeReturnsWhileCliVersionCheckIsBlocked() throws Exception {
        Map<String, Object> configuration = new HashMap<>();
        configuration.put("execPath", "test-speedtest");
        configuration.put("refreshInterval", 0);
        when(thing.getConfiguration()).thenReturn(new Configuration(configuration));
        AsyncInitializationHandler handler = new AsyncInitializationHandler(thing, timeZoneProvider);
        handler.setCallback(callback);
        List<ThingStatusInfo> reportedStatuses = new ArrayList<>();
        org.mockito.Mockito.doAnswer(invocation -> {
            reportedStatuses.add(invocation.getArgument(1));
            return null;
        }).when(callback).statusUpdated(any(), any());

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Future<?> initialization = executor.submit(handler::initialize);
            try {
                assertTrue(handler.versionRequestStarted.await(1, TimeUnit.SECONDS));
                initialization.get(1, TimeUnit.SECONDS);
                assertNotSame(Thread.currentThread(), handler.versionRequestThread.get());
            } finally {
                handler.dispose();
                handler.releaseVersionRequest.countDown();
            }
            assertTrue(handler.versionRequestFinished.await(1, TimeUnit.SECONDS));
        }

        assertTrue(reportedStatuses.stream().noneMatch(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void staleSuccessfulRunDoesNotOverrideInvalidReconfigurationStatus() throws Exception {
        BlockingSpeedtestHandler handler = createHandler(RESULT);
        Thing updatedThing = mock(Thing.class);
        when(updatedThing.getUID()).thenReturn(new ThingUID("speedtest:speedtest:test"));
        when(updatedThing.getStatus()).thenReturn(ThingStatus.OFFLINE);
        Map<String, Object> configuration = new HashMap<>();
        configuration.put("execPath",
                new File(System.getProperty("java.io.tmpdir"), "missing-speedtest-" + System.nanoTime())
                        .getAbsolutePath());
        configuration.put("refreshInterval", 0);
        when(updatedThing.getConfiguration()).thenReturn(new Configuration(configuration));
        List<ThingStatusInfo> reportedStatuses = new ArrayList<>();
        org.mockito.Mockito.doAnswer(invocation -> {
            reportedStatuses.add(invocation.getArgument(1));
            return null;
        }).when(callback).statusUpdated(any(), any());

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Future<?> oldRun = executor.submit(() -> handler.getSpeed(0));
            handler.awaitRequest();
            handler.thingUpdated(updatedThing);
            handler.releaseRequest();
            oldRun.get(1, TimeUnit.SECONDS);
        }

        verify(callback, atLeastOnce()).statusUpdated(any(), any());
        ThingStatusInfo lastStatus = reportedStatuses.get(reportedStatuses.size() - 1);
        assertEquals(ThingStatus.OFFLINE, lastStatus.getStatus());
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, lastStatus.getStatusDetail());
    }

    @Test
    void staleFailedRunDoesNotSetThingOffline() throws Exception {
        BlockingSpeedtestHandler handler = createHandler(null);

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Future<?> run = executor.submit(() -> handler.getSpeed(0));
            handler.awaitRequest();
            handler.dispose();
            handler.releaseRequest();
            run.get(1, TimeUnit.SECONDS);
        }

        verify(callback, never()).statusUpdated(any(), any());
    }

    @Test
    void stalePollingExceptionDoesNotSetThingOffline() throws Exception {
        BlockingSpeedtestHandler handler = createHandler(RESULT);
        handler.throwAfterRelease = true;

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Future<?> run = executor.submit(handler::runSpeedTest);
            handler.awaitRequest();
            handler.dispose();
            handler.releaseRequest();
            run.get(1, TimeUnit.SECONDS);
        }

        verify(callback, never()).statusUpdated(any(), any());
    }

    @Test
    void commandTimeoutStopsAProcessThatProducesNoOutput() {
        BlockingSpeedtestHandler handler = createHandler(null);
        String command = SpeedtestHandler.getOperatingSystemType() == SpeedtestHandler.OS.WINDOWS
                ? "ping -n 6 127.0.0.1 > nul"
                : "sleep 5";

        assertTimeoutPreemptively(Duration.ofSeconds(3), () -> handler.executeCmd(command, 100));
    }

    @Test
    void commandTimeoutBoundsOutputDrainAfterShellExits() {
        BlockingSpeedtestHandler handler = createHandler(null);
        String command = SpeedtestHandler.getOperatingSystemType() == SpeedtestHandler.OS.WINDOWS
                ? "start /b ping -n 6 127.0.0.1"
                : "sleep 5 &";

        assertTimeoutPreemptively(Duration.ofSeconds(3), () -> handler.executeCmd(command, 200));
    }

    @Test
    void commandTimeoutTerminatesBackgroundChildWhenShellExits() throws Exception {
        assumeTrue(SpeedtestHandler.getOperatingSystemType() != SpeedtestHandler.OS.WINDOWS);
        BlockingSpeedtestHandler handler = createHandler(null);
        Path pidFile = Files.createTempFile("speedtest-child-", ".pid");
        String quotedPidFile = "'" + pidFile.toString().replace("'", "'\"'\"'") + "'";
        String command = "sleep 30 & child=$!; printf '%s' \"$child\" > " + quotedPidFile + "; exit";
        try {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> handler.executeCmd(command, 1000));
            long childPid = Long.parseLong(Files.readString(pidFile));
            ProcessHandle.of(childPid).ifPresent(child -> {
                try {
                    child.onExit().get(5, TimeUnit.SECONDS);
                    assertFalse(child.isAlive());
                } catch (Exception e) {
                    throw new AssertionError("Background child survived command timeout", e);
                }
            });
        } finally {
            String childPid = Files.readString(pidFile);
            if (!childPid.isBlank()) {
                ProcessHandle.of(Long.parseLong(childPid)).ifPresent(ProcessHandle::destroyForcibly);
            }
            Files.deleteIfExists(pidFile);
        }
    }

    @Test
    void interruptedOutputReadTerminatesProcessAndRestoresInterrupt() throws Exception {
        BlockingSpeedtestHandler handler = createHandler(null);
        Process process = mock(Process.class);
        ProcessHandle processHandle = mock(ProcessHandle.class);
        InputStream interruptedStream = new InputStream() {
            @Override
            public int read() throws InterruptedIOException {
                throw new InterruptedIOException("interrupted output read");
            }
        };
        when(process.getInputStream()).thenReturn(interruptedStream);
        when(process.toHandle()).thenReturn(processHandle);
        when(processHandle.descendants()).thenReturn(Stream.empty());
        when(process.destroyForcibly()).thenReturn(process);
        when(process.waitFor(100, TimeUnit.MILLISECONDS)).thenThrow(new InterruptedException());

        try {
            handler.readOutput(process, "test command");
            assertTrue(Thread.currentThread().isInterrupted());
            verify(process).destroyForcibly();
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void failedOutputReadDiscardsPartialOutput() throws Exception {
        BlockingSpeedtestHandler handler = createHandler(null);
        Process process = mock(Process.class);
        byte[] partialOutput = "partial output\n".getBytes(StandardCharsets.UTF_8);
        InputStream failingStream = new InputStream() {
            private int offset;

            @Override
            public int read() throws IOException {
                if (offset == partialOutput.length) {
                    throw new IOException("output stream failed");
                }
                return partialOutput[offset++];
            }

            @Override
            public int read(byte[] bytes, int byteOffset, int length) throws IOException {
                if (offset == partialOutput.length) {
                    throw new IOException("output stream failed");
                }
                int bytesToCopy = Math.min(length, partialOutput.length - offset);
                System.arraycopy(partialOutput, offset, bytes, byteOffset, bytesToCopy);
                offset += bytesToCopy;
                return bytesToCopy;
            }
        };
        when(process.getInputStream()).thenReturn(failingStream);

        assertEquals("", handler.readOutput(process, "test command"));
    }

    @Test
    void overlappingSpeedTestsAreIgnored() throws Exception {
        BlockingSpeedtestHandler handler = createHandler(RESULT);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<?> firstRun = executor.submit(() -> handler.getSpeed(0));
            handler.awaitRequest();
            Future<?> secondRun = executor.submit(() -> handler.getSpeed(0));
            try {
                secondRun.get(500, TimeUnit.MILLISECONDS);
                assertEquals(1, handler.requestCount.get());
            } finally {
                handler.releaseRequest();
            }
            firstRun.get(1, TimeUnit.SECONDS);
        }
    }

    @Test
    void reconfigurationStartsFirstPollWhileStaleManualTestIsRunning() throws Exception {
        Map<String, Object> configuration = new HashMap<>();
        configuration.put("execPath", "test-speedtest");
        configuration.put("refreshInterval", 60);
        when(thing.getConfiguration()).thenReturn(new Configuration(configuration));
        ReconfigurationSpeedtestHandler handler = new ReconfigurationSpeedtestHandler(thing, timeZoneProvider);
        handler.setCallback(callback);
        CountDownLatch channelsUpdated = new CountDownLatch(1);
        org.mockito.Mockito.doAnswer(invocation -> {
            channelsUpdated.countDown();
            return null;
        }).when(callback).stateUpdated(any(), any());
        ChannelUID triggerChannel = new ChannelUID(thing.getUID(), SpeedtestBindingConstants.TRIGGER_TEST);

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Future<?> oldRun = executor.submit(() -> handler.handleCommand(triggerChannel, OnOffType.ON));
            try {
                handler.awaitRequest();
                assertEquals(1, handler.measurementCount.get());
                handler.thingUpdated(thing);
                assertTrue(handler.newRequestStarted.await(5, TimeUnit.SECONDS));
                handler.getSpeed(1);
                assertEquals(2, handler.measurementCount.get());

                handler.releaseRequest();
                oldRun.get(5, TimeUnit.SECONDS);
                verify(callback, never()).stateUpdated(any(), any());

                handler.getSpeed(0);
                handler.getSpeed(1);
                assertEquals(2, handler.measurementCount.get());
                handler.newRequestRelease.countDown();
                assertTrue(channelsUpdated.await(5, TimeUnit.SECONDS));
            } finally {
                handler.releaseRequest();
                handler.newRequestRelease.countDown();
                handler.dispose();
            }
        }
    }

    private BlockingSpeedtestHandler createHandler(@Nullable ResultContainer result) {
        BlockingSpeedtestHandler handler = new BlockingSpeedtestHandler(thing, timeZoneProvider, result);
        handler.setCallback(callback);
        return handler;
    }

    private static class AsyncInitializationHandler extends SpeedtestHandler {
        private final CountDownLatch versionRequestStarted = new CountDownLatch(1);
        private final CountDownLatch releaseVersionRequest = new CountDownLatch(1);
        private final CountDownLatch versionRequestFinished = new CountDownLatch(1);
        private final AtomicReference<Thread> versionRequestThread = new AtomicReference<>();

        AsyncInitializationHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
            super(thing, timeZoneProvider);
        }

        @Override
        public boolean checkConfig(String execPath) {
            return true;
        }

        @Override
        protected @Nullable <T> T doExecuteRequest(String arguments, Class<T> type) {
            if (type == String.class) {
                versionRequestThread.set(Thread.currentThread());
                versionRequestStarted.countDown();
                boolean interrupted = false;
                while (true) {
                    try {
                        releaseVersionRequest.await();
                        break;
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                }
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
                versionRequestFinished.countDown();
                return type.cast("Speedtest by Ookla 1.0" + System.lineSeparator());
            }

            ResultsContainerServerList serverList = new ResultsContainerServerList();
            serverList.servers = List.of();
            return type.cast(serverList);
        }
    }

    private static class ReconfigurationSpeedtestHandler extends BlockingSpeedtestHandler {
        private final AtomicInteger measurementCount = new AtomicInteger();
        private final CountDownLatch newRequestStarted = new CountDownLatch(1);
        private final CountDownLatch newRequestRelease = new CountDownLatch(1);

        ReconfigurationSpeedtestHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
            super(thing, timeZoneProvider, RESULT);
        }

        @Override
        public boolean checkConfig(String execPath) {
            return true;
        }

        @Override
        protected @Nullable <T> T doExecuteRequest(String arguments, Class<T> type) {
            if (type == String.class) {
                return type.cast("Speedtest by Ookla 1.0" + System.lineSeparator());
            }
            if (type == ResultsContainerServerList.class) {
                ResultsContainerServerList serverList = new ResultsContainerServerList();
                serverList.servers = List.of();
                return type.cast(serverList);
            }
            if (measurementCount.incrementAndGet() == 1) {
                return super.doExecuteRequest(arguments, type);
            }
            newRequestStarted.countDown();
            try {
                assertTrue(newRequestRelease.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
            return type.cast(RESULT);
        }
    }

    private static class BlockingSpeedtestHandler extends SpeedtestHandler {
        private final CountDownLatch requestStarted = new CountDownLatch(1);
        private final CountDownLatch requestRelease = new CountDownLatch(1);
        private final @Nullable ResultContainer result;
        final AtomicInteger requestCount = new AtomicInteger();
        volatile boolean throwAfterRelease;

        BlockingSpeedtestHandler(Thing thing, TimeZoneProvider timeZoneProvider, @Nullable ResultContainer result) {
            super(thing, timeZoneProvider);
            this.result = result;
        }

        @Override
        protected @Nullable <T> T doExecuteRequest(String arguments, Class<T> type) {
            requestCount.incrementAndGet();
            requestStarted.countDown();
            boolean interrupted = false;
            while (true) {
                try {
                    requestRelease.await();
                    break;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
            if (throwAfterRelease) {
                throw new IllegalStateException("test failure");
            }
            return result == null ? null : type.cast(result);
        }

        void awaitRequest() throws InterruptedException {
            requestStarted.await(1, TimeUnit.SECONDS);
        }

        void releaseRequest() {
            requestRelease.countDown();
        }
    }
}
