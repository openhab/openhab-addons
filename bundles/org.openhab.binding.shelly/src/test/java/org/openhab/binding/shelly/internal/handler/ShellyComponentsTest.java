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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.SHELLY_API_INVTEMP;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellyExtAnalogInput;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellyExtDigitalInput;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellyExtHumidity;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellyExtTemperature;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellyExtTemperature.ShellyShortTemp;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellyExtVoltage;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.UnDefType;

/**
 * Tests for {@link ShellyComponents} — addon sensor update path.
 *
 * Covers:
 * - {@link ShellyComponents#hasAddon} detection logic
 * - {@link ShellyComponents#updateTempChannel} INVTEMP → UNDEF promotion
 * - {@link ShellyComponents#updateSensors} lastUpdate heartbeat for relay+addon devices
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyComponentsTest {

    // ── hasAddon ─────────────────────────────────────────────────────────────

    @Test
    void hasAddon_allNull_returnsFalse() {
        assertThat(ShellyComponents.hasAddon(new ShellySettingsStatus()), is(false));
    }

    @Test
    void hasAddon_extTemperature_returnsTrue() {
        ShellySettingsStatus s = new ShellySettingsStatus();
        s.extTemperature = new ShellyExtTemperature();
        assertThat(ShellyComponents.hasAddon(s), is(true));
    }

    @Test
    void hasAddon_extHumidity_returnsTrue() {
        ShellySettingsStatus s = new ShellySettingsStatus();
        s.extHumidity = new ShellyExtHumidity();
        assertThat(ShellyComponents.hasAddon(s), is(true));
    }

    @Test
    void hasAddon_extVoltage_returnsTrue() {
        ShellySettingsStatus s = new ShellySettingsStatus();
        s.extVoltage = new ShellyExtVoltage();
        assertThat(ShellyComponents.hasAddon(s), is(true));
    }

    @Test
    void hasAddon_extDigitalInput_returnsTrue() {
        ShellySettingsStatus s = new ShellySettingsStatus();
        s.extDigitalInput = new ShellyExtDigitalInput();
        assertThat(ShellyComponents.hasAddon(s), is(true));
    }

    @Test
    void hasAddon_extAnalogInput_returnsTrue() {
        ShellySettingsStatus s = new ShellySettingsStatus();
        s.extAnalogInput = new ShellyExtAnalogInput();
        assertThat(ShellyComponents.hasAddon(s), is(true));
    }

    // ── updateTempChannel ─────────────────────────────────────────────────────

    @Test
    void updateTempChannel_nullSensor_returnsFalse() {
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        assertThat(ShellyComponents.updateTempChannel(null, handler, CHANNEL_ESENSOR_TEMP1), is(false));
        verify(handler, never()).updateChannel(anyString(), anyString(), any());
    }

    @Test
    void updateTempChannel_invTemp_publishesUndef() {
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.updateChannel(anyString(), anyString(), any())).thenReturn(true);

        ShellyShortTemp sensor = new ShellyShortTemp();
        sensor.tC = SHELLY_API_INVTEMP;

        assertThat(ShellyComponents.updateTempChannel(sensor, handler, CHANNEL_ESENSOR_TEMP1), is(true));
        verify(handler).updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_ESENSOR_TEMP1, UnDefType.UNDEF);
    }

    @Test
    void updateTempChannel_validTemp_publishesQuantityType() {
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.updateChannel(anyString(), anyString(), any())).thenReturn(true);

        ShellyShortTemp sensor = new ShellyShortTemp();
        sensor.tC = 22.5;

        assertThat(ShellyComponents.updateTempChannel(sensor, handler, CHANNEL_ESENSOR_TEMP1), is(true));
        verify(handler).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_ESENSOR_TEMP1),
                argThat(s -> s instanceof QuantityType<?>));
    }

    @Test
    void updateTempChannel_nullTc_returnsFalse() {
        ShellyThingInterface handler = mock(ShellyThingInterface.class);

        ShellyShortTemp sensor = new ShellyShortTemp();
        sensor.tC = null;

        assertThat(ShellyComponents.updateTempChannel(sensor, handler, CHANNEL_ESENSOR_TEMP1), is(false));
        verify(handler, never()).updateChannel(anyString(), anyString(), any());
    }

    // ── updateSensors — relay + addon ─────────────────────────────────────────

    @Test
    void updateSensors_relayWithAddonTemp_updatesLastUpdate() throws Exception {
        ShellyThingInterface handler = relayHandlerWith(new ShellySettingsStatus());
        ShellySettingsStatus status = new ShellySettingsStatus();
        status.extTemperature = new ShellyExtTemperature();
        status.extTemperature.sensor1 = sensorAt(20.0);

        ShellyComponents.updateSensors(handler, status);

        verify(handler).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_LAST_UPDATE), any());
    }

    @Test
    void updateSensors_relayWithoutAddon_noLastUpdate() throws Exception {
        ShellyThingInterface handler = relayHandlerWith(new ShellySettingsStatus());
        ShellySettingsStatus status = new ShellySettingsStatus();
        // no extTemperature, extHumidity, etc.

        ShellyComponents.updateSensors(handler, status);

        verify(handler, never()).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_LAST_UPDATE), any());
    }

    @Test
    void updateSensors_relayWithInvTemp_publishesUndefAndUpdatesLastUpdate() throws Exception {
        ShellyThingInterface handler = relayHandlerWith(new ShellySettingsStatus());
        ShellySettingsStatus status = new ShellySettingsStatus();
        status.extTemperature = new ShellyExtTemperature();
        status.extTemperature.sensor1 = sensorAt(SHELLY_API_INVTEMP);

        ShellyComponents.updateSensors(handler, status);

        verify(handler).updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_ESENSOR_TEMP1, UnDefType.UNDEF);
        verify(handler).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_LAST_UPDATE), any());
    }

    @Test
    void updateSensors_pureRelayNoAddon_lastUpdateNeverWritten() throws Exception {
        ShellyThingInterface handler = relayHandlerWith(new ShellySettingsStatus());

        ShellyComponents.updateSensors(handler, new ShellySettingsStatus());

        verify(handler, never()).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_LAST_UPDATE), any());
    }

    @Test
    void updateSensors_lastUpdateWrittenEvenWhenTempValueUnchanged() throws Exception {
        // Critical heartbeat test: even when the temperature channel is cache-deduplicated
        // (updateChannel returns false), sensors#lastUpdate must still be written so the
        // channel acts as a "sensor alive" indicator.
        ShellyThingInterface handler = relayHandlerWith(new ShellySettingsStatus());
        when(handler.updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_ESENSOR_TEMP1), any())).thenReturn(false); // simulate
                                                                                                                   // cache
                                                                                                                   // dedup

        ShellySettingsStatus status = new ShellySettingsStatus();
        status.extTemperature = new ShellyExtTemperature();
        status.extTemperature.sensor1 = sensorAt(22.5);

        boolean result = ShellyComponents.updateSensors(handler, status);

        assertThat("updated must be true even when temp is deduplicated", result, is(true));
        verify(handler).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_LAST_UPDATE), any());
    }

    @Test
    void updateSensors_nullSensorSlot_skipsPublish() throws Exception {
        // sensor1 valid, sensor2 null — only sensor1 published, sensor2 not touched
        ShellyThingInterface handler = relayHandlerWith(new ShellySettingsStatus());
        ShellySettingsStatus status = new ShellySettingsStatus();
        status.extTemperature = new ShellyExtTemperature();
        status.extTemperature.sensor1 = sensorAt(21.0);
        status.extTemperature.sensor2 = null;

        ShellyComponents.updateSensors(handler, status);

        verify(handler).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_ESENSOR_TEMP1), any());
        verify(handler, never()).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_ESENSOR_TEMP2), any());
    }

    @Test
    void updateSensors_addonHumidity_updatesLastUpdate() throws Exception {
        ShellyThingInterface handler = relayHandlerWith(new ShellySettingsStatus());
        ShellySettingsStatus status = new ShellySettingsStatus();
        status.extHumidity = new ShellyExtHumidity(55.0);

        ShellyComponents.updateSensors(handler, status);

        verify(handler).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_LAST_UPDATE), any());
    }

    @Test
    void hasAddon_multipleFieldsSet_returnsTrue() {
        ShellySettingsStatus s = new ShellySettingsStatus();
        s.extTemperature = new ShellyExtTemperature();
        s.extHumidity = new ShellyExtHumidity();
        assertThat(ShellyComponents.hasAddon(s), is(true));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static ShellyThingInterface relayHandlerWith(ShellySettingsStatus profileStatus) {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1PM);
        profile.isSensor = false;
        profile.hasBattery = false;
        profile.status = profileStatus;

        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.getProfile()).thenReturn(profile);
        when(handler.areChannelsCreated()).thenReturn(true);
        when(handler.updateChannel(anyString(), anyString(), any())).thenReturn(true);
        return handler;
    }

    private static ShellyShortTemp sensorAt(double tC) {
        ShellyShortTemp s = new ShellyShortTemp();
        s.tC = tC;
        return s;
    }
}
