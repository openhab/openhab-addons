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
package org.openhab.binding.mqtt.homeassistant.internal.component;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.CommandOption;

/**
 * Tests for {@link DeviceTrigger}
 *
 * @author Cody Cutrer - Initial contribution
 */
@SuppressWarnings("null")
@NonNullByDefault
public class SceneTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC_1 = "scene/12345_14/scene_1";
    public static final String CONFIG_TOPIC_2 = "scene/12345_14/scene_2";

    @Test
    public void test() throws InterruptedException {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC_1), """
                {
                  "command_topic": "zigbee2mqtt/Theater Room Lights/set",
                  "name": "House",
                  "object_id": "theater_room_lights_1_house",
                  "payload_on": "{ \\"scene_recall\\": 1 }",
                  "unique_id": "14_scene_1_zigbee2mqtt"
                }
                                      """);

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("Scene"));

        assertChannel(component, Scene.SCENE_CHANNEL_ID, "", "zigbee2mqtt/Theater Room Lights/set", "Scene",
                Scene.SceneValue.class);
        linkAllChannels(component);

        component.getChannel(Scene.SCENE_CHANNEL_ID).getState().publishValue(new StringType("scene_1"));
        assertPublished("zigbee2mqtt/Theater Room Lights/set", "{ \"scene_recall\": 1 }");

        component.getChannel(Scene.SCENE_CHANNEL_ID).getState().publishValue(new StringType("House"));
        assertPublished("zigbee2mqtt/Theater Room Lights/set", "{ \"scene_recall\": 1 }", 2);
    }

    @Test
    public void testMerge() throws InterruptedException {
        var component1 = (Scene) discoverComponent(configTopicToMqtt(CONFIG_TOPIC_1), """
                {
                  "command_topic": "zigbee2mqtt/Theater Room Lights/set",
                  "name": "House",
                  "object_id": "theater_room_lights_1_house",
                  "payload_on": "{ \\"scene_recall\\": 1 }",
                  "unique_id": "14_scene_1_zigbee2mqtt"
                }
                                    """);
        discoverComponent(configTopicToMqtt(CONFIG_TOPIC_2), """
                {
                  "command_topic": "zigbee2mqtt/Theater Room Lights/set",
                  "name": "Menu",
                  "object_id": "theater_room_lights_2_menu",
                  "payload_on": "{ \\"scene_recall\\": 2 }",
                  "unique_id": "14_scene_2_zigbee2mqtt"
                }
                                    """);

        assertThat(component1.channels.size(), is(1));

        ComponentChannel channel = Objects.requireNonNull(component1.getChannel(Scene.SCENE_CHANNEL_ID));
        Value value = channel.getState().getCache();
        List<CommandOption> options = value.createCommandDescription().build().getCommandOptions();
        assertThat(options.size(), is(2));
        assertThat(options.get(0).getCommand(), is("scene_1"));
        assertThat(options.get(1).getCommand(), is("scene_2"));
        Configuration channelConfig = channel.getChannel().getConfiguration();
        Object config = channelConfig.get("config");
        assertNotNull(config);
        assertThat(config.getClass(), is(ArrayList.class));
        List<?> configList = (List<?>) config;
        assertThat(configList.size(), is(2));

        linkAllChannels(component1);

        component1.getChannel(Scene.SCENE_CHANNEL_ID).getState().publishValue(new StringType("House"));
        assertPublished("zigbee2mqtt/Theater Room Lights/set", "{ \"scene_recall\": 1 }");

        component1.getChannel(Scene.SCENE_CHANNEL_ID).getState().publishValue(new StringType("scene_2"));
        assertPublished("zigbee2mqtt/Theater Room Lights/set", "{ \"scene_recall\": 2 }");
    }

    @Test
    public void testMultipleTopics() throws InterruptedException {
        var component1 = (Scene) discoverComponent(configTopicToMqtt(CONFIG_TOPIC_1), """
                {
                  "command_topic": "zigbee2mqtt/Theater Room Lights/set",
                  "name": "House",
                  "object_id": "theater_room_lights_1_house",
                  "payload_on": "{ \\"scene_recall\\": 1 }",
                  "unique_id": "14_scene_1_zigbee2mqtt"
                }
                                    """);
        discoverComponent(configTopicToMqtt(CONFIG_TOPIC_2), """
                {
                  "command_topic": "zigbee2mqtt/Theater Room Lights 2/set",
                  "name": "Menu",
                  "object_id": "theater_room_lights_2_menu",
                  "payload_on": "{ \\"scene_recall\\": 2 }",
                  "unique_id": "14_scene_2_zigbee2mqtt"
                }
                                    """);

        assertThat(component1.channels.size(), is(1));

        ComponentChannel channel = Objects.requireNonNull(component1.getChannel(Scene.SCENE_CHANNEL_ID));
        Value value = channel.getState().getCache();
        List<CommandOption> options = value.createCommandDescription().build().getCommandOptions();
        assertThat(options.size(), is(2));
        assertThat(options.get(0).getCommand(), is("scene_1"));
        assertThat(options.get(1).getCommand(), is("scene_2"));
        Configuration channelConfig = channel.getChannel().getConfiguration();
        Object config = channelConfig.get("config");
        assertNotNull(config);
        assertThat(config.getClass(), is(ArrayList.class));
        List<?> configList = (List<?>) config;
        assertThat(configList.size(), is(2));

        linkAllChannels(component1);

        component1.getChannel(Scene.SCENE_CHANNEL_ID).getState().publishValue(new StringType("House"));
        assertPublished("zigbee2mqtt/Theater Room Lights/set", "{ \"scene_recall\": 1 }");

        component1.getChannel(Scene.SCENE_CHANNEL_ID).getState().publishValue(new StringType("scene_2"));
        assertPublished("zigbee2mqtt/Theater Room Lights 2/set", "{ \"scene_recall\": 2 }");
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC_1, CONFIG_TOPIC_2);
    }
}
