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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.now;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
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
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault({})
@SuppressWarnings("null")
class ShellyBaseHandlerWatchdogTest {
    private static final int HOUR = 3600;

    @ParameterizedTest
    @MethodSource("provideSilentSleepingDevices")
    void refreshStatusDetectsMissedWakeupWithoutPolling(ThingTypeUID thingType, int silentHours, boolean expectOffline)
            throws Exception {
        ShellyApiInterface api = mock(ShellyApiInterface.class);
        ShellyBaseHandler handler = prepareHandler(api, thingType);
        setField(handler, "watchdog", now() - silentHours * HOUR);
        doNothing().when(handler).setThingOfflineAndDisconnect(any(ThingStatusDetail.class), anyString());

        handler.refreshStatus();

        verify(api, never()).getStatus();
        verify(handler, times(expectOffline ? 1 : 0))
                .setThingOfflineAndDisconnect(eq(ThingStatusDetail.COMMUNICATION_ERROR), anyString());
    }

    private static Stream<Arguments> provideSilentSleepingDevices() {
        return Stream.of( //
                Arguments.of(THING_TYPE_SHELLYHT, 23, false), //
                Arguments.of(THING_TYPE_SHELLYHT, 27, true), //
                Arguments.of(THING_TYPE_SHELLYPLUSHT, 27, true), //
                Arguments.of(THING_TYPE_SHELLYBLUHT, 27, true));
    }

    @Test
    void refreshStatusDefersSettingsRefreshOfSleepingDevice() throws Exception {
        ShellyApiInterface api = mock(ShellyApiInterface.class);
        ShellyBaseHandler handler = prepareHandler(api, THING_TYPE_SHELLYHT);
        setField(handler, "refreshSettings", true);

        handler.refreshStatus();
        handler.refreshStatus();

        verify(api, never()).getStatus();
        verify(api, never()).getDeviceProfile(any(), any());
    }

    @ParameterizedTest
    @CsvSource({ "false, 1", "true, 0" })
    void wakeupOfOnlineSleepingDeviceRetriesPendingSettingsRefresh(boolean alwaysOn, int expectedUpdates)
            throws Exception {
        ShellyBaseHandler handler = prepareHandler(mock(ShellyApiInterface.class), THING_TYPE_SHELLYPLUSHT);
        handler.profile.alwaysOn = alwaysOn;
        setField(handler, "refreshSettings", true);
        doReturn(true).when(handler).updateChannel(anyString(), anyString(), any(State.class));

        handler.setThingOnline();

        assertThat(handler.scheduledUpdates, is(expectedUpdates));
    }

    @ParameterizedTest
    @CsvSource({ "true, false", "false, true" })
    void goingOfflineForcesReinitOnlyForAlwaysOnDevices(boolean alwaysOn, boolean keepsInitialized) throws Exception {
        ShellyBaseHandler handler = prepareHandler(mock(ShellyApiInterface.class), THING_TYPE_SHELLYHT);
        handler.profile.alwaysOn = alwaysOn;

        handler.setThingOfflineAndDisconnect(ThingStatusDetail.COMMUNICATION_ERROR, "offline.status-error-watchdog");

        assertThat(handler.profile.initialized, is(keepsInitialized));
    }

    @Test
    void offlineSleepingDeviceIsNotFlaggedAgainWhileWaitingForWakeup() throws Exception {
        ShellyBaseHandler handler = prepareHandler(mock(ShellyApiInterface.class), THING_TYPE_SHELLYHT);
        setField(handler, "watchdog", now() - 27 * HOUR);
        handler.setThingOfflineAndDisconnect(ThingStatusDetail.COMMUNICATION_ERROR, "offline.status-error-watchdog");
        clearInvocations(handler);

        handler.refreshStatus();

        verify(handler, never()).setThingOfflineAndDisconnect(any(ThingStatusDetail.class), anyString());
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

    private static ShellyBaseHandler prepareHandler(ShellyApiInterface api, ThingTypeUID thingType) throws Exception {
        ShellyBaseHandler handler = mock(ShellyBaseHandler.class, CALLS_REAL_METHODS);
        Thing thing = mock(Thing.class);
        ShellyDeviceProfile profile = new ShellyDeviceProfile(thingType);
        profile.initialized = true;
        profile.alwaysOn = false;
        profile.updateWatchdogPeriod();

        setField(handler, "api", api);
        setField(handler, "logger", LoggerFactory.getLogger(ShellyBaseHandler.class));
        setField(handler, "messages", mock(ShellyTranslationProvider.class));
        setField(handler, "stats", new ShellyDeviceStats());
        setField(handler, "skipCount", 1);
        setField(handler, "watchdog", now());
        setField(handler, "thing", thing);
        handler.profile = profile;
        doReturn(thing).when(handler).getThing();
        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(thing.getThingTypeUID()).thenReturn(thingType);
        doReturn(ThingStatusDetail.NONE).when(handler).getThingStatusDetail();
        doNothing().when(handler).updateStatus(any(ThingStatus.class), any(ThingStatusDetail.class), any());
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
