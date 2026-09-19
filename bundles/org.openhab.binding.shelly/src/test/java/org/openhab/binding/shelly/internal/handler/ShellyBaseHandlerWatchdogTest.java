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

import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.now;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.shelly.internal.api.ShellyApiInterface;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.LoggerFactory;

/**
 * Verifies that the watchdog never forces event-driven buttons/remotes offline for staying silent, while sleeping
 * sensors and periodically reporting devices are still flagged as expired.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault({})
@SuppressWarnings("null")
class ShellyBaseHandlerWatchdogTest {

    static Stream<ThingTypeUID> eventDrivenBluTypes() {
        return Stream.of(THING_TYPE_SHELLYBUTTON1, THING_TYPE_SHELLYBLUBUTTON1, THING_TYPE_SHELLYBLUWALLSWITCH4,
                THING_TYPE_SHELLYBLURCBUTTON4, THING_TYPE_SHELLYBLUREMOTE);
    }

    static Stream<ThingTypeUID> policedTypes() {
        return Stream.of(THING_TYPE_SHELLYPLUS1PM, THING_TYPE_SHELLYHT, THING_TYPE_SHELLYPLUSHT,
                THING_TYPE_SHELLYBLUDISTANCE, THING_TYPE_SHELLYBLUHT, THING_TYPE_SHELLYBLUDW,
                THING_TYPE_SHELLYBLUMOTION, THING_TYPE_SHELLYBLUWS90);
    }

    @ParameterizedTest
    @MethodSource("eventDrivenBluTypes")
    void refreshStatusSkipsOfflineForEventDrivenBluDespiteStaleWatchdog(ThingTypeUID thingType) throws Exception {
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
        setField(handler, "stats", new ShellyDeviceStats());
        doNothing().when(handler).setThingOfflineAndDisconnect(any(ThingStatusDetail.class), anyString());

        handler.refreshStatus();

        verify(handler).setThingOfflineAndDisconnect(eq(ThingStatusDetail.COMMUNICATION_ERROR), anyString());
    }

    private static void prepareHandler(ShellyBaseHandler handler, ShellyApiInterface api, ThingTypeUID thingType)
            throws Exception {
        Thing thing = mock(Thing.class);
        ShellyDeviceProfile profile = new ShellyDeviceProfile(thingType);
        profile.initialized = true;
        profile.alwaysOn = false;
        profile.updatePeriod = 60;

        setField(handler, "api", api);
        setField(handler, "logger", LoggerFactory.getLogger(ShellyBaseHandler.class));
        setField(handler, "skipCount", 1);
        setField(handler, "watchdog", now() - 999_999);
        handler.profile = profile;
        doReturn(thing).when(handler).getThing();
        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(thing.getThingTypeUID()).thenReturn(thingType);
        doReturn(ThingStatusDetail.NONE).when(handler).getThingStatusDetail();
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getSuperclass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
