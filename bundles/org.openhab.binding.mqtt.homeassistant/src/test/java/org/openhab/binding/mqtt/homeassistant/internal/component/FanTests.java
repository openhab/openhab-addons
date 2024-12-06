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

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.PercentageValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.types.UnDefType;

/**
 * Tests for {@link Fan}
 *
 * @author Anton Kharuzhy - Initial contribution
 */
@NonNullByDefault
public class FanTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "fan/0x0000000000000000_fan_zigbee2mqtt";

    @SuppressWarnings("null")
    @Test
    public void test() throws InterruptedException {
        // @formatter:off
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                """
                { \
                  "availability": [ \
                    { \
                      "topic": "zigbee2mqtt/bridge/state" \
                    } \
                  ], \
                  "device": { \
                    "identifiers": [ \
                      "zigbee2mqtt_0x0000000000000000" \
                    ], \
                    "manufacturer": "Fans inc", \
                    "model": "Fan", \
                    "name": "FanBlower", \
                    "sw_version": "Zigbee2MQTT 1.18.2" \
                  }, \
                  "name": "fan", \
                  "payload_off": "OFF_", \
                  "payload_on": "ON_", \
                  "state_topic": "zigbee2mqtt/fan/state", \
                  "command_topic": "zigbee2mqtt/fan/set/state" \
                }\
                """);
        // @formatter:on

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("fan"));

        assertChannel(component, Fan.SWITCH_CHANNEL_ID_DEPRECATED, "zigbee2mqtt/fan/state", "zigbee2mqtt/fan/set/state",
                "On/Off State", OnOffValue.class, null);

        publishMessage("zigbee2mqtt/fan/state", "ON_");
        assertState(component, Fan.SWITCH_CHANNEL_ID_DEPRECATED, OnOffType.ON);
        publishMessage("zigbee2mqtt/fan/state", "ON_");
        assertState(component, Fan.SWITCH_CHANNEL_ID_DEPRECATED, OnOffType.ON);
        publishMessage("zigbee2mqtt/fan/state", "OFF_");
        assertState(component, Fan.SWITCH_CHANNEL_ID_DEPRECATED, OnOffType.OFF);
        publishMessage("zigbee2mqtt/fan/state", "ON_");
        assertState(component, Fan.SWITCH_CHANNEL_ID_DEPRECATED, OnOffType.ON);

        component.getChannel(Fan.SWITCH_CHANNEL_ID_DEPRECATED).getState().publishValue(OnOffType.OFF);
        assertPublished("zigbee2mqtt/fan/set/state", "OFF_");
        component.getChannel(Fan.SWITCH_CHANNEL_ID_DEPRECATED).getState().publishValue(OnOffType.ON);
        assertPublished("zigbee2mqtt/fan/set/state", "ON_");
    }

    @SuppressWarnings("null")
    @Test
    public void testPercentageWithTemplates() throws InterruptedException {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                """
                        {
                          "availability": [
                            {
                              "topic": "zigbee2mqtt/bridge/state"
                            }
                          ],
                          "device": {
                            "identifiers": [
                              "zigbee2mqtt_0x0000000000000000"
                            ],
                            "manufacturer": "Fans inc",
                            "model": "Fan",
                            "name": "FanBlower",
                            "sw_version": "Zigbee2MQTT 1.18.2"
                          },
                          "name": "fan",
                          "state_topic": "zigbee2mqtt/fan",
                          "state_value_template": "{{ value_json.fan_state }}",
                          "command_topic": "zigbee2mqtt/fan/set/fan_state",
                          "percentage_command_template": "{{ {0:'off', 1:'low', 2:'medium', 3:'high'}[value] | default('') }}",
                          "percentage_command_topic": "zigbee2mqtt/fan/set/fan_mode",
                          "percentage_state_topic": "zigbee2mqtt/fan",
                          "percentage_value_template": "{{ {'off':0, 'low':1, 'medium':2, 'high':3}[value_json.fan_mode] | default('None') }}",
                          "speed_range_max": 3,
                          "speed_range_min": 1
                        }
                        """);

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("fan"));

        assertChannel(component, Fan.SPEED_CHANNEL_ID, "zigbee2mqtt/fan", "zigbee2mqtt/fan/set/fan_mode", "Speed",
                PercentageValue.class, null);

        publishMessage("zigbee2mqtt/fan", "{ \"fan_state\": \"OFF\", \"fan_mode\": \"high\"}");
        assertState(component, Fan.SPEED_CHANNEL_ID, PercentType.ZERO);
        publishMessage("zigbee2mqtt/fan", "{ \"fan_state\": \"ON\", \"fan_mode\": \"high\"}");
        assertState(component, Fan.SPEED_CHANNEL_ID, PercentType.HUNDRED);
        publishMessage("zigbee2mqtt/fan", "{ \"fan_state\": \"ON\", \"fan_mode\": \"medium\"}");
        assertState(component, Fan.SPEED_CHANNEL_ID,
                new PercentType(new BigDecimal(200).divide(new BigDecimal(3), MathContext.DECIMAL128)));
        publishMessage("zigbee2mqtt/fan", "{ \"fan_state\": \"ON\", \"fan_mode\": \"low\"}");
        assertState(component, Fan.SPEED_CHANNEL_ID,
                new PercentType(new BigDecimal(100).divide(new BigDecimal(3), MathContext.DECIMAL128)));

        component.getChannel(Fan.SPEED_CHANNEL_ID).getState().publishValue(OnOffType.OFF);
        assertPublished("zigbee2mqtt/fan/set/fan_state", "OFF");
        component.getChannel(Fan.SPEED_CHANNEL_ID).getState().publishValue(OnOffType.ON);
        assertPublished("zigbee2mqtt/fan/set/fan_state", "ON");
        component.getChannel(Fan.SPEED_CHANNEL_ID).getState().publishValue(PercentType.HUNDRED);
        assertPublished("zigbee2mqtt/fan/set/fan_mode", "high");
        component.getChannel(Fan.SPEED_CHANNEL_ID).getState().publishValue(PercentType.ZERO);
        assertPublished("zigbee2mqtt/fan/set/fan_mode", "off");
        component.getChannel(Fan.SPEED_CHANNEL_ID).getState().publishValue(new PercentType(33));
        assertPublished("zigbee2mqtt/fan/set/fan_mode", "low");
        component.getChannel(Fan.SPEED_CHANNEL_ID).getState().publishValue(new PercentType(66));
        assertPublished("zigbee2mqtt/fan/set/fan_mode", "medium");
    }

    @SuppressWarnings("null")
    @Test
    public void testInferredOptimistic() throws InterruptedException {
        // @formatter:off
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                """
                { \
                  "availability": [ \
                    { \
                      "topic": "zigbee2mqtt/bridge/state" \
                    } \
                  ], \
                  "device": { \
                    "identifiers": [ \
                      "zigbee2mqtt_0x0000000000000000" \
                    ], \
                    "manufacturer": "Fans inc", \
                    "model": "Fan", \
                    "name": "FanBlower", \
                    "sw_version": "Zigbee2MQTT 1.18.2" \
                  }, \
                  "name": "fan", \
                  "payload_off": "OFF_", \
                  "payload_on": "ON_", \
                  "command_topic": "zigbee2mqtt/fan/set/state"
                }\
                """);
        // @formatter:on

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("fan"));

        assertChannel(component, Fan.SWITCH_CHANNEL_ID_DEPRECATED, "", "zigbee2mqtt/fan/set/state", "On/Off State",
                OnOffValue.class, AutoUpdatePolicy.RECOMMEND);
    }

    @SuppressWarnings("null")
    @Test
    public void testForcedOptimistic() throws InterruptedException {
        // @formatter:off
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                """
                { \
                  "availability": [ \
                    { \
                      "topic": "zigbee2mqtt/bridge/state" \
                    } \
                  ], \
                  "device": { \
                    "identifiers": [ \
                      "zigbee2mqtt_0x0000000000000000" \
                    ], \
                    "manufacturer": "Fans inc", \
                    "model": "Fan", \
                    "name": "FanBlower", \
                    "sw_version": "Zigbee2MQTT 1.18.2" \
                  }, \
                  "name": "fan", \
                  "payload_off": "OFF_", \
                  "payload_on": "ON_", \
                  "state_topic": "zigbee2mqtt/fan/state", \
                  "command_topic": "zigbee2mqtt/fan/set/state", \
                  "optimistic": true \
                }\
                """);
        // @formatter:on

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("fan"));

        assertChannel(component, Fan.SWITCH_CHANNEL_ID_DEPRECATED, "zigbee2mqtt/fan/state", "zigbee2mqtt/fan/set/state",
                "On/Off State", OnOffValue.class, AutoUpdatePolicy.RECOMMEND);
    }

    @SuppressWarnings("null")
    @Test
    public void testInferredOptimisticWithPosition() throws InterruptedException {
        // @formatter:off
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                """
                { \
                  "availability": [ \
                    { \
                      "topic": "zigbee2mqtt/bridge/state" \
                    } \
                  ], \
                  "device": { \
                    "identifiers": [ \
                      "zigbee2mqtt_0x0000000000000000" \
                    ], \
                    "manufacturer": "Fans inc", \
                    "model": "Fan", \
                    "name": "FanBlower", \
                    "sw_version": "Zigbee2MQTT 1.18.2" \
                  }, \
                  "name": "fan", \
                  "payload_off": "OFF_", \
                  "payload_on": "ON_", \
                  "command_topic": "zigbee2mqtt/fan/set/state", \
                  "percentage_command_topic": "bedroom_fan/speed/percentage" \
                }\
                """);
        // @formatter:on

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("fan"));

        assertChannel(component, Fan.SPEED_CHANNEL_ID, "", "bedroom_fan/speed/percentage", "Speed",
                PercentageValue.class, AutoUpdatePolicy.RECOMMEND);
    }

    @SuppressWarnings("null")
    @Test
    public void testCommandTemplate() throws InterruptedException {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                        {
                            "availability": [
                            {
                                "topic": "zigbee2mqtt/bridge/state"
                            }
                            ],
                            "device": {
                            "identifiers": [
                                "zigbee2mqtt_0x0000000000000000"
                            ],
                            "manufacturer": "Fans inc",
                            "model": "Fan",
                            "name": "FanBlower",
                            "sw_version": "Zigbee2MQTT 1.18.2"
                            },
                            "name": "fan",
                            "payload_off": "OFF_",
                            "payload_on": "ON_",
                            "state_topic": "zigbee2mqtt/fan/state",
                            "command_topic": "zigbee2mqtt/fan/set/state",
                            "command_template": "set to {{ value }}"
                        }
                """);

        assertThat(component.channels.size(), is(1));

        component.getChannel(Fan.SWITCH_CHANNEL_ID_DEPRECATED).getState().publishValue(OnOffType.OFF);
        assertPublished("zigbee2mqtt/fan/set/state", "set to OFF_");
    }

    @SuppressWarnings("null")
    @Test
    public void testComplex() throws InterruptedException {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                {
                  "availability": [
                    {
                      "topic": "zigbee2mqtt/bridge/state"
                    }
                  ],
                  "device": {
                    "identifiers": [
                      "zigbee2mqtt_0x0000000000000000"
                    ],
                    "manufacturer": "Fans inc",
                    "model": "Fan",
                    "name": "FanBlower",
                    "sw_version": "Zigbee2MQTT 1.18.2"
                  },
                  "name": "Bedroom Fan",
                  "payload_off": "false",
                  "payload_on": "true",
                  "state_topic": "bedroom_fan/on/state",
                  "command_topic": "bedroom_fan/on/set",
                  "direction_state_topic": "bedroom_fan/direction/state",
                  "direction_command_topic": "bedroom_fan/direction/set",
                  "oscillation_state_topic": "bedroom_fan/oscillation/state",
                  "oscillation_command_topic": "bedroom_fan/oscillation/set",
                  "percentage_state_topic": "bedroom_fan/speed/percentage_state",
                  "percentage_command_topic": "bedroom_fan/speed/percentage",
                  "preset_mode_state_topic": "bedroom_fan/preset/preset_mode_state",
                  "preset_mode_command_topic": "bedroom_fan/preset/preset_mode",
                  "preset_modes": [
                    "auto",
                    "smart",
                    "whoosh",
                    "eco",
                    "breeze"
                  ],
                  "payload_oscillation_on": "true",
                  "payload_oscillation_off": "false",
                  "speed_range_min": 1,
                  "speed_range_max": 10
                }
                """);

        assertThat(component.channels.size(), is(4));
        assertThat(component.getName(), is("Bedroom Fan"));

        assertChannel(component, Fan.SPEED_CHANNEL_ID, "bedroom_fan/speed/percentage_state",
                "bedroom_fan/speed/percentage", "Speed", PercentageValue.class);
        var channel = Objects.requireNonNull(component.getChannel(Fan.SPEED_CHANNEL_ID));
        assertThat(channel.getStateDescription().getStep(), is(BigDecimal.valueOf(10)));
        assertChannel(component, Fan.OSCILLATION_CHANNEL_ID, "bedroom_fan/oscillation/state",
                "bedroom_fan/oscillation/set", "Oscillation", OnOffValue.class);
        assertChannel(component, Fan.DIRECTION_CHANNEL_ID, "bedroom_fan/direction/state", "bedroom_fan/direction/set",
                "Direction", TextValue.class);
        assertChannel(component, Fan.PRESET_MODE_CHANNEL_ID, "bedroom_fan/preset/preset_mode_state",
                "bedroom_fan/preset/preset_mode", "Preset Mode", TextValue.class);

        publishMessage("bedroom_fan/on/state", "true");
        assertState(component, Fan.SPEED_CHANNEL_ID, PercentType.HUNDRED);
        publishMessage("bedroom_fan/on/state", "false");
        assertState(component, Fan.SPEED_CHANNEL_ID, PercentType.ZERO);
        publishMessage("bedroom_fan/on/state", "true");
        publishMessage("bedroom_fan/speed/percentage_state", "5");
        assertState(component, Fan.SPEED_CHANNEL_ID, new PercentType(50));
        publishMessage("bedroom_fan/on/state", "false");
        // Off, even though we got an updated speed
        assertState(component, Fan.SPEED_CHANNEL_ID, PercentType.ZERO);
        publishMessage("bedroom_fan/speed/percentage_state", "2");
        assertState(component, Fan.SPEED_CHANNEL_ID, PercentType.ZERO);
        publishMessage("bedroom_fan/on/state", "true");
        // Now that it's on, the channel reflects the proper speed
        assertState(component, Fan.SPEED_CHANNEL_ID, new PercentType(20));

        publishMessage("bedroom_fan/oscillation/state", "true");
        assertState(component, Fan.OSCILLATION_CHANNEL_ID, OnOffType.ON);
        publishMessage("bedroom_fan/oscillation/state", "false");
        assertState(component, Fan.OSCILLATION_CHANNEL_ID, OnOffType.OFF);

        publishMessage("bedroom_fan/direction/state", "forward");
        assertState(component, Fan.DIRECTION_CHANNEL_ID, new StringType("forward"));
        publishMessage("bedroom_fan/direction/state", "backward");
        assertState(component, Fan.DIRECTION_CHANNEL_ID, new StringType("backward"));

        publishMessage("bedroom_fan/preset/preset_mode_state", "auto");
        assertState(component, Fan.PRESET_MODE_CHANNEL_ID, new StringType("auto"));
        publishMessage("bedroom_fan/preset/preset_mode_state", "None");
        assertState(component, Fan.PRESET_MODE_CHANNEL_ID, UnDefType.NULL);

        component.getChannel(Fan.SPEED_CHANNEL_ID).getState().publishValue(OnOffType.OFF);
        assertPublished("bedroom_fan/on/set", "false");
        component.getChannel(Fan.SPEED_CHANNEL_ID).getState().publishValue(OnOffType.ON);
        assertPublished("bedroom_fan/on/set", "true");
        // Setting to a specific speed turns it on first
        component.getChannel(Fan.SPEED_CHANNEL_ID).getState().publishValue(PercentType.HUNDRED);
        assertPublished("bedroom_fan/on/set", "true");
        assertPublished("bedroom_fan/speed/percentage", "10");

        component.getChannel(Fan.OSCILLATION_CHANNEL_ID).getState().publishValue(OnOffType.ON);
        assertPublished("bedroom_fan/oscillation/set", "true");

        component.getChannel(Fan.DIRECTION_CHANNEL_ID).getState().publishValue(new StringType("forward"));
        assertPublished("bedroom_fan/direction/set", "forward");

        component.getChannel(Fan.PRESET_MODE_CHANNEL_ID).getState().publishValue(new StringType("eco"));
        assertPublished("bedroom_fan/preset/preset_mode", "eco");
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
