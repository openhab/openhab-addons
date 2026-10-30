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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.LevelControlCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.OnOffCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.StateDescription;

import com.google.gson.JsonNull;

/**
 * Test class for LevelControlConverter
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class LevelControlConverterTest extends BaseMatterConverterTest {

    @Mock
    @NonNullByDefault({})
    private LevelControlCluster mockCluster;
    @NonNullByDefault({})
    private LevelControlConverter converter;

    private static final ChannelUID LEVEL_CHANNEL = new ChannelUID("matter:node:test:12345:1#levelcontrol-level");

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        mockCluster.featureMap = new LevelControlCluster.FeatureMap(true, true, false);
        converter = new LevelControlConverter(mockCluster, mockHandler, 1, "TestLabel");
    }

    private LevelControlConverter nonLightingConverter() {
        mockCluster.featureMap = new LevelControlCluster.FeatureMap(true, false, false);
        return new LevelControlConverter(mockCluster, mockHandler, 1, "TestLabel");
    }

    private void sendLevel(LevelControlConverter converter, int level) {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = LevelControlCluster.ATTRIBUTE_CURRENT_LEVEL;
        message.value = level;
        converter.onEvent(message);
    }

    @Test
    void testCreateChannels() {
        ChannelGroupUID channelGroupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(channelGroupUID);
        assertEquals(1, channels.size());
        Channel channel = channels.keySet().iterator().next();
        assertEquals("matter:node:test:12345:1#levelcontrol-level", channel.getUID().toString());
        assertEquals("Dimmer", channel.getAcceptedItemType());
    }

    @Test
    void testOnEventWithLevel() {
        AttributeChangedMessage levelMessage = new AttributeChangedMessage();
        levelMessage.path = new Path();
        levelMessage.path.attributeName = LevelControlCluster.ATTRIBUTE_CURRENT_LEVEL;
        levelMessage.value = 254;

        // Set lastOnOff to ON to ensure level update is processed
        AttributeChangedMessage onOffMessage = new AttributeChangedMessage();
        onOffMessage.path = new Path();
        onOffMessage.path.attributeName = OnOffCluster.ATTRIBUTE_ON_OFF;
        onOffMessage.value = Boolean.TRUE;
        converter.onEvent(onOffMessage);

        converter.onEvent(levelMessage);
        verify(mockHandler, times(1)).updateState(eq(1), eq("levelcontrol-level"), eq(new PercentType(100)));
    }

    @Test
    void testOffEventWithOnOff() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = OnOffCluster.ATTRIBUTE_ON_OFF;
        message.value = Boolean.FALSE;

        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("levelcontrol-level"), eq(OnOffType.OFF));
    }

    @Test
    void testInitState() {
        mockCluster.currentLevel = 254;
        converter.initState();
        verify(mockHandler, times(1)).updateState(eq(1), eq("levelcontrol-level"), eq(new PercentType(100)));
    }

    @Test
    void testInitStateOff() {
        mockCluster.currentLevel = 254;
        converter.initState(false);
        verify(mockHandler, times(1)).updateState(eq(1), eq("levelcontrol-level"), eq(OnOffType.OFF));
    }

    @Test
    void testLitLightAtItsDimmestIsNotReportedAsOff() {
        mockCluster.currentLevel = 254;
        converter.initState(true);
        sendLevel(converter, 1);
        verify(mockHandler, times(1)).updateState(eq(1), eq("levelcontrol-level"), eq(new PercentType(1)));
    }

    @Test
    void testZeroPercentTurnsOffALight() {
        converter.handleCommand(new ChannelUID("matter:node:test:12345:1#levelcontrol-level"), PercentType.ZERO);
        verify(mockHandler, times(1)).sendClusterCommand(eq(1), eq(OnOffCluster.CLUSTER_NAME), eq(OnOffCluster.off()));
    }

    @Test
    void testZeroPercentMovesANonLightingDeviceToLevelZero() {
        // Without the Lighting feature level 0 is valid, and is how the device is turned off
        LevelControlConverter converter = nonLightingConverter();
        converter.handleCommand(new ChannelUID("matter:node:test:12345:1#levelcontrol-level"), PercentType.ZERO);
        ClusterCommand command = capturedLevelCommand();
        assertEquals(0, command.args.get("level"));
        assertEquals(JsonNull.INSTANCE, command.args.get("transitionTime"));
    }

    @Test
    void testLevelCommandWithoutChannelTransitionLetsTheDeviceChooseTheTransition() {
        converter.handleCommand(LEVEL_CHANNEL, new PercentType(50));
        ClusterCommand command = capturedLevelCommand();
        assertTrue(command.args.containsKey("transitionTime"));
        assertEquals(JsonNull.INSTANCE, command.args.get("transitionTime"));
    }

    @Test
    void testLevelCommandUsesTheChannelTransitionTime() {
        setChannelTransitionTime(LEVEL_CHANNEL, "1.5");
        converter.handleCommand(LEVEL_CHANNEL, new PercentType(50));
        assertEquals(15, capturedLevelCommand().args.get("transitionTime"));
    }

    @Test
    void testChannelTransitionTimeIsRoundedToTenths() {
        setChannelTransitionTime(LEVEL_CHANNEL, "0.15");
        converter.handleCommand(LEVEL_CHANNEL, new PercentType(50));
        assertEquals(2, capturedLevelCommand().args.get("transitionTime"));
    }

    @Test
    void testZeroChannelTransitionTimeIsSent() {
        setChannelTransitionTime(LEVEL_CHANNEL, "0");
        converter.handleCommand(LEVEL_CHANNEL, new PercentType(50));
        assertEquals(0, capturedLevelCommand().args.get("transitionTime"));
    }

    @Test
    void testChannelTransitionTimeAtTheLimitIsSent() {
        setChannelTransitionTime(LEVEL_CHANNEL, "6553.4");
        converter.handleCommand(LEVEL_CHANNEL, new PercentType(50));
        assertEquals(65534, capturedLevelCommand().args.get("transitionTime"));
    }

    @Test
    void testOutOfRangeChannelTransitionTimeIsIgnored() {
        setChannelTransitionTime(LEVEL_CHANNEL, "-1");
        converter.handleCommand(LEVEL_CHANNEL, new PercentType(50));
        assertEquals(JsonNull.INSTANCE, capturedLevelCommand().args.get("transitionTime"));
    }

    @Test
    void testChannelTransitionTimeAboveTheLimitIsIgnored() {
        setChannelTransitionTime(LEVEL_CHANNEL, "6553.5");
        converter.handleCommand(LEVEL_CHANNEL, new PercentType(50));
        assertEquals(JsonNull.INSTANCE, capturedLevelCommand().args.get("transitionTime"));
    }

    @Test
    void testNonLightingOnUsesTheChannelTransitionTimeAndFullLevel() {
        LevelControlConverter converter = nonLightingConverter();
        setChannelTransitionTime(LEVEL_CHANNEL, "2");
        converter.handleCommand(LEVEL_CHANNEL, OnOffType.ON);
        ClusterCommand command = capturedLevelCommand();
        assertEquals(254, command.args.get("level"));
        assertEquals(20, command.args.get("transitionTime"));
    }

    @Test
    void testNonLightingOnFallsBackToTheOnTransitionTime() {
        mockCluster.onTransitionTime = 12;
        LevelControlConverter converter = nonLightingConverter();
        converter.handleCommand(LEVEL_CHANNEL, OnOffType.ON);
        assertEquals(12, capturedLevelCommand().args.get("transitionTime"));
    }

    @Test
    void testNonLightingOffWithoutOffTransitionTimeLetsTheDeviceChoose() {
        mockCluster.onTransitionTime = 12;
        LevelControlConverter converter = nonLightingConverter();
        converter.handleCommand(LEVEL_CHANNEL, OnOffType.OFF);
        ClusterCommand command = capturedLevelCommand();
        assertEquals(0, command.args.get("level"));
        assertEquals(JsonNull.INSTANCE, command.args.get("transitionTime"));
    }

    @Test
    void testNonLightingOffUsesAnOffTransitionTimeChangedOnTheDevice() {
        LevelControlConverter converter = nonLightingConverter();
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = LevelControlCluster.ATTRIBUTE_OFF_TRANSITION_TIME;
        message.value = 9;
        converter.onEvent(message);
        converter.handleCommand(LEVEL_CHANNEL, OnOffType.OFF);
        assertEquals(9, capturedLevelCommand().args.get("transitionTime"));
    }

    @Test
    void testDeviceDefaultIsCopiedToTheConfiguration() {
        mockCluster.onOffTransitionTime = 10;
        mockCluster.currentLevel = 254;
        converter.initState();
        verify(mockHandler, times(1))
                .updateConfiguration(eq(Map.of("defaultTransitionTime-1", BigDecimal.valueOf(10, 1))));
    }

    @Test
    void testUnchangedDeviceDefaultIsNotSavedAgain() {
        mockCluster.onOffTransitionTime = 10;
        mockCluster.currentLevel = 254;
        mockHandler.getThing().getConfiguration().put("defaultTransitionTime-1", BigDecimal.valueOf(10, 1));
        converter.initState();
        verify(mockHandler, never()).updateConfiguration(anyMap());
    }

    @Test
    void testDeviceDefaultIsOnlyOfferedWhenTheDeviceSupportsIt() {
        mockCluster.currentLevel = 254;
        converter.initState();
        verify(mockConfigDescriptionProvider, never()).addConfigDescription(any());

        mockCluster.onOffTransitionTime = 10;
        converter.initState();
        assertEquals(List.of("defaultTransitionTime-1"), configParameterNames());
    }

    @Test
    void testInvalidDeviceDefaultIsNotWrittenAndIsRestored() {
        mockCluster.onOffTransitionTime = 10;
        mockCluster.currentLevel = 254;
        converter.initState();
        converter.handleConfigurationUpdate(new Configuration(Map.of("defaultTransitionTime-1", new BigDecimal("-1"))));
        verify(mockHandler, never()).writeAttribute(anyInt(), anyString(), anyString(), anyString());
        verify(mockHandler, times(2))
                .updateConfiguration(eq(Map.of("defaultTransitionTime-1", BigDecimal.valueOf(10, 1))));
    }

    @Test
    void testDeviceDefaultAtTheAttributeLimitIsWritten() {
        doReturn(CompletableFuture.completedFuture(null)).when(mockHandler).writeAttribute(anyInt(), anyString(),
                anyString(), anyString());
        mockCluster.onOffTransitionTime = 10;
        mockCluster.currentLevel = 254;
        converter.initState();
        converter.handleConfigurationUpdate(
                new Configuration(Map.of("defaultTransitionTime-1", new BigDecimal("6553.5"))));
        verify(mockHandler, times(1)).writeAttribute(eq(1), eq(LevelControlCluster.CLUSTER_NAME),
                eq(LevelControlCluster.ATTRIBUTE_ON_OFF_TRANSITION_TIME), eq("65535"));
    }

    @Test
    void testChangedDeviceDefaultIsWrittenToTheDevice() {
        doReturn(CompletableFuture.completedFuture(null)).when(mockHandler).writeAttribute(anyInt(), anyString(),
                anyString(), anyString());
        mockCluster.onOffTransitionTime = 10;
        mockCluster.currentLevel = 254;
        converter.initState();
        converter
                .handleConfigurationUpdate(new Configuration(Map.of("defaultTransitionTime-1", new BigDecimal("2.5"))));
        verify(mockHandler, times(1)).writeAttribute(eq(1), eq(LevelControlCluster.CLUSTER_NAME),
                eq(LevelControlCluster.ATTRIBUTE_ON_OFF_TRANSITION_TIME), eq("25"));
    }

    @Test
    void testUnchangedDeviceDefaultIsNotWritten() {
        mockCluster.onOffTransitionTime = 10;
        mockCluster.currentLevel = 254;
        converter.initState();
        converter
                .handleConfigurationUpdate(new Configuration(Map.of("defaultTransitionTime-1", new BigDecimal("1.0"))));
        verify(mockHandler, never()).writeAttribute(anyInt(), anyString(), anyString(), anyString());
    }

    @Test
    void testFailedDeviceDefaultWriteRestoresTheConfiguration() {
        doReturn(CompletableFuture.failedFuture(new IllegalStateException("offline"))).when(mockHandler)
                .writeAttribute(anyInt(), anyString(), anyString(), anyString());
        mockCluster.onOffTransitionTime = 10;
        mockCluster.currentLevel = 254;
        converter.initState();
        converter
                .handleConfigurationUpdate(new Configuration(Map.of("defaultTransitionTime-1", new BigDecimal("2.5"))));
        verify(mockHandler, times(2))
                .updateConfiguration(eq(Map.of("defaultTransitionTime-1", BigDecimal.valueOf(10, 1))));
    }

    @Test
    void testDeviceDefaultChangedElsewhereUpdatesTheConfiguration() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = LevelControlCluster.ATTRIBUTE_ON_OFF_TRANSITION_TIME;
        message.value = 7;
        converter.onEvent(message);
        verify(mockHandler, times(1))
                .updateConfiguration(eq(Map.of("defaultTransitionTime-1", BigDecimal.valueOf(7, 1))));
    }

    @Test
    void testDeviceDefaultIsNotWrittenWhenTheDeviceDoesNotSupportIt() {
        mockCluster.currentLevel = 254;
        converter.initState();
        converter
                .handleConfigurationUpdate(new Configuration(Map.of("defaultTransitionTime-1", new BigDecimal("2.5"))));
        verify(mockHandler, never()).writeAttribute(anyInt(), anyString(), anyString(), anyString());
        verify(mockHandler, never()).updateConfiguration(anyMap());
    }

    private ClusterCommand capturedLevelCommand() {
        ArgumentCaptor<ClusterCommand> captor = ArgumentCaptor.forClass(ClusterCommand.class);
        verify(mockHandler, atLeastOnce()).sendClusterCommand(eq(1), eq(LevelControlCluster.CLUSTER_NAME),
                captor.capture());
        return captor.getValue();
    }

    private List<String> configParameterNames() {
        ArgumentCaptor<ConfigDescription> captor = ArgumentCaptor.forClass(ConfigDescription.class);
        verify(mockConfigDescriptionProvider, atLeastOnce()).addConfigDescription(captor.capture());
        return captor.getValue().getParameters().stream().map(ConfigDescriptionParameter::getName).toList();
    }

    @Test
    void testNonLightingLevelZeroIsReportedAsZeroPercent() {
        LevelControlConverter converter = nonLightingConverter();
        mockCluster.currentLevel = 254;
        converter.initState(true);
        sendLevel(converter, 0);
        verify(mockHandler, times(1)).updateState(eq(1), eq("levelcontrol-level"), eq(PercentType.ZERO));
    }
}
