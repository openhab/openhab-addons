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
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyApiInterface;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.util.ShellyChannelCache;
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
class ShellyBaseHandlerStatsLifecycleTest {

    private ShellyBaseHandler handler;
    private ShellyApiInterface api;

    @BeforeEach
    void setUp() throws Exception {
        handler = mock(ShellyBaseHandler.class, CALLS_REAL_METHODS);
        api = mock(ShellyApiInterface.class);
        when(api.getDeviceInfo()).thenThrow(new ShellyApiException("stop after stats handling"));
        Thing thing = mock(Thing.class);
        when(thing.getThingTypeUID()).thenReturn(THING_TYPE_SHELLYPLUS1PM);
        when(thing.getStatus()).thenReturn(ThingStatus.OFFLINE);
        when(thing.getStatusInfo()).thenReturn(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.NONE, null));

        setField("api", api);
        setField("logger", LoggerFactory.getLogger(ShellyBaseHandler.class));
        setField("initLock", new Object());
        setField("thing", thing);
        setField("stats", new ShellyDeviceStats());
        setField("cache", mock(ShellyChannelCache.class));
        setField("lastWakeupReason", new AtomicReference<>(""));
        setField("scheduler", mock(ScheduledExecutorService.class));
        handler.profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1PM);
    }

    @Test
    void reinitializationKeepsCountersAndAlarm() throws Exception {
        ShellyDeviceStats stats = handler.getStats();
        stats.restarts.set(1);
        stats.alarms.set(1);
        stats.lastUptime.set(1000);

        assertThrows(ShellyApiException.class, handler::initializeThing);

        assertSame(stats, handler.getStats());
        assertEquals(1, stats.restarts.get());
        assertEquals(1, stats.alarms.get());
        assertEquals(0, stats.lastUptime.get());
        verify(api, never()).resetTimeoutCounters();
    }

    @Test
    void handlerInitializationResetsStats() {
        ShellyDeviceStats stats = handler.getStats();
        stats.restarts.set(1);

        handler.initialize();

        assertNotSame(stats, handler.getStats());
        assertEquals(0, handler.getStats().restarts.get());
        verify(api).resetTimeoutCounters();
    }

    private void setField(String fieldName, Object value) throws Exception {
        for (Class<?> c = handler.getClass(); c != null; c = c.getSuperclass()) {
            try {
                Field f = c.getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(handler, value);
                return;
            } catch (NoSuchFieldException e) {
                continue;
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
