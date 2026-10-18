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
package org.openhab.binding.homewizard.internal.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homewizard.internal.HomeWizardBindingConstants;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Tests device information retries and lifecycle changes during polling.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class HomeWizardDeviceHandlerLifecycleTest extends HomeWizardHandlerTest {
    private static final String DEVICE_INFORMATION = """
            {"product_type":"HWE-P1","product_name":"P1 Meter","firmware_version":"5.18","api_version":"v1"}
            """;

    private static Thing mockThing() {
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID(HomeWizardBindingConstants.THING_TYPE_HWE_P1, "lifecycle-test"));
        when(thing.getConfiguration()).thenReturn(CONFIG_V1);
        when(thing.getProperties()).thenReturn(Map.of());
        return thing;
    }

    @Test
    public void malformedDeviceInformationIsRetried() {
        Thing thing = mockThing();
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        TestHandler handler = new TestHandler(thing);
        handler.laterInformation = DEVICE_INFORMATION;
        handler.setCallback(callback);

        try {
            handler.initialize();
            Runnable poll = handler.dataPolls.get(0);
            poll.run();
            verify(callback).statusUpdated(eq(thing), argThat(info -> info.getStatus() == ThingStatus.OFFLINE));

            poll.run();
            verify(callback).statusUpdated(eq(thing), argThat(info -> info.getStatus() == ThingStatus.ONLINE));
            verify(thing).setProperty("productName", "P1 Meter");
        } finally {
            handler.dispose();
        }
    }

    @Test
    public void failedOldPollCannotChangeNewStatus() throws Exception {
        Thing thing = mockThing();
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        TestHandler handler = new TestHandler(thing);
        handler.blockFirstCall = true;
        handler.laterInformation = DEVICE_INFORMATION;
        handler.setCallback(callback);
        CompletableFuture<Void> oldPoll = null;

        try {
            handler.initialize();
            oldPoll = CompletableFuture.runAsync(handler.dataPolls.get(0));
            assertTrue(handler.firstCallStarted.await(30, TimeUnit.SECONDS));

            handler.dispose();
            handler.initialize();
            assertEquals(2, handler.dataPolls.size());
            handler.dataPolls.get(1).run();

            handler.releaseFirstCall.countDown();
            oldPoll.get(30, TimeUnit.SECONDS);
            verify(callback, never()).statusUpdated(eq(thing),
                    argThat(info -> info.getStatus() == ThingStatus.OFFLINE));
            verify(callback).statusUpdated(eq(thing), argThat(info -> info.getStatus() == ThingStatus.ONLINE));
        } finally {
            handler.releaseFirstCall.countDown();
            if (oldPoll != null) {
                oldPoll.get(30, TimeUnit.SECONDS);
            }
            handler.dispose();
        }
    }

    @Test
    public void successfulOldPollCannotReplaceNewProperties() throws Exception {
        Thing thing = mockThing();
        TestHandler handler = new TestHandler(thing);
        handler.blockFirstCall = true;
        handler.firstInformation = DEVICE_INFORMATION.replace("P1 Meter", "Old Meter");
        handler.laterInformation = DEVICE_INFORMATION;
        handler.setCallback(mock(ThingHandlerCallback.class));
        CompletableFuture<Void> oldPoll = null;

        try {
            handler.initialize();
            oldPoll = CompletableFuture.runAsync(handler.dataPolls.get(0));
            assertTrue(handler.firstCallStarted.await(30, TimeUnit.SECONDS));

            handler.dispose();
            handler.initialize();
            handler.dataPolls.get(1).run();

            handler.releaseFirstCall.countDown();
            oldPoll.get(30, TimeUnit.SECONDS);
            verify(thing).setProperty("productName", "P1 Meter");
            verify(thing, never()).setProperty("productName", "Old Meter");
        } finally {
            handler.releaseFirstCall.countDown();
            if (oldPoll != null) {
                oldPoll.get(30, TimeUnit.SECONDS);
            }
            handler.dispose();
        }
    }

    @Test
    public void oldMeasurementCannotRestoreOnlineStatus() throws Exception {
        Thing thing = mockThing();
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        TestHandler handler = new TestHandler(thing);
        handler.firstInformation = DEVICE_INFORMATION;
        handler.laterInformation = DEVICE_INFORMATION;
        handler.blockFirstMeasurement = true;
        handler.setCallback(callback);
        CompletableFuture<Void> oldPoll = null;

        try {
            handler.initialize();
            oldPoll = CompletableFuture.runAsync(handler.dataPolls.get(0));
            assertTrue(handler.firstMeasurementStarted.await(30, TimeUnit.SECONDS));

            handler.dispose();
            handler.initialize();
            handler.dataPolls.get(1).run();

            handler.releaseFirstMeasurement.countDown();
            oldPoll.get(30, TimeUnit.SECONDS);
            verify(callback, times(1)).statusUpdated(eq(thing),
                    argThat(info -> info.getStatus() == ThingStatus.ONLINE));
        } finally {
            handler.releaseFirstMeasurement.countDown();
            if (oldPoll != null) {
                oldPoll.get(30, TimeUnit.SECONDS);
            }
            handler.dispose();
        }
    }

    private static class TestHandler extends HomeWizardDeviceHandler {
        final List<Runnable> dataPolls = new ArrayList<>();
        final CountDownLatch firstCallStarted = new CountDownLatch(1);
        final CountDownLatch releaseFirstCall = new CountDownLatch(1);
        final CountDownLatch firstMeasurementStarted = new CountDownLatch(1);
        final CountDownLatch releaseFirstMeasurement = new CountDownLatch(1);
        private final AtomicInteger informationCalls = new AtomicInteger();
        private final AtomicInteger measurementCalls = new AtomicInteger();
        volatile boolean blockFirstCall;
        volatile boolean blockFirstMeasurement;
        volatile String firstInformation = "";
        volatile String laterInformation = "";

        TestHandler(Thing thing) {
            super(thing);
            supportedTypes.add(HomeWizardBindingConstants.HWE_P1);
            ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
            executorService = scheduler;
            doAnswer(invocation -> {
                if (invocation.getArgument(3) == TimeUnit.SECONDS) {
                    dataPolls.add(invocation.getArgument(0));
                }
                return mock(ScheduledFuture.class);
            }).when(scheduler).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
        }

        @Override
        public String getDeviceInformationData() {
            if (informationCalls.getAndIncrement() == 0) {
                firstCallStarted.countDown();
                if (blockFirstCall) {
                    try {
                        if (!releaseFirstCall.await(30, TimeUnit.SECONDS)) {
                            throw new AssertionError("First device information request did not finish");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new AssertionError(e);
                    }
                }
                return firstInformation;
            }
            return laterInformation;
        }

        @Override
        public String getSystemData() {
            return "{}";
        }

        @Override
        public String getMeasurementData() {
            if (measurementCalls.getAndIncrement() == 0 && blockFirstMeasurement) {
                firstMeasurementStarted.countDown();
                try {
                    if (!releaseFirstMeasurement.await(30, TimeUnit.SECONDS)) {
                        throw new AssertionError("First measurement request did not finish");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError(e);
                }
            }
            return "{}";
        }

        @Override
        protected void handleSystemData(String data) {
        }

        @Override
        protected void handleMeasurementData(String data) {
        }
    }
}
