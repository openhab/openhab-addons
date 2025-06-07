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
package org.openhab.binding.zwavejs.internal.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openhab.binding.zwavejs.internal.BindingConstants.BINDING_ID;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.zwavejs.internal.BindingConstants;
import org.openhab.binding.zwavejs.internal.DataUtil;
import org.openhab.binding.zwavejs.internal.api.dto.Node;
import org.openhab.binding.zwavejs.internal.api.dto.messages.ResultMessage;
import org.openhab.binding.zwavejs.internal.handler.mock.ZwaveJSChannelTypeInMemmoryProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.types.StateDescription;

/**
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class ZwaveJSTypeGeneratorTest {

    @Nullable
    ZwaveJSTypeGenerator provider;
    ZwaveJSChannelTypeProvider channelTypeProvider = new ZwaveJSChannelTypeInMemmoryProvider();
    ZwaveJSConfigDescriptionProvider configDescriptionProvider = new ZwaveJSConfigDescriptionProviderImpl();

    @BeforeEach
    public void setup() {
        ThingRegistry thingRegistry = mock(ThingRegistry.class);
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID(BindingConstants.BINDING_ID, "test-thing"));
        when(thing.getBridgeUID()).thenReturn(new ThingUID(BindingConstants.BINDING_ID, "test-bridge"));
        when(thingRegistry.get(any())).thenReturn(thing);
        provider = new ZwaveJSTypeGeneratorImpl(channelTypeProvider, configDescriptionProvider, thingRegistry);
    }

    private Channel getChannel(String store, int nodeId, String channelId) throws IOException {
        return getChannel(store, nodeId, channelId, false);
    }

    private Channel getChannel(String store, int nodeId, String channelId, boolean configurationAsChannels)
            throws IOException {
        Node node = DataUtil.getNodeFromStore(store, nodeId);
        ZwaveJSTypeGeneratorResult results = Objects.requireNonNull(provider).generate(
                new ThingUID(BINDING_ID, "test-bridge", "test-thing"), Objects.requireNonNull(node),
                configurationAsChannels);
        return Objects.requireNonNull(results.channels.get(channelId));
    }

    @Test
    public void testGenerateChannelTypeForNode3() throws IOException {
        Node node = DataUtil.getNodeFromStore("store_1.json", 3);

        ZwaveJSTypeGeneratorResult results = Objects.requireNonNull(provider)
                .generate(new ThingUID(BINDING_ID, "test-thing"), Objects.requireNonNull(node), false);

        assertEquals(7, results.channels.values().stream().map(f -> f.getChannelTypeUID()).distinct().count());
    }

    @Test
    public void testGenerateChannelTypeForNode3AsChannels() throws IOException {
        Node node = DataUtil.getNodeFromStore("store_1.json", 3);

        ZwaveJSTypeGeneratorResult results = Objects.requireNonNull(provider)
                .generate(new ThingUID(BINDING_ID, "test-thing"), Objects.requireNonNull(node), true);

        assertEquals(13, results.channels.values().stream().map(f -> f.getChannelTypeUID()).distinct().count());
    }

    @Test
    public void testGenerateChannelTypeStore4Node7ConfigWriteProperty() throws IOException {
        Channel channel = getChannel("store_4.json", 7,
                "configuration-key-s-1-associations-send-on-with-single-click-1", true);

        assertEquals("24", channel.getConfiguration().get(BindingConstants.CONFIG_CHANNEL_WRITE_PROPERTY));
    }

    @Test
    public void testGenerateChannelTypeNode6() throws IOException {
        Node node = DataUtil.getNodeFromStore("store_1.json", 6);

        ZwaveJSTypeGeneratorResult results = Objects.requireNonNull(provider)
                .generate(new ThingUID(BINDING_ID, "test-thing"), Objects.requireNonNull(node), false);

        assertEquals(3, results.channels.values().stream().map(f -> f.getChannelTypeUID()).distinct().count());
    }

    @Test
    public void testGenerateChannelStore2Node2() throws IOException {
        Node node = DataUtil.getNodeFromStore("store_2.json", 2);

        ZwaveJSTypeGeneratorResult results = Objects.requireNonNull(provider)
                .generate(new ThingUID(BINDING_ID, "test-thing"), Objects.requireNonNull(node), false);

        assertEquals(15, results.channels.size());
    }

    @Test
    public void testGenerateChannelTypeStore1Node6Label() throws IOException {
        Channel channel = getChannel("store_1.json", 6, "meter-reset");

        assertEquals("Reset Accumulated Values", channel.getLabel());
        assertNull(channel.getDescription());
    }

    @Test
    public void testGenerateChannelTypeStore1Node6WriteProperty() throws IOException {
        Channel channel = getChannel("store_1.json", 6, "binary-switch-value");

        assertEquals("targetValue", channel.getConfiguration().get(BindingConstants.CONFIG_CHANNEL_WRITE_PROPERTY));
    }

    @Test
    public void testGenerateChannelTypeStore1Node6ChannelType() throws IOException {
        Channel channel = getChannel("store_1.json", 6, "meter-value-65537");
        ChannelType type = channelTypeProvider.getChannelType(Objects.requireNonNull(channel.getChannelTypeUID()),
                null);

        assertNotNull(type);
    }

    @Test
    public void testGenerateChannelTypeStore1Node3NotificationType() throws IOException {
        Channel channel = getChannel("store_1.json", 3, "notification-power-management-over-load-status");
        ChannelType type = channelTypeProvider.getChannelType(Objects.requireNonNull(channel.getChannelTypeUID()),
                null);

        assertNotNull(type);
        assertEquals("Switch", type.getItemType());
    }

    @Test
    public void testGenerateChannelTypeStore2Node14MultilevelSwitchType() throws IOException {
        Channel channel = getChannel("store_2.json", 14, "multilevel-switch-value");
        ChannelType type = channelTypeProvider.getChannelType(Objects.requireNonNull(channel.getChannelTypeUID()),
                null);
        Configuration configuration = channel.getConfiguration();

        assertNotNull(type);
        assertEquals("zwavejs:test-bridge:test-thing:multilevel-switch-value", channel.getUID().getAsString());
        assertEquals("Dimmer", Objects.requireNonNull(type).getItemType());
        assertEquals("Current Value", channel.getLabel());
        assertNotNull(configuration.get(BindingConstants.CONFIG_CHANNEL_WRITE_PROPERTY));

        StateDescription statePattern = type.getState();
        assertNotNull(statePattern);
        assertEquals(BigDecimal.valueOf(0), statePattern.getMinimum());
        assertEquals(BigDecimal.valueOf(100), statePattern.getMaximum());
        assertNull(statePattern.getStep());
        assertEquals("%1d %%", statePattern.getPattern());
    }

    @Test
    public void testGenerateChannelTypeStore2AllNodes() throws IOException {
        ResultMessage resultMessage = DataUtil.fromJson("store_2.json", ResultMessage.class);
        Map<String, Channel> channels = new HashMap<>();

        for (Node node : resultMessage.result.state.nodes) {
            ZwaveJSTypeGeneratorResult results = Objects.requireNonNull(provider)
                    .generate(new ThingUID(BINDING_ID, "test-thing"), Objects.requireNonNull(node), false);
            channels.putAll(results.channels);
        }

        assertEquals(32, channels.values().stream().map(f -> f.getChannelTypeUID()).distinct().count());
    }

    @Test
    public void testGenerateChannelTypeStore3AllNodes() throws IOException {
        ResultMessage resultMessage = DataUtil.fromJson("store_3.json", ResultMessage.class);
        Map<String, Channel> channels = new HashMap<>();

        for (Node node : resultMessage.result.state.nodes) {
            ZwaveJSTypeGeneratorResult results = Objects.requireNonNull(provider)
                    .generate(new ThingUID(BINDING_ID, "test-thing"), Objects.requireNonNull(node), false);
            channels.putAll(results.channels);
        }

        assertEquals(41, channels.values().stream().map(f -> f.getChannelTypeUID()).distinct().count());
    }

    @Test
    public void testGenerateChannelTypeStore4Node02SensorSwitchType() throws IOException {
        Channel channel = getChannel("store_4.json", 2, "binary-sensor-any");
        ChannelType type = channelTypeProvider.getChannelType(Objects.requireNonNull(channel.getChannelTypeUID()),
                null);

        assertNotNull(type);
        assertEquals("zwavejs:test-bridge:test-thing:binary-sensor-any", channel.getUID().getAsString());
        assertEquals("Switch", Objects.requireNonNull(type).getItemType());
        assertEquals("Sensor State (Any)", channel.getLabel());

        assertNull(type.getState());
    }

    @Test
    public void testGenerateChannelTypeStore4Node07MultilevelSwitchType() throws IOException {
        Channel channel = getChannel("store_4.json", 7, "multilevel-switch-value-1");
        ChannelType type = channelTypeProvider.getChannelType(Objects.requireNonNull(channel.getChannelTypeUID()),
                null);
        Configuration configuration = channel.getConfiguration();

        assertNotNull(type);
        assertEquals("zwavejs:test-bridge:test-thing:multilevel-switch-value-1", channel.getUID().getAsString());
        assertEquals("Dimmer", Objects.requireNonNull(type).getItemType());
        assertEquals("EP1 Current Value", channel.getLabel());
        assertNotNull(configuration.get(BindingConstants.CONFIG_CHANNEL_WRITE_PROPERTY));

        StateDescription statePattern = type.getState();
        assertNotNull(statePattern);
        assertEquals(BigDecimal.valueOf(0), statePattern.getMinimum());
        assertEquals(BigDecimal.valueOf(100), statePattern.getMaximum());
        assertNull(statePattern.getStep());
        assertEquals("%1d %%", statePattern.getPattern());

        assertNotNull(type);
        assertEquals("Dimmer", type.getItemType());
    }

    @Test
    public void testGenerateChannelTypeStore4Node44ColorType() throws IOException {
        Channel channel = getChannel("store_4.json", 44, "color-switch-hex-color");
        ChannelType type = channelTypeProvider.getChannelType(Objects.requireNonNull(channel.getChannelTypeUID()),
                null);
        Configuration configuration = channel.getConfiguration();

        assertNotNull(type);
        assertEquals("zwavejs:test-bridge:test-thing:color-switch-hex-color", channel.getUID().getAsString());
        assertEquals("Color", Objects.requireNonNull(type).getItemType());
        assertEquals("RGB Color", channel.getLabel());
        assertNotNull(configuration.get(BindingConstants.CONFIG_CHANNEL_WRITE_PROPERTY));

        StateDescription statePattern = type.getState();
        assertNotNull(statePattern);

        assertNotNull(type);
        assertEquals("Color", type.getItemType());
    }

    @Test
    public void testGenerateChannelTypeStore4AllNodes() throws IOException {
        ResultMessage resultMessage = DataUtil.fromJson("store_4.json", ResultMessage.class);
        Map<String, Channel> channels = new HashMap<>();

        for (Node node : resultMessage.result.state.nodes) {
            ZwaveJSTypeGeneratorResult results = Objects.requireNonNull(provider)
                    .generate(new ThingUID(BINDING_ID, "test-thing"), Objects.requireNonNull(node), false);
            channels.putAll(results.channels);
        }
        ;

        assertEquals(27, channels.values().stream().map(f -> f.getChannelTypeUID()).distinct().count());
    }
}
