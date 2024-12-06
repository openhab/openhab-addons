/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
import org.openhab.core.config.core.Configuration;

/**
 * Tests for {@link DeviceTrigger}
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class DeviceTriggerTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC_1 = "device_automation/0x8cf681fffe2fd2a6/press";
    public static final String CONFIG_TOPIC_2 = "device_automation/0x8cf681fffe2fd2a6/release";

    @SuppressWarnings("null")
    @Test
    public void test() throws InterruptedException {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC_1), """
                {
                    "automation_type": "trigger",
                    "device": {
                        "configuration_url": "#/device/0x8cf681fffe2fd2a6/info",
                        "identifiers": [
                            "zigbee2mqtt_0x8cf681fffe2fd2a6"
                        ],
                        "manufacturer": "IKEA",
                        "model": "TRADFRI shortcut button (E1812)",
                        "name": "Charge Now Button",
                        "sw_version": "2.3.015"
                    },
                    "payload": "on",
                    "subtype": "on",
                    "topic": "zigbee2mqtt/Charge Now Button/action",
                    "type": "action"
                }
                    """);

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("MQTT Device Trigger"));

        assertChannel(component, "on", "zigbee2mqtt/Charge Now Button/action", "", "MQTT Device Trigger",
                TextValue.class);

        publishMessage("zigbee2mqtt/Charge Now Button/action", "on");
        assertTriggered(component, "on", "on");

        publishMessage("zigbee2mqtt/Charge Now Button/action", "off");
        assertNotTriggered(component, "on", "off");
    }

    @SuppressWarnings("null")
    @Test
    public void testMerge() throws InterruptedException {
        var component1 = (DeviceTrigger) discoverComponent(configTopicToMqtt(CONFIG_TOPIC_1), """
                {
                    "automation_type": "trigger",
                    "device": {
                        "configuration_url": "#/device/0x8cf681fffe2fd2a6/info",
                        "identifiers": [
                            "zigbee2mqtt_0x8cf681fffe2fd2a6"
                        ],
                        "manufacturer": "IKEA",
                        "model": "TRADFRI shortcut button (E1812)",
                        "name": "Charge Now Button",
                        "sw_version": "2.3.015"
                    },
                    "payload": "press",
                    "subtype": "turn_on",
                    "topic": "zigbee2mqtt/Charge Now Button/action",
                    "type": "button_long_press"
                }
                    """);
        var component2 = (DeviceTrigger) discoverComponent(configTopicToMqtt(CONFIG_TOPIC_2), """
                {
                    "automation_type": "trigger",
                    "device": {
                        "configuration_url": "#/device/0x8cf681fffe2fd2a6/info",
                        "identifiers": [
                            "zigbee2mqtt_0x8cf681fffe2fd2a6"
                        ],
                        "manufacturer": "IKEA",
                        "model": "TRADFRI shortcut button (E1812)",
                        "name": "Charge Now Button",
                        "sw_version": "2.3.015"
                    },
                    "payload": "release",
                    "subtype": "turn_on",
                    "topic": "zigbee2mqtt/Charge Now Button/action",
                    "type": "button_long_release"
                }
                    """);

        assertThat(component1.channels.size(), is(1));

        ComponentChannel channel = Objects.requireNonNull(component1.getChannel("turn_on"));
        TextValue value = (TextValue) channel.getState().getCache();
        Set<String> payloads = value.getStates();
        assertNotNull(payloads);
        assertThat(payloads.size(), is(2));
        assertThat(payloads.contains("press"), is(true));
        assertThat(payloads.contains("release"), is(true));
        Configuration channelConfig = channel.getChannel().getConfiguration();
        Object config = channelConfig.get("config");
        assertNotNull(config);
        assertThat(config.getClass(), is(ArrayList.class));
        List<?> configList = (List<?>) config;
        assertThat(configList.size(), is(2));

        publishMessage("zigbee2mqtt/Charge Now Button/action", "press");
        assertTriggered(component1, "turn_on", "press");

        publishMessage("zigbee2mqtt/Charge Now Button/action", "release");
        assertTriggered(component1, "turn_on", "release");

        publishMessage("zigbee2mqtt/Charge Now Button/action", "otherwise");
        assertNotTriggered(component1, "turn_on", "otherwise");
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC_1, CONFIG_TOPIC_2);
    }

    @Override
    protected boolean useNewStyleChannels() {
        return true;
    }
}
