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
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYBLURCBUTTON4;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYPLUS1PM;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.now;

import java.lang.reflect.Field;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyApiInterface;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.LoggerFactory;

/**
 * Verifies that the watchdog does not force a BLU RC Button 4 (a pure event-driven remote with no periodic
 * sleep/wakeup-and-report cycle) offline just because it has been silent for longer than a regular sensor's
 * expected wakeup interval, while a regular sleeping sensor of the same age is still flagged as expired.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault({})
@SuppressWarnings("null")
class ShellyBaseHandlerWatchdogTest {

    @Test
    void refreshStatusSkipsOfflineForRcButtonDespiteStaleWatchdog() throws Exception {
        ShellyBaseHandler handler = mock(ShellyBaseHandler.class, CALLS_REAL_METHODS);
        ShellyApiInterface api = mock(ShellyApiInterface.class);
        Thing thing = mock(Thing.class);
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYBLURCBUTTON4);
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
        doReturn(ThingStatusDetail.NONE).when(handler).getThingStatusDetail();

        handler.refreshStatus();

        verify(handler, never()).setThingOfflineAndDisconnect(any(), any(), any());
        verify(api, never()).getStatus();
    }

    @Test
    void refreshStatusFlagsRegularSleepingSensorOfflineOnStaleWatchdog() throws Exception {
        ShellyBaseHandler handler = mock(ShellyBaseHandler.class, CALLS_REAL_METHODS);
        ShellyApiInterface api = mock(ShellyApiInterface.class);
        Thing thing = mock(Thing.class);
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1PM);
        profile.initialized = true;
        profile.alwaysOn = false;
        profile.updatePeriod = 60;

        setField(handler, "api", api);
        setField(handler, "logger", LoggerFactory.getLogger(ShellyBaseHandler.class));
        setField(handler, "skipCount", 1);
        setField(handler, "watchdog", now() - 999_999);
        setField(handler, "stats", new ShellyDeviceStats());
        handler.profile = profile;
        doReturn(thing).when(handler).getThing();
        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);
        doReturn(ThingStatusDetail.NONE).when(handler).getThingStatusDetail();
        doNothing().when(handler).setThingOfflineAndDisconnect(any(ThingStatusDetail.class), anyString());

        handler.refreshStatus();

        verify(handler).setThingOfflineAndDisconnect(eq(ThingStatusDetail.COMMUNICATION_ERROR), anyString());
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getSuperclass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
