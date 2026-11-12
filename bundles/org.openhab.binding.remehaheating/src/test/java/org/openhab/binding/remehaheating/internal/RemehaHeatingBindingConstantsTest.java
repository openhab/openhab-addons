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
package org.openhab.binding.remehaheating.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RemehaHeatingBindingConstants}.
 *
 * @author Michael Fraedrich - Initial contribution
 */
@NonNullByDefault
public class RemehaHeatingBindingConstantsTest {

    @Test
    public void testThingTypeUID() {
        assertEquals("remehaheating", RemehaHeatingBindingConstants.THING_TYPE_BOILER.getBindingId());
        assertEquals("boiler", RemehaHeatingBindingConstants.THING_TYPE_BOILER.getId());
    }

    @Test
    public void testChannelConstants() {
        assertEquals("room-temperature", RemehaHeatingBindingConstants.CHANNEL_ROOM_TEMPERATURE);
        assertEquals("target-temperature", RemehaHeatingBindingConstants.CHANNEL_TARGET_TEMPERATURE);
        assertEquals("dhw-temperature", RemehaHeatingBindingConstants.CHANNEL_DHW_TEMPERATURE);
        assertEquals("dhw-target", RemehaHeatingBindingConstants.CHANNEL_DHW_TARGET);
        assertEquals("water-pressure", RemehaHeatingBindingConstants.CHANNEL_WATER_PRESSURE);
        assertEquals("outdoor-temperature", RemehaHeatingBindingConstants.CHANNEL_OUTDOOR_TEMPERATURE);
        assertEquals("status", RemehaHeatingBindingConstants.CHANNEL_STATUS);
        assertEquals("dhw-mode", RemehaHeatingBindingConstants.CHANNEL_DHW_MODE);
        assertEquals("water-pressure-ok", RemehaHeatingBindingConstants.CHANNEL_WATER_PRESSURE_OK);
        assertEquals("dhw-status", RemehaHeatingBindingConstants.CHANNEL_DHW_STATUS);
        assertEquals("thermal-mode", RemehaHeatingBindingConstants.CHANNEL_THERMAL_MODE);
        assertEquals("operating-mode", RemehaHeatingBindingConstants.CHANNEL_OPERATING_MODE);
        assertEquals("seasonal-mode", RemehaHeatingBindingConstants.CHANNEL_SEASONAL_MODE);
        assertEquals("holiday-active", RemehaHeatingBindingConstants.CHANNEL_HOLIDAY_ACTIVE);
        assertEquals("zone-mode", RemehaHeatingBindingConstants.CHANNEL_ZONE_MODE);
        assertEquals("heating-demand", RemehaHeatingBindingConstants.CHANNEL_HEATING_DEMAND);
        assertEquals("next-setpoint", RemehaHeatingBindingConstants.CHANNEL_NEXT_SETPOINT);
        assertEquals("next-switch-time", RemehaHeatingBindingConstants.CHANNEL_NEXT_SWITCH_TIME);
        assertEquals("fireplace-mode-active", RemehaHeatingBindingConstants.CHANNEL_FIREPLACE_MODE_ACTIVE);
        assertEquals("dhw-next-switch-time", RemehaHeatingBindingConstants.CHANNEL_DHW_NEXT_SWITCH_TIME);
        assertEquals("dhw-anti-legionella", RemehaHeatingBindingConstants.CHANNEL_DHW_ANTI_LEGIONELLA);
        assertEquals("heating-curve-slope", RemehaHeatingBindingConstants.CHANNEL_HEATING_CURVE_SLOPE);
        assertEquals("heating-curve-base-setpoint", RemehaHeatingBindingConstants.CHANNEL_HEATING_CURVE_BASE_SETPOINT);
        assertEquals("heating-curve-max-flow-temperature",
                RemehaHeatingBindingConstants.CHANNEL_HEATING_CURVE_MAX_FLOW_TEMPERATURE);
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
