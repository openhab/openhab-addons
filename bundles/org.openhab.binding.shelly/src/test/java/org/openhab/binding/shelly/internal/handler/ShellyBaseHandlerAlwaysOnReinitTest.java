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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYPLUS1PM;

import java.lang.reflect.Field;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyApiInterface;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.binding.shelly.internal.util.ShellyChannelCache;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.LoggerFactory;

/**
 * Verifies that {@link ShellyBaseHandler#initializeThing} does not optimistically flip an always-on device that
 * is currently OFFLINE back to ONLINE/CONFIGURATION_PENDING before a reconnect attempt has actually succeeded.
 * Doing so unconditionally previously caused a flip-flop between OFFLINE and ONLINE/CONFIGURATION_PENDING on
 * every poll cycle while the device stayed genuinely unreachable.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault({})
@SuppressWarnings("null")
class ShellyBaseHandlerAlwaysOnReinitTest {

    @Test
    void initializeThingDoesNotFlipOfflineAlwaysOnDeviceToPendingWhileStillUnreachable() throws Exception {
        ShellyBaseHandler handler = prepare(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);

        assertThrows(ShellyApiException.class, () -> handler.initializeThing());

        verify(handler, never()).updateStatus(eq(ThingStatus.ONLINE), eq(ThingStatusDetail.CONFIGURATION_PENDING),
                any());
    }

    @Test
    void initializeThingStillFlipsNonOfflineAlwaysOnDeviceToPending() throws Exception {
        ShellyBaseHandler handler = prepare(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);

        assertThrows(ShellyApiException.class, () -> handler.initializeThing());

        verify(handler).updateStatus(eq(ThingStatus.ONLINE), eq(ThingStatusDetail.CONFIGURATION_PENDING), any());
    }

    private static ShellyBaseHandler prepare(ThingStatus status, ThingStatusDetail detail) throws Exception {
        ShellyBaseHandler handler = mock(ShellyBaseHandler.class, CALLS_REAL_METHODS);
        ShellyApiInterface api = mock(ShellyApiInterface.class);
        Thing thing = mock(Thing.class);
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1PM);
        profile.alwaysOn = true;
        profile.initialized = false;

        setField(handler, "api", api);
        setField(handler, "logger", LoggerFactory.getLogger(ShellyBaseHandler.class));
        setField(handler, "cache", mock(ShellyChannelCache.class));
        setField(handler, "thing", thing);
        setField(handler, "messages", mock(ShellyTranslationProvider.class));
        handler.profile = profile;
        doReturn(thing).when(handler).getThing();
        when(thing.getStatus()).thenReturn(status);
        when(thing.getThingTypeUID()).thenReturn(THING_TYPE_SHELLYPLUS1PM);
        doReturn(detail).when(handler).getThingStatusDetail();
        doNothing().when(handler).updateStatus(any(ThingStatus.class), any(ThingStatusDetail.class), any());
        doThrow(new ShellyApiException("device still unreachable")).when(api).getDeviceInfo();
        return handler;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field f = clazz.getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
