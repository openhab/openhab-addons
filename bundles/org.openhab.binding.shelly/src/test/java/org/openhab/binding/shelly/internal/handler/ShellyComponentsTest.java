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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.SHELLY_API_INVTEMP;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyEMNCurrentSettings;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyEMNCurrentStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsEMeter;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsMeter;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellyExtAnalogInput;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellyExtDigitalInput;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellyExtHumidity;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellyExtTemperature;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellyExtTemperature.ShellyShortTemp;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellyExtVoltage;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Tests for {@link ShellyComponents} — addon sensor update path and meter update path.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
public class ShellyComponentsTest {

    @Test
    void hasAddonAllNullReturnsFalse() {
        assertThat(ShellyComponents.hasAddon(new ShellySettingsStatus()), is(false));
    }

    @Test
    void hasAddonExtTemperatureReturnsTrue() {
        ShellySettingsStatus s = new ShellySettingsStatus();
        s.extTemperature = new ShellyExtTemperature();
        assertThat(ShellyComponents.hasAddon(s), is(true));
    }

    @Test
    void hasAddonExtHumidityReturnsTrue() {
        ShellySettingsStatus s = new ShellySettingsStatus();
        s.extHumidity = new ShellyExtHumidity();
        assertThat(ShellyComponents.hasAddon(s), is(true));
    }

    @Test
    void hasAddonExtVoltageReturnsTrue() {
        ShellySettingsStatus s = new ShellySettingsStatus();
        s.extVoltage = new ShellyExtVoltage();
        assertThat(ShellyComponents.hasAddon(s), is(true));
    }

    @Test
    void hasAddonExtDigitalInputReturnsTrue() {
        ShellySettingsStatus s = new ShellySettingsStatus();
        s.extDigitalInput = new ShellyExtDigitalInput();
        assertThat(ShellyComponents.hasAddon(s), is(true));
    }

    @Test
    void hasAddonExtAnalogInputReturnsTrue() {
        ShellySettingsStatus s = new ShellySettingsStatus();
        s.extAnalogInput = new ShellyExtAnalogInput();
        assertThat(ShellyComponents.hasAddon(s), is(true));
    }

    @Test
    void hasAddonMultipleFieldsSetReturnsTrue() {
        ShellySettingsStatus s = new ShellySettingsStatus();
        s.extTemperature = new ShellyExtTemperature();
        s.extHumidity = new ShellyExtHumidity();
        assertThat(ShellyComponents.hasAddon(s), is(true));
    }

    @Test
    void updateTempChannelNullSensorReturnsFalse() {
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        assertThat(ShellyComponents.updateTempChannel(null, handler, CHANNEL_ESENSOR_TEMP1), is(false));
        verify(handler, never()).updateChannel(anyString(), anyString(), any());
    }

    @Test
    void updateTempChannelInvTempPublishesUndef() {
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.updateChannel(anyString(), anyString(), any())).thenReturn(true);

        ShellyShortTemp sensor = new ShellyShortTemp();
        sensor.tC = SHELLY_API_INVTEMP;

