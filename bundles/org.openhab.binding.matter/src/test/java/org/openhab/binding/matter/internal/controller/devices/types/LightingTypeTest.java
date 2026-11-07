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
package org.openhab.binding.matter.internal.controller.devices.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.matter.internal.MatterChannelTypeProvider;
import org.openhab.binding.matter.internal.MatterConfigDescriptionProvider;
import org.openhab.binding.matter.internal.MatterStateDescriptionOptionProvider;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BaseCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ColorControlCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.DeviceTypes;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.LevelControlCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.OnOffCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;
import org.openhab.binding.matter.internal.controller.devices.converter.TestMatterBaseThingHandler;
import org.openhab.binding.matter.internal.util.TranslationService;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;

/**
 * Tests for {@link LightingType}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class LightingTypeTest {
    private static final ChannelGroupUID GROUP = new ChannelGroupUID("matter:node:test:12345:1");
    private static final ChannelUID COLOR_CHANNEL = new ChannelUID(GROUP, "colorcontrol-color");

    @Mock
    @NonNullByDefault({})
    private BaseThingHandlerFactory thingHandlerFactory;
    @Mock
    @NonNullByDefault({})
    private MatterStateDescriptionOptionProvider stateDescriptionProvider;
    @Mock
    @NonNullByDefault({})
    private MatterChannelTypeProvider channelTypeProvider;
    @Mock
    @NonNullByDefault({})
    private MatterConfigDescriptionProvider configDescriptionProvider;
    @Mock
    @NonNullByDefault({})
    private TranslationService translationService;
    @NonNullByDefault({})
    private TestMatterBaseThingHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = Mockito.spy(new TestMatterBaseThingHandler(new MatterBridgeClient(), thingHandlerFactory,
                stateDescriptionProvider, channelTypeProvider, configDescriptionProvider, translationService));
    }

    @Test
    void testColorLightUsesADefaultReportedByTheDevice() {
        LightingType type = lightingType(DeviceTypes.EXTENDED_COLOR_LIGHT);
        assertFalse(type.getClusterConverters().containsKey(LevelControlCluster.CLUSTER_ID));

        type.onEvent(onOffTransitionTime(12));
        type.handleCommand(COLOR_CHANNEL, new HSBType("120,100,100"));
        assertEquals(12, lastCommand(ColorControlCluster.CLUSTER_NAME).args.get("transitionTime"));
    }

    @Test
    void testLightWithBothConvertersSharesOneTransition() {
        LightingType type = lightingType(DeviceTypes.DIMMABLE_LIGHT);
        assertTrue(type.getClusterConverters().containsKey(LevelControlCluster.CLUSTER_ID));
        assertTrue(type.getClusterConverters().containsKey(ColorControlCluster.CLUSTER_ID));

        type.onEvent(onOffTransitionTime(12));
        type.handleCommand(COLOR_CHANNEL, new HSBType("120,100,100"));
        assertEquals(12, lastCommand(ColorControlCluster.CLUSTER_NAME).args.get("transitionTime"));
    }

    private LightingType lightingType(int deviceType) {
        OnOffCluster onOff = new OnOffCluster(BigInteger.ONE, 1);
        onOff.featureMap = new OnOffCluster.FeatureMap(true, false, false);
        onOff.onOff = true;
        LevelControlCluster level = new LevelControlCluster(BigInteger.ONE, 1);
        level.featureMap = new LevelControlCluster.FeatureMap(true, true, false);
        level.currentLevel = 254;
        ColorControlCluster color = new ColorControlCluster(BigInteger.ONE, 1);
        color.featureMap = new ColorControlCluster.FeatureMap(true, false, false, false, true);
        color.colorTempPhysicalMinMireds = 153;
        color.colorTempPhysicalMaxMireds = 500;

        Map<String, BaseCluster> clusters = new LinkedHashMap<>();
        clusters.put(OnOffCluster.CLUSTER_NAME, onOff);
        clusters.put(LevelControlCluster.CLUSTER_NAME, level);
        clusters.put(ColorControlCluster.CLUSTER_NAME, color);

        LightingType type = new LightingType(deviceType, handler, 1);
        type.createChannels(1, clusters, GROUP);
        type.initState();
        return type;
    }

    private AttributeChangedMessage onOffTransitionTime(int tenths) {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.clusterId = LevelControlCluster.CLUSTER_ID;
        message.path.attributeName = LevelControlCluster.ATTRIBUTE_ON_OFF_TRANSITION_TIME;
        message.value = tenths;
        return message;
    }

    private ClusterCommand lastCommand(String clusterName) {
        ArgumentCaptor<ClusterCommand> captor = ArgumentCaptor.forClass(ClusterCommand.class);
        verify(handler, atLeastOnce()).sendClusterCommand(eq(1), eq(clusterName), captor.capture());
        return captor.getValue();
    }
}
