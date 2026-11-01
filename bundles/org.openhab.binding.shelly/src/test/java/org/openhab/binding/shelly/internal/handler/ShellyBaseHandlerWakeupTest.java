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
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYPLUS1PM;

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
import org.slf4j.LoggerFactory;

/**
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault({})
@SuppressWarnings("null")
class ShellyBaseHandlerWakeupTest {

    @Test
    void onDeviceWakeupRunsStatusUpdateRightAway() throws Exception {
        ShellyBaseHandler handler = mock(ShellyBaseHandler.class, CALLS_REAL_METHODS);
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        setField(handler, "scheduler", scheduler);
        doReturn(true).when(handler).requestUpdates(1, true);
        doNothing().when(handler).refreshStatus();

        handler.onDeviceWakeup();

        verify(handler).requestUpdates(1, true);
        ArgumentCaptor<Runnable> job = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).execute(job.capture());
        job.getValue().run();
        verify(handler).refreshStatus();
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
