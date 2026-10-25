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
package org.openhab.binding.systeminfo.internal.handler;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.binding.systeminfo.internal.SystemInfoBindingConstants.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.openhab.binding.systeminfo.internal.SystemInfoThingTypeProvider;
import org.openhab.binding.systeminfo.internal.model.SystemInfoInterface;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests for the asynchronous SystemInfo handler lifecycle.
 *
 * @author Leo Siepel - Initial contribution
 */
class SystemInfoHandlerTest {
    private static final int TIMEOUT_SECONDS = 30;

    @Test
    void disposedInitializationCannotUpdateThingAfterReinitialization() throws InterruptedException {
        Thing thing = ThingBuilder.create(THING_TYPE_COMPUTER, "test")
                .withConfiguration(new Configuration(Map.of(HIGH_PRIORITY_REFRESH_TIME, BigDecimal.valueOf(60),
                        MEDIUM_PRIORITY_REFRESH_TIME, BigDecimal.valueOf(60))))
                .build();
        SystemInfoThingTypeProvider thingTypeProvider = mock(SystemInfoThingTypeProvider.class);
        when(thingTypeProvider.restoreChannelsConfig(thing.getUID())).thenReturn(Map.of());

        SystemInfoInterface systemInfo = mock(SystemInfoInterface.class);
        CountDownLatch firstStarted = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        CountDownLatch secondStarted = new CountDownLatch(1);
        CountDownLatch releaseSecond = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        doAnswer(invocation -> {
            if (calls.incrementAndGet() == 1) {
                firstStarted.countDown();
                assertTrue(releaseFirst.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            } else {
                secondStarted.countDown();
                assertTrue(releaseSecond.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            }
            return null;
        }).when(systemInfo).initializeSystemInfo();
        when(systemInfo.getCpuLogicalCores()).thenReturn(new DecimalType(1));
        when(systemInfo.getCpuPhysicalCores()).thenReturn(new DecimalType(1));
        when(systemInfo.getOsFamily()).thenReturn(new StringType("Linux"));
        when(systemInfo.getOsManufacturer()).thenReturn(new StringType("Test"));
        when(systemInfo.getOsVersion()).thenReturn(new StringType("1"));

        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        CountDownLatch online = new CountDownLatch(1);
        doAnswer(invocation -> {
            if (invocation.getArgument(1, ThingStatusInfo.class).getStatus() == ThingStatus.ONLINE) {
                online.countDown();
            }
            return null;
        }).when(callback).statusUpdated(any(), any());

        SystemInfoHandler handler = new SystemInfoHandler(thing, thingTypeProvider, systemInfo);
        handler.setCallback(callback);
        try {
            handler.initialize();
            assertTrue(firstStarted.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            handler.dispose();
            handler.initialize();
            releaseFirst.countDown();
            assertTrue(secondStarted.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            verifyNoInteractions(callback);

            releaseSecond.countDown();
            assertTrue(online.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            verify(callback).thingUpdated(thing);
            verify(callback, times(1)).statusUpdated(any(), any());
        } finally {
            releaseFirst.countDown();
            releaseSecond.countDown();
            handler.dispose();
        }
    }
}
