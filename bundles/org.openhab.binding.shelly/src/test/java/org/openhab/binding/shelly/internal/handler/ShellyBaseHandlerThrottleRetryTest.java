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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
class ShellyBaseHandlerThrottleRetryTest {

    @ParameterizedTest
    @ValueSource(ints = { 0, 1 })
    void throttledPollLeavesRetryScheduled(int pendingUpdates) throws Exception {
        ShellyBaseHandler handler = buildThrottledHandler();
        handler.scheduledUpdates = pendingUpdates;

        handler.refreshStatus();

        assertEquals(1, handler.scheduledUpdates);
        verify(handler, never()).setThingOfflineAndDisconnect(any(), any(), any());
    }

    @Test
    void throttledRetrySchedulesNoFurtherRetry() throws Exception {
        ShellyBaseHandler handler = buildThrottledHandler();

        handler.refreshStatus();
        handler.refreshStatus();

        assertEquals(0, handler.scheduledUpdates);
    }

    private ShellyBaseHandler buildThrottledHandler() throws Exception {
        ShellyBaseHandler handler = mock(ShellyBaseHandler.class, CALLS_REAL_METHODS);
        ShellyApiInterface api = mock(ShellyApiInterface.class);
        when(api.getStatus()).thenThrow(throttled());
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1PM);
        profile.initialized = true;
        profile.alwaysOn = true;
        Thing thing = mock(Thing.class);
        when(thing.getThingTypeUID()).thenReturn(THING_TYPE_SHELLYPLUS1PM);
        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(thing.getStatusInfo()).thenReturn(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));

        setField(handler, "api", api);
        setField(handler, "logger", LoggerFactory.getLogger(ShellyBaseHandler.class));
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
