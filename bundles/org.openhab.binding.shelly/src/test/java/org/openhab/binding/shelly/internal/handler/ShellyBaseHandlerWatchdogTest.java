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
import static org.openhab.binding.shelly.internal.util.ShellyUtils.now;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.shelly.internal.api.ShellyApiInterface;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySensorSleepMode;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.State;
import org.slf4j.LoggerFactory;

/**
 * Verifies the watchdog of sleeping and event-driven devices: event-driven buttons/remotes are never forced offline,
 * sleeping devices are flagged once they miss their (configured, assumed or learned) wakeup period.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault({})
@SuppressWarnings("null")
class ShellyBaseHandlerWatchdogTest {
    private static final int HOUR = 3600;

    static Stream<ThingTypeUID> eventDrivenTypes() {
        return Stream.of(THING_TYPE_SHELLYBUTTON1, THING_TYPE_SHELLYBLUBUTTON1, THING_TYPE_SHELLYBLUWALLSWITCH4,
                THING_TYPE_SHELLYBLURCBUTTON4, THING_TYPE_SHELLYBLUREMOTE);
    }

    static Stream<ThingTypeUID> policedTypes() {
        return Stream.of(THING_TYPE_SHELLYPLUS1PM, THING_TYPE_SHELLYHT, THING_TYPE_SHELLYPLUSHT,
                THING_TYPE_SHELLYBLUDISTANCE, THING_TYPE_SHELLYBLUHT, THING_TYPE_SHELLYBLUDW,
                THING_TYPE_SHELLYBLUMOTION, THING_TYPE_SHELLYBLUWS90);
    }

    static Stream<ThingTypeUID> sleepingTypesWithoutKnownWakeupPeriod() {
        return Stream.of(THING_TYPE_SHELLYHT, THING_TYPE_SHELLYFLOOD, THING_TYPE_SHELLYPLUSHT);
    }

    @ParameterizedTest
    @MethodSource("eventDrivenTypes")
    void refreshStatusSkipsOfflineForEventDrivenDeviceDespiteStaleWatchdog(ThingTypeUID thingType) throws Exception {
        ShellyBaseHandler handler = mock(ShellyBaseHandler.class, CALLS_REAL_METHODS);
        ShellyApiInterface api = mock(ShellyApiInterface.class);
        prepareHandler(handler, api, thingType);

        handler.refreshStatus();

        verify(handler, never()).setThingOfflineAndDisconnect(any(), any(), any());
        verify(api, never()).getStatus();
    }

    @ParameterizedTest
    @MethodSource("policedTypes")
    void refreshStatusFlagsPeriodicDeviceOfflineOnStaleWatchdog(ThingTypeUID thingType) throws Exception {
        ShellyBaseHandler handler = mock(ShellyBaseHandler.class, CALLS_REAL_METHODS);
        ShellyApiInterface api = mock(ShellyApiInterface.class);
        prepareHandler(handler, api, thingType);
        expectOffline(handler);

        handler.refreshStatus();

        verify(handler).setThingOfflineAndDisconnect(eq(ThingStatusDetail.COMMUNICATION_ERROR), anyString());
    }

    @ParameterizedTest
    @MethodSource("sleepingTypesWithoutKnownWakeupPeriod")
    void refreshStatusKeepsSleepingDeviceOnlineWithinLongestWakeupPeriod(ThingTypeUID thingType) throws Exception {
        ShellyBaseHandler handler = mock(ShellyBaseHandler.class, CALLS_REAL_METHODS);
        ShellyApiInterface api = mock(ShellyApiInterface.class);
        ShellyDeviceProfile profile = prepareHandler(handler, api, thingType);
        profile.updateWatchdogPeriod();
        setField(handler, "watchdog", now() - 23 * HOUR);

        handler.refreshStatus();

        verify(handler, never()).setThingOfflineAndDisconnect(any(), any(), any());
        verify(api, never()).getStatus();
    }

    @ParameterizedTest
    @MethodSource("sleepingTypesWithoutKnownWakeupPeriod")
    void refreshStatusFlagsSleepingDeviceOfflineAfterLongestWakeupPeriod(ThingTypeUID thingType) throws Exception {
        ShellyBaseHandler handler = mock(ShellyBaseHandler.class, CALLS_REAL_METHODS);
        ShellyApiInterface api = mock(ShellyApiInterface.class);
        ShellyDeviceProfile profile = prepareHandler(handler, api, thingType);
        profile.updateWatchdogPeriod();
        setField(handler, "watchdog", now() - 27 * HOUR);
        expectOffline(handler);

        handler.refreshStatus();

        verify(handler).setThingOfflineAndDisconnect(eq(ThingStatusDetail.COMMUNICATION_ERROR), anyString());
    }

    @Test
    void restartWatchdogLearnsLongerWakeupPeriodOfSleepingDevice() throws Exception {
        ShellyBaseHandler handler = mock(ShellyBaseHandler.class, CALLS_REAL_METHODS);
        ShellyDeviceProfile profile = prepareHandler(handler, mock(ShellyApiInterface.class), THING_TYPE_SHELLYHT);
        profile.settings.sleepMode = sleepMode(1, "h");
        profile.updateWatchdogPeriod();
        setField(handler, "lastReport", now() - 6 * HOUR);
        stubHeartbeat(handler);

        handler.restartWatchdog();

        assertThat(profile.learnedWakeupPeriod >= 6 * HOUR && profile.learnedWakeupPeriod < 6 * HOUR + 10, is(true));
        assertThat(profile.updatePeriod, is(equalTo((int) Math.round(profile.learnedWakeupPeriod * 1.1) + 60)));
    }

    @Test
    void restartWatchdogDoesNotLearnFromFirstReport() throws Exception {
        ShellyBaseHandler handler = mock(ShellyBaseHandler.class, CALLS_REAL_METHODS);
        ShellyDeviceProfile profile = prepareHandler(handler, mock(ShellyApiInterface.class), THING_TYPE_SHELLYHT);
        profile.settings.sleepMode = sleepMode(1, "h");
        profile.updateWatchdogPeriod();
        stubHeartbeat(handler);

        handler.restartWatchdog();

        assertThat(profile.learnedWakeupPeriod, is(equalTo(0)));
    }

    @ParameterizedTest
    @MethodSource("eventDrivenTypes")
    void restartWatchdogDoesNotLearnFromEventDrivenDevice(ThingTypeUID thingType) throws Exception {
        ShellyBaseHandler handler = mock(ShellyBaseHandler.class, CALLS_REAL_METHODS);
        ShellyDeviceProfile profile = prepareHandler(handler, mock(ShellyApiInterface.class), thingType);
        setField(handler, "lastReport", now() - 6 * HOUR);
        stubHeartbeat(handler);

        handler.restartWatchdog();

        assertThat(profile.learnedWakeupPeriod, is(equalTo(0)));
        assertThat(profile.updatePeriod, is(equalTo(60)));
    }

    @Test
    void getProfileRecomputesWatchdogPeriodFromRefreshedSettings() throws Exception {
        ShellyBaseHandler handler = mock(ShellyBaseHandler.class, CALLS_REAL_METHODS);
        ShellyApiInterface api = mock(ShellyApiInterface.class);
        ShellyDeviceProfile profile = prepareHandler(handler, api, THING_TYPE_SHELLYHT);
        profile.settings.sleepMode = sleepMode(2, "h");
        profile.learnedWakeupPeriod = 20 * HOUR;
        when(api.getDeviceProfile(any(), any())).thenReturn(profile);

        handler.getProfile(true);

        assertThat(profile.learnedWakeupPeriod, is(equalTo(0)));
        assertThat(profile.updatePeriod, is(equalTo((int) Math.round(2 * HOUR * 1.1) + 60)));
    }

    private static ShellyDeviceProfile prepareHandler(ShellyBaseHandler handler, ShellyApiInterface api,
            ThingTypeUID thingType) throws Exception {
        Thing thing = mock(Thing.class);
        ShellyDeviceProfile profile = new ShellyDeviceProfile(thingType);
        profile.initialized = true;
        profile.alwaysOn = false;
        profile.updatePeriod = 60;

        setField(handler, "api", api);
        setField(handler, "logger", LoggerFactory.getLogger(ShellyBaseHandler.class));
        setField(handler, "skipCount", 1);
        setField(handler, "watchdog", now() - 999_999);
        setField(handler, "thing", thing);
        handler.profile = profile;
        doReturn(thing).when(handler).getThing();
        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(thing.getThingTypeUID()).thenReturn(thingType);
        doReturn(ThingStatusDetail.NONE).when(handler).getThingStatusDetail();
        return profile;
    }

    private static void expectOffline(ShellyBaseHandler handler) throws Exception {
        setField(handler, "stats", new ShellyDeviceStats());
        doNothing().when(handler).setThingOfflineAndDisconnect(any(ThingStatusDetail.class), anyString());
    }

    private static void stubHeartbeat(ShellyBaseHandler handler) {
        doReturn(true).when(handler).updateChannel(anyString(), anyString(), any(State.class));
    }

    private static ShellySensorSleepMode sleepMode(int period, String unit) {
        ShellySensorSleepMode sleepMode = new ShellySensorSleepMode();
        sleepMode.period = period;
        sleepMode.unit = unit;
        return sleepMode;
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
