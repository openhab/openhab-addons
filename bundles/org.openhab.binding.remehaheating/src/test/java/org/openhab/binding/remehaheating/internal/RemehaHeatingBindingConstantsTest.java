/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.remehaheating.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RemehaHeatingBindingConstants}.
 *
 * @author Michael Fraedrich - Initial contribution
 */
public class RemehaHeatingBindingConstantsTest {

    @Test
    public void testThingTypeUID() {
        assertEquals("remehaheating", RemehaHeatingBindingConstants.THING_TYPE_BOILER.getBindingId());
        assertEquals("boiler", RemehaHeatingBindingConstants.THING_TYPE_BOILER.getId());
    }

    @Test
    public void testChannelConstants() {
        assertEquals("roomTemperature", RemehaHeatingBindingConstants.CHANNEL_ROOM_TEMPERATURE);
        assertEquals("targetTemperature", RemehaHeatingBindingConstants.CHANNEL_TARGET_TEMPERATURE);
        assertEquals("dhwTemperature", RemehaHeatingBindingConstants.CHANNEL_DHW_TEMPERATURE);
        assertEquals("dhwTarget", RemehaHeatingBindingConstants.CHANNEL_DHW_TARGET);
        assertEquals("waterPressure", RemehaHeatingBindingConstants.CHANNEL_WATER_PRESSURE);
        assertEquals("outdoorTemperature", RemehaHeatingBindingConstants.CHANNEL_OUTDOOR_TEMPERATURE);
        assertEquals("status", RemehaHeatingBindingConstants.CHANNEL_STATUS);
        assertEquals("dhwMode", RemehaHeatingBindingConstants.CHANNEL_DHW_MODE);
        assertEquals("waterPressureOK", RemehaHeatingBindingConstants.CHANNEL_WATER_PRESSURE_OK);
        assertEquals("dhwStatus", RemehaHeatingBindingConstants.CHANNEL_DHW_STATUS);
    }

    @Test
    public void testConfigConstants() {
        assertEquals("email", RemehaHeatingBindingConstants.CONFIG_EMAIL);
        assertEquals("password", RemehaHeatingBindingConstants.CONFIG_PASSWORD);
        assertEquals("refreshInterval", RemehaHeatingBindingConstants.CONFIG_REFRESH_INTERVAL);
    }

    @Test
    public void testDhwModeConstants() {
        assertEquals("anti-frost", RemehaHeatingBindingConstants.DHW_MODE_ANTI_FROST);
        assertEquals("schedule", RemehaHeatingBindingConstants.DHW_MODE_SCHEDULE);
        assertEquals("continuous-comfort", RemehaHeatingBindingConstants.DHW_MODE_CONTINUOUS_COMFORT);
    }
}
