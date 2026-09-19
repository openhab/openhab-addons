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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;

import java.lang.reflect.Field;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyApiInterface;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.slf4j.LoggerFactory;

/**
 * Verifies how {@link ShellyBaseHandler} goes OFFLINE: always-on devices are re-initialized on reconnect, sleeping
 * devices keep their profile until their next wakeup, and a stopping handler no longer publishes status changes.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault({})
@SuppressWarnings("null")
class ShellyBaseHandlerOfflineHandlingTest {

    @Test
    void goingOfflineForcesReinitOfAlwaysOnDevice() throws Exception {
        ShellyBaseHandler handler = prepare(THING_TYPE_SHELLYPLUS1PM, true, ThingStatus.ONLINE);

        handler.setThingOfflineAndDisconnect(ThingStatusDetail.COMMUNICATION_ERROR, "offline.status-error-watchdog");

        assertThat(handler.profile.initialized, is(false));
    }

    @Test
    void goingOfflineKeepsProfileOfSleepingDevice() throws Exception {
        ShellyBaseHandler handler = prepare(THING_TYPE_SHELLYHT, true, ThingStatus.ONLINE);
        handler.profile.alwaysOn = false;

        handler.setThingOfflineAndDisconnect(ThingStatusDetail.COMMUNICATION_ERROR, "offline.status-error-watchdog");

        assertThat(handler.profile.initialized, is(true));
    }

    @Test
    void goingOfflineClosesApiAndDisarmsWatchdog() throws Exception {
        ShellyBaseHandler handler = prepare(THING_TYPE_SHELLYHT, true, ThingStatus.ONLINE);
        handler.profile.alwaysOn = false;
        ShellyApiInterface api = mock(ShellyApiInterface.class);
        setField(handler, "api", api);

        handler.setThingOfflineAndDisconnect(ThingStatusDetail.COMMUNICATION_ERROR, "offline.status-error-watchdog");

        verify(api).close();
        assertThat(getField(handler, "watchdog"), is(equalTo((Object) 0.0)));
    }

    @Test
    void goingOfflinePublishesOfflineStatusOnce() throws Exception {
        ShellyBaseHandler handler = prepare(THING_TYPE_SHELLYHT, true, ThingStatus.ONLINE);
        handler.profile.alwaysOn = false;

        handler.setThingOfflineAndDisconnect(ThingStatusDetail.COMMUNICATION_ERROR, "offline.status-error-watchdog");

        verify(handler).updateStatus(eq(ThingStatus.OFFLINE), eq(ThingStatusDetail.COMMUNICATION_ERROR), any());
    }

    @Test
    void alreadyOfflineDeviceKeepsItsStatus() throws Exception {
        ShellyBaseHandler handler = prepare(THING_TYPE_SHELLYHT, true, ThingStatus.OFFLINE);
        handler.profile.alwaysOn = false;

        handler.setThingOfflineAndDisconnect(ThingStatusDetail.COMMUNICATION_ERROR, "offline.status-error-watchdog");

        verify(handler, never()).updateStatus(any(ThingStatus.class), any(ThingStatusDetail.class), any());
    }

    @Test
    void stoppingHandlerDoesNotPublishStatus() throws Exception {
        ShellyBaseHandler handler = mock(ShellyBaseHandler.class, CALLS_REAL_METHODS);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        setField(handler, "thing", mock(Thing.class));
        setField(handler, "stopping", true);
        handler.setCallback(callback);

        handler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "gone");

        verify(callback, never()).statusUpdated(any(), any());
    }

    @Test
    void runningHandlerPublishesStatus() throws Exception {
        ShellyBaseHandler handler = mock(ShellyBaseHandler.class, CALLS_REAL_METHODS);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        setField(handler, "thing", mock(Thing.class));
        setField(handler, "stopping", false);
        handler.setCallback(callback);

        handler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "gone");

        verify(callback).statusUpdated(any(), any());
    }

    @Test
    void offlineSleepDeviceIsNotFlaggedAgainWhileWaitingForWakeup() throws Exception {
        ShellyBaseHandler handler = prepare(THING_TYPE_SHELLYHT, true, ThingStatus.ONLINE);
        handler.profile.alwaysOn = false;
        handler.profile.updatePeriod = 60;
        setField(handler, "skipCount", 1);
        handler.setThingOfflineAndDisconnect(ThingStatusDetail.COMMUNICATION_ERROR, "offline.status-error-watchdog");
        clearInvocations(handler);

        handler.refreshStatus();

        verify(handler, never()).setThingOfflineAndDisconnect(any(), any(), any());
    }

    private static ShellyBaseHandler prepare(ThingTypeUID thingType, boolean initialized, ThingStatus status)
            throws Exception {
        ShellyBaseHandler handler = mock(ShellyBaseHandler.class, CALLS_REAL_METHODS);
        Thing thing = mock(Thing.class);
        ShellyDeviceProfile profile = new ShellyDeviceProfile(thingType);
        profile.initialized = initialized;

        setField(handler, "api", mock(ShellyApiInterface.class));
        setField(handler, "logger", LoggerFactory.getLogger(ShellyBaseHandler.class));
        setField(handler, "messages", mock(ShellyTranslationProvider.class));
        setField(handler, "thing", thing);
        handler.profile = profile;
        doReturn(thing).when(handler).getThing();
        when(thing.getStatus()).thenReturn(status);
        when(thing.getThingTypeUID()).thenReturn(thingType);
        doNothing().when(handler).updateStatus(any(ThingStatus.class), any(ThingStatusDetail.class), any());
        return handler;
    }

    private static Field findField(Object target, String fieldName) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field f = clazz.getDeclaredField(fieldName);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        findField(target, fieldName).set(target, value);
    }

    private static Object getField(Object target, String fieldName) throws Exception {
        return findField(target, fieldName).get(target);
    }
}
