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
package org.openhab.binding.shelly.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYBLUHT;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYMOTION;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYPLUS1PM;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYPLUSSMOKE;

import java.lang.reflect.Field;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyApiInterface;
import org.openhab.binding.shelly.internal.api.ShellyApiResult;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.LoggerFactory;

/**
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault({})
@SuppressWarnings("null")
class ShellyBaseHandlerWakeupTest {

    @Test
    void onDeviceWakeupUpdatesInitializedThingWithPendingConfiguration() throws Exception {
        assertWakeupRunsUpdate(true);
    }

    @Test
    void onDeviceWakeupUpdatesUninitializedThingWithPendingConfiguration() throws Exception {
        assertWakeupRunsUpdate(false);
    }

    @Test
    void onDeviceWakeupIgnoresOnlineThing() throws Exception {
        assertWakeupIgnored(THING_TYPE_SHELLYPLUSSMOKE, ThingStatus.ONLINE, ThingStatusDetail.NONE);
    }

    @Test
    void onDeviceWakeupIgnoresOfflineThing() throws Exception {
        assertWakeupIgnored(THING_TYPE_SHELLYPLUSSMOKE, ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
    }

    @Test
    void onDeviceWakeupIgnoresOfflineThingWithPendingConfiguration() throws Exception {
        assertWakeupIgnored(THING_TYPE_SHELLYPLUSSMOKE, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING);
    }

    @Test
    void onDeviceWakeupIgnoresAlwaysOnThing() throws Exception {
        assertWakeupIgnored(THING_TYPE_SHELLYMOTION, ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING);
    }

    @Test
    void onDeviceWakeupIgnoresBluThing() throws Exception {
        assertWakeupIgnored(THING_TYPE_SHELLYBLUHT, ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING);
    }

    private void assertWakeupRunsUpdate(boolean initialized) throws Exception {
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        ShellyBaseHandler handler = buildWakeupHandler(THING_TYPE_SHELLYPLUSSMOKE, initialized, ThingStatus.ONLINE,
                ThingStatusDetail.CONFIGURATION_PENDING, scheduler);

        handler.onDeviceWakeup();

        verify(handler, never()).restartWatchdog();
        verify(handler).requestUpdates(1, false);
        ArgumentCaptor<Runnable> job = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).execute(job.capture());
        job.getValue().run();
        verify(handler).refreshStatus();
    }

    private void assertWakeupIgnored(ThingTypeUID thingType, ThingStatus status, ThingStatusDetail detail)
            throws Exception {
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        ShellyBaseHandler handler = buildWakeupHandler(thingType, true, status, detail, scheduler);

        handler.onDeviceWakeup();

        verify(handler, never()).restartWatchdog();
        verify(handler, never()).requestUpdates(anyInt(), anyBoolean());
        verifyNoInteractions(scheduler);
    }

    private ShellyBaseHandler buildWakeupHandler(ThingTypeUID thingType, boolean initialized, ThingStatus status,
            ThingStatusDetail detail, ScheduledExecutorService scheduler) throws Exception {
        ShellyBaseHandler handler = mock(ShellyBaseHandler.class, CALLS_REAL_METHODS);
        ShellyDeviceProfile profile = new ShellyDeviceProfile(thingType);
        profile.initialized = initialized;
        handler.profile = profile;
        setField(handler, "logger", LoggerFactory.getLogger(ShellyBaseHandler.class));
        setField(handler, "scheduler", scheduler);
        doNothing().when(handler).restartWatchdog();
        doReturn(status).when(handler).getThingStatus();
        doReturn(detail).when(handler).getThingStatusDetail();
        doReturn(true).when(handler).requestUpdates(1, false);
        doNothing().when(handler).refreshStatus();
        return handler;
    }

    @Test
    void refreshStatusKeepsPendingUpdateWhileAnotherCycleIsRunning() throws Exception {
        ShellyApiInterface api = mock(ShellyApiInterface.class);
        ShellyBaseHandler handler = buildHandler(api);
        setField(handler, "statusRefreshRunning", new AtomicBoolean(true));
        handler.scheduledUpdates = 1;

        handler.refreshStatus();

        assertEquals(1, handler.scheduledUpdates);
        verify(api, never()).getStatus();
    }

    @Test
    void refreshStatusSkipsCycleStartedDuringRunningCycle() throws Exception {
        ShellyApiInterface api = mock(ShellyApiInterface.class);
        ShellyBaseHandler handler = buildHandler(api);
        when(api.getStatus()).thenAnswer(invocation -> {
            handler.refreshStatus();
            throw throttled();
        });

        handler.refreshStatus();

        verify(api, times(1)).getStatus();
    }

    @Test
    void refreshStatusRunsAgainAfterCycleFailed() throws Exception {
        ShellyApiInterface api = mock(ShellyApiInterface.class);
        ShellyBaseHandler handler = buildHandler(api);
        when(api.getStatus()).thenThrow(throttled());

        handler.refreshStatus();
        handler.refreshStatus();

        verify(api, times(2)).getStatus();
    }

    private ShellyBaseHandler buildHandler(ShellyApiInterface api) throws Exception {
        ShellyBaseHandler handler = mock(ShellyBaseHandler.class, CALLS_REAL_METHODS);
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1PM);
        profile.initialized = true;
        profile.alwaysOn = true;
        Thing thing = mock(Thing.class);
        when(thing.getThingTypeUID()).thenReturn(THING_TYPE_SHELLYPLUS1PM);
        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(thing.getStatusInfo()).thenReturn(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));

        setField(handler, "api", api);
        setField(handler, "logger", LoggerFactory.getLogger(ShellyBaseHandler.class));
        setField(handler, "vibrationFilter", new AtomicInteger());
        setField(handler, "statusRefreshRunning", new AtomicBoolean());
        setField(handler, "initLock", new Object());
        setField(handler, "skipCount", 1);
        setField(handler, "thing", thing);
        handler.profile = profile;
        return handler;
    }

    private static ShellyApiException throttled() {
        ShellyApiResult result = ShellyApiResult.builder().httpCode(HttpStatus.TOO_MANY_REQUESTS_429).build();
        return new ShellyApiException(result);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        for (Class<?> c = target.getClass(); c != null; c = c.getSuperclass()) {
            try {
                Field f = c.getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                continue;
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
