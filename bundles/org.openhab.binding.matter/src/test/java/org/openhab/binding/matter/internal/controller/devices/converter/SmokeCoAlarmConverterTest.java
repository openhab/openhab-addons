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
package org.openhab.binding.matter.internal.controller.devices.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.SmokeCoAlarmCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.SmokeCoAlarmCluster.AlarmStateEnum;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.SmokeCoAlarmCluster.ContaminationStateEnum;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.SmokeCoAlarmCluster.EndOfServiceEnum;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.SmokeCoAlarmCluster.ExpressedStateEnum;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.SmokeCoAlarmCluster.MuteStateEnum;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.SmokeCoAlarmCluster.SensitivityEnum;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.types.StateDescription;

/**
 * Test class for {@link SmokeCoAlarmConverter}
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class SmokeCoAlarmConverterTest extends BaseMatterConverterTest {

    @Mock
    @NonNullByDefault({})
    private SmokeCoAlarmCluster mockCluster;
    @NonNullByDefault({})
    private SmokeCoAlarmConverter converter;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        mockCluster.expressedState = ExpressedStateEnum.NORMAL;
        mockCluster.smokeState = AlarmStateEnum.NORMAL;
        mockCluster.coState = AlarmStateEnum.NORMAL;
        mockCluster.batteryAlert = AlarmStateEnum.NORMAL;
        mockCluster.deviceMuted = MuteStateEnum.NOT_MUTED;
        mockCluster.testInProgress = false;
        mockCluster.hardwareFaultAlert = false;
        mockCluster.endOfServiceAlert = EndOfServiceEnum.NORMAL;
        mockCluster.interconnectSmokeAlarm = AlarmStateEnum.NORMAL;
        mockCluster.interconnectCoAlarm = AlarmStateEnum.NORMAL;
        mockCluster.contaminationState = ContaminationStateEnum.NORMAL;
        mockCluster.smokeSensitivityLevel = SensitivityEnum.STANDARD;
        mockCluster.expiryDate = 1735689600; // 2025-01-01
        mockCluster.featureMap = new SmokeCoAlarmCluster.FeatureMap(true, true);
        converter = new SmokeCoAlarmConverter(mockCluster, mockHandler, 1, "TestLabel");
    }

    @Test
    void testCreateChannelsWithAllFeatures() {
        ChannelGroupUID channelGroupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(channelGroupUID);

        // Should have all channels when both smoke and CO features are enabled and all optional attributes are present
        // expressedState, smokeState, coState, batteryAlert, deviceMuted, testInProgress, hardwareFault,
        // endOfService, interconnectSmoke, interconnectCo, contaminationState, smokeSensitivity, expiryDate
        assertEquals(13, channels.size());

        // Verify channel UIDs exist
        assertTrue(channels.keySet().stream()
                .anyMatch(c -> c.getUID().toString().contains("smokecoalarm-expressedstate")));
        assertTrue(channels.keySet().stream().anyMatch(c -> c.getUID().toString().contains("smokecoalarm-smokestate")));
        assertTrue(channels.keySet().stream().anyMatch(c -> c.getUID().toString().contains("smokecoalarm-costate")));
        assertTrue(
                channels.keySet().stream().anyMatch(c -> c.getUID().toString().contains("smokecoalarm-batteryalert")));
        assertTrue(
                channels.keySet().stream().anyMatch(c -> c.getUID().toString().contains("smokecoalarm-devicemuted")));
        assertTrue(channels.keySet().stream()
                .anyMatch(c -> c.getUID().toString().contains("smokecoalarm-testinprogress")));
        assertTrue(
                channels.keySet().stream().anyMatch(c -> c.getUID().toString().contains("smokecoalarm-hardwarefault")));
        assertTrue(
                channels.keySet().stream().anyMatch(c -> c.getUID().toString().contains("smokecoalarm-endofservice")));
        assertTrue(channels.keySet().stream()
                .anyMatch(c -> c.getUID().toString().contains("smokecoalarm-interconnectsmoke")));
        assertTrue(channels.keySet().stream()
                .anyMatch(c -> c.getUID().toString().contains("smokecoalarm-interconnectco")));
        assertTrue(channels.keySet().stream()
                .anyMatch(c -> c.getUID().toString().contains("smokecoalarm-contaminationstate")));
        assertTrue(channels.keySet().stream()
                .anyMatch(c -> c.getUID().toString().contains("smokecoalarm-smokesensitivity")));
        assertTrue(channels.keySet().stream().anyMatch(c -> c.getUID().toString().contains("smokecoalarm-expirydate")));
    }

    @Test
    void testCreateChannelsSmokeOnlyFeature() {
        mockCluster.featureMap = new SmokeCoAlarmCluster.FeatureMap(true, false);
        mockCluster.interconnectCoAlarm = null; // Not available without CO feature
        converter = new SmokeCoAlarmConverter(mockCluster, mockHandler, 1, "TestLabel");

        ChannelGroupUID channelGroupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(channelGroupUID);

        // CO-related channels should not be present
        assertTrue(channels.keySet().stream().anyMatch(c -> c.getUID().toString().contains("smokecoalarm-smokestate")));
        assertTrue(channels.keySet().stream().noneMatch(c -> c.getUID().toString().contains("smokecoalarm-costate")));
        assertTrue(channels.keySet().stream()
                .noneMatch(c -> c.getUID().toString().contains("smokecoalarm-interconnectco")));
    }

    @Test
    void testOnEventWithExpressedState() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = SmokeCoAlarmCluster.ATTRIBUTE_EXPRESSED_STATE;
        message.value = ExpressedStateEnum.SMOKE_ALARM;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("smokecoalarm-expressedstate"), eq(new DecimalType(1)));
    }

    @Test
    void testOnEventWithSmokeState() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = SmokeCoAlarmCluster.ATTRIBUTE_SMOKE_STATE;
        message.value = AlarmStateEnum.CRITICAL;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("smokecoalarm-smokestate"), eq(new DecimalType(2)));
    }

    @Test
    void testOnEventWithCoState() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = SmokeCoAlarmCluster.ATTRIBUTE_CO_STATE;
        message.value = AlarmStateEnum.WARNING;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("smokecoalarm-costate"), eq(new DecimalType(1)));
    }

    @Test
    void testOnEventWithBatteryAlert() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = SmokeCoAlarmCluster.ATTRIBUTE_BATTERY_ALERT;
        message.value = AlarmStateEnum.WARNING;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("smokecoalarm-batteryalert"), eq(new DecimalType(1)));
    }

    @Test
    void testOnEventWithDeviceMuted() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = SmokeCoAlarmCluster.ATTRIBUTE_DEVICE_MUTED;
        message.value = MuteStateEnum.MUTED;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("smokecoalarm-devicemuted"), eq(new DecimalType(1)));
    }

    @Test
    void testOnEventWithTestInProgress() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = SmokeCoAlarmCluster.ATTRIBUTE_TEST_IN_PROGRESS;
        message.value = true;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("smokecoalarm-testinprogress"), eq(OnOffType.ON));
    }

    @Test
    void testOnEventWithHardwareFault() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = SmokeCoAlarmCluster.ATTRIBUTE_HARDWARE_FAULT_ALERT;
        message.value = true;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("smokecoalarm-hardwarefault"), eq(OnOffType.ON));
    }

    @Test
    void testOnEventWithEndOfService() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = SmokeCoAlarmCluster.ATTRIBUTE_END_OF_SERVICE_ALERT;
        message.value = EndOfServiceEnum.EXPIRED;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("smokecoalarm-endofservice"), eq(new DecimalType(1)));
    }

    @Test
    void testOnEventWithInterconnectSmoke() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = SmokeCoAlarmCluster.ATTRIBUTE_INTERCONNECT_SMOKE_ALARM;
        message.value = AlarmStateEnum.CRITICAL;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("smokecoalarm-interconnectsmoke"), eq(new DecimalType(2)));
    }

    @Test
    void testOnEventWithInterconnectCo() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = SmokeCoAlarmCluster.ATTRIBUTE_INTERCONNECT_CO_ALARM;
        message.value = AlarmStateEnum.WARNING;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("smokecoalarm-interconnectco"), eq(new DecimalType(1)));
    }

    @Test
    void testOnEventWithContaminationState() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = SmokeCoAlarmCluster.ATTRIBUTE_CONTAMINATION_STATE;
        message.value = ContaminationStateEnum.WARNING;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("smokecoalarm-contaminationstate"), eq(new DecimalType(2)));
    }

    @Test
    void testOnEventWithSmokeSensitivity() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = SmokeCoAlarmCluster.ATTRIBUTE_SMOKE_SENSITIVITY_LEVEL;
        message.value = SensitivityEnum.HIGH;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("smokecoalarm-smokesensitivity"), eq(new DecimalType(0)));
    }

    @Test
    void testInitState() {
        mockCluster.expressedState = ExpressedStateEnum.CO_ALARM;
        mockCluster.smokeState = AlarmStateEnum.NORMAL;
        mockCluster.coState = AlarmStateEnum.CRITICAL;
        mockCluster.batteryAlert = AlarmStateEnum.NORMAL;
        mockCluster.deviceMuted = MuteStateEnum.NOT_MUTED;
        mockCluster.testInProgress = false;
        mockCluster.hardwareFaultAlert = false;
        mockCluster.endOfServiceAlert = EndOfServiceEnum.NORMAL;
        mockCluster.interconnectSmokeAlarm = AlarmStateEnum.NORMAL;
        mockCluster.interconnectCoAlarm = AlarmStateEnum.NORMAL;
        mockCluster.contaminationState = ContaminationStateEnum.NORMAL;
        mockCluster.smokeSensitivityLevel = SensitivityEnum.STANDARD;

        converter.initState();

        verify(mockHandler, times(1)).updateState(eq(1), eq("smokecoalarm-expressedstate"), eq(new DecimalType(2)));
        verify(mockHandler, times(1)).updateState(eq(1), eq("smokecoalarm-smokestate"), eq(new DecimalType(0)));
        verify(mockHandler, times(1)).updateState(eq(1), eq("smokecoalarm-costate"), eq(new DecimalType(2)));
        verify(mockHandler, times(1)).updateState(eq(1), eq("smokecoalarm-batteryalert"), eq(new DecimalType(0)));
        verify(mockHandler, times(1)).updateState(eq(1), eq("smokecoalarm-devicemuted"), eq(new DecimalType(0)));
        verify(mockHandler, times(1)).updateState(eq(1), eq("smokecoalarm-testinprogress"), eq(OnOffType.OFF));
        verify(mockHandler, times(1)).updateState(eq(1), eq("smokecoalarm-hardwarefault"), eq(OnOffType.OFF));
        verify(mockHandler, times(1)).updateState(eq(1), eq("smokecoalarm-endofservice"), eq(new DecimalType(0)));
        verify(mockHandler, times(1)).updateState(eq(1), eq("smokecoalarm-interconnectsmoke"), eq(new DecimalType(0)));
        verify(mockHandler, times(1)).updateState(eq(1), eq("smokecoalarm-interconnectco"), eq(new DecimalType(0)));
        verify(mockHandler, times(1)).updateState(eq(1), eq("smokecoalarm-contaminationstate"), eq(new DecimalType(0)));
        verify(mockHandler, times(1)).updateState(eq(1), eq("smokecoalarm-smokesensitivity"), eq(new DecimalType(1)));
    }
}