        assertThat(ShellyComponents.updateTempChannel(sensor, handler, CHANNEL_ESENSOR_TEMP1), is(true));
        verify(handler).updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_ESENSOR_TEMP1, UnDefType.UNDEF);
    }

    @Test
    void updateTempChannelValidTempPublishesQuantityType() {
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.updateChannel(anyString(), anyString(), any())).thenReturn(true);

        ShellyShortTemp sensor = new ShellyShortTemp();
        sensor.tC = 22.5;

        assertThat(ShellyComponents.updateTempChannel(sensor, handler, CHANNEL_ESENSOR_TEMP1), is(true));
        verify(handler).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_ESENSOR_TEMP1),
                argThat(s -> s instanceof QuantityType<?>));
    }

    @Test
    void updateTempChannelNullTcReturnsFalse() {
        ShellyThingInterface handler = mock(ShellyThingInterface.class);

        ShellyShortTemp sensor = new ShellyShortTemp();
        sensor.tC = null;

        assertThat(ShellyComponents.updateTempChannel(sensor, handler, CHANNEL_ESENSOR_TEMP1), is(false));
        verify(handler, never()).updateChannel(anyString(), anyString(), any());
    }

    @Test
    void updateSensorsRelayWithAddonTempUpdatesLastUpdate() throws Exception {
        ShellyThingInterface handler = relayHandlerWith(new ShellySettingsStatus());
        ShellySettingsStatus status = new ShellySettingsStatus();
        ShellyExtTemperature ext1 = new ShellyExtTemperature();
        ext1.sensor1 = sensorAt(20.0);
        status.extTemperature = ext1;

        ShellyComponents.updateSensors(handler, status);

        verify(handler).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_LAST_UPDATE), any());
    }

    @Test
    void updateSensorsRelayWithoutAddonNoLastUpdate() throws Exception {
        ShellyThingInterface handler = relayHandlerWith(new ShellySettingsStatus());
        ShellySettingsStatus status = new ShellySettingsStatus();

        ShellyComponents.updateSensors(handler, status);

        verify(handler, never()).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_LAST_UPDATE), any());
    }

    @Test
    void updateSensorsRelayWithInvTempPublishesUndefAndUpdatesLastUpdate() throws Exception {
        ShellyThingInterface handler = relayHandlerWith(new ShellySettingsStatus());
        ShellySettingsStatus status = new ShellySettingsStatus();
        ShellyExtTemperature ext2 = new ShellyExtTemperature();
        ext2.sensor1 = sensorAt(SHELLY_API_INVTEMP);
        status.extTemperature = ext2;

        ShellyComponents.updateSensors(handler, status);

        verify(handler).updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_ESENSOR_TEMP1, UnDefType.UNDEF);
        verify(handler).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_LAST_UPDATE), any());
    }

    @Test
    void updateSensorsPureRelayNoAddonLastUpdateNeverWritten() throws Exception {
        ShellyThingInterface handler = relayHandlerWith(new ShellySettingsStatus());

        ShellyComponents.updateSensors(handler, new ShellySettingsStatus());

        verify(handler, never()).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_LAST_UPDATE), any());
    }

    @Test
    void updateSensorsLastUpdateWrittenEvenWhenTempValueUnchanged() throws Exception {
        ShellyThingInterface handler = relayHandlerWith(new ShellySettingsStatus());
        when(handler.updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_ESENSOR_TEMP1), any())).thenReturn(false);

        ShellySettingsStatus status = new ShellySettingsStatus();
        ShellyExtTemperature ext3 = new ShellyExtTemperature();
        ext3.sensor1 = sensorAt(22.5);
        status.extTemperature = ext3;

        boolean result = ShellyComponents.updateSensors(handler, status);

        assertThat("updated must be true even when temp is deduplicated", result, is(true));
        verify(handler).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_LAST_UPDATE), any());
    }

    @Test
    void updateSensorsNullSensorSlotSkipsPublish() throws Exception {
        ShellyThingInterface handler = relayHandlerWith(new ShellySettingsStatus());
        ShellySettingsStatus status = new ShellySettingsStatus();
        ShellyExtTemperature ext4 = new ShellyExtTemperature();
        ext4.sensor1 = sensorAt(21.0);
        ext4.sensor2 = null;
        status.extTemperature = ext4;

        ShellyComponents.updateSensors(handler, status);

        verify(handler).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_ESENSOR_TEMP1), any());
        verify(handler, never()).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_ESENSOR_TEMP2), any());
    }

    @Test
    void updateSensorsAddonHumidityUpdatesLastUpdate() throws Exception {
        ShellyThingInterface handler = relayHandlerWith(new ShellySettingsStatus());
        ShellySettingsStatus status = new ShellySettingsStatus();
        status.extHumidity = new ShellyExtHumidity(55.0);

        ShellyComponents.updateSensors(handler, status);

        verify(handler).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_LAST_UPDATE), any());
    }

    @Test
    void updateSensorsVoltageUpdatesVoltageChannel() throws Exception {
        ShellyThingInterface handler = relayHandlerWith(new ShellySettingsStatus());
        ShellySettingsStatus status = new ShellySettingsStatus();
        status.extVoltage = new ShellyExtVoltage(3.3);

        ShellyComponents.updateSensors(handler, status);

        verify(handler).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_ESENSOR_VOLTAGE),
                argThat(s -> s instanceof QuantityType<?> && ((QuantityType<?>) s).doubleValue() == 3.3));
    }

    @Test
    void updateSensorsDigitalInputUpdatesDigitalInputChannel() throws Exception {
        ShellyThingInterface handler = relayHandlerWith(new ShellySettingsStatus());
        ShellySettingsStatus status = new ShellySettingsStatus();
        status.extDigitalInput = new ShellyExtDigitalInput(true);

        ShellyComponents.updateSensors(handler, status);

        verify(handler).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_ESENSOR_DIGITALINPUT), eq(OnOffType.ON));
    }

    @Test
    void updateSensorsAnalogInputUpdatesAnalogInputChannel() throws Exception {
        ShellyThingInterface handler = relayHandlerWith(new ShellySettingsStatus());
        ShellySettingsStatus status = new ShellySettingsStatus();
        status.extAnalogInput = new ShellyExtAnalogInput(75.0);

        ShellyComponents.updateSensors(handler, status);

        verify(handler).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_ESENSOR_ANALOGINPUT),
                argThat(s -> s instanceof QuantityType<?> && ((QuantityType<?>) s).doubleValue() == 75.0));
    }

    @Test
    void updateSensorsMultipleExtTempSlotsAllPublished() throws Exception {
        ShellyThingInterface handler = relayHandlerWith(new ShellySettingsStatus());
        ShellySettingsStatus status = new ShellySettingsStatus();
        ShellyExtTemperature ext = new ShellyExtTemperature();
        ext.sensor1 = sensorAt(20.0);
        ext.sensor2 = sensorAt(21.0);
        status.extTemperature = ext;

        ShellyComponents.updateSensors(handler, status);

        verify(handler).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_ESENSOR_TEMP1), any());
        verify(handler).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_ESENSOR_TEMP2), any());
        verify(handler, never()).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_ESENSOR_TEMP3), any());
    }

    @Test
    void frequencyNullChannelNotUpdated() {
        ShellyDeviceProfile profile = emeterProfile(false, 1);
        ShellySettingsEMeter emeter = new ShellySettingsEMeter();
        emeter.isValid = true;
        emeter.power = 100.0;
        emeter.frequency = null;

        ShellySettingsStatus status = statusWithEMeters(emeter);
        ShellyThingInterface handler = mockHandler(profile);

        ShellyComponents.updateMeters(handler, status);

        verify(handler, never()).updateChannel(anyString(), eq(CHANNEL_EMETER_FREQUENCY), any(State.class));
    }

    @Test
    void frequencySetChannelUpdated() {
        ShellyDeviceProfile profile = emeterProfile(false, 1);
        ShellySettingsEMeter emeter = new ShellySettingsEMeter();
        emeter.isValid = true;
        emeter.power = 100.0;
        emeter.frequency = 50.0;

        ShellySettingsStatus status = statusWithEMeters(emeter);
        ShellyThingInterface handler = mockHandler(profile);

        ShellyComponents.updateMeters(handler, status);

        verify(handler, atLeastOnce()).updateChannel(anyString(), eq(CHANNEL_EMETER_FREQUENCY), any(State.class));
    }

    @Test
    void gen2RollerFrequencyNotUpdated() {
        ShellyDeviceProfile profile = rollerGen2Profile(1);
        ShellySettingsEMeter emeter = new ShellySettingsEMeter();
        emeter.isValid = true;
        emeter.power = 300.0;
        emeter.frequency = null;

        ShellySettingsStatus status = statusWithEMeters(emeter);
        ShellyThingInterface handler = mockHandler(profile);

        ShellyComponents.updateMeters(handler, status);

        verify(handler, never()).updateChannel(anyString(), eq(CHANNEL_EMETER_FREQUENCY), any(State.class));
    }

    @Test
    void triphaseUpdatesThreeSeparateMeterGroups() {
        ShellyDeviceProfile profile = emeterProfile(false, 3);
        ShellySettingsEMeter em0 = new ShellySettingsEMeter();
        em0.isValid = true;
        em0.power = 100.0;
        ShellySettingsEMeter em1 = new ShellySettingsEMeter();
        em1.isValid = true;
        em1.power = 200.0;
        ShellySettingsEMeter em2 = new ShellySettingsEMeter();
        em2.isValid = true;
        em2.power = 300.0;

        ShellySettingsStatus status = statusWithEMeters(em0, em1, em2);
        ShellyThingInterface handler = mockHandler(profile);

        ShellyComponents.updateMeters(handler, status);

        verify(handler, atLeastOnce()).updateChannel(eq(CHANNEL_GROUP_METER + "1"), eq(CHANNEL_METER_CURRENTWATTS),
                any(State.class));
        verify(handler, atLeastOnce()).updateChannel(eq(CHANNEL_GROUP_METER + "2"), eq(CHANNEL_METER_CURRENTWATTS),
                any(State.class));
        verify(handler, atLeastOnce()).updateChannel(eq(CHANNEL_GROUP_METER + "3"), eq(CHANNEL_METER_CURRENTWATTS),
                any(State.class));
    }

    @Test
    void gen1TwoRelaysTwoMeterGroups() {
        ShellyDeviceProfile profile = simpleRelayProfile(2);
        ShellySettingsMeter m0 = new ShellySettingsMeter();
        m0.isValid = true;
        m0.power = 55.0;
        ShellySettingsMeter m1 = new ShellySettingsMeter();
        m1.isValid = true;
        m1.power = 88.0;

        ShellySettingsStatus status = statusWithMeters(m0, m1);
        ShellyThingInterface handler = mockHandler(profile);

        ShellyComponents.updateMeters(handler, status);

        verify(handler, atLeastOnce()).updateChannel(eq(CHANNEL_GROUP_METER + "1"), eq(CHANNEL_METER_CURRENTWATTS),
                any(State.class));
        verify(handler, atLeastOnce()).updateChannel(eq(CHANNEL_GROUP_METER + "2"), eq(CHANNEL_METER_CURRENTWATTS),
                any(State.class));
    }

    @Test
    void gen1TwoRelaysExtraMeterSlotIgnored() {
        ShellyDeviceProfile profile = simpleRelayProfile(1);
        ShellySettingsMeter m0 = new ShellySettingsMeter();
        m0.isValid = true;
        m0.power = 55.0;
        ShellySettingsMeter m1 = new ShellySettingsMeter();
        m1.isValid = true;
        m1.power = 88.0;

        ShellySettingsStatus status = statusWithMeters(m0, m1);
        ShellyThingInterface handler = mockHandler(profile);

        ShellyComponents.updateMeters(handler, status);

        verify(handler, atLeastOnce()).updateChannel(eq(CHANNEL_GROUP_METER), eq(CHANNEL_METER_CURRENTWATTS),
                any(State.class));
        verify(handler, never()).updateChannel(eq(CHANNEL_GROUP_METER + "2"), anyString(), any(State.class));
    }

    @Test
    void gen1RollerAggregatedMeterSumsBothMotorChannels() {
        ShellyDeviceProfile profile = gen1RollerProfile();
        ShellySettingsMeter m0 = new ShellySettingsMeter();
        m0.isValid = true;
        m0.power = 100.0;
        m0.total = 500.0;
        ShellySettingsMeter m1 = new ShellySettingsMeter();
        m1.isValid = true;
        m1.power = 200.0;
        m1.total = 300.0;

        ShellySettingsStatus status = statusWithMeters(m0, m1);
        ShellyThingInterface handler = mockHandler(profile);

        ShellyComponents.updateMeters(handler, status);

        verify(handler, atLeastOnce()).updateChannel(eq(CHANNEL_GROUP_METER), eq(CHANNEL_METER_CURRENTWATTS),
                any(State.class));
        verify(handler, never()).updateChannel(eq(CHANNEL_GROUP_METER + "1"), anyString(), any(State.class));
    }

    @Test
    void gen1RollerFrequencyChannelNotWritten() {
        ShellyDeviceProfile profile = gen1RollerProfile();
        ShellySettingsMeter m0 = new ShellySettingsMeter();
        m0.isValid = true;
        m0.power = 100.0;

        ShellySettingsStatus status = statusWithMeters(m0);
        ShellyThingInterface handler = mockHandler(profile);

        ShellyComponents.updateMeters(handler, status);

        verify(handler, never()).updateChannel(anyString(), eq(CHANNEL_EMETER_FREQUENCY), any(State.class));
    }

    @Test
    void simpleMetersEmptyCountersArrayNoThrow() {
        ShellyDeviceProfile profile = simpleRelayProfile(1);
        ShellySettingsMeter m0 = new ShellySettingsMeter();
        m0.isValid = true;
        m0.power = 55.0;
        m0.counters = new Double[0];

        ShellySettingsStatus status = statusWithMeters(m0);
        ShellyThingInterface handler = mockHandler(profile);

        ShellyComponents.updateMeters(handler, status);

        verify(handler, never()).updateChannel(anyString(), eq(CHANNEL_METER_LASTMIN1), any(State.class));
    }

    @Test
    void metersNullEmetersReturnsFalse() {
        ShellyDeviceProfile profile = emeterProfile(false, 1);
        ShellySettingsStatus status = new ShellySettingsStatus();
        status.emeters = null;
        status.meters = null;

        ShellyThingInterface handler = mockHandler(profile);

        boolean result = ShellyComponents.updateMeters(handler, status);

        assertFalse(result);
        verify(handler, never()).updateChannel(anyString(), anyString(), any(State.class));
    }

    @Test
    void neutralCurrentUpdatesNMeterChannels() {
        ShellyDeviceProfile profile = emeterProfile(false, 3);
        profile.settings.neutralCurrent = new ShellyEMNCurrentSettings();

        ShellySettingsEMeter em0 = new ShellySettingsEMeter();
        em0.isValid = true;
        em0.power = 100.0;
        ShellySettingsEMeter em1 = new ShellySettingsEMeter();
        em1.isValid = true;
        em1.power = 200.0;
        ShellySettingsEMeter em2 = new ShellySettingsEMeter();
        em2.isValid = true;
        em2.power = 300.0;

        ShellySettingsStatus status = statusWithEMeters(em0, em1, em2);
        ShellyEMNCurrentStatus nCurrent = new ShellyEMNCurrentStatus();
        nCurrent.isValid = true;
        nCurrent.current = 1.23;
        status.neutralCurrent = nCurrent;

        ShellyThingInterface handler = mockHandler(profile);

        ShellyComponents.updateMeters(handler, status);

        verify(handler, atLeastOnce()).updateChannel(eq(CHANNEL_GROUP_NMETER), eq(CHANNEL_NMETER_CURRENT),
                any(State.class));
    }

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

    private static ShellyThingInterface mockHandler(ShellyDeviceProfile profile) {
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.getProfile()).thenReturn(profile);
        when(handler.areChannelsCreated()).thenReturn(true);
        when(handler.updateChannel(anyString(), anyString(), any(State.class))).thenReturn(true);
        return handler;
    }

    private static ShellyShortTemp sensorAt(double tC) {
        ShellyShortTemp s = new ShellyShortTemp();
        s.tC = tC;
        return s;
    }

    private static ShellySettingsStatus statusWithEMeters(ShellySettingsEMeter... emeters) {
        ShellySettingsStatus status = new ShellySettingsStatus();
        status.emeters = new ArrayList<>();
        for (ShellySettingsEMeter em : emeters) {
            status.emeters.add(em);
        }
        return status;
    }

    private static ShellySettingsStatus statusWithMeters(ShellySettingsMeter... meters) {
        ShellySettingsStatus status = new ShellySettingsStatus();
        status.meters = new ArrayList<>();
        for (ShellySettingsMeter m : meters) {
            status.meters.add(m);
        }
        return status;
    }

    private static ShellyDeviceProfile emeterProfile(boolean isEM50, int numMeters) {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1PM);
        profile.isEMeter = true;
        profile.isGen2 = true;
        profile.isEM50 = isEM50;
        profile.numMeters = numMeters;
        return profile;
    }

    private static ShellyDeviceProfile rollerGen2Profile(int numMeters) {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS2PM_ROLLER);
        profile.isEMeter = true;
        profile.isGen2 = true;
        profile.isRoller = true;
        profile.numMeters = numMeters;
        return profile;
    }

    private static ShellyDeviceProfile simpleRelayProfile(int numMeters) {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLY25_RELAY);
        profile.isEMeter = false;
        profile.isGen2 = false;
        profile.isRoller = false;
        profile.numMeters = numMeters;
        return profile;
    }

    private static ShellyDeviceProfile gen1RollerProfile() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLY25_ROLLER);
        profile.isEMeter = false;
        profile.isGen2 = false;
        profile.isRoller = true;
        profile.numMeters = 1;
        return profile;
    }

    @Test
    void deviceTotalKwhEm1dataPresentUsesDeviceTotal() {
        ShellyDeviceProfile profile = emeterProfile(true, 2);
        ShellySettingsEMeter em0 = new ShellySettingsEMeter();
        em0.isValid = true;
        em0.power = 100.0;
        em0.total = 5000.0;
        ShellySettingsEMeter em1 = new ShellySettingsEMeter();
        em1.isValid = true;
        em1.power = 200.0;
        em1.total = 3000.0;

        ShellySettingsStatus status = statusWithEMeters(em0, em1);
        status.totalKWH = 8100.0;

        ShellyThingInterface handler = mockHandler(profile);
        ShellyComponents.updateMeters(handler, status);

        ArgumentCaptor<org.openhab.core.types.State> captor = ArgumentCaptor
                .forClass(org.openhab.core.types.State.class);
        verify(handler, atLeastOnce()).updateChannel(eq(CHANNEL_GROUP_DEV_STATUS), eq(CHANNEL_DEVST_TOTALENERGY),
                captor.capture());
        List<org.openhab.core.types.State> values = captor.getAllValues();
        QuantityType<?> last = (QuantityType<?>) values.get(values.size() - 1);
        assertEquals(8.1, last.doubleValue(), 0.001);
    }

    @Test
    void deviceTotalKwhEm1dataAbsentUsesPerMeterSum() {
        ShellyDeviceProfile profile = emeterProfile(true, 2);
        ShellySettingsEMeter em0 = new ShellySettingsEMeter();
        em0.isValid = true;
        em0.power = 100.0;
        em0.total = 5000.0;
        ShellySettingsEMeter em1 = new ShellySettingsEMeter();
        em1.isValid = true;
        em1.power = 200.0;
        em1.total = 3000.0;

        ShellySettingsStatus status = statusWithEMeters(em0, em1);
        status.totalKWH = null;

        ShellyThingInterface handler = mockHandler(profile);
        ShellyComponents.updateMeters(handler, status);

        ArgumentCaptor<org.openhab.core.types.State> captor = ArgumentCaptor
                .forClass(org.openhab.core.types.State.class);
        verify(handler, atLeastOnce()).updateChannel(eq(CHANNEL_GROUP_DEV_STATUS), eq(CHANNEL_DEVST_TOTALENERGY),
                captor.capture());
        List<org.openhab.core.types.State> values = captor.getAllValues();
        QuantityType<?> last = (QuantityType<?>) values.get(values.size() - 1);
        assertEquals(8.0, last.doubleValue(), 0.001);
    }

    @Test
    void deviceTotalKwhBothNullChannelNotUpdated() {
        ShellyDeviceProfile profile = emeterProfile(true, 2);
        ShellySettingsEMeter em0 = new ShellySettingsEMeter();
        em0.isValid = true;
        em0.power = 100.0;
        em0.total = null;
        ShellySettingsEMeter em1 = new ShellySettingsEMeter();
        em1.isValid = true;
        em1.power = 200.0;
        em1.total = null;

        ShellySettingsStatus status = statusWithEMeters(em0, em1);
        status.totalKWH = null;

        ShellyThingInterface handler = mockHandler(profile);
        ShellyComponents.updateMeters(handler, status);

        verify(handler, never()).updateChannel(eq(CHANNEL_GROUP_DEV_STATUS), eq(CHANNEL_DEVST_TOTALENERGY),
                any(org.openhab.core.types.State.class));
    }

    @Test
    void deviceTotalKwh3emEm1dataAbsentUsesThreePhaseSum() {
        ShellyDeviceProfile profile = emeterProfile(false, 3);
        ShellySettingsEMeter em0 = new ShellySettingsEMeter();
        em0.isValid = true;
        em0.power = 1000.0;
        em0.total = 10000.0;
        ShellySettingsEMeter em1 = new ShellySettingsEMeter();
        em1.isValid = true;
        em1.power = 2000.0;
        em1.total = 20000.0;
        ShellySettingsEMeter em2 = new ShellySettingsEMeter();
        em2.isValid = true;
        em2.power = 3000.0;
        em2.total = 30000.0;

        ShellySettingsStatus status = statusWithEMeters(em0, em1, em2);
        status.totalKWH = null;

        ShellyThingInterface handler = mockHandler(profile);
        ShellyComponents.updateMeters(handler, status);

        ArgumentCaptor<org.openhab.core.types.State> captor = ArgumentCaptor
                .forClass(org.openhab.core.types.State.class);
        verify(handler, atLeastOnce()).updateChannel(eq(CHANNEL_GROUP_DEV_STATUS), eq(CHANNEL_DEVST_TOTALENERGY),
                captor.capture());
        List<org.openhab.core.types.State> values = captor.getAllValues();
        QuantityType<?> last = (QuantityType<?>) values.get(values.size() - 1);
        assertEquals(60.0, last.doubleValue(), 0.001);
    }

    @Test
    void returnedEnergyNonNullRoutedToEachMeterGroup() {
        ShellyDeviceProfile profile = emeterProfile(false, 3);
        ShellySettingsEMeter em0 = new ShellySettingsEMeter();
        em0.isValid = true;
        em0.power = 100.0;
        em0.totalReturned = 1_000_000.0;
        ShellySettingsEMeter em1 = new ShellySettingsEMeter();
        em1.isValid = true;
        em1.power = 200.0;
        em1.totalReturned = 1_000_000.0;
        ShellySettingsEMeter em2 = new ShellySettingsEMeter();
        em2.isValid = true;
        em2.power = 300.0;
        em2.totalReturned = 1_000_000.0;

        ShellySettingsStatus status = statusWithEMeters(em0, em1, em2);
        ShellyThingInterface handler = mockHandler(profile);

        ShellyComponents.updateMeters(handler, status);

        verify(handler, atLeastOnce()).updateChannel(eq("meter1"), eq(CHANNEL_EMETER_TOTALRET), any(State.class));
        verify(handler, atLeastOnce()).updateChannel(eq("meter2"), eq(CHANNEL_EMETER_TOTALRET), any(State.class));
        verify(handler, atLeastOnce()).updateChannel(eq("meter3"), eq(CHANNEL_EMETER_TOTALRET), any(State.class));
    }

    @Test
    void accumulatedReturnedIsSumOfPerPhaseReturned() {
        ShellyDeviceProfile profile = emeterProfile(true, 2);
        ShellySettingsEMeter em0 = new ShellySettingsEMeter();
        em0.isValid = true;
        em0.power = 100.0;
        em0.totalReturned = 2000.0;
        ShellySettingsEMeter em1 = new ShellySettingsEMeter();
        em1.isValid = true;
        em1.power = 200.0;
        em1.totalReturned = 3000.0;

        ShellySettingsStatus status = statusWithEMeters(em0, em1);
        ShellyThingInterface handler = mockHandler(profile);

        ShellyComponents.updateMeters(handler, status);

        ArgumentCaptor<org.openhab.core.types.State> captor = ArgumentCaptor
                .forClass(org.openhab.core.types.State.class);
        verify(handler, atLeastOnce()).updateChannel(eq(CHANNEL_GROUP_DEV_STATUS), eq(CHANNEL_DEVST_ACCURETURNED),
                captor.capture());
        List<org.openhab.core.types.State> values = captor.getAllValues();
        QuantityType<?> last = (QuantityType<?>) values.get(values.size() - 1);
        assertEquals(5.0, last.doubleValue(), 0.001);
    }

    @Test
    void returnedEnergyNullTotalReturnedChannelSkipped() {
        // When emeter.totalReturned is null (no emdata:0 received yet, e.g. before first HTTP poll),
        // the per-meter channel and device-level ACCURETURNED must NOT be updated.
        ShellyDeviceProfile profile = emeterProfile(false, 1);
        ShellySettingsEMeter em0 = new ShellySettingsEMeter();
        em0.isValid = true;
        em0.power = 100.0;
        // totalReturned intentionally left null

        ShellySettingsStatus status = statusWithEMeters(em0);
        ShellyThingInterface handler = mockHandler(profile);
        ShellyComponents.updateMeters(handler, status);

        verify(handler, never()).updateChannel(anyString(), eq(CHANNEL_EMETER_TOTALRET), any(State.class));
        verify(handler, never()).updateChannel(eq(CHANNEL_GROUP_DEV_STATUS), eq(CHANNEL_DEVST_ACCURETURNED),
                any(State.class));
    }

    @Test
    void accumulatedReturnedStatusTotalReturnedTakesPriority() {
        // When status.totalReturned is set (from emdata:0 total_act_ret on HTTP poll, stored as Wh),
        // CHANNEL_DEVST_ACCURETURNED must use that value (÷1000 = kWh) rather than the per-phase sum.
        ShellyDeviceProfile profile = emeterProfile(false, 3);
        ShellySettingsEMeter em0 = new ShellySettingsEMeter();
        em0.isValid = true;
        em0.power = 100.0;
        em0.totalReturned = 1000.0; // per-phase sum would be 6 kWh
        ShellySettingsEMeter em1 = new ShellySettingsEMeter();
        em1.isValid = true;
        em1.power = 200.0;
        em1.totalReturned = 2000.0;
        ShellySettingsEMeter em2 = new ShellySettingsEMeter();
        em2.isValid = true;
        em2.power = 300.0;
        em2.totalReturned = 3000.0;

        ShellySettingsStatus status = statusWithEMeters(em0, em1, em2);
        status.totalReturned = 7000.0; // device hardware counter: 7000 Wh = 7 kWh

        ShellyThingInterface handler = mockHandler(profile);
        ShellyComponents.updateMeters(handler, status);

        ArgumentCaptor<State> captor = ArgumentCaptor.forClass(State.class);
        verify(handler, atLeastOnce()).updateChannel(eq(CHANNEL_GROUP_DEV_STATUS), eq(CHANNEL_DEVST_ACCURETURNED),
                captor.capture());
        List<State> values = captor.getAllValues();
        QuantityType<?> last = (QuantityType<?>) values.get(values.size() - 1);
        assertEquals(7.0, last.doubleValue(), 0.001); // 7000 / 1000 = 7.0 kWh, not the 6.0 per-phase sum
    }

    @Test
    void apparentPowerNonNullRoutedPerMeterAndAccumulated() {
        ShellyDeviceProfile profile = emeterProfile(false, 3);
        ShellySettingsEMeter em0 = new ShellySettingsEMeter();
        em0.isValid = true;
        em0.power = 100.0;
        em0.apparentPower = 1000.0;
        ShellySettingsEMeter em1 = new ShellySettingsEMeter();
        em1.isValid = true;
        em1.power = 200.0;
        em1.apparentPower = 1000.0;
        ShellySettingsEMeter em2 = new ShellySettingsEMeter();
        em2.isValid = true;
        em2.power = 300.0;
        em2.apparentPower = 1000.0;

        ShellySettingsStatus status = statusWithEMeters(em0, em1, em2);
        ShellyThingInterface handler = mockHandler(profile);

        ShellyComponents.updateMeters(handler, status);

        verify(handler, atLeastOnce()).updateChannel(eq("meter1"), eq(CHANNEL_EMETER_APPARENT), any(State.class));
        verify(handler, atLeastOnce()).updateChannel(eq("meter2"), eq(CHANNEL_EMETER_APPARENT), any(State.class));
        verify(handler, atLeastOnce()).updateChannel(eq("meter3"), eq(CHANNEL_EMETER_APPARENT), any(State.class));

        ArgumentCaptor<org.openhab.core.types.State> captor = ArgumentCaptor
                .forClass(org.openhab.core.types.State.class);
        verify(handler, atLeastOnce()).updateChannel(eq(CHANNEL_GROUP_DEV_STATUS), eq(CHANNEL_DEVST_ACCUAPPARENT),
                captor.capture());
        List<org.openhab.core.types.State> values = captor.getAllValues();
        QuantityType<?> last = (QuantityType<?>) values.get(values.size() - 1);
        assertEquals(3000.0, last.doubleValue(), 0.1);
    }

    @Test
    void deviceAccuWattsNullUsesSumOfPerMeterPower() {
        ShellyDeviceProfile profile = emeterProfile(true, 2);
        ShellySettingsEMeter em0 = new ShellySettingsEMeter();
        em0.isValid = true;
        em0.power = 300.0;
        ShellySettingsEMeter em1 = new ShellySettingsEMeter();
        em1.isValid = true;
        em1.power = 700.0;

        ShellySettingsStatus status = statusWithEMeters(em0, em1);
        status.totalPower = null;

        ShellyThingInterface handler = mockHandler(profile);
        ShellyComponents.updateMeters(handler, status);

        ArgumentCaptor<org.openhab.core.types.State> captor = ArgumentCaptor
                .forClass(org.openhab.core.types.State.class);
        verify(handler, atLeastOnce()).updateChannel(eq(CHANNEL_GROUP_DEV_STATUS), eq(CHANNEL_DEVST_ACCUWATTS),
                captor.capture());
        List<org.openhab.core.types.State> values = captor.getAllValues();
        QuantityType<?> last = (QuantityType<?>) values.get(values.size() - 1);
        assertEquals(1000.0, last.doubleValue(), 0.1);
    }

    @Test
    void deviceAccuWattsStatusPresentUsesDeviceValue() {
        ShellyDeviceProfile profile = emeterProfile(true, 2);
        ShellySettingsEMeter em0 = new ShellySettingsEMeter();
        em0.isValid = true;
        em0.power = 300.0;
        ShellySettingsEMeter em1 = new ShellySettingsEMeter();
        em1.isValid = true;
        em1.power = 700.0;

        ShellySettingsStatus status = statusWithEMeters(em0, em1);
        status.totalPower = 1050.0; // device-reported total differs slightly from sum

        ShellyThingInterface handler = mockHandler(profile);
        ShellyComponents.updateMeters(handler, status);

        ArgumentCaptor<org.openhab.core.types.State> captor = ArgumentCaptor
                .forClass(org.openhab.core.types.State.class);
        verify(handler, atLeastOnce()).updateChannel(eq(CHANNEL_GROUP_DEV_STATUS), eq(CHANNEL_DEVST_ACCUWATTS),
                captor.capture());
        List<org.openhab.core.types.State> values = captor.getAllValues();
        QuantityType<?> last = (QuantityType<?>) values.get(values.size() - 1);
        assertEquals(1050.0, last.doubleValue(), 0.1);
    }

    @Test
    void pro3emThirdPhaseInvalidSkipsValueUpdateButLoopContinues() {
        // Regression guard: channel creation was once inside the isValid block; when phase C
        // reported isValid=false on first poll, meter3 channels were never created and stayed
        // missing permanently. The fix moved creation outside isValid. This test verifies
        // the value-update side: phases A/B are updated, phase C is skipped (no value to post),
        // and the loop counter still advances past all three so accuWatts = A+B only, not stuck.
        ShellyDeviceProfile profile = pro3emProfile();

        ShellySettingsEMeter em0 = new ShellySettingsEMeter();
        em0.isValid = true;
        em0.power = 1000.0;
        em0.total = 10000.0;

        ShellySettingsEMeter em1 = new ShellySettingsEMeter();
        em1.isValid = true;
        em1.power = 2000.0;
        em1.total = 20000.0;

        ShellySettingsEMeter em2 = new ShellySettingsEMeter();
        em2.isValid = false; // phase C: no data — all fields null

        ShellySettingsStatus status = statusWithEMeters(em0, em1, em2);
        ShellyThingInterface handler = mockHandler(profile);
        ShellyComponents.updateMeters(handler, status);

        verify(handler, atLeastOnce()).updateChannel(eq("meter1"), eq(CHANNEL_METER_CURRENTWATTS), any(State.class));
        verify(handler, atLeastOnce()).updateChannel(eq("meter2"), eq(CHANNEL_METER_CURRENTWATTS), any(State.class));
        verify(handler, never()).updateChannel(eq("meter3"), eq(CHANNEL_METER_CURRENTWATTS), any(State.class));

        ArgumentCaptor<org.openhab.core.types.State> captor = ArgumentCaptor
                .forClass(org.openhab.core.types.State.class);
        verify(handler, atLeastOnce()).updateChannel(eq(CHANNEL_GROUP_DEV_STATUS), eq(CHANNEL_DEVST_ACCUWATTS),
                captor.capture());
        QuantityType<?> accuWatts = (QuantityType<?>) captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals(3000.0, accuWatts.doubleValue(), 0.1); // A+B only, C had null power
    }

    @Test
    void relayPm2meterAccuReturnedNeverUpdated() {
        // Relay-PM devices (2PM, Plus 1PM) have emeters but updateRelayStatus never sets
        // totalReturned. Verify ACCURETURNED is never written (value guard), which is the
        // value-path complement to the channel-creation fix (is3EM||isEM50 gate).
        ShellyDeviceProfile profile = relayPm2meterProfile();

        ShellySettingsEMeter em0 = new ShellySettingsEMeter();
        em0.isValid = true;
        em0.power = 300.0;
        // totalReturned intentionally null — relay-PM never populates this

        ShellySettingsEMeter em1 = new ShellySettingsEMeter();
        em1.isValid = true;
        em1.power = 700.0;

        ShellySettingsStatus status = statusWithEMeters(em0, em1);
        ShellyThingInterface handler = mockHandler(profile);
        ShellyComponents.updateMeters(handler, status);

        verify(handler, never()).updateChannel(eq(CHANNEL_GROUP_DEV_STATUS), eq(CHANNEL_DEVST_ACCURETURNED),
                any(State.class));
    }

    @Test
    void relayPm2meterAccuApparentNeverUpdated() {
        ShellyDeviceProfile profile = relayPm2meterProfile();

        ShellySettingsEMeter em0 = new ShellySettingsEMeter();
        em0.isValid = true;
        em0.power = 300.0;
        // apparentPower intentionally null — relay-PM never populates this

        ShellySettingsEMeter em1 = new ShellySettingsEMeter();
        em1.isValid = true;
        em1.power = 700.0;

        ShellySettingsStatus status = statusWithEMeters(em0, em1);
        ShellyThingInterface handler = mockHandler(profile);
        ShellyComponents.updateMeters(handler, status);

        verify(handler, never()).updateChannel(eq(CHANNEL_GROUP_DEV_STATUS), eq(CHANNEL_DEVST_ACCUAPPARENT),
                any(State.class));
    }

    private static ShellyDeviceProfile pro3emProfile() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPRO3EM);
        profile.isEMeter = true;
        profile.isGen2 = true;
        profile.numMeters = 3;
        return profile;
    }

    private static ShellyDeviceProfile relayPm2meterProfile() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS2PM_RELAY);
        profile.isEMeter = true;
        profile.isGen2 = true;
        profile.hasRelays = true;
        profile.numRelays = 2;
        profile.numMeters = 2;
        return profile;
    }
}
