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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
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
    public void testGenCTNode2TypeCount() throws IOException {
        Node node = DataUtil.getNodeFromStore("store_4.json", 2);

        ZwaveJSTypeGeneratorResult results = Objects.requireNonNull(provider)
                .generate(new ThingUID(BINDING_ID, "test-thing"), Objects.requireNonNull(node), false);

        assertEquals(8, results.channels.size());
    }

    @Test
    public void testGenCTNode2SensorSwitchType() throws IOException {
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
    public void testGenCTNode7MeterType() throws IOException {
        Channel channel = getChannel("store_4.json", 7, "meter-value-65537-1");
        ChannelType type = channelTypeProvider.getChannelType(Objects.requireNonNull(channel.getChannelTypeUID()),
                null);

        assertNotNull(type);
    }

    @Test
    public void testGenCTNode7NotificationType() throws IOException {
        Channel channel = getChannel("store_4.json", 7, "notification-power-management-over-load-status-1");
        ChannelType type = channelTypeProvider.getChannelType(Objects.requireNonNull(channel.getChannelTypeUID()),
                null);

        assertNotNull(type);
        assertEquals("Switch", type.getItemType());
    }

    @Test
    public void testGenCTNode7TypeCount() throws IOException {
        Node node = DataUtil.getNodeFromStore("store_4.json", 7);

        ZwaveJSTypeGeneratorResult results = Objects.requireNonNull(provider)
                .generate(new ThingUID(BINDING_ID, "test-thing"), Objects.requireNonNull(node), false);

        assertEquals(14, results.channels.values().stream().map(f -> f.getChannelTypeUID()).distinct().count());
    }

    @Test
    public void testGenCTNode7AsChannels() throws IOException {
        Node node = DataUtil.getNodeFromStore("store_4.json", 7);

        ZwaveJSTypeGeneratorResult results = Objects.requireNonNull(provider)
                .generate(new ThingUID(BINDING_ID, "test-thing"), Objects.requireNonNull(node), true);

        assertEquals(47, results.channels.values().stream().map(f -> f.getChannelTypeUID()).distinct().count());
    }

    @Test
    public void testGenCTNode7ConfigWriteProperty() throws IOException {
        Channel channel = getChannel("store_4.json", 7,
                "configuration-key-s-1-associations-send-on-with-single-click-1", true);

        assertNull(channel.getConfiguration().get(BindingConstants.CONFIG_CHANNEL_WRITE_PROPERTY_STR));
        assertEquals(BigDecimal.valueOf(24),
                channel.getConfiguration().get(BindingConstants.CONFIG_CHANNEL_WRITE_PROPERTY_INT));
    }

    @Test
    public void testGenCTNode7Label() throws IOException {
        Channel channel = getChannel("store_4.json", 7, "meter-reset-1");

        assertEquals("EP1 Reset Accumulated Values", channel.getLabel());
        assertNull(channel.getDescription());
    }

    @Test
    public void testGenCTNode7ReadProperty() throws IOException {
        Channel channel = getChannel("store_4.json", 7, "multilevel-switch-value-1");

        assertEquals("currentValue", channel.getConfiguration().get(BindingConstants.CONFIG_CHANNEL_READ_PROPERTY));
    }

    @Test
    public void testGenCTNode7WriteProperty() throws IOException {
        Channel channel = getChannel("store_4.json", 7, "multilevel-switch-value-1");

        assertEquals("targetValue", channel.getConfiguration().get(BindingConstants.CONFIG_CHANNEL_WRITE_PROPERTY_STR));
    }

    @Test
    public void testGenCTNode7MultilevelSwitchType() throws IOException {
        Channel channel = getChannel("store_4.json", 7, "multilevel-switch-value-1");
        ChannelType type = channelTypeProvider.getChannelType(Objects.requireNonNull(channel.getChannelTypeUID()),
                null);
        Configuration configuration = channel.getConfiguration();

        assertNotNull(type);
        assertEquals("zwavejs:test-bridge:test-thing:multilevel-switch-value-1", channel.getUID().getAsString());
        assertEquals("Dimmer", Objects.requireNonNull(type).getItemType());
        assertEquals("EP1 Current Value", channel.getLabel());
        assertNotNull(configuration.get(BindingConstants.CONFIG_CHANNEL_WRITE_PROPERTY_STR));

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
    public void testGenCTNode13MultilevelSwitchType() throws IOException {
        Channel channel = getChannel("store_4.json", 13, "multilevel-switch-value");
        ChannelType type = channelTypeProvider.getChannelType(Objects.requireNonNull(channel.getChannelTypeUID()),
                null);
        Configuration configuration = channel.getConfiguration();

        assertNotNull(type);
        assertEquals("zwavejs:test-bridge:test-thing:multilevel-switch-value", channel.getUID().getAsString());
        assertEquals("Dimmer", Objects.requireNonNull(type).getItemType());
        assertEquals("Current Value", channel.getLabel());
        assertNotNull(configuration.get(BindingConstants.CONFIG_CHANNEL_WRITE_PROPERTY_STR));

        StateDescription statePattern = type.getState();
        assertNotNull(statePattern);
        assertEquals(BigDecimal.valueOf(0), statePattern.getMinimum());
        assertEquals(BigDecimal.valueOf(100), statePattern.getMaximum());
        assertNull(statePattern.getStep());
        assertEquals("%1d %%", statePattern.getPattern());
    }

    @Test
    public void testGenCTNode16MultilevelSwitchType() throws IOException {
        Channel channel = getChannel("store_4.json", 16, "multilevel-switch-value");
        ChannelType type = channelTypeProvider.getChannelType(Objects.requireNonNull(channel.getChannelTypeUID()),
                null);
        Configuration configuration = channel.getConfiguration();

        assertNotNull(type);
        assertEquals("zwavejs:test-bridge:test-thing:multilevel-switch-value", channel.getUID().getAsString());
        assertEquals("Dimmer", Objects.requireNonNull(type).getItemType());
        assertEquals("Current Value", channel.getLabel());
        assertNotNull(configuration.get(BindingConstants.CONFIG_CHANNEL_WRITE_PROPERTY_STR));

        StateDescription statePattern = type.getState();
        assertNotNull(statePattern);
        assertEquals(BigDecimal.valueOf(0), statePattern.getMinimum());
        assertEquals(BigDecimal.valueOf(100), statePattern.getMaximum());
        assertNull(statePattern.getStep());
        assertEquals("%1d %%", statePattern.getPattern());
    }

    @Test
    public void testGenCTNode25WriteProperty() throws IOException {
        Channel channel = getChannel("store_4.json", 25, "binary-switch-value-1");

        assertEquals("targetValue", channel.getConfiguration().get(BindingConstants.CONFIG_CHANNEL_WRITE_PROPERTY_STR));
    }

    @Test
    public void testGenCTNode44ColorType() throws IOException {
        Channel channel = getChannel("store_4.json", 44, "color-switch-hex-color");
        ChannelType type = channelTypeProvider.getChannelType(Objects.requireNonNull(channel.getChannelTypeUID()),
                null);
        Configuration configuration = channel.getConfiguration();

        assertNotNull(type);
        assertEquals("zwavejs:test-bridge:test-thing:color-switch-hex-color", channel.getUID().getAsString());
        assertEquals("Color", Objects.requireNonNull(type).getItemType());
        assertEquals("RGB Color", channel.getLabel());
        assertNotNull(configuration.get(BindingConstants.CONFIG_CHANNEL_WRITE_PROPERTY_STR));

        StateDescription statePattern = type.getState();
        assertNotNull(statePattern);

        assertNotNull(type);
        assertEquals("Color", type.getItemType());
    }

    @Test
    public void testGenCTNode74HumidityInvalidUnit() throws IOException {
        Channel channel = getChannel("store_4.json", 74, "multilevel-sensor-humidity");
        ChannelType type = channelTypeProvider.getChannelType(Objects.requireNonNull(channel.getChannelTypeUID()),
                null);

        assertNotNull(type);
        assertEquals("zwavejs:test-bridge:test-thing:multilevel-sensor-humidity", channel.getUID().getAsString());
        assertEquals("Number:Dimensionless", Objects.requireNonNull(type).getItemType());
        assertEquals("Humidity", channel.getLabel());
        StateDescription statePattern = type.getState();
        assertNotNull(statePattern);
        assertNotNull(type);
    }

    @Test
    public void testGenCTNode186WeirdType() throws IOException {
        Channel channel = getChannel("store_4.json", 186, "door-lock-inside-handles-can-open-door");
        ChannelType type = channelTypeProvider.getChannelType(Objects.requireNonNull(channel.getChannelTypeUID()),
                null);
        Configuration configuration = channel.getConfiguration();

        assertNotNull(type);
        assertEquals("zwavejs:test-bridge:test-thing:door-lock-inside-handles-can-open-door",
                channel.getUID().getAsString());
        assertEquals("String", type.getItemType());
        assertEquals("Which Inside Handles Can Open The Door (Actual Status)", channel.getLabel());
        assertNull(configuration.get(BindingConstants.CONFIG_CHANNEL_WRITE_PROPERTY_STR));

        StateDescription statePattern = type.getState();
        assertNull(statePattern);
    }

    @Test
    public void testGenCTAllNodes() throws IOException {
        ResultMessage resultMessage = DataUtil.fromJson("store_4.json", ResultMessage.class);
        Map<String, Channel> channels = new HashMap<>();

        for (Node node : resultMessage.result.state.nodes) {
            ZwaveJSTypeGeneratorResult results = Objects.requireNonNull(provider)
                    .generate(new ThingUID(BINDING_ID, "test-thing"), Objects.requireNonNull(node), false);
            channels.putAll(results.channels);
        }

        assertEquals(43, channels.values().stream().map(f -> f.getChannelTypeUID()).distinct().count());
        assertTrue(channels.containsKey("color-switch-color-temperature"));
    }
}
